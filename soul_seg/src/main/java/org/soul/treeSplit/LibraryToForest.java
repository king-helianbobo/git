package org.soul.treeSplit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.soul.utility.WordAlter;

public class LibraryToForest {

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
	 * 
	 * @author LiuBo
	 * @since 2014年1月14日
	 * @param values
	 * @return Forest
	 */
	public static Forest makeForest(List<TrieValue> values) {
		Forest forest = new Forest();
		for (TrieValue value : values) {
			insertWord(forest, value.toString());
		}
		return forest;
	}

	/**
	 * 
	 * @author LiuBo
	 * @since 2014年1月14日
	 * @param br
	 * @param forest
	 * @return
	 * @throws Exception
	 *             Forest
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

	public static void insertWord(Forest forest, TrieValue value) {
		_insertWord_(forest, value.getKeyword(), value.getParamers());
	}

	/**
	 * insert one word
	 * 
	 * @param forest
	 * @param temp
	 */
	public static void insertWord(WoodInterface forest, String tmp) {
		String[] param = tmp.split("\t");
		for (int i = 0; i < param.length; i++)
			param[i] = WordAlter.alterAlphaAndNumber(param[i], 0,
					param[i].length());
		String[] resultParams = new String[param.length - 1];
		for (int j = 1; j < param.length; j++) {
			resultParams[j - 1] = param[j];
		}
		_insertWord_(forest, param[0], resultParams);
	}

	private static void _insertWord_(WoodInterface forest, String key,
			String[] param) {
		WoodInterface branch = forest;
		char[] chars = key.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars.length == i + 1) {
				branch.add(new Branch(chars[i], 3, param));
				// 插入的是Branch，而不是Forest，Forest是Trie树的最外一层
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
		char[] chars = WordAlter.alterAlphaAndNumber(word).toCharArray();
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