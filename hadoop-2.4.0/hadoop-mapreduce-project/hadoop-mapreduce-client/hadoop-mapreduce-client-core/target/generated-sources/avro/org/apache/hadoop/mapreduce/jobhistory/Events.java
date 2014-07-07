/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.apache.hadoop.mapreduce.jobhistory;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface Events {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"Events\",\"namespace\":\"org.apache.hadoop.mapreduce.jobhistory\",\"types\":[{\"type\":\"record\",\"name\":\"JhCounter\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"displayName\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"JhCounterGroup\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"displayName\",\"type\":\"string\"},{\"name\":\"counts\",\"type\":{\"type\":\"array\",\"items\":\"JhCounter\"}}]},{\"type\":\"record\",\"name\":\"JhCounters\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"groups\",\"type\":{\"type\":\"array\",\"items\":\"JhCounterGroup\"}}]},{\"type\":\"record\",\"name\":\"JobFinished\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"finishedMaps\",\"type\":\"int\"},{\"name\":\"finishedReduces\",\"type\":\"int\"},{\"name\":\"failedMaps\",\"type\":\"int\"},{\"name\":\"failedReduces\",\"type\":\"int\"},{\"name\":\"totalCounters\",\"type\":\"JhCounters\"},{\"name\":\"mapCounters\",\"type\":\"JhCounters\"},{\"name\":\"reduceCounters\",\"type\":\"JhCounters\"}]},{\"type\":\"record\",\"name\":\"JobInited\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"launchTime\",\"type\":\"long\"},{\"name\":\"totalMaps\",\"type\":\"int\"},{\"name\":\"totalReduces\",\"type\":\"int\"},{\"name\":\"jobStatus\",\"type\":\"string\"},{\"name\":\"uberized\",\"type\":\"boolean\"}]},{\"type\":\"record\",\"name\":\"AMStarted\",\"fields\":[{\"name\":\"applicationAttemptId\",\"type\":\"string\"},{\"name\":\"startTime\",\"type\":\"long\"},{\"name\":\"containerId\",\"type\":\"string\"},{\"name\":\"nodeManagerHost\",\"type\":\"string\"},{\"name\":\"nodeManagerPort\",\"type\":\"int\"},{\"name\":\"nodeManagerHttpPort\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"JobSubmitted\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"jobName\",\"type\":\"string\"},{\"name\":\"userName\",\"type\":\"string\"},{\"name\":\"submitTime\",\"type\":\"long\"},{\"name\":\"jobConfPath\",\"type\":\"string\"},{\"name\":\"acls\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"jobQueueName\",\"type\":\"string\"},{\"name\":\"workflowId\",\"type\":\"string\"},{\"name\":\"workflowName\",\"type\":\"string\"},{\"name\":\"workflowNodeName\",\"type\":\"string\"},{\"name\":\"workflowAdjacencies\",\"type\":\"string\"},{\"name\":\"workflowTags\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"JobInfoChange\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"submitTime\",\"type\":\"long\"},{\"name\":\"launchTime\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"JobPriorityChange\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"priority\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"JobStatusChanged\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"jobStatus\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"JobQueueChange\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"jobQueueName\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"JobUnsuccessfulCompletion\",\"fields\":[{\"name\":\"jobid\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"finishedMaps\",\"type\":\"int\"},{\"name\":\"finishedReduces\",\"type\":\"int\"},{\"name\":\"jobStatus\",\"type\":\"string\"},{\"name\":\"diagnostics\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"MapAttemptFinished\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"attemptId\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"taskStatus\",\"type\":\"string\"},{\"name\":\"mapFinishTime\",\"type\":\"long\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"hostname\",\"type\":\"string\"},{\"name\":\"port\",\"type\":\"int\"},{\"name\":\"rackname\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"},{\"name\":\"counters\",\"type\":\"JhCounters\"},{\"name\":\"clockSplits\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"cpuUsages\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"vMemKbytes\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"physMemKbytes\",\"type\":{\"type\":\"array\",\"items\":\"int\"}}]},{\"type\":\"record\",\"name\":\"ReduceAttemptFinished\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"attemptId\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"taskStatus\",\"type\":\"string\"},{\"name\":\"shuffleFinishTime\",\"type\":\"long\"},{\"name\":\"sortFinishTime\",\"type\":\"long\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"hostname\",\"type\":\"string\"},{\"name\":\"port\",\"type\":\"int\"},{\"name\":\"rackname\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"},{\"name\":\"counters\",\"type\":\"JhCounters\"},{\"name\":\"clockSplits\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"cpuUsages\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"vMemKbytes\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"physMemKbytes\",\"type\":{\"type\":\"array\",\"items\":\"int\"}}]},{\"type\":\"record\",\"name\":\"TaskAttemptFinished\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"attemptId\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"taskStatus\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"rackname\",\"type\":\"string\"},{\"name\":\"hostname\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"},{\"name\":\"counters\",\"type\":\"JhCounters\"}]},{\"type\":\"record\",\"name\":\"TaskAttemptStarted\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"attemptId\",\"type\":\"string\"},{\"name\":\"startTime\",\"type\":\"long\"},{\"name\":\"trackerName\",\"type\":\"string\"},{\"name\":\"httpPort\",\"type\":\"int\"},{\"name\":\"shufflePort\",\"type\":\"int\"},{\"name\":\"containerId\",\"type\":\"string\"},{\"name\":\"locality\",\"type\":\"string\"},{\"name\":\"avataar\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"TaskAttemptUnsuccessfulCompletion\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"attemptId\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"hostname\",\"type\":\"string\"},{\"name\":\"port\",\"type\":\"int\"},{\"name\":\"rackname\",\"type\":\"string\"},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"error\",\"type\":\"string\"},{\"name\":\"counters\",\"type\":\"JhCounters\"},{\"name\":\"clockSplits\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"cpuUsages\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"vMemKbytes\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"physMemKbytes\",\"type\":{\"type\":\"array\",\"items\":\"int\"}}]},{\"type\":\"record\",\"name\":\"TaskFailed\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"error\",\"type\":\"string\"},{\"name\":\"failedDueToAttempt\",\"type\":[\"null\",\"string\"]},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"counters\",\"type\":\"JhCounters\"}]},{\"type\":\"record\",\"name\":\"TaskFinished\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"counters\",\"type\":\"JhCounters\"},{\"name\":\"successfulAttemptId\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"TaskStarted\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"taskType\",\"type\":\"string\"},{\"name\":\"startTime\",\"type\":\"long\"},{\"name\":\"splitLocations\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"TaskUpdated\",\"fields\":[{\"name\":\"taskid\",\"type\":\"string\"},{\"name\":\"finishTime\",\"type\":\"long\"}]},{\"type\":\"enum\",\"name\":\"EventType\",\"symbols\":[\"JOB_SUBMITTED\",\"JOB_INITED\",\"JOB_FINISHED\",\"JOB_PRIORITY_CHANGED\",\"JOB_STATUS_CHANGED\",\"JOB_QUEUE_CHANGED\",\"JOB_FAILED\",\"JOB_KILLED\",\"JOB_ERROR\",\"JOB_INFO_CHANGED\",\"TASK_STARTED\",\"TASK_FINISHED\",\"TASK_FAILED\",\"TASK_UPDATED\",\"NORMALIZED_RESOURCE\",\"MAP_ATTEMPT_STARTED\",\"MAP_ATTEMPT_FINISHED\",\"MAP_ATTEMPT_FAILED\",\"MAP_ATTEMPT_KILLED\",\"REDUCE_ATTEMPT_STARTED\",\"REDUCE_ATTEMPT_FINISHED\",\"REDUCE_ATTEMPT_FAILED\",\"REDUCE_ATTEMPT_KILLED\",\"SETUP_ATTEMPT_STARTED\",\"SETUP_ATTEMPT_FINISHED\",\"SETUP_ATTEMPT_FAILED\",\"SETUP_ATTEMPT_KILLED\",\"CLEANUP_ATTEMPT_STARTED\",\"CLEANUP_ATTEMPT_FINISHED\",\"CLEANUP_ATTEMPT_FAILED\",\"CLEANUP_ATTEMPT_KILLED\",\"AM_STARTED\"]},{\"type\":\"record\",\"name\":\"Event\",\"fields\":[{\"name\":\"type\",\"type\":\"EventType\"},{\"name\":\"event\",\"type\":[\"JobFinished\",\"JobInfoChange\",\"JobInited\",\"AMStarted\",\"JobPriorityChange\",\"JobQueueChange\",\"JobStatusChanged\",\"JobSubmitted\",\"JobUnsuccessfulCompletion\",\"MapAttemptFinished\",\"ReduceAttemptFinished\",\"TaskAttemptFinished\",\"TaskAttemptStarted\",\"TaskAttemptUnsuccessfulCompletion\",\"TaskFailed\",\"TaskFinished\",\"TaskStarted\",\"TaskUpdated\"]}]}],\"messages\":{}}");

  @SuppressWarnings("all")
  public interface Callback extends Events {
    public static final org.apache.avro.Protocol PROTOCOL = org.apache.hadoop.mapreduce.jobhistory.Events.PROTOCOL;
  }
}