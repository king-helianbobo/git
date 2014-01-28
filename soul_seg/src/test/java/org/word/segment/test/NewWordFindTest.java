package org.word.segment.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.splitWord.LearnTool;
import org.soul.splitWord.NlpAnalysis;
import org.soul.treeSplit.StringUtil;

public class NewWordFindTest {
	private final Log log = LogFactory.getLog(NewWordFindTest.class);
	@Test
	public void newWordDetectionTest1() {
		String content = "产量两万年中将增长两倍";
		LearnTool learn = new LearnTool();
		List<Term> pased = NlpAnalysis.parse(StringUtil.rmHtmlTag(content),
				learn);
		List<Entry<String, Double>> topTree = learn.getTopTree(100);
		log.info(topTree);
		log.info(pased);
	}
	@Test
	public void newWordDetectionTest2() throws IOException {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		String content = "冒死记录中国神秘事件（真全本）";
		StringReader reader = new StringReader(content);
		LearnTool learn = new LearnTool();
		long start = System.currentTimeMillis();
		NlpAnalysis nlpAnalysis = new NlpAnalysis(reader, learn);
		Term term = null;
		while ((term = nlpAnalysis.next()) != null) {
			if (!TermNatures.NW.equals(term.getTermNatures())) {
				continue;
			}
			if (hm.containsKey(term.getName())) {
				hm.put(term.getName(), hm.get(term.getName()) + 1);
			} else {
				hm.put(term.getName(), 1);
			}
		}
		log.info(System.currentTimeMillis() - start);
		Set<Entry<String, Integer>> entrySet = hm.entrySet();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : entrySet) {
			sb.append(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		log.info(sb.toString());
	}
}