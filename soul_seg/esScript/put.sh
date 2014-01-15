## 这种方法有效，但test这个index必须存在
curl -XGET 'http://localhost:9200/test/_analyze?analyzer=soul_query&pretty=true' -d '视康隐形眼镜，钓鱼岛不是日本的'
## 接受POST方法，但cars这个index必须存在
curl -XPOST 'http://localhost:9200/cars/_analyze?analyzer=soul_query&pretty' -d '视康隐形眼镜'
## 这种方法无效，方法暂时不允许出现type（type为test1）
curl -XGET 'http://localhost:9200/test/test1/_analyze?analyzer=soul_query' -d '视康隐形眼镜'

curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈阳'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈从文'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '小沈阳'
curl -X GET "http://localhost:9200/cars/_analyze?analyzer=soul_pinyin&pretty=true" -d '沈阳大学'
## 下面这个方法无效，
curl -XGET "http://localhost:9200/_search" -d '{
    "query_string": {
        "analyzer": "soul_index", 
        "default_field": "content", 
        "query": "饮食"
    }
}'
# 正确的形式应该是这样子
curl -XGET "http://localhost:9200/_search" -d '
{
    "query": {
        "query_string": {
            "analyzer": "soul_index", 
            "default_field": "content", 
            "query": "饮食"
        }
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