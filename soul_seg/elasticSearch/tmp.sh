curl -XPUT "localhost:9200/liubo/HongKang/_mapping" -d'
{
   "HongKang": {
      "properties": {
         "name": {
            "type": "string",
             "index_analyzer": "soul_index",
             "search_analyzer": "soul_query"
         }
      }
   }
}'
