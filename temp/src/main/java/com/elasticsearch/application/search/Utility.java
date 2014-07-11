package com.elasticsearch.application.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Utility {

	public static List removeDuplicate4Object(List list) {
		HashSet set = new HashSet(list);
		list.clear();
		list.addAll(set);

		return list;
	}

	public static ArrayList<String> removeDuplicate4List(ArrayList<String> list) {
		HashSet<String> set = new HashSet<String>(list);
		list.clear();
		list.addAll(set);

		return list;
	}

}
