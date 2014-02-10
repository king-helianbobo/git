package org.soul.treeSplit;

import java.util.Arrays;

public class SmartForest<T> implements Comparable<SmartForest<T>> {

	/**
	 * 1 doesn't represent one word ,but could continue, 2 represent one word
	 * but could continue, 3 represent one word ,could not continue
	 */
	public SmartForest<T>[] branches = null; // subTree
	private char c;// root Char
	private byte status = 1;
	SmartForest<T> tmpBranch = null;
	private T param = null; // corresponding parameters

	public SmartForest() {
	}

	private SmartForest(char c) {
		this.c = c;
	}

	/**
	 * add sub branch
	 * 
	 * @param branch
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private SmartForest<T> add(SmartForest<T> branch) {
		if (branches == null) {
			branches = new SmartForest[0];
		}
		int bs = get(branch.getC());
		if (bs > -1) {
			this.tmpBranch = this.branches[bs];
			switch (branch.getStatus()) {
				case -1 : // if already deleted
					this.tmpBranch.setStatus(1);
					break;
				case 1 :
					if (this.tmpBranch.getStatus() == 3) {
						this.tmpBranch.setStatus(2);
					}
					break;
				case 3 :
					if (this.tmpBranch.getStatus() != 3) {
						this.tmpBranch.setStatus(2);
					}
					this.tmpBranch.setParam(branch.getParam());
			}
			return this.tmpBranch;
		}
		if (bs < 0) {
			SmartForest<T>[] newBranches = new SmartForest[branches.length + 1];
			int insert = -(bs + 1);
			System.arraycopy(this.branches, 0, newBranches, 0, insert);
			System.arraycopy(branches, insert, newBranches, insert + 1,
					branches.length - insert);
			newBranches[insert] = branch;
			this.branches = newBranches;
		}
		return branch;
	}

	public SmartForest(char c, int status, T param) {
		this.c = c;
		this.status = (byte) status;
		this.param = param;
	}

	public int get(char c) {
		if (branches == null)
			return -1;
		int i = Arrays.binarySearch(this.branches, new SmartForest<T>(c));
		return i;
	}

	public boolean contains(char c) {
		if (this.branches == null) {
			return false;
		}
		return Arrays.binarySearch(this.branches, c) > -1;
	}

	public int compareTo(char c) {
		if (this.c > c)
			return 1;
		if (this.c < c) {
			return -1;
		}
		return 0;
	}

	public boolean equals(char c) {
		return this.c == c;
	}

	@Override
	public int hashCode() {
		return this.c;
	}

	public byte getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = (byte) status;
	}

	public char getC() {
		return this.c;
	}

	public T getParam() {
		return this.param;
	}

	public void setParam(T param) {
		this.param = param;
	}

	/**
	 * add new word to Forest
	 */
	public void add(String keyWord, T t) {
		SmartForest<T> _branch = this;
		for (int i = 0; i < keyWord.length(); i++) {
			if (keyWord.length() == i + 1) {
				_branch.add(new SmartForest<T>(keyWord.charAt(i), 3, t));
			} else {
				_branch.add(new SmartForest<T>(keyWord.charAt(i), 1, null));
			}
			_branch = _branch.branches[_branch.get(keyWord.charAt(i))];
		}
	}

	public int compareTo(SmartForest<T> o) {
		if (this.c > o.c)
			return 1;
		if (this.c < o.c) {
			return -1;
		}
		return 0;
	}

	/**
	 * get this term's parameters, else return null
	 * 
	 * @param keyWord
	 */
	public SmartForest<T> getBranch(String keyWord) {
		SmartForest<T> tempBranch = this;
		int index = 0;
		for (int j = 0; j < keyWord.length(); j++) {
			index = tempBranch.get(keyWord.charAt(j));
			if (index < 0) {
				return null;
			}
			tempBranch = tempBranch.branches[index];
		}
		return tempBranch;
	}
}
