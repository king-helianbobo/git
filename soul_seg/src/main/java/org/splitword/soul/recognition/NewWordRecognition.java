package org.splitword.soul.recognition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.splitword.soul.domain.NatureInLib;
import org.splitword.soul.domain.NewWord;
import org.splitword.soul.domain.Term;
import org.splitword.soul.treeSplit.SmartForest;
import org.splitword.soul.utility.TermUtil;

public class NewWordRecognition {
	private static Log log = LogFactory.getLog(NewWordRecognition.class);
	private Term[] terms = null;
	private double score;
	private StringBuilder sb = new StringBuilder();
	private SmartForest<NewWord> forest = null;
	private SmartForest<NewWord> branch = null;
	private NatureInLib tempNature;
	private Term from;
	private Term to;
	private int offe;

	public NewWordRecognition(Term[] terms, LearnTool learn) {
		this.terms = terms;
		forest = learn.getForest();
		branch = learn.getForest();
	}

	public void recognition() {
		if (branch == null) {
			return;
		}
		int length = terms.length - 1;
		Term term = null;
		for (int i = 0; i < length; i++) {
			if (terms[i] == null) {
				continue;
			} else {
				from = terms[i].getFrom();
				log.info(from.getName());
				terms[i].score = 0;
				terms[i].selfScore = 0;
			}
			branch = branch.getBranch(terms[i].getName());
			if (branch == null || branch.getStatus() == 3) {
				reset();
				continue;
			}
			offe = i;
			term = terms[i];
			sb.append(term.getName());
			if (branch.getStatus() == 2) {
				term.selfScore = branch.getParam().getScore();
			}
			boolean flag = true;
			while (flag) { // 循环查找
				term = term.getTo();
				branch = branch.getBranch(term.getName());
				if (branch == null) { // 如果没有找到跳出
					break;
				}
				switch (branch.getStatus()) {
				case 1:
					sb.append(term.getName());
					continue;
				case 2:
					sb.append(term.getName());
					score = branch.getParam().getScore();
					tempNature = branch.getParam().getNature();
					to = term.getTo();
					makeNewTerm();
					continue;
				case 3:
					sb.append(term.getName());
					score = branch.getParam().getScore();
					tempNature = branch.getParam().getNature();
					to = term.getTo();
					makeNewTerm();
					flag = false;
					break;
				default:
					break;
				}
			}
			reset();
		}
	}

	private void makeNewTerm() {
		Term term = new Term(sb.toString(), offe, tempNature.natureStr, 1);
		term.selfScore = score;
		term.setNature(tempNature);
		if (sb.length() > 3) {
			log.info("sb.length() > 3 " + sb.toString());
			term.setSubTermList(TermUtil.getSubTermList(from, to));
		}
		TermUtil.termLink(from, term);
		TermUtil.termLink(term, to);
		TermUtil.insertTerm(terms, term);
		TermUtil.parseNewWordNature(term);
	}

	private void reset() {
		offe = -1;
		tempNature = null;
		branch = forest;
		score = 0;
		sb = new StringBuilder();
	}

}
