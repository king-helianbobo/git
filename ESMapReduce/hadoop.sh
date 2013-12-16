#! /bin/bash                                                                                                 
rm -f es.jar
hadoop fs -copyToLocal hdfs://192.168.50.75:9000/user/liubo/1.jar ./es.jar
hadoop fs -rm -r  /liubo_output
hadoop jar es.jar /chenlou_input /liubo_output
