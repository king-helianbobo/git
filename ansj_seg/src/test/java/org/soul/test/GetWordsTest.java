package org.soul.test;

import org.ansj.splitWord.GetWords;
import org.ansj.splitWord.GetWords;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GetWordsTest {
	private static Log log = LogFactory.getLog(GetWordsTest.class);

	public static void main(String[] args) {
		GetWords gwi = new GetWords();
		gwi.setStr("井冈山：党建信息化服务hello,world新平台：硕士研究生产工厂");
		String temp = null;
		while ((temp = gwi.allWords()) != null) {
			log.info(temp + " " + gwi.getOffe());
		}
	}

}
