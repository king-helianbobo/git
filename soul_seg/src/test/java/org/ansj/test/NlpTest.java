package org.ansj.test;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;


import org.soul.splitWord.BasicAnalysis;
import org.soul.splitWord.LearnTool;
import org.soul.splitWord.NlpAnalysis;
import org.soul.treeSplit.IOUtil;

public class NlpTest {
	public static void main(String[] args) throws IOException {
//	    LearnTool learn = new LearnTool() ;
//        NlpAnalysis nlpAnalysis = new NlpAnalysis(IOUtil.getReader("/Users/ansj/Documents/workspace/ElasticSearchServer/sub_test.txt", IOUtil.UTF8), learn) ;
//        while(nlpAnalysis.next()!=null){}
//        List<Entry<String, Double>> topTree = learn.getTopTree(20) ;
//        System.out.println(topTree);
		System.out.println(BasicAnalysis.parse("亚太经合组织"));
	}
}
