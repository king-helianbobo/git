package com.soul.readExcel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CalculatingCorrelation {

	private static Map<String, String> fieldMap = new HashMap<String, String>();
	private static String fileName = "";
	private static Map<String, ArrayList<String>> tableMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, HashMap<String, String>> columnNameMap = new HashMap<String, HashMap<String, String>>();
	private static Map<String, Double> ratioMap = new HashMap<String, Double>();

	static {

		try {
			FileInputStream tableIn = new FileInputStream("table/1.txt");
			BufferedReader tableBr = new BufferedReader(new InputStreamReader(
					tableIn, "utf-8"));
			String lineTxt = null;

			while ((lineTxt = tableBr.readLine()) != null) {
				if (!"".equals(lineTxt)) {
					String[] temp = lineTxt.split("[=]");
					fieldMap.put(temp[0], temp[1]);
				}
			}
			tableIn.close();
			tableBr.close();
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

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		listDirectory("/home/jacky/文档/开发测试数据");

		calculate();

		ratioMap = sortMap(ratioMap);

		FileOutputStream tableOut = new FileOutputStream(
				"table/column/relevancy.txt");
		BufferedWriter tableBw = new BufferedWriter(new OutputStreamWriter(
				tableOut, "utf-8"));

		for (Entry<String, Double> entry : ratioMap.entrySet()) {
			tableBw.append("table " + entry.getKey() + " relevancy is "
					+ entry.getValue() + "\n");
		}
		tableBw.flush();
		tableOut.close();
		tableBw.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map sortMap(Map oldMap) {
		ArrayList<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(
				oldMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<java.lang.String, Double> arg0,
					Entry<java.lang.String, Double> arg1) {
				return (int) (arg1.getValue() - arg0.getValue());
			}
		});
		Map newMap = new LinkedHashMap();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;
	}

	private static void calculate() {
		// TODO Auto-generated method stub
		double ratio = 0;
		for (Entry<String, ArrayList<String>> tableEntry : tableMap.entrySet()) {
			String tableName = tableEntry.getKey();
			ArrayList<String> fieldList = tableEntry.getValue();

			for (Entry<String, String> fieldEntry : fieldMap.entrySet()) {
				String field = fieldEntry.getKey();
				int frequency = Integer.valueOf(fieldEntry.getValue());

				if (fieldList.contains(field)) {
					ratio = ratio + internalCal_1(field, frequency, tableName);
				}
			}
			ratio = Double.valueOf(String.format("%.2f", ratio));
			// System.out.println("ration: " + ratio);
			ratioMap.put(tableName, ratio);
			System.out.println("==========================================");
		}
	}

	private static double internalCal_1(String field, int frequency,
			String tableName) {
		// TODO Auto-generated method stub
		int sum = 0;
		System.out.println(tableName + " relate table is :");
		for (Entry<String, ArrayList<String>> entry : tableMap.entrySet()) {
			String tableName_1 = entry.getKey();
			ArrayList<String> fieldList = entry.getValue();
			if (!tableName.trim().equals(tableName_1.trim())) {
				if (fieldList.contains(field)) {
					sum++;
					System.out.print(tableName_1 + "\t");
				}
			}
		}

		double ratio = (double) sum / (double) frequency;

		System.out.println();
		System.out.println(" by " + field);
		System.out.println();

		return ratio;
	}

	private static void listDirectory(String path)
			throws FileNotFoundException, IOException {
		File dir = new File(path);
		File file[] = dir.listFiles();
		for (int j = 0; j < file.length; j++) {
			if (file[j].isDirectory()) {
				listDirectory(file[j].getAbsolutePath());
			} else if (file[j].getName().endsWith("xls")
					&& !file[j].getName().trim().endsWith("发票开具信息.xls".trim())) {
				String[][] result = Utilitys.getData(file[j], 0);
				fileName = file[j].getName();

				dealTableRelate(result);

			} else {
				// System.out.println("do nothing");
			}
		}
	}

	private static void dealTableRelate(String[][] result) {
		// TODO Auto-generated method stub
		int rowLength = result.length;
		int columnLength = result[0].length;
		String key = fileName.split("[.]")[0].trim();
		ArrayList<String> columnList = new ArrayList<String>();
		HashMap<String, String> columnMap = new HashMap<String, String>();
		// for (int i = 0; i < rowLength; i++) {
		if (!fileName.trim().endsWith("发票开具信息.xls".trim())) {
			for (int j = 1; /* ignore first column */j < columnLength/*
																	 * result[i].
																	 * length
																	 */; j++) {
				columnList.add(result[0][j]);
				columnMap.put(result[0][j], result[1][j]);
			}
			tableMap.put(key, columnList);
			columnNameMap.put(key, columnMap);

		} else {
			for (int j = 1; /* ignore first column */j < columnLength/*
																	 * result[i].
																	 * length
																	 */; j++) {

			}
		}
		// }

	}
}
