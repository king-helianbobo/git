package org.elasticsearch.main;

import java.io.IOException;
import java.util.regex.Pattern;
import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.log4j.*;
import org.elasticsearch.hadoop.mr.*;

public class ESearchTest {
	private static final Log log = LogFactory.getLog(ESearchTest.class);

	public static boolean isNum(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		boolean ret = pattern.matcher(str).matches();
		if ("".equals(str))
			return false;
		else
			return ret;
	}

	/* Convert Seconds to time format HH:mm:ss */
	public static String secondsToTime(Long seconds) {
		String hour = "";
		String minute = "";
		String second = "";

		hour = Long.toString(seconds / 3600);
		minute = Long.toString(seconds % 3600 / 60);
		second = Long.toString(seconds % 60);

		hour = (hour.length() < 2) ? ("0" + hour) : hour;
		minute = (minute.length() < 2) ? ("0" + minute) : minute;
		second = (second.length() < 2) ? ("0" + second) : second;

		return (hour + ":" + minute + ":" + second);
	}

	/* Convert time format HH:mm:ss to seconds */
	public static long timeToSeconds(String time) {

		String[] splits = time.split(":");
		long seconds = 0L;
		int hour = 0;
		int minute = 0;
		int second = 0;

		hour = Integer.parseInt(splits[0]);
		minute = Integer.parseInt(splits[1]);
		second = Integer.parseInt(splits[2]);
		seconds = hour * 3600 + minute * 60 + second;

		return seconds;
	}

	public static class MyPartitioner extends Partitioner<Text, LongWritable> {
		public int getPartition(Text key, LongWritable value, int numPartitions) {
			int number = 0;
			String[] splits = key.toString().split(",");

			number = Integer.parseInt(splits[0].substring(
					splits[0].length() - 2, splits[0].length()));
			return number % numPartitions;
		}
	}

	public static class Map
			extends
				Mapper<LongWritable, Text, Text, LongWritable> {

		private Text keyValue = new Text();
		private LongWritable one = new LongWritable(1);

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = value.toString();
			String[] splits = line.split("\\|");
			String buildStr = "";
			int len = splits.length;

			if (len == 5)
				buildStr = splits[0] + "," + splits[1].replace(" ", ",") + ","
						+ splits[3] + "," + splits[4];
			else if (len == 3)
				buildStr = splits[0] + "," + splits[1].replace(" ", ",")
						+ ",NULL,NULL";

			if (!("".equals(buildStr))) {
				keyValue.set(buildStr);
				context.write(keyValue, one);
			}
		}
	}

	public static class Reduce
			extends
				Reducer<Text, LongWritable, Text, LongWritable> {

		private long lastTime = 0;
		private String cardNo = "";
		private String lastChannel = "";
		private String lastProgram = "";
		private long duration = 0;
		private Text outputKey = new Text();
		private static final Log log = LogFactory.getLog(Reduce.class);

		protected void setup(Context context) throws IOException,
				InterruptedException {
			File file = new File("/tmp/liubo.log");
			if (file.exists())
				file.delete();
			FileAppender fa = new FileAppender();
			fa.setName("FileLogger");
			fa.setFile("/tmp/liubo.log");
			fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
			fa.setThreshold(Level.INFO);
			fa.setAppend(true);
			fa.activateOptions();
			Logger.getRootLogger().getLoggerRepository().resetConfiguration();
			Logger.getRootLogger().addAppender(fa);
		}

		public void reduce(Text key, Iterable<LongWritable> values,
				Context context) throws IOException, InterruptedException {
			long currTime = 0;
			String[] splitKey = key.toString().split(",");
			String outputKeyStr = "";
			currTime = timeToSeconds(splitKey[2]);
			if (splitKey[0].equals(cardNo) && lastTime != 0) {
				if ("NULL".equals(lastChannel) && "NULL".equals(lastProgram)) {
					if ("NULL".equals(splitKey[3])
							&& "NULL".equals(splitKey[4]))
						lastTime = 0;
				} else if (!(splitKey[3].equals(lastChannel) && splitKey[4]
						.equals(lastProgram))) {
					cardNo = splitKey[0];
					duration = currTime - lastTime;
					outputKeyStr = cardNo + "," + splitKey[1] + ","
							+ secondsToTime(lastTime) + "," + lastChannel + ","
							+ lastProgram;
					lastTime = currTime;
					outputKey.set(outputKeyStr);
					// log.info("output = " + outputKeyStr + "duration = "
					// + duration);
					context.write(outputKey, new LongWritable(duration));
				}
			} else if (splitKey[0].equals(cardNo)) {
				lastTime = currTime;
			} else {
				lastTime = currTime;
				cardNo = splitKey[0];
			}
			lastChannel = splitKey[3];
			lastProgram = splitKey[4];
		}
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		conf.set("es.resource", "liubo/test2");
		conf.set("es.host", "192.168.50.75");
		conf.setBoolean("mapred.map.tasks.speculative.execution", false);
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);

		Job job = new Job(conf, "ESearchTest");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setPartitionerClass(MyPartitioner.class);
		job.setNumReduceTasks(10);
		conf.set("mapred.textoutputformat.separator", ",");
		job.setJarByClass(ESearchTest.class);
		TextInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(ESOutputFormat.class);
		job.waitForCompletion(true);
		log.info("ESearchTest");
	}
}
