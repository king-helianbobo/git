#! /bin/bash
mvn clean 
mvn package
scp target/releases/soul_seg-0.2-job.jar root@namenode:/home/user/liubo/
