<head>        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> </head>
#ʵʩ����
* ����ת�������ⲻ�ѣ�ʹ�÷����ּ�����ת����
* ƴ��ת���֣������Ż������⣬������ѣ���ʱû��Ҫ����
* ͬ����ƴд�����ȰѺ���ת��ƴ����Ȼ����п��ܵıȶԣ������л��������͹�->�л����񹲺͹�������Ѿ�ʵ�֡�
* Ӣ��ƴд������ʱ�������ǣ�������վȺ�������ῼ�������
* �ν��ʴ��󣬵��û�ʹ����ʻ�Ǵ�ĳ���ֵ�����ʱ������Ҫ�����ں����Ż������⡣

##ƴд������
ƴ��ת�����뷨�ǽ�Ϊֱ�ӵģ�����һ����ƴ��Ϊterm�Ĳ�ѯ��������posting list��ֻ�����ѯƵ����ߵ�K����ѯ�ʡ�

<table border="2">
    <tbody>
        <tr>
            <td>jiujingkaoyan</td>
            <td>���þ����顱,���ƾ����顱</td>
        </tr>
		<tr>
            <td>zhijiucaotang</td>
            <td>���ӾŲ���', ���Ӿò��á�</td>
        </tr>
		<tr>
            <td>xufuniuza</td>
            <td>���츣ţ�ӡ�,����ţ�ӡ�,���츮ţ�ӡ�</td>
        </tr>
		<tr>
            <td>shoujichongzhi</td>
            <td>���ֻ���ֵ��, ���ֻ���ֵ��</td>
        </tr>
    </tbody>
</table>


��һ���������Զ���ʾ��ʹ�ã����Զ���ʾ�����������ǣ��Զ���ʾ��ƴ��������һ���ֵ������ҲҪ��ʾ���������� ��xufu����Ҫ��ʾ����ţ�ӡ���

ͬ����ƴд����Ҳ����ͬ�����뷨��������Ҫһ�����ܳ���Ĳ�ѯ���б�����б����Ϊ��������м��������

1. ��carotΪ����������carot���ĵ���Ҳ����һЩ����������term carrot��torot���ĵ���
2. ��(1)���ƣ�������carot���ڴʵ���ʱ�����ؾ����Ľ����
3. ��(1)���ƣ�����������carot���ĵ���С��һ��Ԥ�������ֵʱ������ԭʼ��ѯ�����ĵ���С��Ԥ�������ֵʱ������������������Ĵ��б�

���(1)�൱���Ƕ����в�ѯ�����о�����������Щ�����Ƚ��ٵģ��͸���һ��������ʾ�����硰��ԡ�����������Ƚ��٣��������顱���������Ƚ϶࣬��ô���û���������ԡ��ʱ����ʾ�����顱����ʹ����ԡ��Ҳ��һ�������Ĳ�ѯ�ʡ����(2)���ǵ���ѯû�л���ĵ����Ŷ������о�����Ȼ���ѯ��Ӧ��������(3)��һ����ѯ�����ص��ĵ�������һ��Ԥ������ֵʱ���Ž��о�����

Ӣ��ƴд������lucene�����й�����ʵ����spellcheckerģ�飬��Ҫ�㷨�У�Jaro Winkler distance��Levenstein Distance(Edit Distance)��NGram Distance����Lucene�е�ʵ�ֹ��ڼ򵥣�ʹ�������Ƚϣ�ʱ�临������$O(n^2)$��


�ν��ִ����ν���һ�����û��Ǵ��������֣�����ʹ����ʵ��û�������������Ͽ�������SunWb\_mb�ļ��������������ʵı���ͱʻ��ı��룬���ָ����硰���ȡ��ڡ��ʻ����࣬Ҳ���д����ԣ��������ַ�����ȴ����ͬ�ġ�

���Ծ���,������soudex���о���

## ��վȺ��Ҫע�������
������վ�е������ַ���Ҫ����ת����

����������վ�е���ҳ��������ʱ�����ʹ��url��Ϊ�ĵ�id����ͬ���ݵ��ĵ����ܳ��ֶ�Ρ������url�������ַ���Сд��ͬ��ȴ��Ϊ��ͬ��id���֣�����취���ĵ�idһ��Сд��
	
	http://www.wuxi.gov.cn/WebPortal/AskAnswer/Gov_AskAnswer_Info?AnswerID=9174e3c9-4bb6-41cd-ba75-6465a3aa490c
	http://www.wuxi.gov.cn/WEBPORTAL/AskAnswer/Gov_AskAnswer_Info?AnswerID=9174e3c9-4bb6-41cd-ba75-6465a3aa490c
	
