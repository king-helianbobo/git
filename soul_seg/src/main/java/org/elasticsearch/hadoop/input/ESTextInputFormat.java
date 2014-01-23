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
			Path filePath = file.getPath(); // 获得文件路径
			int partition = 0;
			int count = files.size(); // 获得文件总数
			for (int i = 0; i < count; i++) {
				String str = filePath.toString();
				String tmpStr = files.get(i).getPath().toString();
				if (str.equals(tmpStr))
					partition = i; // 获得文件编号，从0开始
			}
			FileSystem fs = filePath.getFileSystem(job.getConfiguration());
			long length = file.getLen();
			BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0,
					length);
			if ((length != 0) && isSplitable(job, filePath)) {
				long blockSize = file.getBlockSize();
				long splitSize = computeSplitSize(blockSize, minSize, maxSize);
				long bytesRemaining = length;
				while (((double) bytesRemaining) / splitSize > 1.1) {
					int blkIndex = getBlockIndex(blkLocations, length
							- bytesRemaining);
					splits.add(new ESFileSplit(filePath, length
							- bytesRemaining, splitSize, blkLocations[blkIndex]
							.getHosts(), partition, count));
					bytesRemaining -= splitSize;
				}
				if (bytesRemaining != 0) {
					splits.add(new ESFileSplit(filePath, length
							- bytesRemaining, bytesRemaining,
							blkLocations[blkLocations.length - 1].getHosts(),
							partition, count));
				}
			} else if (length != 0) { // 如果文件不可分
				splits.add(new ESFileSplit(filePath, 0, length, blkLocations[0]
						.getHosts(), partition, count));
			} else { // 如果文件长度为0
				splits.add(new ESFileSplit(filePath, 0, length, new String[0]));
			}
		}
		// Save the number of input files in the global configuration file
		job.getConfiguration().setLong(NUM_INPUT_FILES, files.size());
		LOG.debug("Total # of splits: " + splits.size());
		return splits;
	}

	public RecordReader<LongWritable, Text> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException {
		return new ESLineRecordReader();
	}

}
