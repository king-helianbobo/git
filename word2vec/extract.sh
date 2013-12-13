#! /bin/bash
# 语料下载地址，http://www.sogou.com/labs/dl/ca.html
DIR=`pwd`
echo $DIR
cd /mnt/f/tmp
#ls *.txt |xargs cat | iconv -f gbk -t utf-8 -c | grep "<content>"
ls *.txt |xargs cat | iconv -f gbk -t utf-8 -c | grep "<content>" > content.txt
# cat news_tensite_xml.dat | iconv -f gbk -t utf-8 -c | grep "<content>"  > corpus.txt
# ./word2vec -train /mnt/e/tmp/resultbig.txt -output vectors.bin -cbow 0 -size 200 -window 5 -negative 0 -hs 1 -sample 1e-3 -threads 12 -binary 1 
# ./distance vectors.bin  
cd $DIR
echo `pwd`