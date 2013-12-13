package org.soul.domain;

public class NewWord {

	private String name;
	private double score;
	private TermNatures nature;
	private int allFreq; // 词频
	private double averageScore; // 平均分数
	private boolean isActive = false;// 此词是否可用

	public NewWord(String name, TermNatures nature, double score, int freq) {
		this.name = name;
		this.nature = nature;
		this.score = getScore(nature, score);
		this.allFreq = freq;
		averageScore = score;
		if (allFreq > 2 || averageScore < -0.5) {
			isActive = true;
		}
	}

	// 更新权重,NW代表newWord,NT代表company,NR代表name
	private double getScore(TermNatures nature, double score) {
		if (TermNatures.NW.equals(nature)) {
			return score * -1;
		} else if (TermNatures.NR.equals(nature)) {
			return score * 100;
		} else if (TermNatures.NT.equals(nature)) {
			return score * 10;
		}
		return score;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getScore() {
		return score;
	}

	public TermNatures getNature() {
		return nature;
	}

	public void setNature(TermNatures nature) {
		this.nature = nature;
	}

	public void update(double score, TermNatures tn, int freq) {
		this.score += getScore(tn, score);
		this.allFreq += freq;
		this.averageScore = this.score / freq;
		if (tn == null || !TermNatures.NW.equals(tn)) {
			this.nature = tn;
		}
		if (allFreq > 2 || averageScore < -0.5) {
			isActive = true;
		}
	}

	@Override
	public String toString() {
		return this.name + "\t" + this.score + "\t"
				+ this.getNature().termNatures[0];
	}

	public int getAllFreq() {
		return allFreq;
	}

	public double getAverageScore() {
		return averageScore;
	}

}
