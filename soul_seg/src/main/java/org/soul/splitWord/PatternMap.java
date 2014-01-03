package org.soul.splitWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.soul.domain.Term;
import org.soul.utility.MathUtil;

public class PatternMap {

	private HashMap<String, Node> map = new HashMap<String, Node>();

	public void addList(List<Term> terms) {
		int length = terms.size();
		if (length < 2) { // one character is not considered as newWord
			return;
		}
		List<Term> all = null;
		for (int i = 0; i < length; i++) {
			all = new ArrayList<Term>(length - i);
			for (int j = i; j < length; j++) {
				all.add(terms.get(j));
				if (all.size() > 1) {
					double leftRightEntropy = MathUtil.leftRightEntropy(all);
					StringBuilder sb = new StringBuilder();
					for (Term term : all) {
						sb.append(term.getName());
					}
					String name = sb.toString();
					Node node = map.get(name);
					if (node == null) {
						node = new Node(name);
						node.score = leftRightEntropy;
						node.freq = 1;
						map.put(name, node);
					} else {
						node.score += leftRightEntropy;
						node.freq += 1;
					}
				}
			}

		}
	}

	public Collection<Node> getWords() {
		Collection<Node> values = map.values();
		return values;
	}

	/**
	 * 验证一个词是否是新词
	 * 
	 * @param node
	 * @param validate
	 * @return false 不合格.true 合格
	 */
	private boolean filter(Node node, double validate) {
		// TODO Auto-generated method stub
		if (node.freq < validate && node.score < 1) {
			return false;
		} else {
			return true;
		}
	}

	class Node implements Comparable<Node> {
		private double score; // 节点分数
		private String name; // 节点代表的词
		private int freq; // 词频

		public Node(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.name.equals(((Node) obj).name);
		}

		@Override
		public int compareTo(Node another) {
			if (this.score < another.score) {
				return 1;
			} else if (this.score > another.score) {
				return -1;
			}
			if (this.name.length() < another.name.length()) {
				return 1;
			} else {
				return -1;
			}
		}

		public int getFreq() {
			return freq;
		}

		public double getScore() {
			return score;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return this.name + ":" + this.score + ":" + this.freq;
		}

	}
}
