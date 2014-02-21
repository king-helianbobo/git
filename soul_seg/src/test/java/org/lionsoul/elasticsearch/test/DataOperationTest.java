package org.lionsoul.elasticsearch.test;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

public class DataOperationTest {
	private static final Log log = LogFactory.getLog(DataOperationTest.class);
	@Ignore("Convert Sogou Txt data to hdfs friendly format")
	@Test
	public void convertDataToHdfsFormat() {
		SogouDataReader reader = new SogouDataReader("/mnt/f/Sogou-mini/");
		try {
			reader.convertToHdfsFormat("/mnt/f/hdfs3/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractTmp5() {
		String paths[] = {"/mnt/f/seg/1-1.txt", "/mnt/f/seg/2-1.txt",
				"/mnt/f/seg/3-1.txt", "/mnt/f/seg/4-1.txt",
				"/mnt/f/seg/5-1.txt", "/mnt/f/seg/8-1.txt"};
		List<Set<String>> tree = new ArrayList<Set<String>>();
		InputStream in;
		BufferedReader reader;
		FileWriter fw;
		BufferedWriter bw;
		String temp = null;
		int num = 0;
		try {
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("\\s+");
					String str1 = strs[0].trim();
					String str2 = strs[1].trim();
					log.info(str1 + " " + str2);
					// bw.write(str1 + " " + str2 + "\n");
					boolean contained = false;
					for (int i = 0; i < tree.size(); i++) {
						Set<String> set = tree.get(i);
						if (set.contains(str1) && set.contains(str2)) {
							log.info("this line has contained!");
							contained = true;
						} else if (set.contains(str1)) {
							set.add(str2);
							contained = true;
						} else if (set.contains(str2)) {
							set.add(str1);
							contained = true;
						} else {
						}
					}
					if (!contained) {
						Set<String> set = new HashSet<String>();
						set.add(str1);
						set.add(str2);
						tree.add(set);
					}
					num++;
				}
			}
			log.info("Total Word count is " + num);
			log.info("Total Word Line Numer is " + tree.size());
			fw = new FileWriter("/tmp/a", false);
			bw = new BufferedWriter(fw);
			for (int i = 0; i < tree.size(); i++) {
				Set<String> set = tree.get(i);
				Iterator<String> it = set.iterator();
				boolean firstWord = true;
				while (it.hasNext()) {
					if (!firstWord)
						bw.write(",");
					else
						firstWord = false;
					String str = it.next();
					bw.write(str);
				}
				bw.newLine();
			}
			bw.close();
			fw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractOtherTxt() {
		String paths[] = {"/mnt/f/tmp/1.txt", "/mnt/f/tmp/2.txt",
				"/mnt/f/tmp/3.txt"};
		String outPaths[] = {"/mnt/f/tmp/1-1.txt", "/mnt/f/tmp/2-1.txt",
				"/mnt/f/tmp/3-1.txt"};
		FileWriter fw;
		BufferedWriter bw;
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			for (int j = 0; j < paths.length; j++) {
				in = new FileInputStream(paths[j]);
				fw = new FileWriter(outPaths[j], true);
				bw = new BufferedWriter(fw);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("	");
					String str1 = strs[0].trim();
					String str2 = strs[1].trim();
					log.info(str1 + " " + str2);
					bw.write(str1 + " " + str2 + "\n");
					num++;
				}
				bw.close();
				fw.close();
			}
			log.info("Total Word count is " + num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractTmp7() {
		String paths[] = {"/mnt/f/tmp/7.txt"};
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			FileWriter fw = new FileWriter("/mnt/f/tmp/7-2.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split(" ");
					for (int i = 0; i < strs.length; i++) {
						String str = strs[i];
						String[] _strs = str.split("——");
						String str1 = _strs[0].trim();
						String str2 = _strs[1].trim();
						log.info(str1 + " " + str2);
						bw.write(str1 + " " + str2 + "\n");
						num++;
						// bw.write(str1 + " " + str2 + "\n");
					}
				}
			}
			bw.close();
			fw.close();
			log.info("Total Word count is " + num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractTmp4() {
		String paths[] = {"/mnt/f/tmp/4.txt"};
		String outPath = "/mnt/f/tmp/4-1.txt";
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			FileWriter fw = new FileWriter(outPath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("=>");
					int len1 = strs[0].trim().length();
					int len2 = strs[1].trim().length();
					String str1 = strs[0].trim().substring(1, len1 - 1);
					String str2 = strs[1].trim().substring(1, len2 - 2);
					log.info(str1 + " " + str2);
					bw.write(str1 + " " + str2 + "\n");
					num++;
				}
			}
			log.info("Total Word count is " + num);
			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void extractTmp8() {
		String paths[] = {"/mnt/f/tmp/8.txt"};
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			FileWriter fw = new FileWriter("/mnt/f/tmp/8-1.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("\\s+");
					String str1 = "";
					String str2 = "";
					for (int i = 0; i < strs.length; i++) {
						if (i % 3 == 0) {
							assertNotNull(Integer.valueOf(strs[i].trim()));
						} else if (i % 3 == 1) {
							str1 = strs[i].trim();
						} else {
							str2 = strs[i].trim();
							log.info(str1 + " " + str2);
							bw.write(str1 + " " + str2 + "\n");
							num++;
						}
					}
				}
			}
			bw.close();
			fw.close();
			log.info("Total Word count is " + num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
