package org.elasticsearch.hadoop.input;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * 类存储了文件在全部文件序列中的编号，编号用于给ES记录提供一个惟一的ID
 */
public class ESFileSplit extends InputSplit implements Writable {
	private Path file;
	private long start;
	private long length;
	private String[] hosts;

	private int seqNumber;// add partition number to generate ES id
	private int count; // number of total files

	ESFileSplit() {
	}

	public ESFileSplit(Path file, long start, long length, String[] hosts,
			int seq, int count) {
		this.file = file;
		this.start = start;
		this.length = length;
		this.hosts = hosts;
		this.seqNumber = seq;
		this.count = count;
	}

	public ESFileSplit(Path file, long start, long length, String[] hosts) {
		this.file = file;
		this.start = start;
		this.length = length;
		this.hosts = hosts;
		this.seqNumber = -1; // if value less than 0,we ignore it
		this.count = -1;// if value less than 0,we ignore it
	}

	public int getSeqNumber() {
		return seqNumber;
	}

	public int getFileCount() {
		return count;
	}

	/** The file containing this split's data. */
	public Path getPath() {
		return file;
	}

	/** The position of the first byte in the file to process. */
	public long getStart() {
		return start;
	}

	/** The number of bytes in the file to process. */
	@Override
	public long getLength() {
		return length;
	}

	@Override
	public String toString() {
		return file + ":" + start + "+" + length + "+" + seqNumber + "+"
				+ count;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		Text.writeString(out, file.toString());
		out.writeLong(start);
		out.writeLong(length);
		out.writeInt(seqNumber);
		out.writeInt(count);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		file = new Path(Text.readString(in));
		start = in.readLong();
		length = in.readLong();
		seqNumber = in.readInt();
		count = in.readInt();
		hosts = null;
	}

	@Override
	public String[] getLocations() throws IOException {
		if (this.hosts == null) {
			return new String[] {};
		} else {
			return this.hosts;
		}
	}
}
