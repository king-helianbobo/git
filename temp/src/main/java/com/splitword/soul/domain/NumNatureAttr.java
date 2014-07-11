package com.splitword.soul.domain;

public class NumNatureAttr {

	public static final NumNatureAttr NULL = new NumNatureAttr();
	// 是有可能是一个数字
	public int numFreq = -1; // 作为数词的频率
	// 数字的结尾
	public int numEndFreq = -1; // 作为量词的频率
	// 最大词性是否是数字
	public boolean flag = false; // 是否最有可能是个数词

	public NumNatureAttr() {
	}
}
