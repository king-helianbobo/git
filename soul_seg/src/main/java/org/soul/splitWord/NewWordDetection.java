package org.soul.splitWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Graph;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.splitWord.PatternMap.Node;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.DictionaryReader;

public class NewWordDetection {

	private static final Log log = LogFactory.getLog(NewWordDetection.class);
	private static final HashSet<String> filterSet = new HashSet<String>();

	// load stop vocabulary
	static {
		BufferedReader filter = null;
		try {
			filter = DictionaryReader.getReader("newWord/newWordFilter.dic");
			String temp = null;
			while ((temp = filter.readLine()) != null) {
				filterSet.add(temp.toLowerCase());
			}
			filterSet.add("－");
			filterSet.add("　");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (filter != null)
				try {
					filter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public Collection<Node> getNewWords(Graph graph) throws IOException {
		// 构建patternTree
		PatternMap pt = makePatHash(graph);
		// 从patternTree中查找出最大公共字串
		Collection<Node> words = pt.getWords();
		return words;
	}

	// construct pat tree
	private PatternMap makePatHash(Graph graph) {
		PatternMap pt = new PatternMap();
		// O(n^2)次遍历
		List<Term> tempList = new ArrayList<Term>(20);
		for (Term term : graph.terms) {
			if (term == null) {
				continue;
			}
			if (filter(term)) {
				tempList.add(term);
			} else {
				// 如果大于,则放到树中
				if (tempList.size() > 1) {
					// 计算分数.并且增加到patTree中
					pt.addList(tempList);
				}
				if (tempList.size() > 0) {
					tempList.clear();
				}
			}
		}
		return pt;
	}

	private boolean filter(Term term) {
		int length = term.getName().length();
		// 只对单字新词发现
		if (length > 1) {
			return false;
		}
		// filter stop word
		if (filterSet.contains(term.getName())
				|| term.getName().trim().length() == 0) {
			return false;
		}
		String natureStr = term.getNatrue().natureStr;
		// v代表动词,d代表副词,m代表数词,z代表状态词,EN代表字母,NB代表数词
		if (natureStr.contains("m")
				|| ("v".equals(natureStr) && term.getTermNatures().allFreq > 100 * length)
				|| ("d".equals(natureStr) && term.getTermNatures().allFreq > 1000)
				|| "z".equals(natureStr)
				|| term.getTermNatures() == TermNatures.NB
				|| term.getTermNatures() == TermNatures.EN) {
			return false;
		}
		return true;
	}
}
