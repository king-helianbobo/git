package com.keyword.compare;

public class TokenPojo {
	String name;
	String nature;
	int docFreq;
	int totalFreq;
	int titleFreq;
	int contentFreq;

	private static final String splitTag = "\t";

	public TokenPojo(String name, String nature, int docFreq, int totalFreq,
			int titleFreq, int contentFreq) {
		this.name = name;
		this.nature = nature;
		this.docFreq = docFreq;
		this.totalFreq = totalFreq;
		this.titleFreq = titleFreq;
		this.contentFreq = contentFreq;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public int getDocFreq() {
		return docFreq;
	}

	public void setDocFreq(int docFreq) {
		this.docFreq = docFreq;
	}

	public int getTotalFreq() {
		return totalFreq;
	}

	public void setTotalFreq(int totalFreq) {
		this.totalFreq = totalFreq;
	}

	public int getTitleFreq() {
		return titleFreq;
	}

	public void setTitleFreq(int titleFreq) {
		this.titleFreq = titleFreq;
	}

	public int getContentFreq() {
		return contentFreq;
	}

	public void setContentFreq(int contentFreq) {
		this.contentFreq = contentFreq;
	}

	public String toString() {
		return name + splitTag + nature + splitTag + String.valueOf(docFreq)
				+ splitTag + String.valueOf(totalFreq) + splitTag
				+ String.valueOf(titleFreq) + splitTag
				+ String.valueOf(contentFreq);
	}
}
