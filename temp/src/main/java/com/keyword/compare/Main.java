package com.keyword.compare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Main {

	private static final Map<String, ArrayList<String>> keywordMap = new HashMap<String, ArrayList<String>>();
	private static final Map<String, ArrayList<HashMap<String, ArrayList<String>>>> map = new HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>>();

	private static final String subjectOneStart = "<subjectone>";
	private static final String subjectOneEnd = "</subjectone>";

	private static final String subjectTwoStart = "<subjecttwo>";
	private static final String subjectTwoEnd = "</subjecttwo>";

	private static final String keywordsStart = "<keywords>";
	private static final String keywordsEnd = "</keywords>";
	private static OutputStream out;
	private static BufferedWriter bw;
	private static int firstNum = 0;
	private static int secondNum = 0;
	private static int unmatchNum = 0;
	private static int totalNum = 0;

	public static void main(String[] args) throws IOException {

		ExtractUsefulData.readFile();

		out = new FileOutputStream("data/result.txt");
		bw = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));

		readKeyWord();

		readSort();

		readData();

		out.close();
		bw.close();
		
		System.out.println("total num: " + totalNum + " , first num: " + firstNum + " , second num: " + secondNum + " , unmatch num: " + unmatchNum);
	}

	public static void readKeyWord() {
		try {
			File file = new File("data/keyword.txt");
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					dealKeyWordStr(lineTxt);
				}

				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * for (Entry<String, ArrayList<String>> entry : keywordMap.entrySet())
		 * { ArrayList<String> valueList = entry.getValue(); StringBuilder sb =
		 * new StringBuilder(); for (String value : valueList) { sb.append(value
		 * + ","); } System.out.println(entry.getKey() + " : " + sb.toString());
		 * }
		 */
	}

	public static void readSort() {
		try {
			File file = new File("data/sort.txt");
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					dealSortStr(lineTxt);
				}

				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(map.size());
	}

	public static void readData() {
		try {
			File file = new File("data/test.txt");
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				int i = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if ("--".equals(lineTxt))
						continue;

					List<String> linkList = new LinkedList<String>();
					while (i <= 2) {
						linkList.add(lineTxt);
						lineTxt = bufferedReader.readLine();
						i++;
					}

					dealLinkList(linkList);
	
					totalNum ++;
					i = 0;
				}
				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void dealLinkList(List<String> linkList) {
		// TODO Auto-generated method stub
		if (null == linkList || linkList.size() != 3)
			return;

		String subjectOne = extract(linkList.get(0), subjectOneStart,
				subjectOneEnd);
		String subjectTwo = extract(linkList.get(1), subjectTwoStart,
				subjectTwoEnd);
		String keyword = extract(linkList.get(2), keywordsStart, keywordsEnd);

		ArrayList<HashMap<String, ArrayList<String>>> first = map
				.get(subjectOne);
		if (null != first && first.size() > 0) {
			for (int i = 0; i < first.size(); i++) {
				ArrayList<String> keywords = first.get(i).get(subjectTwo);
				if (null != keywords && keywords.size() > 0) {
					deal(keywords, keyword, subjectOne, subjectTwo);
					return;
				}
			}

			finalDo(subjectOne,subjectTwo,keyword);
			secondNum ++;
		}
		finalDo(subjectOne,subjectTwo,keyword);
		
		firstNum ++;
		
		/*System.out.println(extract(linkList.get(0),subjectOneStart,subjectOneEnd) + " , " + 
					extract(linkList.get(1),subjectTwoStart,subjectTwoEnd) + " , " + 
					extract(linkList.get(2),keywordsStart,keywordsEnd));*/

			finalDo(subjectOne, subjectTwo, keyword);
		}
		//finalDo(subjectOne, subjectTwo, keyword);
		/*
		 * System.out.println(extract(linkList.get(0),subjectOneStart,subjectOneEnd
		 * ) + " , " + extract(linkList.get(1),subjectTwoStart,subjectTwoEnd) +
		 * " , " + extract(linkList.get(2),keywordsStart,keywordsEnd));
		 */

	//}

	private static void deal(ArrayList<String> keywords, String keyword,
			String subjectOne, String subjectTwo) {
		// TODO Auto-generated method stub
		if (null == keyword || "".equals(keyword))
			finalDo(subjectOne, subjectTwo, keyword);

		String[] temp = keyword.split("、");
		StringBuilder sb = new StringBuilder();
		boolean firstWord = true;

		for (String t : temp) {
			if (!keywords.contains(t.trim())) {
				if (!firstWord)
					sb.append("、");
				else
					firstWord = false;

				sb.append(t);
			} else
				continue;

		}
		if(null != sb.toString() && !"".equals(sb.toString())){
		if (null != sb.toString() && !"".equals(sb.toString()))
			finalDo(subjectOne, subjectTwo, sb.toString());
			unmatchNum ++;
		}
	}

	private static void finalDo(String subjectOne, String subjectTwo,
			String keyword) {
		// TODO Auto-generated method stub
		try {

			bw.append(subjectOneStart + subjectOne + subjectOneEnd + "\n");
			bw.append(subjectTwoStart + subjectTwo + subjectTwoEnd + "\n");
			bw.append(keywordsStart + keyword + keywordsEnd + "\n");
			bw.append("=========================================================================="
					+ "\n");

			bw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void dealSortStr(String lineTxt) {
		// TODO Auto-generated method stub
		if (null == lineTxt || "".equals(lineTxt))
			return;

		String[] temp = lineTxt.split("[:]");
		String key = temp[0];

		String[] valueTmp = temp[1].split("[,]");

		if (null != valueTmp && valueTmp.length > 0) {
			ArrayList<HashMap<String, ArrayList<String>>> list = new ArrayList<HashMap<String, ArrayList<String>>>();
			for (String t : valueTmp) {
				String[] internalTmp = t.split("[|]");
				String internalKey = internalTmp[0];
				HashMap<String, ArrayList<String>> internalMap = new HashMap<String, ArrayList<String>>();
				ArrayList<String> internalValue = getDataFromMap(internalTmp[1]);

				if (null != internalValue && internalValue.size() > 0) {
					internalMap.put(internalKey, internalValue);
					list.add(internalMap);
				}
			}
			map.put(key, list);
		}
	}

	private static void dealKeyWordStr(String lineTxt) {
		// TODO Auto-generated method stub

		if (null == lineTxt || "".equals(lineTxt))
			return;
		String[] temp = lineTxt.split("[:]");
		String key = temp[0];
		ArrayList<String> valueList = new ArrayList<String>();
		String[] valueTmp = temp[1].split(" ");
		if (null != valueTmp) {
			for (String t : valueTmp) {
				valueList.add(t.trim());
			}
		}
		keywordMap.put(key, valueList);
	}

	// 文秘工作|35,应急管理|N,政务督查|35,电子政务|29,保密工作|35,信访|35,参事、文史|26,机关事务|35,其他|综合、行政事务
	private static ArrayList<String> getDataFromMap(String key) {

		if (null == key || "N".equalsIgnoreCase(key) || "".equals(key))
			return null;

		String[] temp = key.split("、");
		ArrayList<String> resultList = new ArrayList<String>();
		if (temp.length > 1) {
			for (String t : temp) {
				resultList.addAll(getData(t));
			}
		} else {
			resultList.addAll(getData(key));
		}

		return resultList;
	}

	private static ArrayList<String> getData(String key) {

		for (Entry<String, ArrayList<String>> entry : keywordMap.entrySet()) {
			ArrayList<String> valueList = entry.getValue();
			String key1 = entry.getKey();

			String[] temp = key1.split("[|]");

			if (isEqual(key, temp)) {
				return valueList;
			} else
				continue;
		}

		return null;
	}

	private static boolean isEqual(String str, String[] array) {
		if (null == array || array.length < 0)
			return false;
		for (String t : array) {
			if (t.equalsIgnoreCase(str))
				return true;
			else
				continue;
		}
		return false;
	}

	private static String extract(String content, String startTag, String endTag) {
		int start = -1;
		int end = -1;
		StringBuilder builder = new StringBuilder();
		start = content.indexOf(startTag);
		end = content.indexOf(endTag, start);
		if (start >= 0 && end >= 0) {
			String key = content.substring(start + startTag.length(), end);
			builder.append(key);
		}
		return builder.toString();
	}
}
