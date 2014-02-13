package org.soul.utility;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Term;
import org.soul.library.BiGramLibrary;
import org.soul.library.NatureLibrary;
import org.soul.recognition.NatureRecognition.NatureTerm;

public class MathUtil {
	public static Log log = LogFactory.getLog(MathUtil.class);

	private static final double dSmoothingPara = 0.1; // smoothing parameter

	private static final int MAX_FREQUENCE = 2079997;// max frequency

	// ﻿Two linked Words frequency, 当x1出现后，x2随机出现的概率
	private static final double dTemp = (double) 1 / MAX_FREQUENCE;

	// 一个词到另一个词的分数,相当于后验概率P(x2|x1)，即x1发生后，x2发生的概率
	public static double compuScore(Term from, Term to) {
		double frequency = from.getTermNatures().allFreq + 1;// 词性频率之和
		if (frequency < 0) { // if no value,return maximum score
			return MAX_FREQUENCE;
		}

		int nTwoWordsFreq = BiGramLibrary.getTwoWordFreq(from, to);
		double value = -Math.log(dSmoothingPara * frequency
				/ (MAX_FREQUENCE + 80000) + (1 - dSmoothingPara)
				* ((1 - dTemp) * nTwoWordsFreq / frequency + dTemp));

		if (value < 0)
			value += frequency;

		if (value < 0) {
			value += frequency;
		}
		return value;
	}

	/**
	 * 词性词频词长.计算出来一个分数
	 * 
	 * @param from
	 * @param term
	 * @return
	 */
	public static double compuScoreFreq(Term from, Term term) {
		// TODO Auto-generated method stub
		return from.getTermNatures().allFreq + term.getTermNatures().allFreq;
	}

	// compute score between two termNatures
	public static double compuNatureFreq(NatureTerm from, NatureTerm to) {
		double twoWordFreq = NatureLibrary.getTwoNatureFreq(
				from.termNature.natureInLib, to.termNature.natureInLib);
		if (twoWordFreq == 0) {
			twoWordFreq = Math.log(from.selfScore + to.selfScore);
		}
		double score = from.score
				+ Math.log((from.selfScore + to.selfScore) * twoWordFreq)
				+ to.selfScore;
		return score;
	}

	// 新词熵及其左右熵
	// public static double leftRightEntropy(List<Term> all) {
	// double score = 0;
	// NewWordNature newWordAttr = null;
	// Term begin = all.get(0);
	//
	// // 查看左邻居
	// int twoWordFreq = NGramLibrary.getTwoWordFreq(begin.getFrom(), begin);
	// score -= twoWordFreq;
	//
	// // 查看右邻居
	// int length = all.size() - 1;
	// Term end = all.get(all.size() - 1);
	// twoWordFreq = NGramLibrary.getTwoWordFreq(end, end.getTo());
	// score -= twoWordFreq;
	//
	// // 查看内部链接
	// for (int i = 0; i < length; i++) {
	// score -= NGramLibrary.getTwoWordFreq(all.get(i), all.get(i + 1));
	// }
	// if (score < -3) {
	// return 0;
	// }
	//
	// // 首字分数
	// newWordAttr = begin.getTermNatures().newWordNature;
	// score += getTermScore(newWordAttr, newWordAttr.getB());
	// // 末字分数
	// newWordAttr = end.getTermNatures().newWordNature;
	// score += getTermScore(newWordAttr, newWordAttr.getE());
	// // 中词分数
	// double midelScore = 0;
	// Term term = null;
	// for (int i = 1; i < length; i++) {
	// term = all.get(i);
	// newWordAttr = term.getTermNatures().newWordNature;
	// midelScore += getTermScore(newWordAttr, newWordAttr.getM());
	// }
	// score += midelScore / (length);
	// return score;
	// }

	// private static double getTermScore(NewWordNature newWordAttr, int freq) {
	// if (newWordAttr == NewWordNature.NULL) {
	// return 3;
	// }
	// return (freq / (double) (newWordAttr.getAll() + 1))
	// * Math.log(500000 / (double) (newWordAttr.getAll() + 1));
	// }

}
