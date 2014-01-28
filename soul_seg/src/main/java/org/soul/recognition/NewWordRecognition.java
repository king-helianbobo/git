package org.soul.recognition;

import org.soul.domain.NewWord;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.domain.TermUtil;
import org.soul.splitWord.LearnTool;
import org.soul.treeSplit.SmartForest;

/**
 * 新词识别
 */
public class NewWordRecognition {

	private Term[] terms = null;
	private double score;
	private StringBuilder sb = new StringBuilder();
	private SmartForest<NewWord> forest = null;
	private SmartForest<NewWord> branch = null;
	private TermNatures tempNatures;
	private Term from;
	private Term to;
	private int offe; // 偏移量

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
					case 1 :
						sb.append(term.getName());
						continue;
					case 2 :
						sb.append(term.getName());
						score = branch.getParam().getScore();
						tempNatures = branch.getParam().getNature();
						to = term.getTo();
						makeNewTerm();
						continue;
					case 3 :
						sb.append(term.getName());
						score = branch.getParam().getScore();
						tempNatures = branch.getParam().getNature();
						to = term.getTo();
						makeNewTerm();
						flag = false;
						break;
					default :
						System.out.println("怎么能出现0呢?");
						break;
				}
			}
			reset();
		}
	}

	private void makeNewTerm() {
		Term term = new Term(sb.toString(), offe, tempNatures);
		term.selfScore = score;
		term.setNature(tempNatures.termNatures[0].nature);
		TermUtil.termLink(from, term);
		TermUtil.termLink(term, to);
		TermUtil.insertTerm(terms, term);
	}

	private void reset() {
		offe = -1;
		tempNatures = null;
		branch = forest;
		score = 0;
		sb = new StringBuilder();
	}

}
