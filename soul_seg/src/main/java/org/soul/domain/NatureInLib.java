package org.soul.domain;

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