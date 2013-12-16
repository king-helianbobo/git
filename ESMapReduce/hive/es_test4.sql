DROP TABLE IF EXISTS artists4;
CREATE EXTERNAL TABLE artists4 (
   cardId STRING, 
   playDate STRING, 
   playTime STRING,
   channel STRING, 
   program STRING
)
STORED BY 'org.elasticsearch.hadoop.hive.ESStorageHandler'
TBLPROPERTIES('es.resource' = 'hivetest/artists4/',
	    'es.host' = '192.168.50.75'
);

INSERT OVERWRITE TABLE artists4  SELECT * FROM cable.testData4;