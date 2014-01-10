## 这种方法有效，但test这个index必须存在
curl -XGET 'http://localhost:9200/test/_analyze?analyzer=soul_query&pretty=true' -d '视康隐形眼镜，钓鱼岛不是日本的'
## 也接受POST方法，但cars这个index必须存在
curl -XPOST 'http://localhost:9200/cars/_analyze?analyzer=soul_query&pretty' -d '视康隐形眼镜'
## 这种方法无效，不允许出现type（type为test1）
curl -XGET 'http://localhost:9200/test/test1/_analyze?analyzer=soul_query' -d '视康隐形眼镜'

curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈阳'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈从文'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '小沈阳'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈阳大学'
## 下面这个方法无效，目前还不清楚为什么？
curl -XGET "http://localhost:9200/_search" -d '{
"query_string" : {
      "default_field" : "name",
      "query" : "总理办公室",
      "analyzer" : "soul_index"
    }
}'

## insert one record to [index:movies,type:movie,id:1]，movies必须存在
curl -XPUT "http://localhost:9200/movies/movie/1" -d'
{
    "title": "The Godfather",
    "director": "Francis Ford Coppola",
    "year": 1972,
    "genres": ["Crime", "Drama"]
}'