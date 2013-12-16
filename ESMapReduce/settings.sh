#! /bin/bash
#curl -XDELETE 'http://namenode:9200/hello/?pretty' # create new index
INDEX_NAME="$1"
#curl -XGET "namenode:9200/$INDEX_NAME/_mapping?pretty"
#echo "curl -XPUT 'http://namenode:9200/$INDEX_NAME/?pretty\n'"
curl -XPUT "http://namenode:9200/$INDEX_NAME/?pretty" # create new index
sleep 1
curl -XPOST "namenode:9200/$INDEX_NAME/_close?pretty" # close index
sleep 1
curl -XPUT "namenode:9200/$INDEX_NAME/_settings?pretty" -d '{
    "index" : {
       "refresh_interval" : "40s",
       "index.translog.flush_threshold_ops": "100000",
       "number_of_replicas" : 0
    }
}'
sleep 1 # because we want the curl can complete
curl -XPOST "namenode:9200/$INDEX_NAME/_open?pretty"
