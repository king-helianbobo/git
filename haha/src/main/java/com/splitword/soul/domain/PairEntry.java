package com.splitword.soul.domain;

import java.io.Serializable;

public class PairEntry implements Comparable<PairEntry>, Serializable {
	private static final long serialVersionUID = 5994821814571715244L;
	public int id;
	public int freq;

	public PairEntry(int id, int freq) {
		this.id = id;
		this.freq = freq;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		return this.id == obj.hashCode();
	}

	@Override
	public int compareTo(PairEntry o) {
		return this.id - o.id;
	}

}
