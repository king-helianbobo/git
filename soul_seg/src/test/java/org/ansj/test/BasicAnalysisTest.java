package org.ansj.test;

import java.io.*;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.Ignore;
import org.soul.domain.Term;
import org.soul.recognition.NatureRecognition;
import org.soul.splitWord.BasicAnalysis;
import org.soul.utility.UserDefineLibrary;
import org.soul.utility.WordAlert;

public class BasicAnalysisTest {
	private final Log log = LogFactory.getLog(BasicAnalysisTest.class);
	// @Ignore()
	@Test
	public void userDefineTest() {
		String[] parseStr = {"哆啦Ａ梦是个玩具", "B超更x-射线有关系吗？", "漂亮mm打拳皇ova很厉害"};
		for (String str : parseStr) {
			BasicAnalysis analysis = new BasicAnalysis(new StringReader(str));
			Term next = null;
			try {
				while ((next = analysis.next()) != null) {
					log.info(next.getName());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Ignore()
	@Test
	public void foreignNameRecongnitionTest() {
		String str = "巴洛斯说俞志龙和陈举亚是南京维数公司的,协会主席亚拉·巴洛斯说他们在1990年开始寻找野生金刚鹦鹉";
		List<Term> parse = BasicAnalysis.parse(str);
		new NatureRecognition(parse).recognition();
		log.info(parse);
	}
	@Ignore()
	@Test
	public void natureRecognizeTest() {
		String str = "结婚的和尚未结婚的和尚不是一类和尚";
		List<Term> terms = BasicAnalysis.parse(str);
		log.info(terms);
		new NatureRecognition(terms).recognition();
		log.info(terms);
		str = "上海电力2012年财务报表";
		UserDefineLibrary.insertWord("上海电力", "词性", 1000);
		terms = BasicAnalysis.parse(str);
		log.info(terms);
	}

	@Test
	public void wordAlertTest() {
		String str = "。，、”ｓｄｆｓｄｆ多啦哆啦Ａａ梦１";
		String result = WordAlert.alertEnglishAndNumber(str, 0, str.length());
		log.info(result);
	}
}
