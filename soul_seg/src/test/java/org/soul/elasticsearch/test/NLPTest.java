package org.soul.elasticsearch.test;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.splitWord.KeyWord;
import org.soul.splitWord.KeyWordExtraction;
import org.soul.splitWord.LearnTool;
import org.soul.splitWord.NlpAnalysis;
import org.soul.treeSplit.StringUtil;

public class NLPTest {
	private final Log log = LogFactory.getLog(NLPTest.class);
	@Test
	public void newWordDetectionTest1() {
		String content = "产量两万年中将增长两倍";
		LearnTool learn = new LearnTool();
		List<Term> pased = NlpAnalysis.parse(StringUtil.rmHtmlTag(content),
				learn);
		List<Entry<String, Double>> topTree = learn.getTopTree(100);
		log.info(topTree);
		log.info(pased);
		log.info("这次训练已经学到了: " + learn.count + " 个词!");
	}
	@Test
	public void newWordDetectionTest2() throws IOException {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		String content = "冒死记录中国神秘事件（真全本）";
		StringReader reader = new StringReader(content);
		LearnTool learn = new LearnTool();
		long start = System.currentTimeMillis();
		NlpAnalysis nlpAnalysis = new NlpAnalysis(reader, learn);
		Term term = null;
		while ((term = nlpAnalysis.next()) != null) {
			if (!TermNatures.NW.equals(term.getTermNatures())) {
				continue;
			}
			if (hm.containsKey(term.getName())) {
				hm.put(term.getName(), hm.get(term.getName()) + 1);
			} else {
				hm.put(term.getName(), 1);
			}
		}
		log.info(System.currentTimeMillis() - start);
		Set<Entry<String, Integer>> entrySet = hm.entrySet();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : entrySet) {
			sb.append(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		log.info(sb.toString());
	}

	@Test
	public void newWordDetectionTest3() {
		List<String> vl = new ArrayList<String>();
		vl.add("二次元乳量大不一定是王道");
		vl.add("贾瑞听了，魂不附体，只说：“好侄儿，只说没有见我，明日我重重的谢你。"
				+ "贾蔷道：“你若谢我，放你不值什么，只不知你谢我多少？况且口说无凭，写一文契来。"
				+ "贾瑞道：“这如何落纸呢？贾蔷道：“这也不妨，写一个赌钱输了外人帐目，借头家银若干两便罢。”"
				+ "贾瑞道：“这也容易．只是此时无纸笔。”贾蔷道：“这也容易。”说罢翻身出来，纸笔现成，"
				+ "拿来命贾瑞写．他两作好作歹，只写了五十两，然后画了押，贾蔷收起来．"
				+ "然后撕逻贾蓉．贾蓉先咬定牙不依，只说：“明日告诉族中的人评评理。”"
				+ "贾瑞急的至于叩头．贾蔷作好作歹的，也写了一张五十两欠契才罢．贾蔷又道：“如今要放你，"
				+ "我就担着不是．老太太那边的门早已关了，老爷正在厅上看南京的东西，那一条路定难过去，"
				+ "如今只好走后门．若这一走，倘或遇见了人，连我也完了．等我们先去哨探哨探，再来领你．"
				+ "这屋你还藏不得，少时就来堆东西．等我寻个地方。”说毕，拉着贾瑞，仍熄了灯，出至院外，"
				+ "摸着大台矶底下，说道：“这窝儿里好，你只蹲着，别哼一声，等我们来再动。”");
		// 此对象可以公用一个.随着语料的增多可以学习新的词语
		LearnTool learn = new LearnTool();
		// 关闭人名识别
		// learn.isAsianName = true;
		// 关闭机构名识别
		// learn.isCompany = true;
		// 关闭外国人名识别
		// learn.isForeignName = true;
		// 关闭新词发现
		// learn.isNewWord = true;
		for (String string : vl) {
			List<Term> parse = NlpAnalysis.parse(string, learn);
			log.info(parse);
		}
		log.info("这次训练已经学到了: " + learn.count + " 个词!");
		// log.info(System.currentTimeMillis() - start);
		log.info(learn.getTopTree(100, TermNatures.NW));
	}

	@Test
	public void keyWordTest() {
		KeyWordExtraction kwe = new KeyWordExtraction(6);
		String title = "维基解密否认斯诺登接受委内瑞拉庇护";
		String content = "俄罗斯国会议员9号在社交网站推特twitter表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";
		Collection<KeyWord> result = kwe.computeArticleTfidf(title, content);
		log.info(result);
	}

	@Test
	public void organizationTest() {
		List<String> all = new ArrayList<String>();
		String example = "江苏宏宝五金股份有限公司（以下简称“本公司”）于2012年11月9日接到实际控制人"
				+ "江苏宏宝集团有限公司（以下简称“宏宝集团”）通知，"
				+ "宏宝集团将其所持本公司无限售条件流通股份500万股（占公司总股本的2．72％）质押给"
				+ "华夏银行股份有限公司苏州分行，为"
				+ "张家港市宏大钢管有限公司向华夏银行股份有限公司苏州分行"
				+ "申请最高融资额提供担保，股权质押登记日为2012年11月8日，质押期限至2013年11月5日止；同日，"
				+ "宏宝集团"
				+ "将其所持本公司无限售条件流通股份1000万股（占公司总股本的5．43％）质押给"
				+ "江苏张家港农村商业银行股份有限公司，为张家港保税区"
				+ "康龙国际贸易有限公司向"
				+ "江苏张家港农村商业银行股份有限公司申请的流动资金贷款提供担保，股权质押登记日为2012年11月8日，质押期限至2014年11月5日止。上述质押登记手续已在中国证券登记结算有限责任公司深圳分公司办理完毕。";
		all.add(example);
		example = " 新浪体育讯　北京时间4月15日03:00(英国当地时间14日20:00)，2009/10赛季英格兰足球超级联赛第34轮一场焦点战在白鹿巷球场展开角逐，阿森纳客场1比2不敌托特纳姆热刺，丹尼-罗斯和拜尔先入两球，本特纳扳回一城。阿森纳仍落后切尔西6分(净胜球少15个)，夺冠几成泡影。热刺近 7轮联赛取得6胜，继续以1分之差紧逼曼城。";
		all.add(example);
		example = "东华能源2012年第四次临时股东大会于2012年11月9日召开，审议通过了《关于同意投资设立“宁波福基石化有限公司”的议案》、《关于“张家港扬子江石化有限公司”新增40万吨/年聚丙烯项目的议案》、《关于对“宁波福基石化有限公司”授权的议案》、《关于对“张家港扬子江石化有限公司”授权的议案》、《关于提请股东大会延长董事会全权办理非公开发行股票事项授权有效期的议案》。";
		all.add(example);
		all.add("事实上，HTC自诞生以来，多数时候都只是在为谷歌等公司代工生产移动终端。但它从2006年开始培育自己的HTC品牌，并在此后的五年时间里迅速成为仅次于诺基亚的全球第二大手机厂商，占有全球18.22%的智能手机份额，在北美智能手机市场的份额也曾一度达到23%，是全美最大的智能手机供应商。");
		all.add("蓝鼎集团资产总额为79.49亿元，净资产9.00亿元。2010年蓝鼎集团总资产64.21亿元，其中所有者权益2.19亿元。这意味着，2010年和2011年蓝鼎集团的资产负债率分别高达96.6%和88.34%。如此高的资产负债率在A股房地产类上市公司中较为少见。有关数据显示，在135家房地产上市公司中，2011年资产负债率高于88%的仅有3家公司，分别是*ST园城[10.61 -0.09% 股吧 研报](107.7%)、高新发展[6.72 -0.59% 股吧 研报](95.5%)以及鲁商置业[4.18 0.48% 股吧 研报](92%)。");
		all.add("能不能试试这个 西伯利亚雅特大教堂位于俄罗斯东西伯利亚地区");
		all.add("【10000亿——阿里巴巴称淘宝和天猫本年度的总零售额突破 10000亿】 阿里巴巴还公布了其它有趣的数据：2012 年第 3 季度中国第三方互联网支付市场交易规模达到 9764 亿元人民币，支付宝占 46.9%，财付通占 20.4%，银联在线占 11.5%");
		LearnTool learn = new LearnTool();
		for (String string : all) {
			List<Term> parse = NlpAnalysis.parse(string, learn);
			log.info(parse);
		}
	}
}