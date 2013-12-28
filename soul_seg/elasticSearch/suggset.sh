#! /bin/bash
curl -X DELETE localhost:9200/cars
curl -X PUT localhost:9200/cars -d '{
  "mappings" : {
    "car" : {
      "properties" : {
        "name" : {
          "type" : "multi_field",
          "fields" : {
            "name":    { "type": "string", "index": "not_analyzed" }
          }
        }
      }
    }
  },
  "settings" : {
    "analysis" : {
      "analyzer" : {
        "suggest_analyzer_stopwords" : {
          "type" : "custom",
          "tokenizer" : "standard",
          "filter" : [ "standard", "lowercase", "stopword_no_position_increment" ]
        },
        "suggest_analyzer_synonyms" : {
          "type" : "custom",
          "tokenizer" : "standard",
          "filter" : [ "standard", "lowercase", "my_synonyms" ]
        }
      },
      "filter" : {
        "stopword_no_position_increment" : {
          "type" : "stop",
          "enable_position_increments" : false
        },
        "my_synonyms" : {
          "type" : "synonym",
          "synonyms" : [ "jetta, bora" ]
        }
      }
    }
  }
}'


curl -X POST localhost:9200/cars/car -d '{ "name" : "The BMW ever" }'
curl -X POST localhost:9200/cars/car -d '{ "name" : "BMW 320" }'
curl -X POST localhost:9200/cars/car -d '{ "name" : "BMW 525d" }'
curl -X POST localhost:9200/cars/car -d '{ "name" : "VW Jetta" }'
curl -X POST localhost:9200/cars/car -d '{ "name" : "VW Bora" }'


curl -X POST localhost:9200/cars/car/__suggest -d '{ "field" : "name", "type": "full", "term" : "vw je", "analyzer" : "suggest_analyzer_synonyms" }'

