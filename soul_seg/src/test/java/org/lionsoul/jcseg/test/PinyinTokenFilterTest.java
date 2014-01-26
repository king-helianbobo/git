package org.lionsoul.jcseg.test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.soul.elasticSearch.plugin.SoulPinyinAnalyzer;
import org.soul.elasticSearch.plugin.PinyinTokenFilter;
import org.soul.elasticSearch.plugin.SoulEdgeNGramTokenFilter;
import org.soul.elasticSearch.plugin.SoulIndexAnalyzer;
import org.testng.annotations.*;

public class PinyinTokenFilterTest {

	private Log log = LogFactory.getLog(PinyinTokenFilterTest.class);

	String[] titles = {"台湾最美胸部再次自拍　		深邃乳沟", "哆啦Ａ梦玩ＣＯＳ穿上“马甲”爆笑造型", "范冰冰",
			"公务员面试名单：４１９江西省气象局", "图文：王北星获速滑１０００米冠军　一脸笑容", "服装：花样长毛衣搭配出新感觉",
			"《长江七号》英文主题曲曝光　温暖而怀旧（图）", "郎朗出任万宝龙文化基金会主席", "超级病菌导致全球两栖动物面临灭绝（组图）",
			"服装：明星支招教你如何穿皮衣", "微软并购雅虎传闻愈演愈烈　杨致远可能走人",
			"南粤风采２６选５电脑福利彩票第８７７期开奖公告", "奥运网络安全应急小组成立", "广西受灾人数升至千万",
			"泰国热播电视剧惹怒空乘人员", "《龙珠》墨西哥热拍　周润发抱怨饮食太差（图）", "中航地产大股东拟１２．４亿增持近２８％股权",
			"北京奥运临近冲突连连　手球联合会表态重罚亚洲"};

	TokenStream result = null;

	@Test
	public void TestPinyinAnalyzer() {
		Analyzer analyzer = new SoulPinyinAnalyzer();
		for (String title : titles) {
			analyze(analyzer, title);
		}
	}

	void analyze(Analyzer analyzer, String text) {
		try {
			// log.info("分词器:" + analyzer.getClass());
			// log.info(Strings.toUnderscoreCase("providerString"));
			// log.info(Strings.toCamelCase("_provider_string"));
			TokenStream tokenStream = analyzer.tokenStream("content",
					new StringReader(text));
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				CharTermAttribute charAttribute = tokenStream
						.getAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAttribute = tokenStream
						.getAttribute(OffsetAttribute.class);
				log.info("[" + offsetAttribute.startOffset() + ","
						+ offsetAttribute.endOffset() + ","
						+ charAttribute.toString() + "]");
			}
			tokenStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
