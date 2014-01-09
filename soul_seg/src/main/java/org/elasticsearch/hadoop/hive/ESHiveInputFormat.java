/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.hadoop.hive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.elasticsearch.hadoop.mr.ESInputFormat;

/**
 * Hive specific InputFormat. Since Hive code base makes a lot of assumptions
 * about the tables being actual files in HDFS (using instanceof checks without
 * proper else) this class tries to 'fix' this by adding a dummy
 * {@link FileInputFormat} to ESInputFormat.
 */

// A quick example would be {@link
// org.apache.hadoop.hive.ql.io.HiveInputFormat.HiveInputSplit#getPath()} which,
// in case the actual InputSplit is not a
// {@link org.apache.hadoop.mapred.FileSplit}, returns an invalid Path.

public class ESHiveInputFormat extends ESInputFormat<Text, MapWritable> {

	static class ESHiveSplit extends FileSplit {
		InputSplit delegate;
		private Path path;

		ESHiveSplit() {
			this(new ShardInputSplit(), null);
		}

		ESHiveSplit(InputSplit delegate, Path path) {
			super(path, 0, 0, (String[]) null);
			this.delegate = delegate;
			this.path = path;
		}

		public long getLength() {
			// TODO: can this be delegated?
			return 1L;
		}

		public String[] getLocations() throws IOException {
			return delegate.getLocations();
		}

		public void write(DataOutput out) throws IOException {
			Text.writeString(out, path.toString());
			delegate.write(out);
		}

		public void readFields(DataInput in) throws IOException {
			path = new Path(Text.readString(in));
			delegate.readFields(in);
		}

		@Override
		public String toString() {
			return delegate.toString();
		}

		@Override
		public Path getPath() {
			return path;
		}
	}

	@Override
	public FileSplit[] getSplits(JobConf job, int numSplits) throws IOException {
		// decorate original splits as FileSplit
		InputSplit[] shardSplits = super.getSplits(job, numSplits);
		FileSplit[] wrappers = new FileSplit[shardSplits.length];
		Path path = new Path(job.get(HiveConstants.TABLE_LOCATION));
		for (int i = 0; i < wrappers.length; i++) {
			wrappers[i] = new ESHiveSplit(shardSplits[i], path);
		}
		return wrappers;
	}

	@Override
	public WritableShardRecordReader getRecordReader(InputSplit split,
			JobConf job, Reporter reporter) {
		return new WritableShardRecordReader(((ESHiveSplit) split).delegate,
				job, reporter);
	}
}