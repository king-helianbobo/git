package org.soul.recognition;

import java.util.ArrayList;
import java.util.List;

import org.soul.domain.CompanyNature;
import org.soul.domain.NewWord;
import org.soul.domain.Term;
import org.soul.domain.TermNatures;
import org.soul.domain.TermUtil;
import org.soul.utility.CompanyAttrLib;

public class CompanyRecogntion {

	private static final double[] FACTORY = CompanyAttrLib.loadFactory();
	private Term[] terms;

	public CompanyRecogntion(Term[] terms) {
		this.terms = terms;
	}

	private Term tempTerm = null;
	private Term maxTerm = null;
	private int offe;
	private Term beginTerm;

	public List<NewWord> getNewWords() {
		List<NewWord> all = new ArrayList<NewWord>();
		Term term = null;
		for (int i = 0; i < terms.length; i++) {
			term = terms[i];
			if (term == null) {
				continue;
			}
			term.selfScore = 0;
			term.score = 0;
			if (term.getTermNatures().companyNature.bb < -0.005
					&& term.getTermNatures().companyNature.b > 1000) {
				double tempScore = term.getTermNatures().companyNature.bb;
				offe = term.getOffe();
				tempTerm = null;
				beginTerm = term.getFrom();
				recogntion_(term, tempScore);
				if (maxTerm != null) {
					// notice:new generated object
					all.add(new NewWord(maxTerm.getName(), TermNatures.NT,
							maxTerm.selfScore, 1));
					maxTerm = null;
				}
			}
		}
		return all;
	}

	public void recogntion() {
		Term term = null;
		for (int i = 0; i < terms.length; i++) {
			term = terms[i];
			if (term == null) {
				continue;
			}
			term.selfScore = 0;
			term.score = 0;
			if (term.getTermNatures().companyNature.bb < -0.005
					&& term.getTermNatures().companyNature.b > 1000) {
				double tempScore = term.getTermNatures().companyNature.bb;
				offe = term.getOffe();
				tempTerm = null;
				beginTerm = term.getFrom();
				recogntion_(term, tempScore);
				if (maxTerm != null) {
					TermUtil.insertTerm(terms, maxTerm);
					maxTerm = null;
				}
			}
		}
	}

	private void recogntion_(Term term, double score) {
		String companyName = term.getName();
		CompanyNature companyAttr = null;
		while ((term = term.getTo()) != null
				&& (companyAttr = term.getTermNatures().companyNature) != CompanyNature.NULL) {
			companyName += term.getName();
			if (companyAttr.eb < -0.005 && companyAttr.e > 200) {
				score += term.getTermNatures().companyNature.eb;
				tempTerm = new Term(companyName, offe, TermNatures.NT);
				tempTerm.selfScore = score;
				// 前缀分数
				if (beginTerm == null
						|| beginTerm.getTermNatures() == TermNatures.BEGIN) {
					tempTerm.selfScore += beginTerm.getTermNatures().companyNature.pb;
				}

				// 后缀分数
				Term to = term.getTo();
				if (to == null || to.getTermNatures() == TermNatures.END) {
					tempTerm.selfScore += to.getTermNatures().companyNature.sb;
				}
				// 计算分数
				int length = companyName.length() > 50 ? 50 : companyName
						.length();
				tempTerm.selfScore *= -Math.log(1 - FACTORY[length]);
				if (maxTerm == null || maxTerm.selfScore > tempTerm.selfScore) {
					maxTerm = tempTerm;
				}
			}
			if (companyAttr.mb < -0.005 && companyAttr.m > 50) {
				score += term.getTermNatures().companyNature.mb;
			} else {
				return;
			}
		}
	}
}
