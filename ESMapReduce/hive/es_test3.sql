DROP TABLE IF EXISTS artists3;
CREATE EXTERNAL TABLE artists3 (
   cardId STRING, 
   playDate STRING, 
   playTime STRING,
   channel STRING, 
   program STRING
)
STORED BY 'org.elasticsearch.hadoop.hive.ESStorageHandler'
TBLPROPERTIES('es.resource' = 'liubotest/hive3/',
	    'es.host' = '192.168.50.75'
);
INSERT OVERWRITE TABLE artists3  SELECT * FROM cable.testData3;