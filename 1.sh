{"index":"test", "ignore_unavailable" : true, "expand_wildcards" : "open,closed"}}
{"query" : {"match_all" {}}}
{"index" : "test", "type" : "type1", "expand_wildcards" : ["open", "closed"]}
{"query" : {"match_all" {}}}
{}
{"query" : {"match_all" {}}}
{"search_type" : "count"}
{"query" : {"match_all" {}}}

{"query" : {"match_all" {}}}