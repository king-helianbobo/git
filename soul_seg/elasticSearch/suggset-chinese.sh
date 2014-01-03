#! /bin/bash
## 首先建立bus索引
curl -XDELETE localhost:9200/bus?pretty
## 使用如下方法设置索引的Analyzer为whitespace，该Analyzer将去除Term中的空格
curl -XPUT http://localhost:9200/bus?pretty -d '{
  "settings":{ 
    "analysis":{
      "analyzer":{
        "default":{
          "type":"whitespace"
        }
      }
    }
  }
}'

## 下面是建立几个测试Term
curl -X POST localhost:9200/bus/bus1?pretty -d '{ "name" : "小王庄庄主" }'
curl -X POST localhost:9200/bus/bus1?pretty -d '{ "name" : "关羽字云长河东解县人" }'
curl -X POST localhost:9200/bus/bus1?pretty -d '{ "name" : "多情剑客无情剑" }'
curl -X POST localhost:9200/bus/bus1?pretty -d '{ "name" : "沈从文" }'
curl -X POST localhost:9200/bus/bus2?pretty -d '{ "name" : "麻辣鸡翅" }'
curl -X POST localhost:9200/bus/bus2?pretty -d '{ "name" : "麻辣烤翅" }'
curl -X POST localhost:9200/bus/bus2?pretty -d '{ "name" : "麻辣砂锅" }'
curl -X POST localhost:9200/bus/bus3?pretty -d '{ "name" : "砂锅似的声音" }'
curl -X POST localhost:9200/bus/bus3?pretty -d '{ "message" : "砂锅似的声音" }'
curl -X POST localhost:9200/bus/bus3?pretty -d '{ "message" : "乱砍一通" }'
curl -X POST localhost:9200/bus/bus3?pretty -d '{ "message" : "银铃似的声音" }'
curl -X POST localhost:9200/bus/bus2?pretty -d '{ "name" : "银铃似的声音" }'
curl -X POST localhost:9200/bus/bus3?pretty -d '{ "message" : "破锣似的声音" }'
curl -X POST localhost:9200/bus/bus1?pretty -d '{ "name" : "砂锅似的声音" }'
curl -X POST localhost:9200/bus/bus4?pretty -d '{ "name" : "砂锅似的声音" }'


## 下面是一个测试的例子
curl -X POST localhost:9200/bus/__suggest?pretty -d '{ "field" : "name", "type": "fst", "term" : "麻辣sha锅", "similarity":"0.8" }'
## 结果不会提示什么，因为'麻辣砂锅'已存在于Index中。
curl -X POST localhost:9200/bus/__suggest?pretty -d '{ "field" : "name", "type": "fst", "term" : "麻辣砂锅", "similarity":"0.8" }'
curl -X POST localhost:9200/bus/__suggest?pretty -d '{ "field" : "name", "type": "fst", "term" : "夺情尖刻无勤见", "similarity":"0.8" }'
## similarity最大值为1,表示精确匹配，只有当拼音或Term本身与目标Term（拼音）相同时，相似度才为1
curl -X POST localhost:9200/bus/__suggest?pretty -d '{ "field" : "name", "type": "fst", "term" : "shencongwen", "similarity":"1.0" }'
## 下例不会输出结果
curl -X POST localhost:9200/bus/__suggest?pretty -d '{ "field" : "name", "type": "fst", "term" : "shencongweng", "similarity":"1.0" }'
## similarity改成0.9后，输出结果'沈从文'
curl -X POST localhost:9200/bus/__suggest?pretty -d '{ "field" : "name", "type": "fst", "term" : "shencongweng", "similarity":"0.9" }'

## 输出所有的Term，field为'name'
curl -XGET 'localhost:9200/bus/_termlist?pretty&totalfreq=true&docfreq=true&field=name'
