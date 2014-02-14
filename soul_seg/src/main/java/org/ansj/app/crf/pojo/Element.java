package org.ansj.app.crf.pojo;

public class Element {
	public char name;
	private int tag = -1;
	public int len = 1;
	public String nature;
	public double[] tagScore;
	public int[] from;

	public Element(char name) {
		this.name = name;
	}

	public Element(Character name, int tag) {
		this.name = name;
		this.tag = tag;
	}

	public int getTag() {
		return tag;
	}

	public Element updateTag(int tag) {
		this.tag = tag;
		return this;
	}

	public Element updateNature(String nature) {
		this.nature = nature;
		return this;
	}

	public void len() {
		len++;
	}

	@Override
	public String toString() {
		return name + "/" + len;
	}

	public void maxFrom(double[][] transW, Element element) {
		if (from == null) {
			from = new int[this.tagScore.length];
		}
		double[] preTagScore = element.tagScore;
		for (int i = 0; i < this.tagScore.length; i++) {
			double maxValue = 0;
			for (int j = 0; j < preTagScore.length; j++) {
				// if ((rate = model.tagRate(j, i)) == 0) {
				if (transW[j][i] == 0) {
					continue;
				}
				double value = (preTagScore[j] + tagScore[i]) + transW[j][i];
				if (value > maxValue) {
					maxValue = value;
					from[i] = j;
				}
			}
			tagScore[i] = maxValue;
		}
	}

	public static char getTagName(int tag) {
		switch (tag) {
		case 0:
			return 'S';
		case 1:
			return 'B';
		case 2:
			return 'M';
		case 3:
			return 'E';
		default:
			return '?';
		}
	}
}
