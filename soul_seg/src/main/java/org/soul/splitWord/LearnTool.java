package org.soul.splitWord;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.ViterbiGraph;
import org.soul.domain.NewWord;
import org.soul.domain.TermNatures;
import org.soul.recognition.AsianNameRecognition;
import org.soul.recognition.CompanyRecogntion;
import org.soul.recognition.ForeignNameRecognition;
import org.soul.splitWord.PatternMap.Node;
import org.soul.treeSplit.CollectionUtil;
import org.soul.treeSplit.SmartForest;

public class LearnTool {
	private static final Log log = LogFactory.getLog(LearnTool.class);
	public boolean isCompany = true;
	public boolean isNewWord = true;
	public boolean isAsianName = true;
	public boolean isForeignName = true;
	public int count; // number of new Word we found
	private final SmartForest<NewWord> sf = new SmartForest<NewWord>();

	public void learn(ViterbiGraph graph) {
		if (isCompany) {
			// find organization
			findCompany(graph);
		}
		if (isAsianName) {
			// find Chinese and Korea Name
			findAsianPerson(graph);
		}
		if (isForeignName) {
			// find English name
			findForeignPerson(graph);
		}
		if (isNewWord) {
			newWordDetection(graph);
		}
	}

	private void findAsianPerson(ViterbiGraph graph) {
		List<NewWord> newWords = new AsianNameRecognition(graph.terms)
				.getNewWords();
		addListToTerm(newWords);
	}

	private void findForeignPerson(ViterbiGraph graph) {
		List<NewWord> newWords = new ForeignNameRecognition(graph.terms)
				.getNewWords();
		addListToTerm(newWords);
	}

	private void findCompany(ViterbiGraph graph) {
		List<NewWord> newWords = new CompanyRecogntion(graph.terms)
				.getNewWords();
		addListToTerm(newWords);
	}

	// add new word to Forest
	private void addListToTerm(List<NewWord> newWords) {
		if (newWords.size() == 0)
			return;
		for (NewWord newWord : newWords) {
			addTerm(newWord);
		}
	}

	private void newWordDetection(ViterbiGraph graph) {
		Collection<Node> newWords = null;
		try {
			newWords = new NewWordDetection().getNewWords(graph);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (newWords == null)
			return;
		NewWord newWord = null;
		for (Node node : newWords) {
			newWord = new NewWord(node.getName(), TermNatures.NW,
					node.getScore(), node.getFreq());
			addTerm(newWord);
		}
	}

	public void addTerm(NewWord newWord) {
		NewWord temp = null;
		SmartForest<NewWord> smartForest = null;
		if ((smartForest = sf.getBranch(newWord.getName())) != null
				&& smartForest.getParam() != null) {
			temp = smartForest.getParam();
			temp.update(newWord.getScore(), newWord.getNature(),
					newWord.getAllFreq());
		} else {
			count++;
			// number of newWord plus 1
			synchronized (sf) {
				sf.add(newWord.getName(), newWord);
			}
		}
	}

	public SmartForest<NewWord> getForest() {
		return this.sf;
	}

	/**
	 * return new word we found
	 * 
	 * @param num
	 *            返回数目.0为全部返回
	 * @return
	 */
	public List<Entry<String, Double>> getTopTree(int num) {
		return getTopTree(num, null);
	}

	public List<Entry<String, Double>> getTopTree(int num, TermNatures nature) {
		if (sf.branches == null) {
			return null;
		}
		HashMap<String, Double> hm = new HashMap<String, Double>();
		for (int i = 0; i < sf.branches.length; i++) {
			valueResult(sf.branches[i], hm, nature);
		}
		List<Entry<String, Double>> sortMapByValue = CollectionUtil
				.sortMapByValue(hm, -1);
		if (num == 0) {
			return sortMapByValue;
		} else {
			num = Math.min(num, sortMapByValue.size());
			return sortMapByValue.subList(0, num);
		}
	}

	private void valueResult(SmartForest<NewWord> smartForest,
			HashMap<String, Double> map, TermNatures nature) {
		for (int i = 0; i < smartForest.branches.length; i++) {
			NewWord param = smartForest.branches[i].getParam();
			if (smartForest.branches[i].getStatus() == 3) {
				if (nature == null || param.getNature().equals(nature)) {
					map.put(param.getName(), param.getScore());

				}
			} else if (smartForest.branches[i].getStatus() == 2) {
				if (nature == null || param.getNature().equals(nature)) {
					map.put(param.getName(), param.getScore());
				}
				valueResult(smartForest.branches[i], map, nature);
			} else {
				valueResult(smartForest.branches[i], map, nature);
			}
		}
	}
}
