<head>        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> </head>
#实施方案
* 繁简转换，问题不难，使用繁体字简体字转换表。
* 拼音转汉字，属于优化的问题，问题较难，暂时没必要做。
* 同音词拼写错误，先把汉字转成拼音，然后进行可能的比对，比如中华人名共和国->中华人民共和国，这个已经实现。
* 英文拼写错误，暂时不作考虑，政府网站群根本不会考虑这个。
* 形近词错误，当用户使用五笔或记错某个字的样子时，有需要，属于后期优化的问题。

##拼写错误检查
拼音转汉字想法是较为直接的，建立一个以拼音为term的查询词索引，posting list中只保存查询频率最高的K个查询词。

<table border="2">
    <tbody>
        <tr>
            <td>jiujingkaoyan</td>
            <td>“久经考验”,“酒精考验”</td>
        </tr>
		<tr>
            <td>zhijiucaotang</td>
            <td>“子九草堂', “子久草堂”</td>
        </tr>
		<tr>
            <td>xufuniuza</td>
            <td>“徐福牛杂”,“许府牛杂”,“徐府牛杂”</td>
        </tr>
		<tr>
            <td>shoujichongzhi</td>
            <td>“手机冲值”, “手机充值”</td>
        </tr>
    </tbody>
</table>


这一步可以在自动提示中使用，但自动提示与它的区别是，自动提示在拼音输入了一部分的情况下也要提示，比如输入 “xufu”就要提示“许府牛杂”。

同音词拼写错误也基于同样的想法，但是需要一个可能出错的查询词列表，这个列表可以为借鉴于下列几种情况：

1. 以carot为例，返回有carot的文档，也返回一些包含纠错后的term carrot和torot的文档。
2. 与(1)相似，但仅当carot不在词典中时，返回纠错后的结果。
3. 与(1)相似，但仅当包含carot的文档数小于一个预定义的阈值时，即当原始查询返回文档数小于预定义的阈值时，搜索引擎给出纠错后的词列表。

情况(1)相当于是对所有查询都进行纠错处理，发现那些搜索比较少的，就给出一个纠错提示，比如“天浴”搜索次数比较少，而“天娱”搜索次数比较多，那么在用户搜索“天浴”时就提示“天娱”，即使“天浴”也是一个正常的查询词。情况(2)就是当查询没有获得文档，才对它进行纠错处理，然后查询相应结果。情况(3)是一个查询它返回的文档数少于一个预定义阈值时，才进行纠错处理。

英文拼写错误，在lucene中已有贡献者实现了spellchecker模块，主要算法有：Jaro Winkler distance，Levenstein Distance(Edit Distance)，NGram Distance。但Lucene中的实现过于简单，使用两两比较，时间复杂性是$O(n^2)$。


形近字错误，形近字一般是用户记错了形声字，或是使用五笔的用户输入错误。在网上可以下载SunWb\_mb文件，它里面包含五笔的编码和笔画的编码，但字根比如“马”比“口”笔画更多，也更有代表性，但在这种方法中却是相同的。

方言纠错,可以用soudex进行纠错

## 网站群需要注意的问题
政府网站中的特殊字符需要进行转换。

当对政府网站中的网页建立索引时，如果使用url作为文档id，相同内容的文档可能出现多次。下面的url仅仅是字符大小写不同，却作为不同的id出现，解决办法是文档id一律小写。
	
	http://www.wuxi.gov.cn/WebPortal/AskAnswer/Gov_AskAnswer_Info?AnswerID=9174e3c9-4bb6-41cd-ba75-6465a3aa490c
	http://www.wuxi.gov.cn/WEBPORTAL/AskAnswer/Gov_AskAnswer_Info?AnswerID=9174e3c9-4bb6-41cd-ba75-6465a3aa490c
	
下面两个url均包含了“\”字符，其后跟随普通ASCII字符时，此时将转化成一转义字符，作为文档id会出错。

	http://www.wuxi.gov.cn/WEBPORTAL/ChiefHall/ChiefHallInfoDetailsXK?SystemID=4028818a28aff36d0128b3861a090c76&ChiefHallType=xk360ChromeURL\Shell\Open\Command
	http://www.wuxi.gov.cn/WEBPORTAL/ChiefHall/ChiefHallInfoDetailsXK?SystemID=4028818a2fbfee4b012fc2fe529909ac&ChiefHallType=xk360ChromeURL\Shell\Open\Command

