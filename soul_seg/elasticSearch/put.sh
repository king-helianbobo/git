#! /bin/bash

curl -XPUT "localhost:9200/test/test1/_mapping" -d'{
   "test1": {
      "properties": {
         "content": {
            "type": "string",
             "index_analyzer": "soul_index",
             "search_analyzer": "soul_query"
         }
      }
   }
}'

curl -XPUT "http://localhost:9200/liubo/HongKang/2" -d '{
    "name": "区政协主席顾智杰，副主席陈晓松、政协主席许海祥、许文强，协理员平文良、宋培功、李广平和秘书长薛伟钢参加了会议，王首一总理。"}'
curl -XPUT "http://localhost:9200/liubo/HongKang/9" -d '{
    "name": "总要解决问题的，姚明打球打的很好吗？"}'

curl -X GET "http://localhost:9200/liubo/HongKang/_analyze?analyzer=soul_index&pretty=true" -d '国务院总理'

curl -XGET "http://localhost:9200/test/test1/_search" -d'
{
"query_string" : {
      "default_field" : "name",
      "query" : "总理办公室",
      "analyzer" : "soul_index"
    }
}'


curl -XPOST http://localhost:9200/liubo/HongKang/_search  -d'
{
    "query" : { "term" : { "name" : "姚明主席" }},
     "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "name" : {}
        }
     }
}'



curl -XPUT "http://localhost:9200/movies/movie/1" -d'
{
    "title": "The Godfather",
    "director": "Francis Ford Coppola",
    "year": 1972,
    "genres": ["Crime", "Drama"]
}'

curl -XPUT "http://localhost:9200/movies/movie/2" -d'
{
    "title": "Lawrence of Arabia",
    "director": "David Lean",
    "year": 1962,
    "genres": ["Adventure", "Biography", "Drama"]
}'

curl -XPUT "http://localhost:9200/movies/movie/3" -d'
{
    "title": "To Kill a Mockingbird",
    "director": "Robert Mulligan",
    "year": 1962,
    "genres": ["Crime", "Drama", "Mystery"]
}'

curl -XPUT "http://localhost:9200/movies/movie/4" -d'
{
    "title": "Apocalypse Now",
    "director": "Francis Ford Coppola",
    "year": 1979,
    "genres": ["Drama", "War"]
}'


