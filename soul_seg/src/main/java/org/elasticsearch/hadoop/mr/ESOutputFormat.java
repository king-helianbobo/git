package org.elasticsearch.hadoop.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobSubmissionFiles;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.Progressable;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.cfg.SettingsManager;
import org.elasticsearch.hadoop.rest.BufferedRestClient;
import org.elasticsearch.hadoop.rest.InitializationUtils;
import org.elasticsearch.hadoop.rest.Node;
import org.elasticsearch.hadoop.rest.Shard;
import org.elasticsearch.hadoop.serialization.MapWritableIdExtractor;
import org.elasticsearch.hadoop.serialization.SerializationUtils;
import org.elasticsearch.hadoop.util.Assert;

/**
 * 写入内容到ElasticSearch中
 */
@SuppressWarnings("rawtypes")
// since this class implements two generic interfaces, to avoid dealing with 4
// types in every declaration, we force raw types...
public class ESOutputFormat extends OutputFormat implements
		org.apache.hadoop.mapred.OutputFormat, ConfigurationOptions {

	protected static Log log = LogFactory.getLog(ESOutputFormat.class);

	// 实现ESOutputCommiter，避免调用默认的FileOutputCommitter
	public static class ESOutputCommitter extends
			org.apache.hadoop.mapreduce.OutputCommitter {
		@Override
		public void setupJob(JobContext jobContext) throws IOException {
		}

		@Deprecated
		public void cleanupJob(JobContext jobContext) throws IOException {
		}

		@Override
		public void setupTask(TaskAttemptContext context) throws IOException {
		}

		@Override
		public boolean needsTaskCommit(TaskAttemptContext cxt)
				throws IOException {
			return false;
		}

		@Override
		public void commitTask(TaskAttemptContext taskContext)
				throws IOException {
		}

		@Override
		public void abortTask(TaskAttemptContext taskContext)
				throws IOException {
		}
	}

	// 实现ESOldAPIOutputCommitter，避免调用默认的FileOutputCommitter
	public static class ESOldOutputCommitter extends
			org.apache.hadoop.mapred.OutputCommitter {
		@Override
		public void setupJob(org.apache.hadoop.mapred.JobContext jobCxt)
				throws IOException {
		}

		@Override
		public void setupTask(
				org.apache.hadoop.mapred.TaskAttemptContext taskCxt)
				throws IOException {
		}

		@Override
		public boolean needsTaskCommit(
				org.apache.hadoop.mapred.TaskAttemptContext taskCxt)
				throws IOException {
			return false;
		}

		@Override
		public void commitTask(
				org.apache.hadoop.mapred.TaskAttemptContext taskCxt)
				throws IOException {
		}

		@Override
		public void abortTask(
				org.apache.hadoop.mapred.TaskAttemptContext taskCxt)
				throws IOException {
		}

		@Override
		@Deprecated
		public void cleanupJob(org.apache.hadoop.mapred.JobContext cxt)
				throws IOException {
		}
	}

	// new API ,转发给旧的API接口
	@Override
	public org.apache.hadoop.mapreduce.RecordWriter getRecordWriter(
			TaskAttemptContext context) {
		return (org.apache.hadoop.mapreduce.RecordWriter) getRecordWriter(null,
				(JobConf) context.getConfiguration(), null, context);
	}

	@Override
	public void checkOutputSpecs(JobContext context) throws IOException {
		// careful as it seems the configuration saved is discarded
		init(context.getConfiguration());
	}

	@Override
	public org.apache.hadoop.mapreduce.OutputCommitter getOutputCommitter(
			TaskAttemptContext context) {
		return new ESOutputCommitter();
	}

	// old API
	@Override
	public org.apache.hadoop.mapred.RecordWriter getRecordWriter(
			FileSystem ignored, JobConf job, String name, Progressable progress) {
		return new ElasticSearchRecordWriter(job);
	}

	@Override
	public void checkOutputSpecs(FileSystem ignored, JobConf cfg)
			throws IOException {
		init(cfg);
	}

	protected static class ElasticSearchRecordWriter extends RecordWriter
			implements org.apache.hadoop.mapred.RecordWriter {

		protected final Configuration cfg;
		protected BufferedRestClient client;
		private String uri, resource;
		protected boolean initialized = false;

		public ElasticSearchRecordWriter(Configuration cfg) {
			this.cfg = cfg;
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException {
			close((Reporter) null);
		}

		@Override
		public void close(Reporter reporter) throws IOException {
			if (log.isTraceEnabled()) {
				log.trace(String.format("Close RecordWriter [%s][%s]", uri,
						resource));
			}
			client.close();
			initialized = false;
		}

		@Override
		public void write(Object key, Object value) throws IOException {
			if (!initialized) {
				initialized = true;
				init();
			}
			client.writeToIndex(value);
		}

		protected void init() throws IOException {
			// this function is executed by each MapTask or ReduceTask
			int seqNumber = detectTaskSeqNumber(cfg); // 获得任务序号
			// this Task's seqNumber
			if (log.isTraceEnabled()) {
				log.trace(String.format(
						"RecordWriter  [%s] initialize inputSplit", seqNumber));
			}
			Settings settings = SettingsManager.loadFrom(cfg);
			SerializationUtils.setValueWriterIfNotSet(settings,
					MapReduceWriter.class, log);
			InitializationUtils.setIdExtractorIfNotSet(settings,
					MapWritableIdExtractor.class, log);
			client = new BufferedRestClient(settings);
			resource = settings.getIndexType();

			// 这个参数不一定必须设置，只有ESTextInputFormat设置了，才能读到
			String suffix = cfg.get("elasticsearch.suffix.name", "");
			if ((suffix != null) && (suffix.length() > 0)) {
				settings.setResource(resource + suffix);
			}
			// create the index if needed
			if (client.touch()) {
				if (client.waitForYellow()) {
					log.warn(String.format(
							"Waiting for index [%s] to reach yellow health",
							resource));
				}
			}

			// 获得分片以及每个分片所处节点信息
			Map<Shard, Node> targetShards = client.getTargetPrimaryShards();
			List<Shard> orderedShards = new ArrayList<Shard>(
					targetShards.keySet());
			// 对分片进行排序，确保有序
			Collections.sort(orderedShards);
			if (log.isTraceEnabled()) {
				log.trace(String
						.format("RecordWriter instance[%s] discovered %s primary shards %s",
								seqNumber, orderedShards.size(), orderedShards));
			}
			int bucket = seqNumber % targetShards.size();
			Shard chosenShard = orderedShards.get(bucket);
			Node targetNode = targetShards.get(chosenShard);
			client.close();
			// 修改全局设置，使MapTask或ReduceTask直接与目标节点通信
			settings.cleanUri().setHost(targetNode.getIpAddress())
					.setPort(targetNode.getHttpPort());
			client = new BufferedRestClient(settings);
			uri = settings.getTargetUri();
			if (log.isDebugEnabled()) {
				log.debug(String
						.format("RecordWriter instance[%s] assigned to primary shard [%s] at address [%s]",
								seqNumber, chosenShard.getName(), uri));
			}
		}

		private static int numberOfTaskAttempts(Configuration conf)
				throws IOException {
			int numReducers = conf.getInt("mapred.reduce.tasks", 1);
			if (numReducers < 1) { // no reducers
				String dir = conf.get("mapreduce.job.dir");
				Path path = JobSubmissionFiles
						.getJobSplitMetaFile(new Path(dir));
				FSDataInputStream in = path.getFileSystem(conf).open(path);
				try {
					byte[] header = new byte[8]; // skip first "META-SPL"
					in.readFully(header);
					WritableUtils.readVInt(in); // skip version
					return WritableUtils.readVInt(in); // read number of splits
				} finally {
					try {
						in.close();
					} catch (Exception ex) {
					}
				}
			}
			return numReducers;
		}

		private int detectTaskSeqNumber(Configuration conf) {
			TaskAttemptID attempt = TaskAttemptID.forName(conf
					.get("mapred.task.id"));
			Assert.notNull(attempt,
					"Unable to determine task id , check setting through the issue tracker");
			log.info("task number = " + attempt.getTaskID().getId());
			try {
				int nummaps = numberOfTaskAttempts(conf);
				log.info("number of maptasks = " + nummaps);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return attempt.getTaskID().getId();
		}
	}

	private void init(Configuration cfg) throws IOException {
		// this function is executed before each MapTask is launched
		Settings settings = SettingsManager.loadFrom(cfg);
		Assert.hasText(settings.getIndexType(),
				String.format("No resource ['%s'] specified", ES_RESOURCE));
		BufferedRestClient client = null;
		InitializationUtils.checkIdForOperation(settings);
		InitializationUtils.checkIndexExistence(settings, client); // 检查index是否存在

		if (cfg.get("mapred.reduce.tasks") != null) {
			if (cfg.getBoolean("mapred.reduce.tasks.speculative.execution",
					true)) {
				// 禁止ReduceTask猜测执行，因为ES是外部资源，不可控制
				log.warn("Speculative execution enabled for reducer - consider disabling it to prevent data corruption");
			}
		} else {
			// 因为ES存储在外部，推测执行时，失败的Task做出的改变无法删除，不允许推测执行
			if (cfg.getBoolean("mapred.map.tasks.speculative.execution", true)) {
				log.warn("Speculative execution enabled for mapper - consider disabling it to prevent data corruption");
			}
		}
		log.info(String.format("Preparing to write/index to [%s][%s]",
				settings.getTargetUri(), settings.getIndexType()));
	}
}