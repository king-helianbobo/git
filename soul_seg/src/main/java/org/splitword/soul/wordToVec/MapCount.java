package org.splitword.soul.wordToVec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class MapCount<T> {
	private HashMap<T, Integer> hm = null;

	public MapCount() {
		hm = new HashMap<T, Integer>();
	}

	public MapCount(int initialCapacity) {
		hm = new HashMap<T, Integer>(initialCapacity);
	}

	/**
	 * 增加一个元素
	 * 
	 * @param t
	 * @param n
	 */
	public void add(T t, int n) {
		Integer integer = null;
		if ((integer = hm.get(t)) != null) {
			hm.put(t, integer + n);
		} else {
			hm.put(t, n);
		}
	}

	public void add(T t) {
		this.add(t, 1);
	}

	/**
	 * map的大小
	 * 
	 * @return
	 */
	public int size() {
		return hm.size();
	}

	public void remove(T t) {
		hm.remove(t);
	}

	public HashMap<T, Integer> get() {
		return this.hm;
	}

	public String getDic() {
		Iterator<Entry<T, Integer>> iterator = this.hm.entrySet().iterator();
		StringBuilder sb = new StringBuilder();
		Entry<T, Integer> next = null;
		while (iterator.hasNext()) {
			next = iterator.next();
			sb.append(next.getKey());
			sb.append("\t");
			sb.append(next.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

}
