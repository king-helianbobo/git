package org.lionsoul.jcseg.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

public class DataOperationTest {
	private static final Log log = LogFactory.getLog(DataOperationTest.class);
	@Ignore("Convert Sogou Txt data to hdfs friendly format")
	@Test
	public void convertDataToHdfsFormat() {
		SogouDataReader reader = new SogouDataReader("/mnt/f/Sogou-mini/");
		try {
			reader.convertToHdfsFormat("/mnt/f/hdfs3/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void dataTest1() {
		final char[] CHARCOVER = new char[65536];
		CHARCOVER['-'] = '·';
		CHARCOVER['•'] = '·';
		CHARCOVER[','] = '。';
		CHARCOVER['，'] = '。';
		CHARCOVER['！'] = '。';
		CHARCOVER['!'] = '。';
		CHARCOVER['？'] = '。';
		CHARCOVER['?'] = '。';
		CHARCOVER['；'] = '。';
		CHARCOVER['`'] = '。';
		CHARCOVER['﹑'] = '。';
		CHARCOVER['^'] = '。';
		CHARCOVER['…'] = '。';
		CHARCOVER['“'] = '"';
		CHARCOVER['”'] = '"';
		CHARCOVER['〝'] = '"';
		CHARCOVER['〞'] = '"';
		CHARCOVER['~'] = '"';
		CHARCOVER['\\'] = '。';
		CHARCOVER['∕'] = '。';
		CHARCOVER['|'] = '。';
		CHARCOVER['¦'] = '。';
		CHARCOVER['‖'] = '。';
		CHARCOVER['—'] = '。';
		CHARCOVER['('] = '《';
		CHARCOVER[')'] = '》';
		CHARCOVER['〈'] = '《';
		CHARCOVER['〉'] = '》';
		CHARCOVER['﹞'] = '》';
		CHARCOVER['﹝'] = '《';
		CHARCOVER['「'] = '《';
		CHARCOVER['」'] = '》';
		CHARCOVER['‹'] = '《';
		CHARCOVER['›'] = '》';
		CHARCOVER['〖'] = '《';
		CHARCOVER['〗'] = '"';
		CHARCOVER['】'] = '》';
		CHARCOVER['【'] = '《';
		CHARCOVER['»'] = '》';
		CHARCOVER['«'] = '《';
		CHARCOVER['』'] = '》';
		CHARCOVER['『'] = '《';
		CHARCOVER['〕'] = '》';
		CHARCOVER['〔'] = '《';
		CHARCOVER['}'] = '》';
		CHARCOVER['{'] = '《';
		CHARCOVER[']'] = '》';
		CHARCOVER['['] = '《';
		CHARCOVER['﹐'] = '。';
		CHARCOVER['¸'] = '。';
		CHARCOVER['︰'] = '﹕';
		CHARCOVER['﹔'] = '。';
		CHARCOVER[';'] = '。';
		CHARCOVER['！'] = '。';
		CHARCOVER['¡'] = '。';
		CHARCOVER['？'] = '。';
		CHARCOVER['¿'] = '。';
		CHARCOVER['﹖'] = '。';
		CHARCOVER['﹌'] = '。';
		CHARCOVER['﹏'] = '。';
		CHARCOVER['﹋'] = '。';
		CHARCOVER['＇'] = '。';
		CHARCOVER['´'] = '。';
		CHARCOVER['ˊ'] = '。';
		CHARCOVER['ˋ'] = '。';
		CHARCOVER['-'] = '。';
		CHARCOVER['―'] = '。';
		CHARCOVER['﹫'] = '@';
		CHARCOVER['︳'] = '。';
		CHARCOVER['︴'] = '。';
		CHARCOVER['﹢'] = '+';
		CHARCOVER['﹦'] = '=';
		CHARCOVER['﹤'] = '《';
		CHARCOVER['<'] = '《';
		CHARCOVER['˜'] = '。';
		CHARCOVER['~'] = '。';
		CHARCOVER['﹟'] = '。';
		CHARCOVER['#'] = '。';
		CHARCOVER['﹩'] = '$';
		CHARCOVER['﹠'] = '。';
		CHARCOVER['&'] = '。';
		CHARCOVER['﹪'] = '%';
		CHARCOVER['﹡'] = '。';
		CHARCOVER['*'] = '。';
		CHARCOVER['﹨'] = '。';
		CHARCOVER['\\'] = '。';
		CHARCOVER['﹍'] = '。';
		CHARCOVER['﹉'] = '。';
		CHARCOVER['﹎'] = '。';
		CHARCOVER['﹊'] = '。';
		CHARCOVER['ˇ'] = '。';
		CHARCOVER['︵'] = '《';
		CHARCOVER['︶'] = '》';
		CHARCOVER['︷'] = '《';
		CHARCOVER['︸'] = '》';
		CHARCOVER['︹'] = '《';
		CHARCOVER['︿'] = '《';
		CHARCOVER['﹀'] = '》';
		CHARCOVER['︺'] = '》';
		CHARCOVER['︽'] = '《';
		CHARCOVER['︾'] = '》';
		CHARCOVER['_'] = '。';
		CHARCOVER['ˉ'] = '。';
		CHARCOVER['﹁'] = '《';
		CHARCOVER['﹂'] = '》';
		CHARCOVER['﹃'] = '《';
		CHARCOVER['﹄'] = '》';
		CHARCOVER['︻'] = '《';
		CHARCOVER['︼'] = '》';
		CHARCOVER['/'] = '。';
		CHARCOVER['（'] = '《';
		CHARCOVER['>'] = '》';
		CHARCOVER['）'] = '》';
		CHARCOVER['<'] = '《';
		HashMap<Character, List<Character>> map = new HashMap<Character, List<Character>>();
		for (int i = 0; i < CHARCOVER.length; i++) {
			if (CHARCOVER[i] > 0) {
				log.info((char) i);
				char c = CHARCOVER[i];
				List<Character> list = map.get(i);
				if (list == null) {
					list = new LinkedList<Character>();
				}
				list.add(c);
				map.put((char) i, list);
			}
		}
		Set<Entry<Character, List<Character>>> entrySet = map.entrySet();
		for (Entry<Character, List<Character>> entry : entrySet) {
			StringBuilder str = new StringBuilder();
			str.append("CHARCOVER['" + entry.getKey() + "']=");
			List<Character> list = entry.getValue();
			for (int i = 0; i < list.size(); i++) {
				str.append("'" + list.get(i) + "';");
			}
			log.info(str.toString());
		}
	}
}
