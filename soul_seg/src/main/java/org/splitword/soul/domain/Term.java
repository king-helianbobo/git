package org.splitword.soul.domain;

import java.util.List;

import org.splitword.soul.utility.MathUtil;

public class Term implements Comparable<Term> {
	private String name;
	private String realName;
	private int offe;
	private TermNatures termNatures = TermNatures.NULL;
	private List<Term> subTerm = null;
	private Term next;
	public double score = 0;
	public double selfScore = 1; // 自身概率
	private Term from; // 起始位置
	private Term to;// 到达位置
	// term本身的词性，必须在词性识别之后才有值，默认为空
	private NatureInLib nature = TermNature.NULL.natureInLib;

	public Term(String name, int offe, TermNatures termNatures) {
		super();
		this.name = name;
		this.offe = offe;
		if (termNatures != null)
			this.termNatures = termNatures;
	}

	public Term(String name, int offe, String natureStr, int natureFreq) {
		super();
		this.name = name;
		this.offe = offe;
		TermNature termNature = new TermNature(natureStr, natureFreq);
		this.nature = termNature.natureInLib;
		this.termNatures = new TermNatures(termNature);
	}

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

	public void setPathScore(Term from) {
		// 维特比构建最优路径
		double score = from.getScore() + MathUtil.compuScore(from, this);
		if (this.from == null || this.getScore() >= score) {
			this.setFromAndScore(from, score);
		}
	}

	public void setPathSelfScore(Term from) {
		// 维特比算法构建最优路径
		double score = from.getScore() + this.selfScore; // 自身权重
		if (this.from == null || this.getScore() >= score) {
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

	public void setNature(NatureInLib nature) {
		this.nature = nature;
	}

	/**
	 * 获得这个词的词性.词性计算后才可生效
	 * 
	 * @return
	 */
	public NatureInLib getNatrue() {
		return nature;
	}

	/**
	 * 将term的所有分数置为0
	 */
	public void clearScore() {
		this.score = 0;
		this.selfScore = 0;
	}

	public void setSubTermList(List<Term> subTerm) {
		this.subTerm = subTerm;
	}

	public List<Term> getSubTermList() {
		return subTerm;
	}

	public String getRealName() {
		if (realName == null) {
			return name;
		}
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Override
	public String toString() {
		if (nature != null && !"null".equals(nature.natureStr)) {
			return "[" + this.name + "/" + nature.natureStr + "]";
		} else {
			return "[" + this.name + "]";
		}
	}
}
