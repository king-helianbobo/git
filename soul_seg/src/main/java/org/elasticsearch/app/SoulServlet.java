package org.elasticsearch.app;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.soul.domain.Term;
import org.soul.recognition.NatureRecognition;
import org.soul.splitWord.BasicAnalysis;
import org.soul.splitWord.IndexAnalysis;
import org.soul.splitWord.KeyWord;
import org.soul.splitWord.KeyWordExtraction;
import org.soul.splitWord.NlpAnalysis;

public class SoulServlet {

	private enum SoulMethod {
		BASE, NLP, KEYWORD, INDEX, MIN_NLP
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
		if (strNature != null && strNature.toLowerCase().equals("false")) {
			nature = false;
		}
		List<Term> terms = null;
		Collection<KeyWord> keyWords = null;
		switch (method) {
		case NLP:
			terms = NlpAnalysis.parse(input);
			break;
		case MIN_NLP:
			terms = NlpAnalysis.parse(input);
		case KEYWORD:
			KeyWordExtraction kwc = new KeyWordExtraction(10);
			keyWords = kwc.computeArticleTfidf(input);
			break;
		case INDEX:
			terms = IndexAnalysis.parse(input);
			break;
		default:
			terms = BasicAnalysis.parse(input);
		}
		if (terms != null) {
			return termToString(terms, nature, method);
		}
		if (keyWords != null) {
			return keyWordsToString(keyWords, nature);
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
		if (nature && method != SoulMethod.NLP && method != SoulMethod.MIN_NLP) {
			new NatureRecognition(terms).recognition();
		}
		StringBuilder sb = new StringBuilder();
		for (Term term : terms) {
			String tmp = null;
			if (method == SoulMethod.MIN_NLP && term.getSubTerm() != null) {
				tmp = term.getSubTerm().toString();
			} else {
				tmp = term.getName();
			}
			if (nature) {
				tmp += "/" + term.getNatrue().natureStr;
			}
			sb.append(tmp + "\t");
		}
		return sb.toString();
	}
}
