#! /bin/bash
curl -XPOST 'http://namenode:9200/hivetest/?pretty' -d '{
    "settings" : {
       "index" : {
       "refresh_interval" : "30s",
       "index.store.type": "mmapfs",
       "indices.memory.index_buffer_size": "30%",
       "index.translog.flush_threshold_ops": "50000"
        }
    }
}'

curl -XDELETE 'http://namenode:9200/mrtest/' # create new index named mrtest
curl -XPUT 'http://namenode:9200/mrtest/' # create new index named mrtest
curl -XPOST 'namenode:9200/mrtest/_close' # close index mrtest
curl -XPUT 'namenode:9200/mrtest/_settings?pretty' -d '{
    "index" : {
       "refresh_interval" : "30s",
       "index.translog.flush_threshold_ops": "50000",
       "number_of_replicas" : 0
    }
}'
curl -XPOST 'namenode:9200/mrtest/_open'

curl -XPUT "localhost:9200/movies/movie/_mapping" -d'
{
   "movie1": {
      "properties": {
         "director": {
            "type": "multi_field",
            "fields": {
                "director": {"type": "string"},
                "original": {"type" : "string", "index" : "not_analyzed"}
            }
         }
      }
   },
   "movie2": {
      "properties": {
         "director": {
            "type": "multi_field",
            "fields": {
                "director": {"type": "numeric"},
                "original": {"type" : "numeric", "index" : "not_analyzed"}
            }
         }
      }
   }
}'

# curl -XPOST 'namenode:9200/hivetest/_close' # close index hivetest
# curl -XPUT 'namenode:9200/hivetest/_settings?pretty' -d '{
#     "index" : {
#        "number_of_replicas" : 1
#     }
# }'
# curl -XPOST 'namenode:9200/hivetest/_open'