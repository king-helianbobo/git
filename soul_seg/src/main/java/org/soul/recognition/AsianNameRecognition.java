package org.soul.recognition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.analysis.BasicAnalysis;
import org.soul.domain.NatureInLib;
import org.soul.domain.NewWord;
import org.soul.domain.PersonNatureAttr;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.library.BiGramLibrary;
import org.soul.utility.TermUtil;

public class AsianNameRecognition {

	// private static Log log = LogFactory.getLog(AsianNameRecognition.class);
	private static final double[] FACTORY = {0.16271366224044456,
			0.8060521860870434, 0.031234151672511947};
	private boolean skip = false;
	private Term[] terms;

	public AsianNameRecognition(Term[] terms) {
		this.terms = terms;
	}

	public void recognition() {
		List<Term> termList = recogntion_();
		for (Term term2 : termList) {
			TermUtil.insertTerm(terms, term2);
		}
	}

	private List<Term> recogntion_() {
		List<Term> termList = new ArrayList<Term>();
		int beginFreq = 10;
		for (int i = 0; i < terms.length; i++) {
			Term term = terms[i];
			if (term == null)
				continue;
			if (!term.getTermNatures().personNature.flag) {
				beginFreq = term.getTermNatures().personNature.begin + 1;
				continue;
			}
			term.score = 0;
			term.selfScore = 0;
			int freq = 0;
			for (int len = 2; len > -1; len--) {
				freq = term.getTermNatures().personNature.getFreq(len, 0);
				// 这段代码不太理解?垃圾代码吗
				if ((freq > 10) || (term.getName().length() == 2 && freq > 10)) {
					Term tmpTerm = nameFind(i, beginFreq, len);
					if (tmpTerm != null) {
						termList.add(tmpTerm);
						if (skip) {
							for (int j2 = i; j2 < tmpTerm.getToValue(); j2++) {
								if (terms[j2] != null) {
									terms[j2].score = 0;
									terms[j2].selfScore = 0;
								}
							}
							i = tmpTerm.getToValue() - 1;
							break;
						}
					}
				}
			}
			beginFreq = term.getTermNatures().personNature.begin + 1;
		}
		return termList;
	}

	private Term nameFind(int offe, int beginFreq, int size) {
		StringBuilder sb = new StringBuilder();
		int undefinite = 0;
		skip = false;
		PersonNatureAttr pna = null;
		int index = 0;
		int freq = 0;
		double allFreq = 0;
		Term term = null;
		int i = offe;

		for (; i < terms.length; i++) {
			if (terms[i] == null) {
				continue;
			}
			term = terms[i];
			pna = term.getTermNatures().personNature;
			// 这个长度的这个位置的词频,如果没有就干掉,跳出循环
			if ((freq = pna.getFreq(size, index)) == 0) {
				return null;
			}
			if (pna.freqSum > 0)
				undefinite++;
			sb.append(term.getName());
			allFreq += Math.log(term.getTermNatures().allFreq + 1);
			allFreq += -Math.log((freq));
			index++;
			if (index == size + 2) {
				break;
			}
		}

		double score = -Math.log(FACTORY[size]);
		score += allFreq;
		double endFreq = 0;

		// 开始寻找结尾词
		boolean flag = true;
		while (flag) {
			i++;
			if (i >= terms.length) {
				endFreq = 10;
				flag = false;
			} else if (terms[i] != null) {
				// int twoWordFreq = NgramLibrary.getTwoWordFreq(term,
				// terms[i]);
				// if (twoWordFreq > 3) { // 如果这两个词之间有比较高的概率在一起，则忽略
				// return null;
				// }
				endFreq = terms[i].getTermNatures().personNature.end + 1;
				flag = false;
			}
		}

		score -= Math.log(endFreq);
		score -= Math.log(beginFreq);

		if (score > -3) { // 如果分数过高，认为没有发现人名
			return null;
		}

		if (allFreq > 0 && undefinite > 0) { // 如果词作为姓名的总体概率很低
			return null;
		}
		skip = undefinite == 0; // 姓名中包含着可能的歧义
		term = new Term(sb.toString(), offe, TermNatures.NR);
		term.selfScore = score; // term 的分数，当然是越低越好
		// term is new generated object ,not reference
		return term;
	}

	public List<NewWord> getNewWords() {
		List<NewWord> all = new ArrayList<NewWord>();
		List<Term> termList = recogntion_();
		for (Term t2 : termList) {
			all.add(new NewWord(t2.getName(), NatureInLib.NR, -t2.getName()
					.length()));
		}
		return all;
	}

	/**
	 * 人名消歧,[邓颖超生前]->[邓颖/超生/前] modify to [邓颖超/生/前]
	 * 
	 * @author LiuBo
	 * @since 2014年1月14日
	 * @param terms
	 *            来自于Graph的Terms列表 void
	 */
	public static void nameAmbiguity(Term[] terms) {
		Term term = null;
		Term next = null;
		for (int i = 0; i < terms.length - 1; i++) {
			term = terms[i];
			if (term != null && term.getTermNatures() == TermNatures.NR
					&& term.getName().length() == 2) {
				next = terms[i + 2];
				if (next.getTermNatures().personNature.split > 0) {
					term.setName(term.getName() + next.getName().charAt(0));
					terms[i + 2] = null;
					terms[i + 3] = new Term(next.getName().substring(1),
							next.getOffe(), TermNatures.NULL);
					TermUtil.termLink(term, terms[i + 3]);
					TermUtil.termLink(terms[i + 3], next.getTo());
				}
			}
		}
	}

}
