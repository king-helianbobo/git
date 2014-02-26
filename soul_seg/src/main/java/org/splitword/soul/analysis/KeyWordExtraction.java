package org.splitword.soul.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.splitword.soul.domain.KeyWord;
import org.splitword.soul.domain.Term;
import org.splitword.soul.recognition.LearnTool;

public class KeyWordExtraction {
	private final Log log = LogFactory.getLog(KeyWordExtraction.class);
	private int nKeyword = 5;

	public KeyWordExtraction() {
		nKeyword = 5;
	}

	public KeyWordExtraction(int nKeyword) {
		this.nKeyword = nKeyword;
	}

	private List<KeyWord> computeArticleTfIdf(String content, int titleLength) {
		Map<String, KeyWord> tm = new HashMap<String, KeyWord>();
		LearnTool learn = new LearnTool();
		List<Term> parse = NlpAnalysis.parse(content, learn);
		// parse = NlpAnalysis.parse(content, learn);

		for (Term term : parse) {
			int weight = getWeight(term, content.length(), titleLength);
			if (weight == 0)
				continue;
			KeyWord keyword = tm.get(term.getName());
			if (keyword == null) {
				keyword = new KeyWord(term.getName(),
						term.getNatrue().allFrequency, weight);
				log.info(term.getName() + " " + term.getNatrue().allFrequency
						+ " " + term.getNatrue().natureStr);
				tm.put(term.getName(), keyword);
			} else {
				keyword.updateWeight(1);
				log.info("Frequency " + keyword.toString() + " , "
						+ keyword.getFreq());
			}
		}
		TreeSet<KeyWord> treeSet = new TreeSet<KeyWord>(tm.values());
		ArrayList<KeyWord> arrayList = new ArrayList<KeyWord>(treeSet);
		if (treeSet.size() < nKeyword) {
			return arrayList;
		} else {
			return arrayList.subList(0, nKeyword);
		}

	}

	/**
	 * 
	 * @param title
	 * 
	 * @param content
	 * 
	 * @return
	 */
	public Collection<KeyWord> computeArticleTfidf(String title, String content) {
		return computeArticleTfIdf(title + "\t" + content, title.length());
	}

	/**
	 * only content, no title
	 * 
	 * @param content
	 * @return
	 */
	public Collection<KeyWord> computeArticleTfidf(String content) {
		return computeArticleTfIdf(content, 30);
	}

	private int getWeight(Term term, int length, int titleLength) {
		if (term.getName().matches("(?s)\\d.*")) {
			// it this is digit
			log.info("这是什么 " + term.getName());
			return 0;
		}

		if (term.getName().trim().length() < 2) {
			return 0;
		}

		String termNature = term.getNatrue().natureStr;

		if (!termNature.startsWith("n") || "num".equals(termNature)) {
			// must be nouns
			return 0;
		}
		int weight = 0;

		if (term.getOffe() < titleLength) { // if this term belongs to title
			return 20;
		}
		// position
		double position = (term.getOffe() + 0.0) / length;
		if (position < 0.05)
			return 10;
		weight += (5 - 5 * position);
		return weight;
	}

}
