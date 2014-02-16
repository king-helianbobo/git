package org.soul.domain;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.analysis.Analysis.Merger;
import org.soul.library.InitDictionary;
import org.soul.utility.WordAlter;

//维特比构建最优路径使用的图

public class ViterbiGraph {
	private static Log log = LogFactory.getLog(ViterbiGraph.class);
	// public char[] chars = null;
	public String convertedStr = null;
	protected String realStr = null;
	public Term[] terms = null;
	protected Term end = null;
	protected Term root = null;
	protected static final String E = "末##末";
	protected static final String B = "始##始";
	public boolean hasPerson; // 是否有人名
	public boolean hasNum; // 是否有数字

	public ViterbiGraph(String str) {
		this.realStr = str;
		this.convertedStr = WordAlter.alterAlphaAndNumber(str, 0, str.length());
		int size = str.length();
		terms = new Term[size + 1];
		end = new Term(E, size, TermNatures.END);
		root = new Term(B, -1, TermNatures.BEGIN);
		terms[size] = end;
	}

	public List<Term> getResult(Merger merger) {
		return merger.merge();
	}

	public void addTerm(Term term) {
		// 是否有数字，比如两，三,等
		if (!hasNum && term.getTermNatures().numNature.numFreq > 0) {
			hasNum = true;
		}
		// 是否有人名
		if (!hasPerson && term.getTermNatures().personNature.flag) {
			hasPerson = true;
		}
		// 将词放到图的位置
		if (terms[term.getOffe()] == null) {
			terms[term.getOffe()] = term;
		} else {
			int offe = term.getOffe();
			Term tmpTerm = terms[offe];
			while (tmpTerm != null) {
				if (tmpTerm.getToValue() == term.getToValue()) { // term 已经加进去了
					return;
				}
				tmpTerm = tmpTerm.getNext();
			}
			// 现在，把term加进来
			terms[offe] = term.setNext(terms[offe]);
		}
	}

	// construct optimal path
	protected Term optimalRoot() {
		Term to = end;
		to.clearScore();
		Term from = null;
		while ((from = to.getFrom()) != null) {
			for (int i = from.getOffe() + 1; i < to.getOffe(); i++) {
				terms[i] = null;
			}
			if (from.getOffe() > -1) {
				terms[from.getOffe()] = from;
			}
			// break linkedList to save memory
			from.setNext(null);
			from.setTo(to);
			from.clearScore();
			to = from;
		}
		return root;
	}

	/**
	 * 删除最短的节点
	 */
	public void rmLittlePath() {
		int maxTo = -1;
		Term temp = null;
		Term maxTerm = null;

		boolean flag = false; // 是否有交叉
		int length = terms.length - 1;
		for (int i = 0; i < length; i++) {
			maxTerm = getMaxTerm(i);
			if (maxTerm == null)
				continue;
			maxTo = maxTerm.getToValue();

			/**
			 * 对字数进行优化.如果一个字.就跳过..两个字.且第二个为null则.也跳过.从第二个后开始
			 */
			switch (maxTerm.getName().length()) {
			case 1:
				continue;
			case 2:
				if (terms[i + 1] == null) {
					i = i + 1;
					continue;
				}
			}

			/**
			 * 判断是否有交叉
			 */
			for (int j = i + 1; j < maxTo; j++) {
				temp = getMaxTerm(j);
				if (temp == null) {
					continue;
				}
				if (maxTo < temp.getToValue()) {
					maxTo = temp.getToValue();
					flag = true;
				}
			}

			if (flag) {
				i = maxTo - 1;
				flag = false;
			} else {
				maxTerm.setNext(null);
				terms[i] = maxTerm;
				for (int j = i + 1; j < maxTo; j++) {
					terms[j] = null;
				}
			}
		}
	}

	/**
	 * 得到本offset下的最长term
	 * 
	 * @param i
	 * @return
	 */
	private Term getMaxTerm(int i) {
		Term maxTerm = terms[i];
		if (maxTerm == null) {
			return null;
		}
		int maxTo = maxTerm.getToValue();
		Term term = maxTerm;
		while ((term = term.getNext()) != null) {
			if (maxTo < term.getToValue()) {
				maxTo = term.getToValue();
				maxTerm = term;
			}
		}
		return maxTerm;
	}

	/**
	 * 删除无意义的节点,防止viterbi太多
	 */
	public void rmLittleSinglePath() {
		int maxTo = -1;
		Term temp = null;
		for (int i = 0; i < terms.length; i++) {
			if (terms[i] == null)
				continue;
			maxTo = terms[i].getToValue();
			if (maxTo - i == 1 || i + 1 == terms.length)
				continue;
			for (int j = i; j < maxTo; j++) {
				temp = terms[j];
				if (temp != null && temp.getToValue() <= maxTo
						&& temp.getName().length() == 1) {
					terms[j] = null;
				}
			}
		}
	}

	public void walkPathByScore() {
		Term term = null;
		mergeByScore(root, 0);
		for (int i = 0; i < terms.length; i++) {
			term = terms[i];
			while (term != null && term.getFrom() != null && term != end) {
				int to = term.getToValue();
				mergeByScore(term, to);
				term = term.getNext();
			}
		}
		optimalRoot();
	}

	public void walkPath() {
		Term term = null;
		// BEGIN先行打分
		merge(root, 0);
		for (int i = 0; i < terms.length; i++) {
			term = terms[i];
			while (term != null && term.getFrom() != null && term != end) {
				int to = term.getToValue();
				merge(term, to);
				term = term.getNext();
			}
		}
		optimalRoot();
	}

	private void merge(Term fromTerm, int to) {
		Term term = null;
		if (terms[to] != null) {
			term = terms[to];
			while (term != null) {
				term.setPathScore(fromTerm);
				term = term.getNext();
			}
		} else {
			char c = this.convertedStr.charAt(to);
			log.info(c);
			TermNatures tn = InitDictionary.termNatures[c];
			if (tn == null)
				tn = TermNatures.NW;
			terms[to] = new Term(String.valueOf(c), to, tn);
			terms[to].setPathScore(fromTerm);
		}
	}

	private void mergeByScore(Term fromTerm, int to) {
		Term term = null;
		if (terms[to] != null) {
			term = terms[to];
			while (term != null) {
				// 关系式to.set(from)
				term.setPathSelfScore(fromTerm);
				term = term.getNext();
			}
		}
	}

}
