package org.soul.splitWord.test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.Ignore;
import org.soul.analysis.Analysis;
import org.soul.analysis.BasicAnalysis;
import org.soul.analysis.IndexAnalysis;
import org.soul.domain.Term;
import org.soul.library.InitDictionary;
import org.soul.library.UserDefineLibrary;
import org.soul.recognition.NatureRecognition;
import org.soul.treeSplit.IOUtil;
import org.soul.utility.FilterModifyWord;
import org.soul.utility.MyStaticValue;
import org.soul.utility.WordAlter;

public class BasicAnalysisTest {
	private final Log log = LogFactory.getLog(BasicAnalysisTest.class);
	// @Ignore()
	@Test
	public void ambiguityLibrarayTest() {
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
	// @Ignore()
	@Test
	public void foreignNameRecongnitionTest() {
		String str = "巴洛斯说俞志龙和陈举亚是南京维数公司的,协会主席亚拉·巴洛斯说他们开始寻找野生金刚鹦鹉";
		List<Term> parse = BasicAnalysis.parse(str);
		new NatureRecognition(parse).recognition();
		log.info(parse);
	}
	// @Ignore()
	@Test
	public void stopWordTest() {
		String parseStr = "()哆啦Ａ梦是\"个玩具";
		HashSet<String> hs = new HashSet<String>();
		try {
			String temp = null;
			BufferedReader br = IOUtil.getReader(MyStaticValue.stopLibrary,
					"UTF-8");
			while ((temp = br.readLine()) != null) {
				temp = temp.trim().toLowerCase();
				hs.add(temp);
			}
			BasicAnalysis analysis = new BasicAnalysis(new StringReader(
					parseStr));
			Term next = null;
			while ((next = analysis.next()) != null) {
				if (!hs.contains(next.getName())) {
					log.info("[" + next.getName() + "] not skipped");
				} else {
					log.info("[" + next.getName() + "] will skip");
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// @Ignore()
	@Test
	public void natureRecognizeTest() {
		String str = "结婚的和尚未结婚的和尚不是一类和尚";
		List<Term> terms = BasicAnalysis.parse(str);
		log.info(terms);
		new NatureRecognition(terms).recognition();
		log.info(terms);
	}
	@Test
	public void userDefineLibraryTest() {
		String str = "soul中文分词是一个不错的系统";
		UserDefineLibrary.insertWordToUserDefineLibrary("soul中文分词",
				"userDefine", 1000);
		List<Term> terms = BasicAnalysis.parse(str);
		log.info(terms);
		UserDefineLibrary.removeWordInUserDefineLibrary("soul中文分词");
		terms = BasicAnalysis.parse(str);
		log.info(terms);
		str = "上海电力a与2012年财务报表";
		UserDefineLibrary.insertWordToUserDefineLibrary("上海电力Ａ", "词性", 1000);
		terms = BasicAnalysis.parse(str);
		log.info(terms);
	}
	@Test
	public void FilterAndUpdateNatureTest() {
		HashMap<String, String> updateDic = new HashMap<String, String>();
		updateDic.put("停用词", "userDefine"); // userDefine TermNature
		updateDic.put("并且", "_stop");// use termNature _stop
		updateDic.put("12345", "number");// 数字
		updateDic.put("但是", FilterModifyWord._stop);
		updateDic.put("，", FilterModifyWord._stop);
		FilterModifyWord.setUpdateDic(updateDic);
		List<Term> parse = BasicAnalysis
				.parse("过滤停用词，并且修正词12345为用户自定义词性，但是必须设置停用词词典");
		// log.info(parse);
		parse = FilterModifyWord.modifResult(parse);
		log.info(parse);
	}

}
