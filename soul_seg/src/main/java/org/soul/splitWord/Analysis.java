package org.soul.splitWord;

import static org.soul.utility.InitDictionary.IN_SYSTEM;
import static org.soul.utility.InitDictionary.TraditionalToSimplified;
import static org.soul.utility.InitDictionary.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Graph;
import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;
import org.soul.treeSplit.GetTrieWords;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.UserDefineLibrary;
import org.soul.utility.WordAlert;

public abstract class Analysis {

	private static Log log = LogFactory.getLog(Analysis.class);
	public int offe;
	private int tempLength;
	private GetWords gwi = new GetWords();
	private BufferedReader br;
	private LinkedList<Term> terms = new LinkedList<Term>();
	private Term term = null;

	public Analysis(Reader reader) {
		br = new BufferedReader(reader);
	}

	protected Analysis() {
	};

	public Term next() throws IOException {

		if (!terms.isEmpty()) {
			term = terms.poll();
			term.updateOffe(offe);
			return term;
		}
		// read next line
		String temp = br.readLine();
		while (StringUtil.isBlank(temp)) {
			if (temp == null) {
				return null;
			} else {
				offe = offe + temp.length() + 1;
				temp = br.readLine();
			}
		}

		offe += tempLength;
		analysisSentence(temp); // analysis string

		if (!terms.isEmpty()) {
			term = terms.poll();
			term.updateOffe(offe); // term's offset in document
			return term;
		}
		return null;
	}

	private void analysisSentence(String sentence) {
		Graph gp = new Graph(sentence);
		int startOffe = 0;
		if (UserDefineLibrary.ambiguityForest != null) {
			// use ambiguity dictionary provided by user
			GetTrieWords getTrieWords = new GetTrieWords(
					UserDefineLibrary.ambiguityForest, sentence);
			String[] params = null;
			while ((getTrieWords.getAllWords()) != null) {
				if (getTrieWords.offe > startOffe) {
					String str = sentence.substring(startOffe,
							getTrieWords.offe);
					analysis(gp, str, startOffe);
				}
				params = getTrieWords.getParams();
				startOffe = getTrieWords.offe;
				for (int i = 0; i < params.length; i += 2) {
					Term term = new Term(params[i], startOffe, new TermNatures(
							new TermNature(params[i + 1], 1))); // 词频按照1来算？
					gp.addTerm(term);
					startOffe += params[i].length();
				}
			}
			if (startOffe != sentence.length()) {
				// log.info(sentence.substring(startOffe, sentence.length()));
				analysis(gp, sentence.substring(startOffe, sentence.length()),
						startOffe);
			}
		} else {
			analysis(gp, sentence, startOffe);
		}
		List<Term> result = this.getResult(gp);
		terms.addAll(result);
	}

	private void analysis(Graph gp, String sentence, int startOffe) {
		int start = 0;
		int end = 0;
		int length = 0;
		length = sentence.length();
		tempLength = length + 1;
		String str = null;
		char c = 0;
		for (int i = 0; i < length; i++) {
			switch (status[TraditionalToSimplified(sentence.charAt(i))]) {
			case 0: // particular symbol
				gp.addTerm(new Term(sentence.charAt(i) + "", startOffe + i,
						TermNatures.NULL));
				break;
			case 4: // consecutive alpha
				start = i;
				end = 1;
				while (++i < length && status[sentence.charAt(i)] == 4) {
					end++;
				}
				str = WordAlert.alertEnglish(sentence, start, end);
				gp.addTerm(new Term(str, start + startOffe, TermNatures.EN));
				// English words use TermNatures.EN
				i--;
				break;
			case 5: // consecutive number
				start = i;
				end = 1;
				while (++i < length && status[sentence.charAt(i)] == 5) {
					end++;
				}
				str = WordAlert.alertNumber(sentence, start, end);
				gp.addTerm(new Term(str, start + startOffe, TermNatures.NB));
				// decimal number use TermNatures.NB
				i--;
				break;
			default:
				start = i;
				end = i;
				c = sentence.charAt(start);
				while (IN_SYSTEM[c] > 0) {
					end++;
					if (++i >= length)
						break;
					c = sentence.charAt(i);
				}
				if (start == end) {
					gp.addTerm(new Term(String.valueOf(c), i + startOffe,
							TermNatures.NULL)); // couldn't determine termNature
				}
				str = sentence.substring(start, end);
				gwi.setStr(str);
				while ((str = gwi.allWords()) != null) {
					gp.addTerm(new Term(str, gwi.offe + start + startOffe, gwi
							.getTermNatures()));
				}
				// 如果未分出词.以未知字符加入到graph中，未知字符不能确定词性
				if (IN_SYSTEM[c] > 0 || status[c] > 3) {
					i -= 1;
				} else {
					gp.addTerm(new Term(String.valueOf(c), i + startOffe,
							TermNatures.NULL));
				}
				break;
			}
		}
	}

	protected List<Term> parseStr(String sentence) {
		analysisSentence(sentence);
		return terms;
	}

	protected abstract List<Term> getResult(Graph graph);

	public abstract class Merger {
		public abstract List<Term> merge();
	}

	public void resetContent(BufferedReader br) {
		this.offe = 0;
		this.tempLength = 0;
		this.br = br;
	}
}
