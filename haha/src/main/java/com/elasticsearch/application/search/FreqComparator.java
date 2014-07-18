package com.elasticsearch.application.search;

import java.util.Comparator;

public class FreqComparator implements Comparator<FreqEntity> {

	@Override
	public int compare(FreqEntity freqEntity1, FreqEntity freqEntity2) {
		if (freqEntity1.getTitle_num() > freqEntity2.getTitle_num())
			return 1;
		if (freqEntity1.getTitle_num() == freqEntity2.getTitle_num())
			return 0;
		return -1;
	}

}
