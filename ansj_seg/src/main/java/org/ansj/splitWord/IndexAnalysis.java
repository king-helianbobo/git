package org.ansj.splitWord;

import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.ansj.domain.Graph;
import org.ansj.domain.Term;
import org.ansj.recognition.AsianNameRecognition;
import org.ansj.recognition.NumberRecognition;
import org.ansj.recognition.UserDefineRecognition;
import org.ansj.treeSplit.Forest;
import org.ansj.util.StaticVariable;

public class IndexAnalysis extends Analysis {

	private Forest[] forests = null;

	public IndexAnalysis(Reader reader) {
		super(reader);
	}

	private IndexAnalysis() {
	};

	public IndexAnalysis(Forest[] forests) {
		this.forests = forests;
	}

	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {

			@Override
			public List<Term> merge() {
				graph.walkPath();
				if (StaticVariable.allowNumRecognize)
					NumberRecognition.recognition(graph.terms);

				if (StaticVariable.allowNameRecognize)
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

	public static List<Term> parse(String str, Forest... forests) {
		return new IndexAnalysis(forests).parseStr(str);

	}
}
