package org.ansj.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;


import org.soul.domain.Term;
import org.soul.splitWord.Analysis;
import org.soul.splitWord.BasicAnalysis;
import org.soul.splitWord.IndexAnalysis;
import org.soul.treeSplit.IOUtil;

public class IndexDefaultContrast {
	public static void main(String[] args) throws IOException {
		HashSet<String> hs = new HashSet<String>() ;
		BufferedReader br = IOUtil.getReader("library/stop/stopLibrary.dic", "UTF-8");
		
		String temp =null ;
		while((temp=br.readLine())!=null){
			temp = temp.trim().toLowerCase() ;
			hs.add(temp) ;
		}
		
		System.out.println(hs.contains("的"));
		
		
		br = IOUtil.getReader("data/1998年人民日报分词语料_未区分.txt", "GBK");
		Analysis ta = new BasicAnalysis(br) ;
		Term term = null ;
		int count =0 ;
		int skip = 0 ;
		while((term=ta.next())!=null){
			count++ ;
		}
		System.out.println(count);
		br.close() ;
		br = IOUtil.getReader("data/1998年人民日报分词语料_未区分.txt", "GBK");
		ta = new IndexAnalysis(br) ;
		while((term=ta.next())!=null){
			if(!hs.contains(term.getName())){
				count++ ;
			}else{
				skip++ ;
			}
		}
		System.out.println(count);
		System.out.println(skip);
		br.close() ;
		
	}
}
