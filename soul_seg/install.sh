#! /bin/bash
CURRENT_PATH=`pwd ./`
ZIP_PATH="file://""$CURRENT_PATH""/target/releases/soul_seg-0.2-plugin.zip"
echo $ZIP_PATH
mvn clean compile
mvn assembly:single
/opt/elasticsearch-0.90.5/bin/plugin --remove suggest
/opt/elasticsearch-0.90.5/bin/plugin --url $ZIP_PATH  --install suggest
