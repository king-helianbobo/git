package org.soul.domain;

import org.soul.library.NatureLibrary;

/**
 * 从词性对照表中加载的，与具体的Term无关
 * 
 * @author liubo
 * 
 */
public class NatureInLib {

	public final String natureStr; // 词性的名称
	public final int index; // 在词性对照表的位置
	public final int natureIndex; // 在词性对照表中的下标值
	public final int allFrequency; // 词性出现频率

	public static final NatureInLib NW = NatureLibrary.getNature("nw");
	public static final NatureInLib NRF = NatureLibrary.getNature("nrf");
	public static final NatureInLib NR = NatureLibrary.getNature("nr");
	public static final NatureInLib NULL = NatureLibrary.getNature("null");

	public NatureInLib(String natureStr, int index, int natureIndex,
			int allFrequency) {
		this.natureStr = natureStr;
		this.index = index;
		this.natureIndex = natureIndex;
		this.allFrequency = allFrequency;
	}

	public NatureInLib(String natureStr) {
		this.natureStr = natureStr;
		this.index = 0;
		this.natureIndex = 0;
		this.allFrequency = 0;
	}

	@Override
	public String toString() {
		return natureStr + ":" + index + ":" + natureIndex;
	}
}