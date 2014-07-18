package com.elasticsearch.application.search;

import java.io.Serializable;

/**
 * Freq entity model
 * 
 * @author root
 * @version 1.0
 * 
 */
public class FreqEntity implements Serializable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FreqEntity other = (FreqEntity) obj;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}

	private static final long serialVersionUID = 3321706329117939785L;

	private String word;

	private String attri;

	private long containPage_num;

	private long total_num;

	private long title_num;

	private long content_num;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getAttri() {
		return attri;
	}

	public void setAttri(String attri) {
		this.attri = attri;
	}

	public long getContainPage_num() {
		return containPage_num;
	}

	public void setContainPage_num(long containPage_num) {
		this.containPage_num = containPage_num;
	}

	public long getTotal_num() {
		return total_num;
	}

	public void setTotal_num(long total_num) {
		this.total_num = total_num;
	}

	public long getTitle_num() {
		return title_num;
	}

	public void setTitle_num(long title_num) {
		this.title_num = title_num;
	}

	public long getContent_num() {
		return content_num;
	}

	public void setContent_num(long content_num) {
		this.content_num = content_num;
	}

}
