package com.splitword.soul.domain;

public class KeyWord implements Comparable<KeyWord> {
	private String name;
	private double score;
	private double termIdf;
	private int termFreq;
	private double termWeight;

	public KeyWord(String name, int docFreq, double weight) {
		this.name = name;
		termIdf = Math.log(10000 + 10000.0 / (docFreq + 1));
		score = termIdf * weight;
		termWeight = weight;
		termFreq = 1;
	}

	public KeyWord(String name, double idf, double weight) {
		this.name = name;
		this.termIdf = idf;
		// this.score = termIdf * weight;
		this.termWeight = weight;
		this.termFreq = 1;
	}

	public void increTermFreq() {
		termFreq++;
	}

	public int getTermFreq() {
		return termFreq;
	}

	public void score(double termFreq) {
		this.score = termIdf * termWeight * termFreq;
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
		return name;
	}

	public double getScore() {
		return score;
	}

}
