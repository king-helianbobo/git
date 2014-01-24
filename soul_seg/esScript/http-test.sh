curl -XPOST "localhost:9200/http_test?pretty"
curl -XPOST "localhost:9200/http_test?pretty"
## 修改soul_test的mapping，index过程使用soul_index的分析器，query过程使用soul_query
curl -XPUT "localhost:9200/http_test/test1/_mapping?pretty" -d' {
   "test1": {
      "properties": {
         "cardid" : {
            "type": "string",
            "index": "not_analyzed"           
         },
         "playdate" : {
            "type": "string",
            "index": "not_analyzed"           
         },
         "channel": {
            "type": "string",
            "index_analyzer": "soul_index",
            "search_analyzer": "soul_query"
         },
         "program": {
            "type": "string",
            "index_analyzer": "soul_index",
            "search_analyzer": "soul_query"
         }
      }
   }
}'
