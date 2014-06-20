<head>        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> </head>

#hadoop2.4.0 cluster setup
## preface
本文目的是为当前最新版本的Hadoop 2.4.0提供最为详细的安装说明，以帮助减少安装过程中遇到的困难，并对一些错误原因进行说明。本文的安装只涉及了hadoop-common，hadoop-hdfs，hadoop-mapreduce，hadoop-yarn，并不包括HBase、Hive和Pig等。

##部署
1. 机器列表，总共3台机器,部署如下所示：

* 192.168.50.70 NameNode & SecondaryNameNode
*　192.168.50.71 DataNode
*　192.168.50.72 DataNode

2. 主机名
*　　Ip                Hostname
*　　192.168.50.70        Master
*　　192.168.50.71        Slave1
*　　192.168.50.72        Slave2
注: 可通过hostname {new name},修改主机名.


