package org.elasticsearch.hadoop.input;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.LineReader;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Key视作行偏移值，Value视作行内容
 */
public class ESLineRecordReader extends RecordReader<LongWritable, Text> {
	private static final Log LOG = LogFactory.getLog(ESLineRecordReader.class);
	private long start;
	private long pos;
	private long end;
	private int seqNumber;
	private int count;
	private LineReader in;
	private int maxLineLength;
	private LongWritable key = null;
	private Text value = null;
	private Seekable filePosition;

	public void initialize(InputSplit genericSplit, TaskAttemptContext context)
			throws IOException {
		ESFileSplit split = (ESFileSplit) genericSplit;
		Configuration job = context.getConfiguration();
		this.maxLineLength = job.getInt("mapred.linerecordreader.maxlength",
				Integer.MAX_VALUE);
		start = split.getStart();
		end = start + split.getLength();
		seqNumber = split.getSeqNumber();
		count = split.getFileCount();

		if (count == 1) {
			// job.set("elasticsearch.suffix.name", "");
			job.setInt("elasticsearch.partition.seqnumber", 0);
		} else if (count > 1) {
			job.setInt("elasticsearch.partition.seqnumber", seqNumber);
			job.set("elasticsearch.suffix.name",
					"-" + String.valueOf(seqNumber));
		} else {
			// do nothing
		}
		final Path file = split.getPath();
		// open the file and seek to the start of the split
		FileSystem fs = file.getFileSystem(job);
		FSDataInputStream fileIn = fs.open(split.getPath());

		fileIn.seek(start);
		in = new LineReader(fileIn, job);
		filePosition = fileIn;

		// If this is not the first split, we always throw away first record
		// because we always (except the last split) read one extra line in
		// next() method
		if (start != 0) {
			start += in.readLine(new Text(), 0, maxBytesToConsume(start));
		}
		this.pos = start;
	}

	private int maxBytesToConsume(long pos) {
		return (int) Math.min(Integer.MAX_VALUE, end - pos);
	}

	private long getFilePosition() throws IOException {
		long retVal = pos;
		return retVal;
	}

	public boolean nextKeyValue() throws IOException {
		if (key == null) {
			key = new LongWritable();
		}
		key.set(pos);
		if (value == null) {
			value = new Text();
		}
		int newSize = 0;
		// We always read one extra line, which lies outside the upper
		// split limit i.e. (end - 1)
		while (getFilePosition() <= end) {
			newSize = in.readLine(value, maxLineLength,
					Math.max(maxBytesToConsume(pos), maxLineLength));
			if (newSize == 0) {
				break;
			}
			pos += newSize;
			if (newSize < maxLineLength) {
				break;
			}

			// line too long. try again
			LOG.info("Skipped line of size " + newSize + " at pos "
					+ (pos - newSize));
		}
		if (newSize == 0) {
			key = null;
			value = null;
			return false;
		} else {
			return true;
		}
	}

	@Override
	public LongWritable getCurrentKey() {
		return key;
	}

	@Override
	public Text getCurrentValue() {
		return value;
	}

	/**
	 * Get the progress within the split
	 */
	public float getProgress() throws IOException {
		if (start == end) {
			return 0.0f;
		} else {
			return Math.min(1.0f, (getFilePosition() - start)
					/ (float) (end - start));
		}
	}

	public synchronized void close() throws IOException {
		try {
			if (in != null) {
				in.close();
			}
		} finally {

		}
	}
}
