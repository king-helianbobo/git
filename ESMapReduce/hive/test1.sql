CREATE DATABASE IF NOT EXISTS cable;
DROP TABLE IF EXISTS cable.testData1;
Create TABLE IF NOT EXISTS cable.testData1
       (cardId STRING, playDate STRING, playTime STRING,
	 channel STRING, program STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';	

DROP TABLE IF EXISTS cable.testData2;
Create TABLE IF NOT EXISTS cable.testData2
       (cardId STRING, playDate STRING, playTime STRING,
	 channel STRING, program STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';

DROP TABLE IF EXISTS cable.testData3;
Create TABLE IF NOT EXISTS cable.testData3
       (cardId STRING, playDate STRING, playTime STRING,
	 channel STRING, program STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';

DROP TABLE IF EXISTS cable.testData4;
Create TABLE IF NOT EXISTS cable.testData4
       (cardId STRING, playDate STRING, playTime STRING,
	 channel STRING, program STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';


DROP TABLE IF EXISTS cable.testData5;
Create TABLE IF NOT EXISTS cable.testData5
       (cardId STRING, playDate STRING, playTime STRING,
	 channel STRING, program STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';

LOAD DATA INPATH '/liubo_output_2/part-00000' INTO TABLE cable.testData1;
LOAD DATA INPATH '/liubo_output_2/part-00001' INTO TABLE cable.testData2;
LOAD DATA INPATH '/liubo_output_2/part-00002' INTO TABLE cable.testData3;
LOAD DATA INPATH '/liubo_output_2/part-00003' INTO TABLE cable.testData4;
LOAD DATA INPATH '/liubo_output_2/part-00004' INTO TABLE cable.testData5;
