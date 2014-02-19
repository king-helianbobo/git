package org.elasticsearch.app;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.soul.analysis.BasicAnalysis;
import org.soul.analysis.IndexAnalysis;
import org.soul.analysis.KeyWord;
import org.soul.analysis.KeyWordExtraction;
import org.soul.analysis.NlpAnalysis;
import org.soul.domain.Term;
import org.soul.recognition.NatureRecognition;

public class SoulServlet {
	private static SoulSearchClient client = new SoulSearchClient();
	private enum SoulMethod {
		BASE, NLP, KEYWORD, INDEX
	}

	public static String processRequest(String input, String strMethod,
			String strNature) throws IOException {
		SoulMethod method = SoulMethod.BASE;
		if (strMethod != null) {
			method = SoulMethod.valueOf(strMethod.toUpperCase());
		} else {
			method = SoulMethod.BASE;
		}
		Boolean nature = true; // 是否做词性识别
		if (strNature != null && strNature.toLowerCase().equals("false"))
			nature = false;

		List<Term> terms = null;
		String resultStr = null;
		Collection<KeyWord> keyWords = null;
		switch (method) {
		case NLP:
			terms = NlpAnalysis.parse(input);
			break;
		// case NLP : // use as search
		// resultStr = client.simpleQueryStringQuery(input);
		// break;
			case KEYWORD :
				KeyWordExtraction kwc = new KeyWordExtraction(10);
				keyWords = kwc.computeArticleTfidf(input);
				break;
			case INDEX :
				terms = IndexAnalysis.parse(input);
				break;
			default :
				terms = BasicAnalysis.parse(input);
		}
		if (terms != null) {
			return termToString(terms, nature, method);
		}
		if (keyWords != null) {
			return keyWordsToString(keyWords, nature);
		}
		if (resultStr != null) {
			return resultStr;
		}
		return "Error happen!";
	}
	private static String keyWordsToString(Collection<KeyWord> keyWords,
			boolean nature) {
		StringBuilder sb = new StringBuilder();
		for (KeyWord keyword : keyWords) {
			String tmp = keyword.getName();
			if (nature) {
				tmp += "/" + keyword.getScore();
			}
			sb.append(tmp + "\t");
		}
		return sb.toString();
	}

	private static String termToString(List<Term> terms, boolean nature,
			SoulMethod method) {
		if (terms == null) {
			return "Failed to parse input";
		}
		if (nature && method != SoulMethod.NLP) {
			new NatureRecognition(terms).recognition();
		}
		StringBuilder sb = new StringBuilder();
		for (Term term : terms) {
			String tmp = term.getName();
			if (nature) {
				tmp += "/" + term.getNatrue().natureStr;
			}
			sb.append(tmp + "\t");
		}
		return sb.toString();
	}
}
