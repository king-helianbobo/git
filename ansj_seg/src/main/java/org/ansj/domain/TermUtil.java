package org.ansj.domain;

import java.util.List;

public class TermUtil {

	/**
	 * 将两个数词/量词term合并为一个全新的term
	 */
	public static Term makeNewTermNum(Term from, Term to,
			TermNatures termNatures) {
		Term term = new Term(from.getName() + to.getName(), from.getOffe(),
				termNatures);
		term.getTermNatures().numNature = from.getTermNatures().numNature;
		TermUtil.termLink(term, to.getTo());
		TermUtil.termLink(term.getFrom(), term);
		return term;
	}

	public static void termLink(Term from, Term to) {
		if (from == null || to == null)
			return;
		from.setTo(to);
		to.setFrom(from);
	}

	/**
	 * 将一个term插入到链表中的对应位置中
	 */
	public static void insertTerm(Term[] terms, Term term) {
		Term temp = terms[term.getOffe()];
		if (temp == null) {
			terms[term.getOffe()] = term;
		} else {
			if (temp.getNext() != null) {
				term.setNext(temp.getNext());
			}
			temp.setNext(term);
		}
	}

	public static void insertTerm(Term[] terms, List<Term> tempList,
			TermNatures nr) { // nr defaults to TermNatures.NR
		StringBuilder sb = new StringBuilder();
		int offe = tempList.get(0).getOffe();
		for (Term term : tempList) {
			sb.append(term.getName());
			terms[term.getOffe()] = null;
		}
		Term term = new Term(sb.toString(), offe, nr);
		terms[term.getOffe()] = term;
	}

	protected static Term setToAndfrom(Term to, Term from) {
		from.setTo(to);
		to.setFrom(from);
		return from;
	}
}
