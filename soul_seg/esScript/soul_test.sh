#! /bin/bash
## 新建名为soul_test的索引，pretty表示格式化输出
curl -XPOST "localhost:9200/soul_test?pretty"
## 修改soul_test的mapping，index过程使用soul_index的分析器，query过程使用soul_query
curl -XPUT "localhost:9200/soul_test/test1/_mapping?pretty" -d' {
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
## 向index为soul_test，type为test1的index插入记录，域名为content，id为1
curl -XPUT "http://localhost:9200/soul_test/test1/1" -d '{"content": "区政协主席顾智杰，副主席陈晓松、政协主席许海祥、许文强，协理员平文良、宋培功、李广平和秘书长薛伟钢参加了会议"}'
## 向index为soul_test，type为test1的index插入记录，域名为content，id为2
curl -XPUT "http://localhost:9200/soul_test/test1/2" -d '{"content": "总要解决问题的，姚明打球打的很好吗？"}'

## 下面的测试能输出结果，因为是精确匹配
curl -XPOST http://localhost:9200/soul_test/test1/_search?pretty  -d '{
    "query" : { "term" : { "content" : "许文强" }},
     "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "content" : {}
        }
     }
}'
## 下面的测试不能输出结果，因为是精确匹配
curl -XPOST http://localhost:9200/soul_test/test1/_search?pretty  -d '{
    "query" : { "term" : { "content" : "许文强人" }},
     "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "content" : {}
        }
     }
}'

curl -XPOST http://localhost:9200/pinyin_test/test1/_search?pretty  -d '{
    "highlight": {
        "fields": {
            "title": {}
        }, 
        "post_tags": [
            "</tag1>", 
            "</tag2>"
        ], 
        "pre_tags": [
            "<tag1>", 
            "<tag2>"
        ]
    }, 
    "query": {
        "term": {
            "title": "\u6df1\u9083"
        }
    }
}
'
