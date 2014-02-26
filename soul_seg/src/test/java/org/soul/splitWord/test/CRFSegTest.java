package org.soul.splitWord.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.splitword.soul.library.InitDictionary;

public class CRFSegTest {
	private final Log log = LogFactory.getLog(CRFSegTest.class);
	String[] texts = {
			"秋后的蚂蚱长不了",
			"最美丽     乡村  	教师",
			"百家讲坛开播了",
			"曹洪手下的宾客在满宠辖界内多次犯法，被满宠收捕治罪。曹洪为此写信给满宠，满宠不加理会。尚书令荀彧、少府孔融等人都嘱咐满宠说：“只应讯问，不要滥加拷打。”满宠对此毫无回应，仍按照常法拷问。",
			"满宠深受曹操的器重，并因屡建功勋而被赏赐封爵。曾以关内侯的身份，两次任南阳太守，所在又政绩斐然。",
			"关羽以水急攻襄阳，在此危急时刻，满宠认为必须先稳住军心，然后分析了关羽军队的弱点，明确提出了退敌的奇计妙策。",
			"满宠不置产业，家中没有多余的财物，皇帝下诏说：“你在外领兵作战，专一操心公事，有行父、祭遵的风范。”",
			"满宠字伯宁,山阳昌邑人也,魏国名将，最初在曹操手下任许县县令。", "掌管司法，以执法严格著称.",
			"转任汝南太守，开始参与军事，曾参与赤壁之战。", "后关羽围攻樊城，满宠协助曹仁守城，劝阻了弃城而逃的计划，成功坚持到援军到来。",
			"曹丕在位期间，满宠驻扎在新野，负责荆州侧的对吴作战。", "曹睿在位期间，满宠转任到扬州，接替曹休负责东线对吴作战，屡有功劳",
			"后因年迈调回中央担任太尉，数年后病逝。"};

	@Test
	public void crfModelSegTest1() {
		StringBuilder sb = new StringBuilder();
		sb.append("012");
		log.info(sb.length());
		sb.append("345");
		log.info(sb.length());
		for (String str : texts) {
			log.info(InitDictionary.getCRFSplitWord().cut(str));
		}
	}
}
