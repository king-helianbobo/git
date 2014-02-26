package org.splitword.soul.domain;

public class NewWord {

	private String name;
	private double score;
	private NatureInLib nature;
	private int allFreq; // 总词频

	public NewWord(String name, NatureInLib nature, double score) {
		this.name = name;
		this.nature = nature;
		this.score = score;
		this.allFreq = 1;
	}

	// public NewWord(String name, NatureInLib nature, double score, int freq) {
	// this.name = name;
	// this.nature = nature;
	// this.score = getScore(nature, score);
	// this.allFreq = freq;
	// averageScore = score;
	// if (allFreq > 2 || averageScore < -0.5) {
	// isActive = true;
	// }
	// }

	// 更新权重,NW代表newWord,NT代表company,NR代表name
	// public double getScore() {
	// // if (TermNatures.NW.equals(nature)) {
	// // return score * -1;
	// // } else if (TermNatures.NR.equals(nature)) {
	// // return score * 100;
	// // } else if (TermNatures.NT.equals(nature)) {
	// // return score * 10;
	// // }
	// return score;
	// }

	public void setScore(double score) {
		this.score = score;
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

	public NatureInLib getNature() {
		return nature;
	}

	public void setNature(NatureInLib nature) {
		this.nature = nature;
	}

	public void update(double score, NatureInLib nature, int freq) {
		this.score += score * freq;
		this.allFreq += freq;
		if (NatureInLib.NW != nature) {
			this.nature = nature;
		}
		// this.score += getScore(nature, score);
		// this.allFreq += freq;
		// this.averageScore = this.score / freq;
		// if (nature == null || !TermNatures.NW.equals(nature)) {
		// this.nature = nature;
		// // }
		// if (allFreq > 2 || averageScore < -0.5) {
		// isActive = true;
		// }
	}

	@Override
	public String toString() {
		return this.name + "\t" + this.score + "\t"
				+ this.getNature().natureStr;
	}

	public int getAllFreq() {
		return allFreq;
	}

}
