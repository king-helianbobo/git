package org.soul.crf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.crf.*;
import org.soul.treeSplit.StringUtil;
import org.soul.utility.MatrixUtil;
import org.soul.utility.WordAlter;

public class SplitWord {
	private static Log log = LogFactory.getLog(SplitWord.class);
	private Model model = null;
	private int[] tagConver = null;
	private int[] revTagConver = null;

	public SplitWord(Model model) {
		this.model = model;
		// try {
		// Model.writeToFile(model, "/home/lau/crf-model-1");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		tagConver = new int[model.template.tagNum];
		revTagConver = new int[model.template.tagNum];
		Set<Entry<String, Integer>> entrySet = model.template.statusMap
				.entrySet();

		// case 0:'S';case 1:'B';case 2:'M';3:'E';
		for (Entry<String, Integer> entry : entrySet) {
			if ("S".equals(entry.getKey())) {
				tagConver[entry.getValue()] = 0;
				revTagConver[0] = entry.getValue();
			} else if ("B".equals(entry.getKey())) {
				tagConver[entry.getValue()] = 1;
				revTagConver[1] = entry.getValue();
			} else if ("M".equals(entry.getKey())) {
				tagConver[entry.getValue()] = 2;
				revTagConver[2] = entry.getValue();
			} else if ("E".equals(entry.getKey())) {
				tagConver[entry.getValue()] = 3;
				revTagConver[3] = entry.getValue();
			}
		}
		model.end1 = model.template.statusMap.get("S");
		model.end2 = model.template.statusMap.get("E");

	};

	public List<String> cut(char[] chars) {
		return cut(new String(chars));
	}

	public List<String> cut(String line) {
		if (StringUtil.isBlank(line)) {
			return Collections.emptyList();
		}
		List<Element> elements = vterbi(line);
		LinkedList<String> result = new LinkedList<String>();

		Element e = null;
		int begin = 0;
		int end = 0;

		for (int i = 0; i < elements.size(); i++) {
			e = elements.get(i);
			switch (fixTag(e.getTag())) {
			case 0:
				end += e.len;
				result.add(line.substring(begin, end));
				begin = end;
				break;
			case 1:
				end += e.len;
				while (fixTag((e = elements.get(++i)).getTag()) != 3) { // 不是“E”
					end += e.len;
				}
				end += e.len;
				result.add(line.substring(begin, end));
				begin = end;
			default:
				break;
			}
		}
		return result;
	}

	private List<Element> vterbi(String line) {
		List<Element> elements = WordAlter.str2Elements(line);

		int length = elements.size();

		if (length == 1) {
			elements.get(0).updateTag(revTagConver[0]); // "S"
			return elements;
		}

		/**
		 * 填充图
		 */
		for (int i = 0; i < length; i++) {
			computeTagScore(elements, i);
		}

		// 如果是开始不可能从 m，e开始 ，所以将它设为一个很小的值
		elements.get(0).tagScore[revTagConver[2]] = -1000;
		elements.get(0).tagScore[revTagConver[3]] = -1000;
		for (int i = 1; i < length; i++) {
			elements.get(i).maxFrom(model.status, elements.get(i - 1));
		}

		// 末位状态只能从S,E开始,end1 represent S ,end2 represent E
		Element next = elements.get(elements.size() - 1);
		Element selfElement = null;
		int maxStatus = next.tagScore[model.end1] > next.tagScore[model.end2] ? model.end1
				: model.end2;
		next.updateTag(maxStatus);
		maxStatus = next.from[maxStatus];
		for (int i = elements.size() - 2; i > 0; i--) {
			selfElement = elements.get(i);
			selfElement.updateTag(maxStatus);
			// maxStatus = selfElement.from[selfElement.getTag()];
			maxStatus = selfElement.from[maxStatus];
			next = selfElement;
		}
		elements.get(0).updateTag(maxStatus);
		return elements;

	}

	private void computeTagScore(List<Element> elements, int index) {
		double[] tagScore = new double[model.template.tagNum];
		Template t = model.template;
		char[] chars = null;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < t.ft.length; i++) {
			chars = new char[t.ft[i].length];
			for (int j = 0; j < chars.length; j++) {
				chars[j] = getElement(elements, index + t.ft[i][j]).name;
			}
			if (i == 0)
				builder.append("[" + String.valueOf(chars));
			else if (i == t.ft.length - 1)
				builder.append("," + String.valueOf(chars) + "]");
			else
				builder.append("," + String.valueOf(chars));

			MatrixUtil.dot(tagScore, model.getFeature(i, chars));
		}
		// log.info("splitWord : " + builder.toString());
		elements.get(index).tagScore = tagScore;
	}

	private Element getElement(List<Element> elements, int i) {
		if (i < 0) {
			return new Element((char) ('B' + i));
		} else if (i >= elements.size()) {
			return new Element((char) ('B' + i - elements.size() + 1));
		} else {
			return elements.get(i);
		}
	}

	public int fixTag(int tag) {
		return tagConver[tag];
	}

}
