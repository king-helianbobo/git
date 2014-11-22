/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cache.recycler;

import com.google.common.base.Strings;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.recycler.AbstractRecyclerC;
import org.elasticsearch.common.recycler.Recycler;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.Arrays;
import java.util.Locale;

import static org.elasticsearch.common.recycler.Recyclers.*;

/** A recycler of fixed-size pages. */
public class PageCacheRecycler extends AbstractComponent {

    public static final String TYPE = "page.type";
    public static final String LIMIT_HEAP = "page.limit.heap";
    public static final String LIMIT_PER_THREAD = "page.limit.per_thread";
    public static final String WEIGHT = "page.weight";

    private final Recycler<byte[]> bytePage;
    private final Recycler<int[]> intPage;
    private final Recycler<long[]> longPage;
    private final Recycler<float[]> floatPage;
    private final Recycler<double[]> doublePage;
    private final Recycler<Object[]> objectPage;

    public void close() {
        bytePage.close();
        intPage.close();
        longPage.close();
        floatPage.close();
        doublePage.close();
        objectPage.close();
    }

    private static int maximumSearchThreadPoolSize(ThreadPool threadPool, Settings settings) {
        ThreadPool.Info searchThreadPool = threadPool.info(ThreadPool.Names.SEARCH);
        assert searchThreadPool != null;
        final int maxSize = searchThreadPool.getMax();
        if (maxSize <= 0) {
            // happens with cached thread pools, let's assume there are at most 3x ${number of processors} threads
            return 3 * EsExecutors.boundedNumberOfProcessors(settings);
        } else {
            return maxSize;
        }
    }

    // return the maximum number of pages that may be cached depending on
    //  - limit: the total amount of memory available
    //  - pageSize: the size of a single page
    //  - weight: the weight for this data type
    //  - totalWeight: the sum of all weights
    private static int maxCount(long limit, long pageSize, double weight, double totalWeight) {
        return (int) (weight / totalWeight * limit / pageSize);
    }

    @Inject
    public PageCacheRecycler(Settings settings, ThreadPool threadPool) {
        super(settings);
        final Type type = Type.parse(componentSettings.get(TYPE));
        final long limit = componentSettings.getAsMemory(LIMIT_HEAP, "10%").bytes();
        final int availableProcessors = EsExecutors.boundedNumberOfProcessors(settings);
        final int searchThreadPoolSize = maximumSearchThreadPoolSize(threadPool, settings);

        // We have a global amount of memory that we need to divide across data types.
        // Since some types are more useful than other ones we give them different weights.
        // Trying to store all of them in a single stack would be problematic because eg.
        // a work load could fill the recycler with only byte[] pages and then another
        // workload that would work with double[] pages couldn't recycle them because there
        // is no space left in the stack/queue. LRU/LFU policies are not an option either
        // because they would make obtain/release too costly: we really need constant-time
        // operations.
        // Ultimately a better solution would be to only store one kind of data and have the
        // ability to intepret it either as a source of bytes, doubles, longs, etc. eg. thanks
        // to direct ByteBuffers or sun.misc.Unsafe on a byte[] but this would have other issues
        // that would need to be addressed such as garbage collection of native memory or safety
        // of Unsafe writes.
        final double bytesWeight = componentSettings.getAsDouble(WEIGHT + ".bytes", 1d);
        final double intsWeight = componentSettings.getAsDouble(WEIGHT + ".ints", 1d);
        final double longsWeight = componentSettings.getAsDouble(WEIGHT + ".longs", 1d);
        final double floatsWeight = componentSettings.getAsDouble(WEIGHT + ".floats", 1d);
        final double doublesWeight = componentSettings.getAsDouble(WEIGHT + ".doubles", 1d);
        // object pages are less useful to us so we give them a lower weight by default
        final double objectsWeight = componentSettings.getAsDouble(WEIGHT + ".objects", 0.1d);

        final double totalWeight = bytesWeight + intsWeight + longsWeight + doublesWeight + objectsWeight;

        bytePage = build(type, maxCount(limit, BigArrays.BYTE_PAGE_SIZE, bytesWeight, totalWeight), searchThreadPoolSize, availableProcessors, new AbstractRecyclerC<byte[]>() {
            @Override
            public byte[] newInstance(int sizing) {
                return new byte[BigArrays.BYTE_PAGE_SIZE];
            }
            @Override
            public void recycle(byte[] value) {
                // nothing to do
            }
        });
        intPage = build(type, maxCount(limit, BigArrays.INT_PAGE_SIZE, intsWeight, totalWeight), searchThreadPoolSize, availableProcessors, new AbstractRecyclerC<int[]>() {
            @Override
            public int[] newInstance(int sizing) {
                return new int[BigArrays.INT_PAGE_SIZE];
            }
            @Override
            public void recycle(int[] value) {
                // nothing to do
            }
        });
        longPage = build(type, maxCount(limit, BigArrays.LONG_PAGE_SIZE, longsWeight, totalWeight), searchThreadPoolSize, availableProcessors, new AbstractRecyclerC<long[]>() {
            @Override
            public long[] newInstance(int sizing) {
                return new long[BigArrays.LONG_PAGE_SIZE];
            }
            @Override
            public void recycle(long[] value) {
                // nothing to do               
            }
        });
        floatPage = build(type, maxCount(limit, BigArrays.FLOAT_PAGE_SIZE, floatsWeight, totalWeight), searchThreadPoolSize, availableProcessors, new AbstractRecyclerC<float[]>() {
            @Override
            public float[] newInstance(int sizing) {
                return new float[BigArrays.FLOAT_PAGE_SIZE];
            }
            @Override
            public void recycle(float[] value) {
                // nothing to do
            }
        });
        doublePage = build(type, maxCount(limit, BigArrays.DOUBLE_PAGE_SIZE, doublesWeight, totalWeight), searchThreadPoolSize, availableProcessors, new AbstractRecyclerC<double[]>() {
            @Override
            public double[] newInstance(int sizing) {
                return new double[BigArrays.DOUBLE_PAGE_SIZE];
            }
            @Override
            public void recycle(double[] value) {
                // nothing to do
            }
        });
        objectPage = build(type, maxCount(limit, BigArrays.OBJECT_PAGE_SIZE, objectsWeight, totalWeight), searchThreadPoolSize, availableProcessors, new AbstractRecyclerC<Object[]>() {
            @Override
            public Object[] newInstance(int sizing) {
                return new Object[BigArrays.OBJECT_PAGE_SIZE];
            }
            @Override
            public void recycle(Object[] value) {
                Arrays.fill(value, null); // we need to remove the strong refs on the objects stored in the array
            }
        });
    }