��������url�������ˡ�\���ַ�����������ͨASCII�ַ�ʱ����ʱ��ת����һת���ַ�����Ϊ�ĵ�id�����

	http://www.wuxi.gov.cn/WEBPORTAL/ChiefHall/ChiefHallInfoDetailsXK?SystemID=4028818a28aff36d0128b3861a090c76&ChiefHallType=xk360ChromeURL\Shell\Open\Command
	http://www.wuxi.gov.cn/WEBPORTAL/ChiefHall/ChiefHallInfoDetailsXK?SystemID=4028818a2fbfee4b012fc2fe529909ac&ChiefHallType=xk360ChromeURL\Shell\Open\Command

�����õ���ҳ�����У������Ϊ���������˺������Ρ���������Ϊ���������˺������� ����ʱ�䣺 2011��11��23�� �޸�ʱ�䣺 2013��10��22�� [ �� �� С ] ��������������ⲿ�������ظ���Ӱ���˼���������ԣ��轫���⣬����ʱ�䣬�޸�ʱ�������������˵���

���Ρ�����ʣ���������ģ��ִʽ����ͬ�������������ӣ�ǰ�߷ֳ��ˡ������Ρ������߷ֳ��ˡ����Ρ�������ʱ������ʶ��Ϊ��������

	curl -XGET 'namenode:9200/official_mini/_analyze?analyzer=soul_index&pretty' -d '��̫���ɵ�������侴���'
	curl -XGET 'namenode:9200/official_mini/_analyze?analyzer=soul_index&pretty' -d '��������������'

��2014��3��18����ȡ����վȺ���ݣ����ִʴ���󣬹���164976����ͬ�Ĵʣ��ܹ��Ĵʸ�����161630251����ʹ��word2vec�������£����һ�д����SogouR.txt���б���ת����

	./word2vec -train /mnt/f/b.txt -output vectors.bin -cbow 0 -size 200 -window 7 -negative 0 -hs 1 -sample 1e-3 -threads 12 -binary 1
	./distance vectors.bin
	cat SogouR.txt | iconv -f gbk -t utf8 -c > SogouR-utf8.txt

synonym-new.txt����synonym.txtû�н����Ĵ�����5689���������а����򱻰�����ϵ�Ĵ�����1464������Ч��Ŀ��1318����,ʣ�µĶ����н����ģ���չ��Ĵ���Ϊ12715����synonym.txt����synonym-new.txtû�н����Ĵ�����1893�������յĴ���Ӧ����21613����

## ShardSuggestServiceģ��

ShardSuggestService�У������ɸ�������IndexShard��cache��Ŀǰ�����cache��Ҫ�У�

* ����ƴд����spellCheckCache
* ����������ʾ��titleSuggestCache
* ����ͳ��ÿ��term��document frequency��term���ĵ���Ŀ����total frequency��term�ܹ����ִ�������termListCache��

ʹ�����������ȡterm��������������Ƶ�ʣ�����ϴ���ʶ��ؼ���ʱ��������ǳ����ã������е�size��ʱû���ô�����

	curl -XGET 'localhost:9200/official_mini/_termlist?pretty' -d '{
    "action": "termlist", 
    "fields": [
     	"contenttitle",
        "content" 
    ], 
    "size": 0, 
    "term": "����"
	}'

������������ڸ��������Ĺؼ���

	curl -XGET 'localhost:9200/official_mini/_termlist?pretty' -d '{
    "action": "keywords", 
    "fields": [
     	"contenttitle",
        "content" 
    ], 
    "size": 0, 
    "term": "����"
	}'
	
ʹ�����������ȡ����ʻ֤���족�ı�����ʾ��
	
	curl -X POST 192.168.50.70:9200/official_mini/table/__suggest?pretty -d '{
    "field": "contenttitle", 
    "size": 15, 
    "term": "��ʻ֤����", 
    "type": "suggest"
	}'

����ĵ�һ�����װ�ر�����*contenttitle*��cache�����cache���ڣ�����ʲô������ֱ�ӷ��أ�����װ��cache���ڶ������ˢ�±�����*contenttitle*��cache�����cache�����ڣ��򷵻أ�����ˢ�¸�cache��typeΪsuggest��ʾװ��titleSuggestCache�����Ϊspell����ʾװ��spellCheckCache��

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
	
##�Զ������ٶ�

ʹ��curl����������7ҳ��ע��pn=60���ٶ�Ĭ��һ��ҳ��10����¼���ġ��ɿ�����

	http://www.baidu.com/s?wd=�ɿ���&pn=60
	
* �Ƚ�cookies�����ļ�cookie.txt��λ�ڵ�ǰĿ¼��
* curl -c cookie.txt www.baidu.com
* cat cookie.txt

��ǰurlʹ��ָ����cookies�ļ���-A����ַ���Ϊuser agent����agent������FireFox��

	curl -v --cookie ./cookie.txt  -A "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)"  "http://www.baidu.com/s?^&wd=�ɿ���&pn=0";
	
urlΪ<http://www.baidu.com/s?cl=3^&wd=������>ò��Ҳ�ɣ��类�ٶȷ���ϵͳ��գ���ɾ��cookies�ļ������������ɣ�Ȼ������������



