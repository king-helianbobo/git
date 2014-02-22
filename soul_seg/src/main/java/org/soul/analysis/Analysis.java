package org.soul.analysis;

import static org.soul.library.InitDictionary.IN_SYSTEM;
//import static org.soul.library.InitDictionary.TraditionalToSimplified;
import static org.soul.library.InitDictionary.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.GetWords;
import org.soul.domain.ViterbiGraph;
import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;
import org.soul.library.UserDefineLibrary;
import org.soul.treeSplit.Forest;
import org.soul.treeSplit.GetTrieWords;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.WordAlter;

public abstract class Analysis {

	private static Log log = LogFactory.getLog(Analysis.class);
	public int offe;
	private int tempLength;
	private GetWords gwi = new GetWords();
	private BufferedReader br;
	private LinkedList<Term> terms = new LinkedList<Term>();
	private Term term = null;
	protected Forest[] forests = null;

	public Analysis(Reader reader) {
		br = new BufferedReader(reader);
		resetContent(br);
	}

	protected Analysis() {
	};

	public Term next() throws IOException {
		if (!terms.isEmpty()) {
			term = terms.poll();
			term.updateOffe(offe);
			return term;
		}
		String temp = br.readLine(); // read next line
		while (StringUtil.isBlank(temp)) {
			if (temp == null) {
				return null;
			} else {
				offe = offe + temp.length() + 1;
				// 为何加1？
				temp = br.readLine();
			}
		}
		offe += tempLength;
		analysisSentence(temp); // analysis string
		tempLength = temp.length();
		if (!terms.isEmpty()) {
			term = terms.poll();
			term.updateOffe(offe); // term's offset in document
			return term;
		}
		return null;
	}

	private void analysisSentence(String tmpStr) {
		ViterbiGraph gp = new ViterbiGraph(tmpStr);
		int startOffe = 0;
		if (UserDefineLibrary.ambiguityForest != null) {
			// use ambiguity dictionary provided by user
			GetTrieWords getTrieWords = new GetTrieWords(
					UserDefineLibrary.ambiguityForest, gp.convertedStr);
			String[] params = null;
			while ((getTrieWords.getOneWord()) != null) {
				if (getTrieWords.offe > startOffe) {
					_analysis(gp, startOffe, getTrieWords.offe);
				}
				params = getTrieWords.getParams();
				startOffe = getTrieWords.offe;
				for (int i = 0; i < params.length; i += 2) {
					Term term = new Term(params[i], startOffe, new TermNatures(
							new TermNature(params[i + 1], 1)));
					// 词频按照1来算，词频意义不大，歧义词的优先权最高
					gp.addTerm(term);
					startOffe += params[i].length();
				}
			}
			int length = gp.convertedStr.length();
			if (startOffe < length - 1) {
				// log.info(sentence.substring(startOffe, sentence.length()));
				_analysis(gp, startOffe, gp.convertedStr.length());
			}
		} else {
			_analysis(gp, startOffe, gp.convertedStr.length());
		}
		List<Term> result = this.getResult(gp);
		terms.addAll(result);
	}

	private void _analysis(ViterbiGraph gp, int startOffe, int endOffe) {
		int start = 0;
		int end = 0;
		String sentence = gp.convertedStr;
		String str = null;
		char c = 0;
		for (int i = startOffe; i < endOffe; i++) {
			switch (status[sentence.charAt(i)]) {
			case 0: // particular symbol
				gp.addTerm(new Term(sentence.charAt(i) + "", i,
						TermNatures.NULL));
				break;
			case 4: // consecutive alpha
				start = i;
				end = 1;
				while (++i < endOffe && status[sentence.charAt(i)] == 4) {
					end++;
				}
				str = WordAlter.alterAlpha(sentence, start, end);
				gp.addTerm(new Term(str, start, TermNatures.EN));
				// English words use TermNatures.EN
				i--;
				break;
			case 5: // consecutive number
				start = i;
				end = 1;
				while (++i < endOffe && status[sentence.charAt(i)] == 5) {
					end++;
				}
				str = WordAlter.alterNumber(sentence, start, end);
				gp.addTerm(new Term(str, start, TermNatures.M));
				i--;
				break;
			default: // 普通汉字
				start = i;
				end = i;
				c = sentence.charAt(start);
				while (IN_SYSTEM[c] > 0) {
					end++;
					if (++i >= endOffe)
						break;
					c = sentence.charAt(i);
				}
				if (start == end) {
					gp.addTerm(new Term(String.valueOf(c), i, TermNatures.NULL));
					continue;
					// couldn't determine termNature
				} else {
					str = sentence.substring(start, end);
					gwi.setStr(str);
					while ((str = gwi.fetchOneWord()) != null) {
						gp.addTerm(new Term(str, gwi.offe + start, gwi
								.getTermNatures()));
					}
				}
				if (IN_SYSTEM[c] > 0 || status[c] > 3) {
					i -= 1;
				} else { // 以未知字符加入到graph中，未知字符不能确定词性
					gp.addTerm(new Term(String.valueOf(c), i, TermNatures.NULL));
				}
				break;
			}
		}
	}

	protected List<Term> parseStr(String sentence) {
		analysisSentence(sentence);
		return terms;
	}

	protected abstract List<Term> getResult(ViterbiGraph graph);

	public abstract class Merger {
		public abstract List<Term> merge();
	}

	public void resetContent(BufferedReader br) {
		this.offe = 0;
		this.tempLength = 0;
		this.br = br;
	}
}
