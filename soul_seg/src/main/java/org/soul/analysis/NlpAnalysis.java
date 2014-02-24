package org.soul.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lionsoul.jcseg.pinyin.ChineseHelper;
import org.soul.crf.SplitWord;
import org.soul.domain.NewWord;
import org.soul.domain.TermNatures;
import org.soul.domain.ViterbiGraph;
import org.soul.domain.Term;
import org.soul.library.InitDictionary;
import org.soul.library.NatureLibrary;
import org.soul.recognition.AsianNameRecognition;
import org.soul.recognition.LearnTool;
import org.soul.recognition.NatureRecognition;
import org.soul.recognition.NewWordRecognition;
import org.soul.recognition.NumberRecognition;
import org.soul.recognition.UserDefineRecognition;
import org.soul.utility.TermUtil;
import org.soul.utility.WordAlter;

/**
 * Native language processing
 */
public class NlpAnalysis extends Analysis {
	private static Log log = LogFactory.getLog(NlpAnalysis.class);
	private LearnTool learn = null;
	// private static final SplitWord crfSplitModel = InitDictionary
	// .getCRFSplitWord();

	private static final SplitWord crfSplitModel = null;

	public NlpAnalysis(Reader reader, LearnTool learn) {
		super(reader);
		this.learn = learn;
	}

	private NlpAnalysis(LearnTool learn) {
		this.learn = learn;
	}

	public static List<Term> parse(String str, LearnTool learn) {
		return new NlpAnalysis(learn).parseStr(str);
	}

	public static List<Term> parse(String str) {
		return new NlpAnalysis(new LearnTool()).parseStr(str);
	}

	private void checkTerm(int startOffe, String word, ViterbiGraph graph) {
		if ((startOffe >= 0) && (startOffe < graph.terms.length - 3)) {
			Term term1 = graph.terms[startOffe];
			Term term2 = graph.terms[startOffe + 1];
			Term term3 = graph.terms[startOffe + 3];
			if ((term1 != null) && (term2 != null) && (term3 != null)
					&& (term1.getName().length() == 1)
					&& (term2.getName().length() == 2)
					&& (term3.getName().length() == 1)) {
				if (ChineseHelper.allChineseChar(term1.getName())
						&& ChineseHelper.allChineseChar(term2.getName())
						&& ChineseHelper.allChineseChar(term3.getName())) {
					StringBuilder builder = new StringBuilder();
					builder.append(graph.convertedStr.charAt(startOffe + 2));
					builder.append(graph.convertedStr.charAt(startOffe + 3));
					Term tmpTerm1 = new Term(word, startOffe, TermNatures.NW);
					Term tmpTerm2 = new Term(builder.toString(), startOffe + 2,
							TermNatures.NW);
					TermUtil.insertTerm(graph.terms, tmpTerm1, tmpTerm2);
				} else
					return;
			}
		}
		if ((startOffe >= 2) && (startOffe < graph.terms.length - 1)) {
			Term term1 = graph.terms[startOffe - 2];
			Term term2 = graph.terms[startOffe - 1];
			Term term3 = graph.terms[startOffe + 1];
			if ((term1 != null) && (term2 != null) && (term3 != null)
					&& (term1.getName().length() == 1)
					&& (term2.getName().length() == 2)
					&& (term3.getName().length() == 1)) {
				if (ChineseHelper.allChineseChar(term1.getName())
						&& ChineseHelper.allChineseChar(term2.getName())
						&& ChineseHelper.allChineseChar(term3.getName())) {
					StringBuilder builder = new StringBuilder();
					builder.append(graph.convertedStr.charAt(startOffe - 2));
					builder.append(graph.convertedStr.charAt(startOffe - 1));
					Term tmpTerm1 = new Term(builder.toString(), startOffe - 2,
							TermNatures.NW);
					Term tmpTerm2 = new Term(word, startOffe, TermNatures.NW);
					TermUtil.insertTerm(graph.terms, tmpTerm1, tmpTerm2);
				} else
					return;
			}
		}
	}
	@Override
	protected List<Term> getResult(final ViterbiGraph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merge() {
				graph.walkPath(); // get optimal path
				if (graph.hasNum)
					NumberRecognition.recognition(graph.terms);
				List<Term> result = getResult();
				new NatureRecognition(result).recognition(); // 词性标注
				if (learn == null) {
					learn = new LearnTool();
				}
				learn.learn(graph);
				if (crfSplitModel != null) {
					List<String> words = crfSplitModel.cut(graph.convertedStr);
					int startOffe = 0;
					int tmpLength = 0;
					for (String word : words) {
						startOffe += tmpLength;
						tmpLength = word.length();
						if (word.length() < 2
								|| InitDictionary.isInSystemDic(word)
								|| WordAlter.isRuleWord(word)) {
							continue;
						} else if (word.length() == 2) {
							checkTerm(startOffe, word, graph);
							continue;
						}
						learn.addTerm(new NewWord(word, NatureLibrary
								.getNature("nw"), -word.length()));
						log.info("crf , word = " + word);
					}
				}
				// 用户自定义词典
				new UserDefineRecognition(graph.terms).recognition();
				graph.walkPathByScore();

				new NewWordRecognition(graph.terms, learn).recognition();
				graph.walkPathByScore();

				AsianNameRecognition.nameAmbiguity(graph.terms); // 修复人名左右连接
				log.info(getResult());
				return getResult();
			}

			private List<Term> getResult() {
				List<Term> result = new ArrayList<Term>();
				int length = graph.terms.length - 1;
				for (int i = 0; i < length; i++) {
					if (graph.terms[i] != null) {
						result.add(graph.terms[i]);
					}
				}
				return result;
			}
		};
		return merger.merge();
	}
}
