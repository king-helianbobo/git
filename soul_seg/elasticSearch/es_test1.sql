use cable;
set mapred.max.split.size=32000000;
add jar hdfs://namenode:9000/es-hadoop.jar;
DROP TABLE IF EXISTS artists3;
CREATE EXTERNAL TABLE artists3 (
   cardId STRING, 
   playDate STRING, 
   playTime STRING,
   channel STRING, 
   program STRING
)
STORED BY 'org.elasticsearch.hadoop.hive.ESStorageHandler'
TBLPROPERTIES('es.resource' = 'hivetest/hive4/',
	    'es.host' = '192.168.50.75'
);
INSERT OVERWRITE TABLE artists3  SELECT * FROM cable.testData3;



-- DROP TABLE IF EXISTS artists1;
-- CREATE EXTERNAL TABLE artists1 (
--    cardId STRING, 
--    playDate STRING, 
--    playTime STRING,
--    channel STRING, 
--    program STRING
-- )
-- STORED BY 'org.elasticsearch.hadoop.hive.ESStorageHandler'
-- TBLPROPERTIES('es.resource' = 'liubotest/hive1/',
-- 	    'es.host' = '192.168.50.75'
-- );
-- INSERT OVERWRITE TABLE artists1  SELECT * FROM cable.testData1;

-- use cable;
-- DROP TABLE IF EXISTS artists2;
-- CREATE EXTERNAL TABLE artists2 (
--    cardId STRING, 
--    playDate STRING, 
--    playTime STRING,
--    channel STRING, 
--    program STRING
-- )
-- STORED BY 'org.elasticsearch.hadoop.hive.ESStorageHandler'
-- TBLPROPERTIES('es.resource' = 'liubotest/hive2/',
-- 	    'es.host' = '192.168.50.75'
-- );
-- INSERT OVERWRITE TABLE artists2  SELECT * FROM cable.testData2;