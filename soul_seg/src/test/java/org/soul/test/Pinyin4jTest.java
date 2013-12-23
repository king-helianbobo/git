package org.soul.test;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pinyin4jTest {
	public static String getPinYin(String src) {
		StringBuilder pinyinBuf = new StringBuilder();
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

		try {
			for (int i = 0; i < src.length(); i++) {
				String[] pinYins = PinyinHelper.toHanyuPinyinStringArray(
						src.charAt(i), outputFormat);

				if (pinYins != null && pinYins.length > 0) {// 汉语
					for (int j = 0; j < pinYins.length; j++)
						pinyinBuf.append(pinYins[j] + " ");
					// pinyinBuf.append(pinYins[0] + " ");
				} else {// 非汉语
					pinyinBuf.append(src.charAt(i));
				}
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return pinyinBuf.toString();
	}

	public static void main(String[] args) {
		System.out.println(getPinYin("Hello,欢迎来到长春,厦门,红色中国"));
	}
}
