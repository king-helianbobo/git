#! /bin/bash
mvn clean compile
mvn assembly:single
/opt/elasticsearch-0.90.5/bin/plugin --remove suggest
/opt/elasticsearch-0.90.5/bin/plugin --url file:///home/liubo/git/soul_seg/target/releases/soul_seg-0.2-plugin.zip  --install suggest

