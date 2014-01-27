package org.soul.recognition;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.domain.TermUtil;
import org.soul.utility.InitDictionary;
import org.soul.utility.StaticVarForSegment;

public class NumberRecognition {
	private static Log log = LogFactory.getLog(NumberRecognition.class);
	private static String tmpStr = "百分之,仨,伍,第,七百,双百,十之八九,百分,七五,千千万万,上万,零,九五,双,亿,十几,七,九,百,六,千,八,十,首,五,万,二,四,几,三,两,一,第一,第二,第三,第四,第五,第六,第七,第八,第九,半,伍,俩,七百,双百,百分,仟,佰,卅,叁,壹,廿,捌,柒,玖,贰,,丙丑,丁亥,丙亥,乙丑,万代,乙亥,丁午,丙午,丁卯,丙卯,乙午,乙卯,壬丑,壬亥,丙子,丁寅,丙寅,乙子,乙寅,壬午,壬卯,丁巳,丙巳,庚丑,乙巳,庚亥,丁戌,丙戌,乙戌,庚午,庚卯,壬子,壬寅,丁未,丙未,壬巳,乙未,壬戌,庚子,庚寅,庚巳,壬未,庚戌,戊丑,戊亥,丁申,丙申,乙申,庚未,戊午,戊卯,八成,壬申,戊子,戊巳,庚申,戊戌,己丑,己亥,戊未,己午,己卯,丁辰,丙辰,乙辰,丁酉,丙酉,乙酉,己子,己寅,壬辰,戊申,己巳,壬酉,己戌,癸丑,癸亥,庚辰,庚酉,己未,癸午,癸卯,半世,癸子,癸寅,癸巳,己申,癸戌,戊辰,戊酉,癸未,分之,五十八,癸申,己辰,己酉,癸辰,癸酉,辛丑,辛亥,辛午,辛卯,甲丑,辛子,辛寅,甲亥,辛巳,挂零,甲午,甲卯,辛戌,辛未,甲子,甲寅,甲巳,甲戌,甲未,辛申,甲申,辛辰,辛酉,甲辰,甲酉";

	/**
	 * 合并连续数词,合并数词和量词，比如将[三/年]合并为[三年] 但目前尚未实现将[3/年]合并成[3年]
	 * 
	 * @author LiuBo
	 * @since 2014年1月14日
	 * @param terms
	 *            void
	 */
	public static void recognition(Term[] terms) {
		Map<String, Integer> tree = new HashMap<String, Integer>();
		String arrays[] = tmpStr.split(",");
		for (int i = 0; i < arrays.length; i++)
			tree.put(arrays[i].trim(), 1);
		int length = terms.length - 1;
		for (int i = 0; i < length; i++) {
			if (terms[i] == null) {
				continue;
			} else if (".".equals(terms[i].getName())
					|| "．".equals(terms[i].getName())) {
				// 对符号'.'，如果它前后都是数字，则可能是小数
				Term to = terms[i].getTo();
				Term from = terms[i].getFrom();
				if (from.getTermNatures().numNature.flag
						&& to.getTermNatures().numNature.flag
						&& (tree.get(from.getName()) != null)
						&& (tree.get(to.getName()) != null)) {
					from.setName(from.getName() + "." + to.getName());
					tree.put(from.getName(), 1);
					TermUtil.termLink(from, to.getTo());
					terms[to.getOffe()] = null;
					terms[i] = null;
					i = from.getOffe() - 1;
				}
				continue;
			} else if (!terms[i].getTermNatures().numNature.flag) {
				continue;
			} else if (terms[i].getTermNatures().numNature.flag
					&& (tree.get(terms[i].getName()) == null)) {
				continue;
			} else {
				Term temp = terms[i];
				// 合并数字，比如“万”也是个数字，两万年
				while ((temp = temp.getTo()).getTermNatures().numNature.flag
						&& (tree.get(temp.getName()) != null)) {
					terms[i].setName(terms[i].getName() + temp.getName());
				}
				tree.put(terms[i].getName(), 1);
				// 如果是量词，如‘年’，‘把’，‘倍’
				if (StaticVarForSegment.allowQuantifierRecognize
						&& temp.getTermNatures().numNature.numEndFreq > 0) {
					terms[i].setName(terms[i].getName() + temp.getName());
					temp = temp.getTo();
				}
				// 重新设置term的词性
				if (InitDictionary.isInSystemDic(terms[i].getName())) {
					int id = InitDictionary.getWordId(terms[i].getName());
					TermNatures termNatures = InitDictionary.termNatures[id];
					terms[i].setTermNatures(termNatures);
				}
				if (terms[i].getTo() != temp) {
					TermUtil.termLink(terms[i], temp);
					for (int j = i + 1; j < temp.getOffe(); j++)
						terms[j] = null;
					i = temp.getOffe() - 1;
				}
			}
		}
	}
}
