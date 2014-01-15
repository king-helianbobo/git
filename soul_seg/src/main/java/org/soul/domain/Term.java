package org.soul.domain;

import org.soul.recognition.ForeignNameRecognition;
import org.soul.utility.MathUtil;

public class Term implements Comparable<Term> {
	public static final Term NULL = new Term("NULL", 0, TermNatures.NULL);

	private String name;
	private int offe;
	private TermNatures termNatures = null;

	private Term next;
	public double score = 0;
	public double selfScore = 1; // 自身概率
	private Term from; // 起始位置
	private Term to;// 到达位置
	// 本身词性.需要在词性识别之后才会有值,默认是空
	private Nature nature = TermNature.NULL.nature;

	// 是否是外国人名
	public boolean isFName = false;

	public Term(String name, int offe, TermNatures termNatures) {
		super();
		this.name = name;
		this.offe = offe;
		this.termNatures = termNatures;
		if (termNatures == TermNatures.NR || termNatures == TermNatures.NULL
				|| name.length() == 1) {
			isFName = ForeignNameRecognition.isFName(this.name);
		}
	}

	// 可以到达的位置
	public int getToValue() {
		return offe + name.length();
	}

	public int getOffe() {
		return offe;
	}

	public void setOffe(int offe) {
		this.offe = offe;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// 维特比构建最优路径
	public void setPathScore(Term from) {
		double score = from.getScore() + MathUtil.compuScore(from, this);
		if (this.from == null || this.getScore() >= score) {
			this.setFromAndScore(from, score);
		}
	}

	// 维特比算法构建最优路径
	public void setPathSelfScore(Term from) {
		double score = from.getScore() + this.selfScore;
		if (this.from == null || this.getScore() > score) {
			this.setFromAndScore(from, score);
		}
	}

	private void setFromAndScore(Term from, double score) {
		this.from = from;
		this.score = score;
	}

	public Term merge(Term to) {
		this.name = this.name + to.getName();
		return this;
	}

	public void updateOffe(int offe) {
		this.offe += offe;
	}

	public Term getNext() {
		return next;
	}

	public Term setNext(Term next) {
		this.next = next;
		return this;
	}

	public double getScore() {
		return this.score;
	}

	public Term getFrom() {
		return from;
	}

	public Term getTo() {
		return to;
	}

	public void setFrom(Term from) {
		this.from = from;
	}

	public void setTo(Term to) {
		this.to = to;
	}

	public TermNatures getTermNatures() {
		return termNatures;
	}

	public void setTermNatures(TermNatures termNatures) {
		this.termNatures = termNatures;
	}

	@Override
	public int compareTo(Term o) {
		if (this.score > o.score)
			return 0;
		else
			return 1;
	}

	public void setNature(Nature nature) {
		this.nature = nature;
	}

	/**
	 * 获得这个词的词性.词性计算后才可生效
	 * 
	 * @return
	 */
	public Nature getNatrue() {
		return nature;
	}

	@Override
	public String toString() {
		if (nature != null && !"null".equals(nature.natureStr)) {
			return "[" + this.name + "/" + nature.natureStr + "]";
		} else {
			return "[" + this.name + "]";
		}
	}

	/**
	 * 将term的所有分数置为0
	 */
	public void clearScore() {
		this.score = 0;
		this.selfScore = 0;
	}
}
