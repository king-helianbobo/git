package org.soul.splitWord;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Graph;
import org.soul.domain.Term;
import org.soul.recognition.AsianNameRecognition;
import org.soul.recognition.ForeignNameRecognition;
import org.soul.recognition.NumberRecognition;
import org.soul.recognition.UserDefineRecognition;
import org.soul.treeSplit.Forest;
import org.soul.utility.StaticVariable;
import org.soul.utility.UserDefineLibrary;

public class BasicAnalysis extends Analysis {

	private static Log log = LogFactory.getLog(BasicAnalysis.class);
	private Forest[] forests = null;

	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merge() {
				// 先进行人名识别，然后再数字识别，因为某些姓名中含有数字（例如三本五十六）
				graph.walkPath();// construct optimal path
				if (graph.hasPerson && StaticVariable.allowNameRecognize) {
					new AsianNameRecognition(graph.terms).recognition();
					graph.walkPathByScore();
					AsianNameRecognition.nameAmbiguity(graph.terms);
					new ForeignNameRecognition(graph.terms).recognition();
					graph.walkPathByScore();
				}
				if (graph.hasNum) { // recognize consecutive numbers
					NumberRecognition.recognition(graph.terms);
				}
				if (forests == null) {
					userDefineRecognize(graph, null);
				} else {
					for (Forest forest : forests) {
						if (forest == null)
							continue;
						// 识别自定义词，对于部分自定义词(如拳皇ova)，放在ambiguityLibrary里无效
						// 因为对[漂亮mm打拳皇ova很厉害]这句话，由于[打拳]使得[拳皇ova]分不出来
						userDefineRecognize(graph, forest);
					}
				}
				log.info(getResult());
				return getResult();
			}

			private void userDefineRecognize(final Graph graph, Forest forest) {
				new UserDefineRecognition(graph.terms, forest).recognition();
				graph.rmLittlePath();
				graph.walkPathByScore();
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

	public BasicAnalysis() {
	};

	public BasicAnalysis(Forest[] forests) {
		if (forests == null) {
			forests = new Forest[]{UserDefineLibrary.userDefineForest};
		}
		this.forests = forests;
	}

	public BasicAnalysis(Reader reader) {
		super(reader);
	}

	public BasicAnalysis(Reader reader, Forest[] forests) {
		super(reader);
		if (forests == null) {
			forests = new Forest[]{UserDefineLibrary.userDefineForest};
		}
		this.forests = forests;
	}

	public static List<Term> parse(String str) {
		return new BasicAnalysis().parseStr(str);
	}

}
