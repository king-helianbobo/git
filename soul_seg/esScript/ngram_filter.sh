## 使用这种mapping来测试自动提示无效，可能需要看下ES源码
curl -XPUT "localhost:9200/test?pretty" -d'{
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
    }
}'