    public Recycler.V<byte[]> bytePage(boolean clear) {
        final Recycler.V<byte[]> v = bytePage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), (byte) 0);
        }
        return v;
    }

    public Recycler.V<int[]> intPage(boolean clear) {
        final Recycler.V<int[]> v = intPage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), 0);
        }
        return v;
    }

    public Recycler.V<long[]> longPage(boolean clear) {
        final Recycler.V<long[]> v = longPage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), 0L);
        }
        return v;
    }

    public Recycler.V<float[]> floatPage(boolean clear) {
        final Recycler.V<float[]> v = floatPage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), 0f);
        }
        return v;
    }

    public Recycler.V<double[]> doublePage(boolean clear) {
        final Recycler.V<double[]> v = doublePage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), 0d);
        }
        return v;
    }

    public Recycler.V<Object[]> objectPage() {
        // object pages are cleared on release anyway
        return objectPage.obtain();
    }

    private static <T> Recycler<T> build(Type type, int limit, int estimatedThreadPoolSize, int availableProcessors, Recycler.C<T> c) {
        final Recycler<T> recycler;
        if (limit == 0) {
            recycler = none(c);
        } else {
            recycler = type.build(c, limit, estimatedThreadPoolSize, availableProcessors);
        }
        return recycler;
    }

    public static enum Type {
        QUEUE {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int estimatedThreadPoolSize, int availableProcessors) {
                return concurrentDeque(c, limit);
            }
        },
        SOFT_CONCURRENT {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int estimatedThreadPoolSize, int availableProcessors) {
                return concurrent(softFactory(dequeFactory(c, limit / availableProcessors)), availableProcessors);
            }
        },
        CONCURRENT {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int estimatedThreadPoolSize, int availableProcessors) {
                return concurrent(dequeFactory(c, limit / availableProcessors), availableProcessors);
            }
        },
        NONE {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int estimatedThreadPoolSize, int availableProcessors) {
                return none(c);
            }
        };

        public static Type parse(String type) {
            if (Strings.isNullOrEmpty(type)) {
                return CONCURRENT;
            }
            try {
                return Type.valueOf(type.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ElasticsearchIllegalArgumentException("no type support [" + type + "]");
            }
        }

        abstract <T> Recycler<T> build(Recycler.C<T> c, int limit, int estimatedThreadPoolSize, int availableProcessors);
    }
}
