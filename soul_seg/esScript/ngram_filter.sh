## 使用这种mapping无效，可能需要看下ES源码
curl -XPUT "localhost:9200/test?pretty" -d' {
    "settings": {
        "analysis": {
            "analyzer": "soul_pinyin"
        }
    }
}'

## 使用这种mapping有效，索引时指定indexer为soul_pinyin，查询时指定searcher为whitespace，去除空格
curl -XPUT "localhost:9200/test?pretty" -d '{
    "mappings": {
        "test1": {
            "properties": {
                "content": {
                    "index_analyzer": "soul_pinyin", 
                    "search_analyzer": "whitespace", 
                    "type": "string"
                }
            }
        }
    }
}'
## 插入若干条记录
curl -XPOST "http://localhost:9200/test/test1?pretty" -d '{"content": "沈阳"}'
curl -XPOST "http://localhost:9200/test/test1?pretty" -d '{"content": "沈从文"}'
curl -XPOST "http://localhost:9200/test/test1?pretty" -d '{"content": "小沈阳"}'
curl -XPOST "http://localhost:9200/test/test1?pretty" -d '{"content": "沈阳大学"}'
curl -XPOST "http://localhost:9200/test/test1?pretty" -d '{"content": "沈阳市市长"}'

## 下面是测试命令
curl -XPOST http://localhost:9200/test/_search?pretty=true  -d '{
    "query": {
        "term": {
            "content": "sheny"
        }
    }
}'

curl -XPOST http://localhost:9200/test/_search?pretty=true  -d '{
    "query": {
        "term": {
            "content": "沈从"
        }
    },
     "highlight" : {
        "pre_tags" : ["<tag1>", "<tag2>"],
        "post_tags" : ["</tag1>", "</tag2>"],
        "fields" : {
            "content" : {}
        }
     }
}'

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


## 对index：pinyin_test进行自动提示测试
curl -XPOST http://localhost:9200/pinyin_test/_search?pretty=true  -d '{
    "query": {
        "term": {
            "title": "chao"
        }
    },
     "highlight" : {
        "pre_tags" : ["<tag1>"],
        "post_tags" : ["</tag1>"],
        "fields" : {
            "title" : {}
        }
     }
}'

## 对sogou_mini索引，做自动提示，    
## "fields": ["contenttitle", "postTime"]只输出这两个域，否则content内容过多，emacs显示很慢
## sogou_mini中文档数目是31152个
curl -XPOST http://localhost:9200/sogou_mini/_search?pretty=true  -d '{
    "fields": ["contenttitle", "postTime"],
    "query": {
        "term": {
            "contenttitle": "面试"
        }
    },
     "highlight" : {
        "pre_tags" : ["<t>"],
        "post_tags" : ["</t>"],
        "fields" : {
            "contenttitle" : {}
        }
     }
}'