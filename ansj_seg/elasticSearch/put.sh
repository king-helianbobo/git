#! /bin/bash
##curl -X GET http://localhost/jdbc/jdbc/_search?pretty=true -d ''
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

curl -XPUT "http://localhost:9200/movies/movie/5" -d'
{
    "title": "Kill Bill: Vol. 1",
    "director": "Quentin Tarantino",
    "year": 2003,
    "genres": ["Action", "Crime", "Thriller"]
}'

curl -XPUT "http://localhost:9200/movies/movie/6" -d'
{
    "title": "The Assassination of Jesse James by the Coward Robert Ford",
    "director": "Andrew Dominik",
    "year": 2007,
    "genres": ["Biography", "Crime", "Drama"]
}'