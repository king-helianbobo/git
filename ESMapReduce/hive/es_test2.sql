use cable;
DROP TABLE IF EXISTS artists2;
CREATE EXTERNAL TABLE artists2 (
   cardId STRING, 
   playDate STRING, 
   playTime STRING,
   channel STRING, 
   program STRING
)
STORED BY 'org.elasticsearch.hadoop.hive.ESStorageHandler'
TBLPROPERTIES('es.resource' = 'hivetest/artists2/',
	    'es.host' = '192.168.50.75'
);

INSERT OVERWRITE TABLE artists2  SELECT * FROM cable.testData2;