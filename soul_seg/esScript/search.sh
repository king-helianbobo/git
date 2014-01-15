#!/bin/bash
curl -X GET "http://namenode:9200/_search?pretty=true" -d'
{
    "query": {
        "query_string": {
            "query": "drama",
            "fields": ["genres"]
        }
    },
    "filter" : { "term": {"year":1979} }
}'

curl -X GET "http://namenode:9200/_search?pretty=true" -d'
{
    "query": {
        "query_string": {
            "query": "drama",
            "fields": ["genres"]
        }
    },
    "filter" : { "term": {"year":1979} }
}'


curl -XGET 'http://localhost:9200/hivetest/hive1/_count' -d '
{
"query": {
"match_all" : {}
}
}'

curl -X GET "http://namenode:9200/liubotest/hive1/_search?pretty&size=100" -d'
{
    "query": {
        "query_string": {
            "query": "象棋2011全国甲级联赛",
             "fields": ["program"]
        }
    }
}'
# curl -X GET "localhost:9200/_search?pretty=true" -d '{
#     "query": {
#            "match_all" : {}
#     },
#     "filter": {
#        "term": {"director.original": "Francis Ford Coppola"}
#     }
# }'
# curl -XGET "namenode:9200/movies/_search?pretty=true"
# curl -XGET "namenode:9200/_nodes/?pretty"

## 判断index:hivetest中type:hive1有几个分片，以及每片的具体信息
##curl -XGET "namenode:9200/hivetest/hive1/_search_shards/?pretty"