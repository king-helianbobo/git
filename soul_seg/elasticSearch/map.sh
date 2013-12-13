# curl -X PUT namenode:9200/movies/movie/_mapping -d'
# {
#    "movie": {
#       "properties": {
#          "director": {
#             "type": "string",
#             "index": "not_analyzed"
#         }
#       }
#    }
# }'
curl -XPUT "localhost:9200/movies/movie/_mapping" -d'
{
   "movie": {
      "properties": {
         "director": {
            "type": "multi_field",
            "fields": {
                "director": {"type": "string"},
                "original": {"type" : "string", "index" : "not_analyzed"}
            }
         }
      }
   }
}'