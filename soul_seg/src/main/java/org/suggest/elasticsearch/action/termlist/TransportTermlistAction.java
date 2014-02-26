package org.suggest.elasticsearch.action.termlist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.TransportBroadcastOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.GroupShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.service.InternalIndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.splitword.soul.utility.CompactHashMap;
import org.suggest.elasticsearch.service.ShardSuggestService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

/**
 * Term list index/indices action
 */
public class TransportTermlistAction
		extends
			TransportBroadcastOperationAction<TermlistRequest, TermlistResponse, ShardTermlistRequest, ShardTermlistResponse> {
	private static Log log = LogFactory.getLog(TransportTermlistAction.class);
	private final IndicesService indicesService;

	@Inject
	public TransportTermlistAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService,
			IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	@Override
	protected String transportAction() {
		return TermlistAction.NAME;
	}

	@Override
	protected TermlistRequest newRequest() {
		return new TermlistRequest();
	}

	@Override
	protected TermlistResponse newResponse(TermlistRequest request,
			AtomicReferenceArray shardsResponses, ClusterState clusterState) {
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;
		Map<String, TermInfoPojo> map = new CompactHashMap<String, TermInfoPojo>();
		for (int i = 0; i < shardsResponses.length(); i++) {
			Object shardResponse = shardsResponses.get(i);
			if (shardResponse instanceof BroadcastShardOperationFailedException) {
				failedShards++;
				if (shardFailures == null) {
					shardFailures = newLinkedList();
				}
				shardFailures
						.add(new DefaultShardOperationFailedException(
								(BroadcastShardOperationFailedException) shardResponse));
			} else {
				successfulShards++;
				if (shardResponse instanceof ShardTermlistResponse) {
					ShardTermlistResponse resp = (ShardTermlistResponse) shardResponse;
					merge(map, resp.getTermList());
				}
			}
		}
		map = request.getWithTotalFreq()
				? sortTotalFreq(map, request.getSize())
				: request.getWithDocFreq()
						? sortDocFreq(map, request.getSize())
						: truncate(map, request.getSize());

		return new TermlistResponse(shardsResponses.length(), successfulShards,
				failedShards, shardFailures, map);
	}

	@Override
	protected ShardTermlistRequest newShardRequest() {
		return new ShardTermlistRequest();
	}

	@Override
	protected ShardTermlistRequest newShardRequest(ShardRouting shard,
			TermlistRequest request) {
		return new ShardTermlistRequest(shard.index(), shard.id(), request);
	}

	@Override
	protected ShardTermlistResponse newShardResponse() {
		return new ShardTermlistResponse();
	}

	/**
	 * The termlist request works against primary shards.
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState,
			TermlistRequest request, String[] concreteIndices) {
		return clusterState.routingTable().activePrimaryShardsGrouped(
				concreteIndices, true);
	}

	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state,
			TermlistRequest request) {
		return state.blocks()
				.globalBlockedException(ClusterBlockLevel.METADATA);
	}

	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state,
			TermlistRequest request, String[] concreteIndices) {
		return state.blocks().indicesBlockedException(
				ClusterBlockLevel.METADATA, concreteIndices);
	}

	@Override
	protected ShardTermlistResponse shardOperation(ShardTermlistRequest request)
			throws ElasticSearchException {
		InternalIndexShard indexShard = (InternalIndexShard) indicesService
				.indexServiceSafe(request.index()).shardSafe(request.shardId());
		Engine.Searcher searcher = indexShard.engine().acquireSearcher(
				"termlist");
		try {
			Map<String, TermInfoPojo> map = new CompactHashMap<String, TermInfoPojo>();
			IndexReader reader = searcher.reader();
			Fields fields = MultiFields.getFields(reader);

			if (fields != null) {
				for (String field : fields) {
					log.info(field + "/" + request.getField());
					if (field.charAt(0) == '_') {
						// skip internal fields
						continue;
					}
					if (request.getField() == null
							|| field.equals(request.getField())) {
						Terms terms = fields.terms(field);
						if (terms != null) {
							TermsEnum termsEnum = terms.iterator(null);
							BytesRef text;
							while ((text = termsEnum.next()) != null) {
								// skip invalid terms
								if (termsEnum.docFreq() < 1)
									continue;
								if (termsEnum.totalTermFreq() < 1)
									continue;
								log.info("docFreq = " + termsEnum.docFreq()
										+ "/" + termsEnum.totalTermFreq());
								String str = text.utf8ToString();
								TermInfoPojo t = new TermInfoPojo();
								if (request.getWithDocFreq()) {
									t.docfreq(termsEnum.docFreq());
									if (map.containsKey(str))
										t.docfreq(termsEnum.docFreq()
												+ map.get(str).getDocFreq());

								}
								if (request.getWithTotalFreq()) {
									t.totalFreq(termsEnum.totalTermFreq());
									if (map.containsKey(str))
										t.totalFreq(termsEnum.totalTermFreq()
												+ map.get(str).getTotalFreq());
								}
								// log.info(tOther.getKey());
								map.put(text.utf8ToString(), t);

							}
						}
					}
				}
			}
			return new ShardTermlistResponse(request.index(),
					request.shardId(), map);
		} catch (IOException ex) {
			throw new ElasticSearchException(ex.getMessage(), ex);
		} finally {
			searcher.release();
		}
	}
	private void merge(Map<String, TermInfoPojo> map, Map<String, TermInfoPojo> other) {
		for (Map.Entry<String, TermInfoPojo> tOther : other.entrySet()) {
			// log.info(tOther.getKey());
			if (map.containsKey(tOther.getKey())) {
				log.info(tOther.getKey());
				TermInfoPojo info = map.get(tOther.getKey());
				Integer docFreq = info.getDocFreq();
				if (docFreq != null) {
					if (tOther.getValue().getDocFreq() != null) {
						info.docfreq(docFreq + tOther.getValue().getDocFreq());
					}
				} else {
					if (tOther.getValue().getDocFreq() != null) {
						info.docfreq(tOther.getValue().getDocFreq());
					}
				}
				Long totalFreq = info.getTotalFreq();
				if (totalFreq != null) {
					if (tOther.getValue().getTotalFreq() != null) {
						info.totalFreq(totalFreq
								+ tOther.getValue().getTotalFreq());
					}
				} else {
					if (tOther.getValue().getTotalFreq() != null) {
						info.totalFreq(tOther.getValue().getTotalFreq());
					}
				}
			} else {
				map.put(tOther.getKey(), tOther.getValue());
			}
		}
	}

	private SortedMap<String, TermInfoPojo> sortTotalFreq(
			final Map<String, TermInfoPojo> map, Integer size) {
		Comparator<String> comp = new Comparator<String>() {
			@Override
			public int compare(String t1, String t2) {
				Long l1 = map.get(t1).getTotalFreq();
				String s1 = Long.toString(l1).length() + Long.toString(l1) + t1;
				Long l2 = map.get(t2).getTotalFreq();
				String s2 = Long.toString(l2).length() + Long.toString(l2) + t2;
				return -s1.compareTo(s2);
			}
		};
		TreeMap<String, TermInfoPojo> m = new TreeMap<String, TermInfoPojo>(comp);
		m.putAll(map);
		if (size != null && size > 0) {
			TreeMap<String, TermInfoPojo> n = new TreeMap<String, TermInfoPojo>(comp);
			Map.Entry<String, TermInfoPojo> me = m.pollFirstEntry();
			while (me != null && size-- > 0) {
				n.put(me.getKey(), me.getValue());
				me = m.pollFirstEntry();
			}
			return n;
		}
		return m;
	}

	private SortedMap<String, TermInfoPojo> sortDocFreq(
			final Map<String, TermInfoPojo> map, Integer size) {
		Comparator<String> comp = new Comparator<String>() {
			@Override
			public int compare(String t1, String t2) {
				Integer i1 = map.get(t1).getDocFreq();
				String s1 = Integer.toString(i1).length()
						+ Integer.toString(i1) + t1;
				Integer i2 = map.get(t2).getDocFreq();
				String s2 = Integer.toString(i2).length()
						+ Integer.toString(i2) + t2;
				return -s1.compareTo(s2);
			}
		};
		TreeMap<String, TermInfoPojo> m = new TreeMap<String, TermInfoPojo>(comp);
		m.putAll(map);
		if (size != null && size > 0) {
			TreeMap<String, TermInfoPojo> n = new TreeMap<String, TermInfoPojo>(comp);
			Map.Entry<String, TermInfoPojo> me = m.pollFirstEntry();
			while (me != null && size-- > 0) {
				n.put(me.getKey(), me.getValue());
				me = m.pollFirstEntry();
			}
			return n;
		}
		return m;
	}

	private Map<String, TermInfoPojo> truncate(Map<String, TermInfoPojo> source,
			Integer max) {
		if (max == null || max < 1) {
			return source;
		}
		int count = 0;
		Map<String, TermInfoPojo> target = new CompactHashMap<String, TermInfoPojo>();
		for (Map.Entry<String, TermInfoPojo> entry : source.entrySet()) {
			if (count >= max) {
				break;
			}
			target.put(entry.getKey(), entry.getValue());
			count++;
		}
		return target;
	}

}