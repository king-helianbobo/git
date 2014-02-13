package org.elasticsearch.app;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.input.ESTextInputFormat;
import org.elasticsearch.hadoop.mr.ESOutputFormat;
import org.elasticsearch.hadoop.util.WritableUtils;

public class MapReduceSogou extends Configured implements Tool {

	private static final Log log = LogFactory.getLog(MapReduceSogou.class);

	public static class JsonMapper
			extends
				Mapper<LongWritable, Text, LongWritable, MapWritable> {

		protected void setup(Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			// partition = conf.getInt("elasticsearch.partition.seqnumber", 0);
			FileAppender fa = new FileAppender();
			fa.setName("FileLogger");
			fa.setFile("/tmp/3.log");
			fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
			fa.setThreshold(Level.INFO);
			fa.setAppend(true);
			fa.activateOptions();
			Logger.getRootLogger().getLoggerRepository().resetConfiguration();
			Logger.getRootLogger().addAppender(fa);
		}

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] splits = line.split("\\$\\$\\$\\$");
			Map<String, String> entry = new LinkedHashMap<String, String>();
			entry.put("url", splits[0]);
			entry.put("docno", splits[1]);
			entry.put("postTime", splits[2]);
			entry.put("contenttitle", splits[3]);
			entry.put("content", splits[4]);
			// log.info(splits[0] + "  " + splits[1] + " " + splits[2] + " "
			// + splits[3] + " " + splits[4]);
			context.write(key, (MapWritable) WritableUtils.toWritable(entry));
		}
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		// conf.set("fs.default.name", "192.168.50.75:9000");
		// conf.set("mapred.job.tracker", "192.168.50.75:9001");
		conf.set(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		conf.set(ConfigurationOptions.ES_MAPPING_ID, "docno");
		conf.set(ConfigurationOptions.ES_RESOURCE, "sogou_test/table");
		conf.set(ConfigurationOptions.ES_UPSERT_DOC, "false");
		conf.set("es.host", "192.168.50.75");
		conf.setBoolean("mapred.map.tasks.speculative.execution", false);
		conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
		Job job = new Job(conf);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(ESOutputFormat.class);
		job.setMapOutputValueClass(MapWritable.class);
		job.setMapperClass(JsonMapper.class);
		job.setNumReduceTasks(0);
		// ESTextInputFormat.addInputPath(job, new Path("/liubo_output_2"));
		ESTextInputFormat.addInputPath(job, new Path(args[0]));
		job.setJarByClass(MapReduceSogou.class);
		job.setJobName("MapReduceSogou");
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new MapReduceSogou(), args);
		System.exit(ret);
	}
}