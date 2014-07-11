package com.elasticsearch.application.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * read file
 * 
 * @author root
 * 
 */
public class ReadFreqFile {

	private static final String className = "com.elasticsearch.application.search.FreqEntity";
	private static final String splitStr = "	";
	private final static String[] filterAttr = new String[] { "nr", "nrf",
			"rule" };
	private static ArrayList<String> resultList = new ArrayList<String>();
	static {
		ReadImportabtAndSynonymFile readImportabtAndSynonymFile = new ReadImportabtAndSynonymFile(
				",");
		resultList = Utility.removeDuplicate4List(readImportabtAndSynonymFile
				.readFile("/home/data/synonym-new.txt",
						readImportabtAndSynonymFile.readFile(
								"/home/data/important-0609.txt", null)));
	}

	public static ArrayList<FreqEntity> readFile(String filePath) {
		// TODO Auto-generated method stub
		ArrayList<FreqEntity> freqEntityList = new ArrayList<FreqEntity>();
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					FreqEntity freqEntity = (FreqEntity) Class.forName(
							className).newInstance();
					freqEntity = dealString(freqEntity, lineTxt);
					if (null != freqEntity)
						freqEntityList.add(freqEntity);
				}
				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return freqEntityList;
	}

	public static FreqEntity dealString(Object obj, String text) {
		// TODO Auto-generated method stub
		String[] temp = text.split(splitStr);
		if (temp.length == 6 && temp[0].length() > 1
				&& Long.valueOf(temp[4]) == 0 //title_sum
				&& Long.valueOf(temp[5]) > 5 //content _sum
				/* && Long.valueOf(temp[3]) >= 20 */
				/*&& filterAttr(temp[1])*/ && !resultList.contains(temp[0])) {
			((FreqEntity) obj).setWord(temp[0]);
			((FreqEntity) obj).setAttri(temp[1]);
			((FreqEntity) obj).setContainPage_num(Long.valueOf(temp[2]));
			((FreqEntity) obj).setTotal_num(Long.valueOf(temp[3]));
			((FreqEntity) obj).setTitle_num(Long.valueOf(temp[4]));
			((FreqEntity) obj).setContent_num(Long.valueOf(temp[5]));

			return ((FreqEntity) obj);
		}
		return null;
	}

	static boolean filterAttr(String attr) {
		for (String filter : filterAttr) {
			if (attr.trim().equals(filter.trim()))
				return false;
			else
				continue;
		}
		return true;
	}
}