爬虫获得的网页内容中，如标题为《京杭大运河无锡段》，而内容为《京杭大运河无锡段 发布时间： 2011年11月23日 修改时间： 2013年10月22日 [ 大 中 小 ] 浏览次数：》，这部分内容重复，影响了检索的相关性，需将标题，发布时间，修改时间和浏览次数过滤掉。

春涛”这个词，结合上下文，分词结果不同，下面两个例子，前者分成了“春，涛”，后者分成了“春涛”（这个词被错误的识别为人名）。

	curl -XGET 'namenode:9200/official_mini/_analyze?analyzer=soul_index&pretty' -d '登太湖仙岛、观鼋渚春涛'
	curl -XGET 'namenode:9200/official_mini/_analyze?analyzer=soul_index&pretty' -d '鼓浪屿上听春涛'

对2014年3月18号爬取的网站群数据，作分词处理后，共有164976个不同的词，总共的词个数是161630251个，使用word2vec代码如下，最后一行代码对SogouR.txt进行编码转换。

	./word2vec -train /mnt/f/b.txt -output vectors.bin -cbow 0 -size 200 -window 7 -negative 0 -hs 1 -sample 1e-3 -threads 12 -binary 1
	./distance vectors.bin
	cat SogouR.txt | iconv -f gbk -t utf8 -c > SogouR-utf8.txt

synonym-new.txt中与synonym.txt没有交集的词条共5689个，与其有包含或被包含关系的词条共1464个（有效条目是1318个）,剩下的都是有交集的，扩展后的词条为12715个。synonym.txt中与synonym-new.txt没有交集的词条共1893个，最终的词条应该是21613个。

## ShardSuggestService模块

ShardSuggestService中，有若干个隶属于IndexShard的cache，目前保存的cache主要有：

* 用于拼写检查的spellCheckCache
* 用于智能提示的titleSuggestCache
* 用于统计每个term的document frequency（term的文档数目）和total frequency（term总共出现次数）的termListCache。

使用如下命令，获取term“垃圾”的两个频率，当结合词性识别关键字时，该命令非常有用（命令中的size暂时没有用处）。

	curl -XGET 'localhost:9200/official_mini/_termlist?pretty' -d '{
    "action": "termlist", 
    "fields": [
     	"contenttitle",
        "content" 
    ], 
    "size": 0, 
    "term": "垃圾"
	}'

下面的命令用于更新索引的关键字

	curl -XGET 'localhost:9200/official_mini/_termlist?pretty' -d '{
    "action": "keywords", 
    "fields": [
     	"contenttitle",
        "content" 
    ], 
    "size": 0, 
    "term": "垃圾"
	}'
	
使用如下命令，获取“驾驶证补办”的标题提示。
	
	curl -X POST 192.168.50.70:9200/official_mini/table/__suggest?pretty -d '{
    "field": "contenttitle", 
    "size": 15, 
    "term": "驾驶证补办", 
    "type": "suggest"
	}'

下面的第一个命令，装载标题域*contenttitle*的cache，如果cache存在，则不做什么动作，直接返回，否则装载cache。第二个命令，刷新标题域*contenttitle*的cache，如果cache不存在，则返回，否则刷新该cache。type为suggest表示装载titleSuggestCache，如果为spell，表示装载spellCheckCache。

	curl -XPOST localhost:9200/official_mini/table/__suggestRefresh -d'{
    "action": "load", 
    "field": "contenttitle", 
    "type": "suggest"
	}'
	
	curl -XPOST localhost:9200/official_mini/table/__suggestRefresh -d'{   
    "action": "refresh", 
    "field": "contenttitle", 
    "type": "suggest"
	}'
	
##自动搜索百度

使用curl命令搜索第7页（注意pn=60，百度默认一个页面10条记录）的“成奎安”

	http://www.baidu.com/s?wd=成奎安&pn=60
	
* 先将cookies存入文件cookie.txt（位于当前目录）
* curl -c cookie.txt www.baidu.com
* cat cookie.txt

当前url使用指定的cookies文件，-A后的字符串为user agent，该agent拷贝自FireFox。

	curl -v --cookie ./cookie.txt  -A "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)"  "http://www.baidu.com/s?^&wd=成奎安&pn=0";
	
url为<http://www.baidu.com/s?cl=3^&wd=×××>貌似也可，如被百度防御系统封闭，可删除cookies文件，再重新生成，然后调用上述命令。



