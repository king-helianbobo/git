package com.splitword.soul.utility;

public class Forest implements WoodInterface {
	private WoodInterface[] branchs = new WoodInterface[65536];

	public WoodInterface add(WoodInterface branch) {
		WoodInterface temp = this.branchs[branch.getC()];
		if (temp == null)
			this.branchs[branch.getC()] = branch;
		else {
			switch (branch.getStatus()) {
			case 1:
				if (temp.getStatus() == 3) {
					temp.setStatus(2);
				}
				break;
			case 3:
				if (temp.getStatus() == 1) {
					temp.setStatus(2);
				}
				temp.setParam(branch.getParams());
			}
		}
		return this.branchs[branch.getC()];
	}

	public boolean contains(char c) {
		return this.branchs[c] != null;
	}

	public WoodInterface get(char c) {
		if (c > 66535) {
			System.out.println(c);
			return null;
		}
		return this.branchs[c];
	}

	public int compareTo(char c) {
		return 0;
	}

	public boolean equals(char c) {
		return false;
	}

	public char getC() {
		return '\000';
	}

	public int getNature() {
		return 0;
	}

	public byte getStatus() {
		return 0;
	}

	public void setNature(int nature) {
	}

	public void setStatus(int status) {
	}

	public int getSize() {
		return this.branchs.length;
	}

	public String[] getParams() {
		return null;
	}

	public void setParam(String[] param) {
	}

	public WoodInterface getParams(String keyWord) {
		WoodInterface branch = this;
		for (int j = 0; j < keyWord.length(); j++) {
			branch = branch.get(keyWord.charAt(j));
			if (branch == null) {
				return null;
			}
		}
		return branch;
	}

	/**
	 * 得到一个分词对象
	 * 
	 * @param content
	 * @return
	 */
	public GetTrieWords getWord(String content) {
		return new GetTrieWords(this, content);
	}

	/**
	 * 清空树释放内存
	 */
	public void clear() {
		branchs = new WoodInterface[65535];
	}
}