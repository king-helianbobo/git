package org.elasticsearch.hadoop.input;

import java.io.*;
import java.util.*;
import org.apache.commons.logging.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;

@SuppressWarnings("rawtypes")
public class ESTextInputFormat extends FileInputFormat<LongWritable, Text> {
	private static final Log LOG = LogFactory.getLog(ESTextInputFormat.class);

	// text file must be splitable
	protected boolean isSplitable(JobContext context, Path filename) {
		return true;
	}

	/**
	 * Generate the list of files and make them into FileSplits.
	 */
	public List<InputSplit> getSplits(JobContext job) throws IOException {
		long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
		long maxSize = getMaxSplitSize(job);
		// generate splits
		List<InputSplit> splits = new ArrayList<InputSplit>();
		List<FileStatus> files = listStatus(job);
		for (FileStatus file : files) {
			Path path = file.getPath();
			int partition = 0;
			int count = files.size();
			for (int i = 0; i < count; i++) {
				String str = path.toString();
				String tmpStr = files.get(i).getPath().toString();
				if (str.equals(tmpStr))
					partition = i;
			}
			FileSystem fs = path.getFileSystem(job.getConfiguration());
			long length = file.getLen();
			BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0,
					length);
			if ((length != 0) && isSplitable(job, path)) {
				long blockSize = file.getBlockSize();
				long splitSize = computeSplitSize(blockSize, minSize, maxSize);
				long bytesRemaining = length;
				while (((double) bytesRemaining) / splitSize > 1.1) {
					int blkIndex = getBlockIndex(blkLocations, length
							- bytesRemaining);
					splits.add(new ESFileSplit(path, length - bytesRemaining,
							splitSize, blkLocations[blkIndex].getHosts(),
							partition, count));
					bytesRemaining -= splitSize;
				}

				if (bytesRemaining != 0) {
					splits.add(new ESFileSplit(path, length - bytesRemaining,
							bytesRemaining,
							blkLocations[blkLocations.length - 1].getHosts(),
							partition, count));
				}
			} else if (length != 0) {
				splits.add(new ESFileSplit(path, 0, length, blkLocations[0]
						.getHosts(), partition, count));
			} else {
				// Create empty hosts array for zero length files
				splits.add(new ESFileSplit(path, 0, length, new String[0], 0, 1));
			}
		}
		// Save the number of input files in the job-conf
		job.getConfiguration().setLong(NUM_INPUT_FILES, files.size());
		LOG.debug("Total # of splits: " + splits.size());
		return splits;
	}

	public RecordReader<LongWritable, Text> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException {

		return new ESLineRecordReader();
	}

}
