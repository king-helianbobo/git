package org.ansj.demo;

import java.io.IOException;
import java.io.StringReader;

import org.soul.domain.Term;
import org.soul.splitWord.Analysis;
import org.soul.splitWord.BasicAnalysis;

/**
 * 标注的分词方式,这里面的流你可以传入任何流.除了流氓
 * 
 * @author ansj
 * 
 */
public class Demo {
	public static void main(String[] args) throws IOException {
		Analysis udf = new BasicAnalysis(new StringReader("孙健用java重写了张华平老师的分词."));
		Term term = null;
		while ((term = udf.next()) != null) {
			System.out.print(term.getName() + " ");
		}
	}
}