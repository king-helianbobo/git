package com.soul.readExcel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.codehaus.jackson.map.ObjectMapper;

public class ExtractExcelData {

	private static FileOutputStream out;
	private static FileOutputStream tableOut;
	private static FileOutputStream relateOut;
	private static BufferedWriter bw;
	private static BufferedWriter tableBw;
	private static BufferedWriter relateBw;
	private static String fileName = "";
	private static ObjectMapper mapper = new ObjectMapper();
	private static Map<String, ArrayList<String>> tableMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, HashMap<String, String>> columnNameMap = new HashMap<String, HashMap<String, String>>();

	static {
		try {
			out = new FileOutputStream("excel/result.txt");
			bw = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));

			tableOut = new FileOutputStream("excel/tableColumn.txt");
			tableBw = new BufferedWriter(new OutputStreamWriter(tableOut,
					"utf-8"));

			relateOut = new FileOutputStream("excel/样本数据关联关系.txt");
			relateBw = new BufferedWriter(new OutputStreamWriter(relateOut,
					"utf-8"));

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		listDirectory("加工测试数据2");

		generateRelateFile(tableMap);

		close();
	}

	private static void close() {
		try {

			if (out != null)
				out.close();
			if (bw != null)
				bw.close();

			if (tableOut != null)
				tableOut.close();
			if (tableBw != null)
				tableBw.close();

			if (relateOut != null)
				relateOut.close();
			if (relateBw != null)
				relateBw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void listDirectory(String path)
			throws FileNotFoundException, IOException {
		File dir = new File(path);
		File file[] = dir.listFiles();
		int total = 0;
		for (int j = 0; j < file.length; j++) {
			if (file[j].isDirectory()) {
				listDirectory(file[j].getAbsolutePath());
			} else if (file[j].getName().endsWith("xls")) {
				fileName = file[j].getName();
				String[][] result = Utilitys.getData(file[j], 0);
				outPutDataToTxt(result);

				outPutTableColumn(result);

				dealTableRelate(result);

				total++;
			} else {
				// do nothing;
				System.out.println("do nothing");
			}
		}

		// System.out.println("total: " + total);
	}

	private static void generateRelateFile(
			Map<String, ArrayList<String>> tableMap2) throws IOException {
		// TODO Auto-generated method stub
		Iterator<Map.Entry<String, ArrayList<String>>> it = tableMap2
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = it.next();
			String key = entry.getKey();
			compare(entry.getValue(),tableMap2,key);
			it.remove(); 
			compare(entry.getValue(), tableMap2, key);
			//it.remove(); // 
		}
	}

	private static void compare(ArrayList<String> value,
			Map<String, ArrayList<String>> tableMap2, String key)
			throws IOException {
		// TODO Auto-generated method stub

		for (Entry<String, ArrayList<String>> entry : tableMap2.entrySet()) {
			ArrayList<String> valueList = entry.getValue();
			String key1 = entry.getKey();

			if (key.trim().equals(key1.trim()))
				continue;
			else
				findTheSameElem(value, key, valueList, key1);
		}
	}

	private static void findTheSameElem(ArrayList<String> value, String key,
			ArrayList<String> valueList, String key1) throws IOException {
		// TODO Auto-generated method stub
		ArrayList<String> relateColumn = new ArrayList<String>();
		for (String s2 : value) {
			boolean flag = false;
			for (String s1 : valueList) {
				if (s2.equals(s1)) {
					flag = true;
					break;
				}
			}
			if (flag) {
				if (!s2.endsWith("SJ"))
					relateColumn.add(s2 + "(" + columnNameMap.get(key).get(s2)
							+ ")" + "\t");
			}
		}

		if (null != relateColumn && relateColumn.size() > 0) {
			relateBw.append(key + " and " + key1 + " relate column is: " + "\n");
			for (String column : relateColumn) {
				relateBw.append(column);
			}
			relateBw.append("\n"
					+ "========================================================================"
					+ "\n");
			relateBw.flush();
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

	private static void outPutTableColumn(String[][] result) throws IOException {
		// TODO Auto-generated method stub

		tableBw.append(fileName.split("[.]")[0] + "\n");
		int rowLength = result.length;
		int columnLength = result[0].length;

		// for (int i = 0; i < rowLength; i++) {
		if (!fileName.trim().endsWith("发票开具信息.xls".trim())) {
			for (int j = 1; /* ignore first column */j < columnLength/*
																	 * result[i].
																	 * length
																	 */; j++) {
				tableBw.append(result[0][j] + "\t\t" + result[1][j] + "\n");
			}
		} else {
			for (int j = 1; /* ignore first column */j < columnLength/*
																	 * result[i].
																	 * length
																	 */; j++) {
				tableBw.append(result[0][j] + "\t\t" + "\n");
			}
		}

		tableBw.append("============================================================================"
				+ "\n");
		tableBw.flush();

		// }
	}

	private static void outPutDataToTxt(String[][] result) throws IOException {

		FileOutputStream jsonOut = new FileOutputStream("excel/"
				+ fileName.split("[.]")[0] + ".txt");
		BufferedWriter jsonBw = new BufferedWriter(new OutputStreamWriter(
				jsonOut, "utf-8"));

		int rowLength = result.length;
		bw.append(fileName.split("[.]")[0] + "\n");
		for (int i = 0; i < rowLength; i++) {
			Map<String, String> dataMap = new HashMap<String, String>();
			StringBuilder sb = new StringBuilder();

			for (int j = 1; /* ignore first column */j < result[i].length; j++) {
				if (i == 0) {
					bw.append(result[i][j] + "\t\t");
				} else {
					bw.append(result[i][j] + "\t\t");
					dataMap.put(result[0][j], result[i][j]);
					// System.out.print(result[i][j] + "\t\t");
				}
			}
			if (null != dataMap && dataMap.size() > 0)
				jsonBw.append(mapper.writeValueAsString(dataMap) + "\n");
			jsonBw.flush();

			bw.append("\n");
			// System.out.println();
		}
		bw.append("================================================================================================================================================="
				+ "\n");
		bw.flush();

		jsonOut.close();
		jsonBw.close();
	}
}