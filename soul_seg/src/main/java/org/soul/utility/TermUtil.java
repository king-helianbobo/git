package org.soul.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;
import org.soul.library.CompanyAttrLib;
import org.soul.library.NatureLibrary;
import org.soul.recognition.ForeignNameRecognition;
import org.soul.recognition.NewWordRecognition;

public class TermUtil {
	private static Log log = LogFactory.getLog(TermUtil.class);
	private static final HashMap<String, int[]> companyMap = CompanyAttrLib
			.getCompanyMap();

	/**
	 * 将两个数词/量词term合并为一个全新的term
	 */
	public static Term makeNewNumTerm(Term from, Term to,
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
	 * 将一个term插入到链表中的对应位置
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

	// set new word's nature
	public static void parseNewWordNature(Term term) {
		if (!TermNature.NW.equals(term.getNatrue())) {
			return;
		}
		String name = term.getName();
		if (name.length() <= 3) {
			return;
		}
		// if this term is foreign name
		if (ForeignNameRecognition.isFName(name)) {
			term.setNature(NatureLibrary.getNature("nrf"));
			return;
		}
		List<Term> subTerm = term.getSubTermList();
		term.setSubTermList(subTerm);
		Term first = subTerm.get(0);
		Term last = subTerm.get(subTerm.size() - 1);
		int[] is = companyMap.get(first.getName()); // is organization?
		is = companyMap.get(last.getName());
		int all = 0;
		if (is != null)
			all += is[1];
		if (all > 1000) {
			term.setNature(NatureLibrary.getNature("nt"));
			return;
		}
	}

	/**
	 * 从from到to生成term列表
	 * 
	 * @param terms
	 * @param from
	 * @param to
	 * @return
	 */
	public static List<Term> getSubTermList(Term from, Term to) {
		List<Term> subTerm = new ArrayList<Term>();
		while ((from = from.getTo()) != to) {
			// log.info(from.getName());
			subTerm.add(from);
		}
		return subTerm;
	}
}
