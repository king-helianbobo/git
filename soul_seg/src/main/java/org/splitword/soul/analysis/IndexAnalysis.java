package org.splitword.soul.analysis;

import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.splitword.soul.domain.Term;
import org.splitword.soul.domain.ViterbiGraph;
import org.splitword.soul.recognition.AsianNameRecognition;
import org.splitword.soul.recognition.NumberRecognition;
import org.splitword.soul.recognition.UserDefineRecognition;
import org.splitword.soul.treeSplit.Forest;
import org.splitword.soul.utility.MyStaticValue;

public class IndexAnalysis extends Analysis {
	private static Log log = LogFactory.getLog(IndexAnalysis.class);
	private Forest[] forests = null;
	public IndexAnalysis(Reader reader) {
		super(reader);
	}

	private IndexAnalysis() {
	};

	@Override
	protected List<Term> getResult(final ViterbiGraph graph) {
		Merger merger = new Merger() {

			@Override
			public List<Term> merge() {
				graph.walkPath();
				if (MyStaticValue.allowNumRecognize)
					NumberRecognition.recognition(graph.terms);

				if (MyStaticValue.allowNameRecognize)
					new AsianNameRecognition(graph.terms).recognition();

				if (forests == null) {
					new UserDefineRecognition(graph.terms).recognition();
				} else {
					for (Forest forest : forests) {
						if (forest == null)
							continue;
						new UserDefineRecognition(graph.terms, forest)
								.recognition();
					}
				}
				log.info(result());
				return result();
			}

			private List<Term> result() {
				List<Term> all = new LinkedList<Term>();
				int length = graph.terms.length - 1;
				for (int i = 0; i < length; i++) {
					String temp = null;
					Term term = graph.terms[i];
					while (term != null) {
						all.add(term);
						temp = term.getName();
						term = term.getNext();
						if (term == null || term.getName().length() == 1
								|| temp.equals(term.getName())) {
							break;
						}
					}
				}
				return all;
			}
		};
		return merger.merge();
	}

	public static List<Term> parse(String str) {
		return new IndexAnalysis().parseStr(str);
	}

}
