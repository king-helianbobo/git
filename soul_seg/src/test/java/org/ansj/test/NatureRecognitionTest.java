package org.ansj.test;

import java.util.List;

import org.soul.domain.Term;
import org.soul.recognition.NatureRecognition;
import org.soul.splitWord.BasicAnalysis;

/**
 * 词性标注的一个例子
 * 
 * @author ansj
 * 
 */
public class NatureRecognitionTest {
	public static void main(String[] args) {
		String str = "结婚的和尚未结婚的孙建是一个好人";
		List<Term> terms = BasicAnalysis.parse(str);
		new NatureRecognition(terms).recognition();
	}
}
