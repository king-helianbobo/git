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
import org.soul.util.StaticVariable;
import org.soul.util.UserDefineLibrary;

public class BasicAnalysis extends Analysis {

	private static Log log = LogFactory.getLog(BasicAnalysis.class);
	private Forest[] forests = null;

	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merge() {
				graph.walkPath();// construct optimal path
				if (graph.hasPerson && StaticVariable.allowNameRecognize) {
					new AsianNameRecognition(graph.terms).recognition();
					graph.walkPathByScore();
					AsianNameRecognition.nameAmbiguity(graph.terms);
					new ForeignNameRecognition(graph.terms).recognition();
					graph.walkPathByScore();
				}
				log.info(getResult());
				if (graph.hasNum) { // recognize consecutive number
					NumberRecognition.recognition(graph.terms);
				}

				if (forests == null) {
					userDefineRecognize(graph, null);
				} else {
					for (Forest forest : forests) {
						if (forest == null)
							continue;
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

	private BasicAnalysis() {
	};

	public BasicAnalysis(Forest[] forests) {
		if (forests == null) {
			forests = new Forest[] { UserDefineLibrary.FOREST };
		}
		this.forests = forests;
	}

	public BasicAnalysis(Reader reader) {
		super(reader);
	}

	public BasicAnalysis(Reader reader, Forest[] forests) {
		super(reader);
		if (forests == null) {
			forests = new Forest[] { UserDefineLibrary.FOREST };
		}
		this.forests = forests;
	}

	public static List<Term> parse(String str) {
		return new BasicAnalysis().parseStr(str);
	}

}
