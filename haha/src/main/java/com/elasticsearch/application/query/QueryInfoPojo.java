package com.elasticsearch.application.query;

public class QueryInfoPojo {
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void seqNumber(int seq) {
		this.seqNumber = seq;
	}

	public int seqNumber() {
		return this.seqNumber;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public QueryInfoPojo(String name, String nature, int position) {
		this.name = name;
		this.nature = nature;
		this.position = position;
	}

	private Integer position = 0;
	private String name = null;
	private String nature = null;
	private int seqNumber = 0;
}
