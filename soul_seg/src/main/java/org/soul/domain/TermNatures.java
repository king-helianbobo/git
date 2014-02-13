package org.soul.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TermNatures {
	private static Log log = LogFactory.getLog(TermNatures.class);
	public static final TermNatures NULL = new TermNatures(TermNature.NULL);
	public static final TermNatures M = new TermNatures(TermNature.M);
	// public static final TermNatures NB = new TermNatures(TermNature.NB);
	public static final TermNatures NR = new TermNatures(TermNature.NR);
	public static final TermNatures EN = new TermNatures(TermNature.EN);
	public static final TermNatures END = new TermNatures(TermNature.END,
			50610, -1);
	public static final TermNatures BEGIN = new TermNatures(TermNature.BEGIN,
			50610, 0);
	public static final TermNatures NT = new TermNatures(TermNature.NT);
	public static final TermNatures NW = new TermNatures(TermNature.NW);
	public TermNature[] termNatures = null;

	public NumNatureAttr numNature = NumNatureAttr.NULL; // 数字属性
	public PersonNatureAttr personNature = PersonNatureAttr.NULL;// 人名词性
	// public CompanyNature companyNature = CompanyNature.NULL;// 机构名词性
	// public NewWordNature newWordNature = NewWordNature.NULL;// 新词词性
	public int allFreq = 0;// 词频之和
	public int id = -2;// term在baseArray中的id

	public TermNatures(TermNature[] termNatures, int id) {
		this.id = id;
		this.termNatures = termNatures;
		serAttribute();
	}

	public TermNatures(TermNature termNature) {
		termNatures = new TermNature[1];
		this.termNatures[0] = termNature;
		serAttribute();
	}

	public TermNatures(TermNature termNature, int allFreq, int id) {
		this.id = id;
		termNatures = new TermNature[1];
		termNature.frequency = allFreq;
		this.termNatures[0] = termNature;
		this.allFreq = allFreq;
	}

	private void serAttribute() {
		TermNature termNature = null;
		int max = -1;
		NumNatureAttr numNatureAttr = null;
		for (int i = 0; i < termNatures.length; i++) {
			termNature = termNatures[i];
			allFreq += termNature.frequency;
			max = Math.max(max, termNature.frequency);
			switch (termNature.natureInLib.index) {
				case 18 : // m
					if (numNatureAttr == null)
						numNatureAttr = new NumNatureAttr();
					numNatureAttr.numFreq = termNature.frequency;
					break;
				case 29 : // q,代表量词吗?即允许数字结尾，'倍''年'等量词都具有numEndFreq这个属性
					if (numNatureAttr == null)
						numNatureAttr = new NumNatureAttr();
					numNatureAttr.numEndFreq = termNature.frequency;
					break;
			}
		}
		if (numNatureAttr != null) {
			if (max == numNatureAttr.numFreq)
				numNatureAttr.flag = true; // 如果是数词，而非量词
			this.numNature = numNatureAttr;
		}
	}

	public void setPersonNatureAttr(PersonNatureAttr personAttr) {
		this.personNature = personAttr;
	}
}
