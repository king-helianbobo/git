package org.ansj.splitWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.ansj.domain.Graph;
import org.ansj.domain.Term;
import org.ansj.domain.TermNatures;
import org.ansj.splitWord.PatternHashMap.Node;
import org.ansj.treeSplit.StringUtil;
import org.ansj.util.DictionaryReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		PatternHashMap pt = makePatHash(graph);
		// 从patternTree中查找出最大公共字串
		Collection<Node> words = pt.getWords();
		return words;
	}

	// construct pat tree
	private PatternHashMap makePatHash(Graph graph) {
		PatternHashMap pt = new PatternHashMap();
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

	public static void main(String[] args) throws IOException {

		// String content =
		// "企业为了刻意凸显自身的先进性和本就薄弱的领导力，总会打出诸如颠覆、革命等旗号，以集聚人气和关注。而这一切对万达来说，只是浮云。在当前电子商务营商环境日趋成熟，网民习惯逐渐形成的大环境下，万达电商可以高薪挖来成熟市场内的人才搭建技术平台，也不必费尽心机去络线下资源，聚合及管理供应链，甚至在自身强大的线下门店配合下，也能很轻松地越过支付和配送的壁垒，扮演电子商务";
		// content =
		// "【长乐一老板嫁女嫁妆2.1亿 席开300多桌】近日，网友微博爆料“长乐一企业家嫁女，嫁妆高达2.1亿元”。记者18日核实了此事，长乐金峰镇金峰村一李姓企业家16日嫁女，嫁妆为2.1亿元的“创业基金”。据参加婚礼的村民介绍，婚宴摆了300多桌，场面相当豪华。福州日报(图：@木板儿)";

		String content = "产量两万年中将增长两倍";
		LearnTool learn = new LearnTool();
		List<Term> paser = NlpAnalysis.parse(StringUtil.rmHtmlTag(content),
				learn);
		List<Entry<String, Double>> topTree = learn.getTopTree(100);
		log.info(topTree);
		log.info(paser);
		// System.out.println(paser);
		// List<Term> paser = ToAnalysis.paser(content);
		// System.out.println(new NewWordFind().getNewWords(paser));
	}
}
