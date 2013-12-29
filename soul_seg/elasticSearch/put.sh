#! /bin/bash
http://jprante.github.io/lessons/2012/05/16/multilingual-analysis-for-title-search.html
Guava Cache是什么东西呢？


curl -XPUT "localhost:9200/test/test1/_mapping?pretty" -d' {
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
# 这种方法有效
curl -XGET 'http://localhost:9200/test/_analyze?analyzer=soul_query' -d '视康隐形眼镜，钓鱼岛是日本的'
curl -XPOST 'http://localhost:9200/cars/_analyze?analyzer=soul_query&pretty' -d '视康隐形眼镜'
# 这种方法无效
##curl -XGET 'http://localhost:9200/test/test1/_analyze?analyzer=soul_query' -d '视康隐形眼镜'

curl -XPUT "http://localhost:9200/test/test1/1" -d '{"name": "区政协主席顾智杰，副主席陈晓松、政协主席许海祥、许文强，协理员平文良、宋培功、李广平和秘书长薛伟钢参加了会议，国务院总理李克强出席了亚太经合组织第一次会议。"}'
curl -XPUT "http://localhost:9200/test/test1/2" -d '{"name": "总要解决问题的，姚明打球打的很好吗？"}'


curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈阳'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈从文'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '小沈阳'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈阳大学'


curl -XGET "http://localhost:9200/_search" -d'
{
"query_string" : {
      "default_field" : "name",
      "query" : "总理办公室",
      "analyzer" : "soul_index"
    }
}'


curl -XPOST http://localhost:9200/test/test1/_search  -d '{
    "query" : { "term" : { "content" : "姚明主席" }},
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



curl -XPOST "http://localhost:9200/test/test1" -d '{"content": "沈阳"}'
curl -XPOST "http://localhost:9200/test/test1" -d '{"content": "沈从文"}'
curl -XPOST "http://localhost:9200/test/test1" -d '{"content": "小沈阳"}'
curl -XPOST "http://localhost:9200/test/test1" -d '{"content": "沈阳大学"}'
curl -XPOST "http://localhost:9200/test/test1" -d '{"content": "沈阳市市长"}'
