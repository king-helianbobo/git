package org.soul.treeSplit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class Library {

	public static Forest makeForest(String path) throws Exception {
		return makeForest(new FileInputStream(path));
	}

	public static Forest makeForest(InputStream inputStream) throws Exception {
		return makeForest(IOUtil.getReader(inputStream, "UTF-8"));
	}

	public static Forest makeForest(BufferedReader br) throws Exception {
		return makeLibrary(br, new Forest());
	}

	/**
	 * 传入value数组.构造树
	 * 
	 * @param values
	 * @param forest
	 * @return
	 */
	public static Forest makeForest(List<Value> values) {
		Forest forest = new Forest();
		for (Value value : values) {
			insertWord(forest, value.toString());
		}
		return forest;
	}

	/**
	 * 词典树的构造方法
	 * 
	 * @param br
	 * @param forest
	 * @return
	 * @throws Exception
	 */
	private static Forest makeLibrary(BufferedReader br, Forest forest)
			throws Exception {
		if (br == null)
			return forest;
		try {
			String temp = null;
			while ((temp = br.readLine()) != null) {
				insertWord(forest, temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
		return forest;
	}

	public static void insertWord(Forest forest, Value value) {
		insertWord(forest, value.getKeyword(), value.getParamers());
	}

	/**
	 * insert one word
	 * 
	 * @param forest
	 * @param temp
	 */
	public static void insertWord(WoodInterface forest, String tmp) {
		String[] param = tmp.split("\t");
		String[] resultParams = new String[param.length - 1];
		for (int j = 1; j < param.length; j++) {
			resultParams[j - 1] = param[j];
		}
		insertWord(forest, param[0], resultParams);
	}

	private static void insertWord(WoodInterface forest, String key,
			String[] param) {
		WoodInterface branch = forest;
		char[] chars = key.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars.length == i + 1) {
				branch.add(new Branch(chars[i], 3, param));
				// 这里插入的是Branch，而不是Forest，Forest只是最外一层
			} else {
				branch.add(new Branch(chars[i], 1, null));
			}
			branch = branch.get(chars[i]);
		}
	}

	/**
	 * delete one word
	 * 
	 * @param forest
	 * @param temp
	 */
	public static void removeWord(Forest forest, String word) {
		WoodInterface branch = forest;
		char[] chars = word.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (branch == null)
				return;
			if (chars.length == i + 1) {
				branch.add(new Branch(chars[i], -1, null));
			}
			branch = branch.get(chars[i]);
		}
	}
}