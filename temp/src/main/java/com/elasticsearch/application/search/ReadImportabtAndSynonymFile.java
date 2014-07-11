package com.elasticsearch.application.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReadImportabtAndSynonymFile {
	private String splitStr;

	public ReadImportabtAndSynonymFile(String splitStr) {
		this.splitStr = splitStr;
	}

	public ArrayList<String> readFile(String filePath,
			ArrayList<String> resultList) {
		// TODO Auto-generated method stub
		if (null == resultList)
			resultList = new ArrayList<String>();
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					dealString(lineTxt, resultList);
				}
				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}

	public void dealString(String text, ArrayList<String> resultList) {
		// TODO Auto-generated method stub
		String[] temp = text.split(splitStr);
		for (String t : temp) {
			if (t.length() > 0 && !"".equals(t))
				resultList.add(t);
		}
	}
}
