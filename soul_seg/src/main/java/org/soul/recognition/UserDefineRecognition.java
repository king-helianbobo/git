package org.soul.recognition;

import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.domain.TermNatures;
import org.soul.domain.TermUtil;
import org.soul.treeSplit.Forest;
import org.soul.treeSplit.ObjectBean;
import org.soul.treeSplit.WoodInterface;
import org.soul.util.UserDefineLibrary;

/**
 * 用户自定义词典.又称补充词典
 * 
 * @author ansj
 * 
 */
public class UserDefineRecognition {

	private Term[] terms = null;

	private WoodInterface forest = UserDefineLibrary.FOREST;

	private WoodInterface branch = forest;

	private int offe = -1;
	private int endOffe = -1;
	private int tempFreq = 50;
	private String tempNature;

	public UserDefineRecognition(Term[] terms) {
		this.terms = terms;
	}

	public UserDefineRecognition(Term[] terms, Forest forest) {
		this.terms = terms;
		if (forest != null) {
			this.forest = forest;
			branch = this.forest;
		}

	}

	public void recognition() { // 识别用户自定义词典中的词
		if (branch == null) {
			return;
		}
		int length = terms.length - 1;
		boolean flag = true;
		for (int i = 0; i < length; i++) {
			if (terms[i] == null)
				continue;
			if (branch == forest) {
				flag = false;
			} else {
				flag = true;
			}
			branch = termStatus(branch, terms[i]);
			if (branch == null) {
				if (offe != -1) {
					i = offe;
				}
				reset();
			} else if (branch.getStatus() == 3) { // term 存在于用户词典中
				endOffe = i;
				tempNature = branch.getParams()[0];
				tempFreq = ObjectBean.getInt(branch.getParams()[1], 50);
				if (offe != -1 && offe < endOffe) {
					i = offe;
					makeNewTerm();
					reset();
				} else {
					reset();
				}
			} else if (branch.getStatus() == 2) {
				endOffe = i;
				if (offe == -1) {
					offe = i;
				} else {
					tempNature = branch.getParams()[0];
					tempFreq = ObjectBean.getInt(branch.getParams()[1], 50);
					if (flag) {
						makeNewTerm();
					}
				}
			} else if (branch.getStatus() == 1) {
				if (offe == -1) {
					offe = i;
				}
			}
		}
		if (offe != -1 && offe < endOffe) {
			makeNewTerm();
		}
	}

	private void makeNewTerm() {
		StringBuilder sb = new StringBuilder();
		for (int j = offe; j <= endOffe; j++) {
			if (terms[j] == null) {
				continue;
			} else {
				sb.append(terms[j].getName());
			}
			// terms[j] = null;
		}
		TermNatures termNatures = new TermNatures(new TermNature(tempNature,
				tempFreq));
		Term term = new Term(sb.toString(), offe, termNatures);
		term.setNature(termNatures.termNatures[0].nature);
		term.selfScore = -1 * tempFreq;
		TermUtil.insertTerm(terms, term);
		// reset();
	}

	/**
	 * 重置
	 */
	private void reset() {
		offe = -1;
		endOffe = -1;
		tempFreq = 50;
		tempNature = null;
		branch = forest;
	}

	/**
	 * 传入一个term 返回这个term的状态
	 * 
	 * @param branch
	 * @param term
	 * @return
	 */
	private WoodInterface termStatus(WoodInterface branch, Term term) {
		String name = term.getName();
		for (int j = 0; j < name.length(); j++) {
			branch = branch.get(name.charAt(j));
			if (branch == null) {
				return null;
			}
		}
		return branch;
	}

}