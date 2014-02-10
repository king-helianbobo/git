package org.soul.splitWord;

public class KeyWord implements Comparable<KeyWord> {
	private String name;
	private double score;
	private double idf;
	private int freq;

	public KeyWord(String name, int docFreq, int weight) {
		this.name = name;
		this.idf = Math.log(10000 + 10000.0 / (docFreq + 1));
		this.score = idf * weight;
		freq++;
	}

	public void updateWeight(int weight) {
		this.score += weight * idf;
		freq++;
	}

	public int getFreq() {
		return freq;
	}

	@Override
	public int compareTo(KeyWord o) {
		if (this.score < o.score) {
			return 1;
		} else {
			return -1;
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KeyWord) {
			KeyWord k = (KeyWord) obj;
			return k.name.equals(name);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return name;// "="+score+":"+freq+":"+idf;
	}

}
