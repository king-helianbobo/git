package org.splitword.lionsoul.jcseg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.splitword.lionsoul.jcseg.Word;
import org.splitword.lionsoul.jcseg.core.IWord;

/**
 * Jcseg dictionary merge class. all the duplicate entries will be removed,
 * sorted them by natural order.
 * 
 * demo: 人事部/nt/ren shi bu/人事管理部門,人事管理部 . <br />
 * 
 * <ul>
 * <li>1. the pinyin will be merged.</li>
 * <li>2. the part of the speech will be merged.</li>
 * <li>3. the synonyms words will be merged.</li>
 * </ul>
 */
public class DictionaryMerge {

	private static boolean inArray(String[] arr, String item) {
		for (int j = 0; j < arr.length; j++)
			if (arr[j].equals(item))
				return true;
		return false;
	}

	/**
	 * merge two jcseg dictionary files, remove the duplicate entries and store
	 * the entris in a specified file. <br />
	 * 
	 * @param srcFiles
	 * @param dstfile
	 * @return int
	 * @throws IOException
	 */
	public static int merge(File[] srcFiles, File dstFile) throws IOException {
		// merge the source dictionary.
		IWord word = null;
		BufferedReader reader = null;
		String keywords = null;
		HashMap<String, IWord> entries = new HashMap<String, IWord>();
		for (int j = 0; j < srcFiles.length; j++) {
			String line = null;
			reader = new BufferedReader(new FileReader(srcFiles[j]));
			keywords = reader.readLine();
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				// clear the comment and the whitespace
				if (line.equals(""))
					continue;
				if (line.length() > 1 && line.charAt(0) == '#')
					continue;
				if (line.indexOf('/') == -1) { // simple word
					if (!entries.containsKey(line))
						entries.put(line, new Word(line, 1));
					continue;
				}
				// 人事部/nt/ren shi bu/人事管理部門,人事管理部
				String[] splits = line.split("/");
				if (splits.length < 4) {
					line = null;
					splits = null; // let the garbage collection do its work.
					continue;
				}
				// get the entries
				word = entries.get(splits[0]);
				if (word == null) {
					int type = 0, frequency = 0;
					if (splits.length > 4) {
						frequency = Integer.parseInt(splits[4]);
						type = 2;
					}
					word = new Word(splits[0], frequency, type);
					if (!splits[1].equals("null")) // part of the speech
						word.setPartSpeech(splits[1].split(","));
					if (!(splits[2].equals("%") || splits[2].equals("null"))) // pinyin
						word.setPinyin(splits[2]);
					if (!splits[3].equals("null")) // synonyms
						word.setSyn(splits[3].split(","));
					entries.put(splits[0], word);
				} else {
					// check check the part of the speech
					if (!splits[1].equals("null")) {
						String[] pps = splits[1].split(",");
						if (word.getPartSpeech() == null)
							word.setPartSpeech(pps);
						else {
							String[] ps = word.getPartSpeech();
							for (int i = 0; i < pps.length; i++)
								if (!inArray(ps, pps[i]))
									word.addPartSpeech(pps[i]);
						}
						pps = null;
					}
					// check the pinyin
					if (word.getPinyin() == null && !splits[2].equals("null"))
						word.setPinyin(splits[2]);
					// check the synonyms.
					if (!splits[3].equals("null")) {
						String[] syns = splits[3].split(",");
						if (word.getSyn() == null)
							word.setSyn(syns);
						else {
							String[] syn = word.getSyn();
							for (int i = 0; i < syns.length; i++)
								if (!inArray(syn, syns[i]))
									word.addSyn(syns[i]);
						}
						syns = null;
					}
				}
			} // end of while
			reader.close();
		}
		// sort the entries by natural order.
		String[] keys = new String[entries.size()];
		entries.keySet().toArray(keys);
		Sort.quicksort(keys);
		// write the merged entries to the destination file.
		BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));
		writer.write(keywords);
		writer.write('\n');

		IStringBuffer isb = new IStringBuffer();
		for (int j = 0; j < keys.length; j++) {
			word = entries.get(keys[j]);
			/*
			 * Here: if the orgin lexicon is simple lexicon. we just need the
			 * word item.
			 * 
			 * @added 2013-11-28
			 */
			if (word.getType() == 1) { // 1 represent simple lexicon
				writer.write(word.getValue());
				writer.write('\n');
				continue;
			}
			isb.clear();
			isb.append(word.getValue()); // word
			isb.append('/');
			if (word.getPartSpeech() == null) // part of speech
				isb.append("null");
			else {
				String[] ps = word.getPartSpeech();
				for (int i = 0; i < ps.length; i++) {
					if (i == 0)
						isb.append(ps[0]);
					else {
						isb.append(',');
						isb.append(ps[i]);
					}
				}
			}
			isb.append('/');
			if (word.getPinyin() == null) // pinyin
				isb.append("null");
			else
				isb.append(word.getPinyin());
			isb.append('/');
			if (word.getSyn() == null) // synonyms
				isb.append("null");
			else {
				String[] syn = word.getSyn();
				for (int i = 0; i < syn.length; i++) {
					if (i == 0)
						isb.append(syn[0]);
					else {
						isb.append(',');
						isb.append(syn[i]);
					}
				}
			}
			if (word.getType() == 2) { // single word degree
				isb.append("/" + word.getFrequency());
			}
			writer.write(isb.buffer(), 0, isb.length());
			writer.write('\n');
		}
		writer.close();
		return keys.length;
	}
}
