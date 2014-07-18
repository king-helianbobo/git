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
import java.util.Collections;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.codehaus.jackson.map.ObjectMapper;

public class ExtractExcelDataKaka {

	private static FileOutputStream out;
	private static FileOutputStream tableOut;
	private static FileOutputStream relateOut;
	private static FileOutputStream keyOut;
	private static BufferedWriter bw;
	private static BufferedWriter tableBw;
	private static BufferedWriter relateBw;
	private static BufferedWriter keyBw;
	private static String fileName = "";
	private static ObjectMapper mapper = new ObjectMapper();
	private static Map<String, ArrayList<String>> tableMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, HashMap<String, String>> columnNameMap = new HashMap<String, HashMap<String, String>>();
	private static Map<String, HashMap<String, ArrayList<String>>> columnMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	private static Map<String, ArrayList<String>> columnNotInOneKeyMap = new HashMap<String, ArrayList<String>>();

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

			keyOut = new FileOutputStream("excel/primaryKey.txt");
			keyBw = new BufferedWriter(new OutputStreamWriter(keyOut, "utf-8"));

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		listDirectory("开发测试数据");

		generateRelateFile(tableMap);

		generatePriKeyFile(columnMap);

		close();
	}

	public static String[][] getData(File file, int ignoreRows)
			throws FileNotFoundException, IOException {
		List<String[]> result = new ArrayList<String[]>();
		int columnSize = 0;
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFCell cell = null;
		// for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets();
		// sheetIndex++) {
		HSSFSheet st = wb.getSheetAt(/* sheetIndex */0); // read only first sheet
		for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
			HSSFRow row = st.getRow(rowIndex);
			if (row == null) {
				continue;
			}
			int tempRowSize = row.getLastCellNum();
			if (tempRowSize > columnSize) {
				columnSize = tempRowSize;
			}
			String[] values = new String[columnSize];
			Arrays.fill(values, "");
			boolean hasValue = false;
			for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
				String value = "";
				cell = row.getCell(columnIndex);
				if (cell != null) {
					cell.setEncoding(HSSFCell.ENCODING_UTF_16);
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_STRING:
						value = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						if (HSSFDateUtil.isCellDateFormatted(cell)) {
							Date date = cell.getDateCellValue();
							if (date != null) {
								value = new SimpleDateFormat("yyyy-MM-dd")
										.format(date);
							} else {
								value = "";
							}
						} else {
							value = new DecimalFormat("0").format(cell
									.getNumericCellValue());
						}
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						if (!cell.getStringCellValue().equals("")) {
							value = cell.getStringCellValue();
						} else {
							value = cell.getNumericCellValue() + "";
						}
						break;
					case HSSFCell.CELL_TYPE_BLANK:
						break;
					case HSSFCell.CELL_TYPE_ERROR:
						value = "";
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN:
						value = (cell.getBooleanCellValue() == true ? "Y" : "N");
						break;
					default:
						value = "";
					}
				}
				if (/* columnIndex == 0 && */value.trim().equals("")) {
					// break;
					continue;
				}
				values[columnIndex] = rightTrim(value);
				hasValue = true;
			}
			if (hasValue) {
				result.add(values);
			}
		}
		// }

		in.close();
		String[][] returnArray = new String[result.size()][columnSize];
		for (int i = 0; i < returnArray.length; i++) {
			returnArray[i] = (String[]) result.get(i);
		}
		return returnArray;
	}

	private static String rightTrim(String str) {
		if (str == null) {
			return "";
		}
		int length = str.length();
		for (int i = length - 1; i >= 0; i--) {
			if (str.charAt(i) != 0x20) {
				break;
			}
			length--;
		}
		return str.substring(0, length);
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

			if (keyOut != null)
				keyOut.close();
			if (keyBw != null)
				keyBw.close();

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
				String[][] result = getData(file[j], 0);

				// for(int i=0;i<result.length;i++){
				// System.out.println(result[i].length);
				// }

				outPutDataToTxt(result);

				outPutTableColumn(result);

				dealTableRelate(result);

				dealColumnStringMap(result);

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

			compare(entry.getValue(), tableMap2, key);
			it.remove(); // OK
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
		if (!fileName.trim().endsWith("05发票开具信息.xls".trim())) {
			for (int j = 1; /* ignore first column */j < columnLength/*
																	 * result[i].
																	 * length
																	 */; j++) {
				columnList.add(result[0][j]);
				columnMap.put(result[0][j], result[1][j]);
			}
			columnNameMap.put(key, columnMap);
			tableMap.put(key, columnList);

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
		if (!fileName.trim().endsWith("05发票开具信息.xls".trim())) {
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

	/**
	 * 产生所有表的所有主键的输出文件
	 * 
	 * @param columnMap2
	 * @throws IOException
	 */
	private static void generatePriKeyFile(
			Map<String, HashMap<String, ArrayList<String>>> columnMap2)
			throws IOException {
		// TODO Auto-generated method stub
		Iterator<Map.Entry<String, HashMap<String, ArrayList<String>>>> itOut = columnMap2
				.entrySet().iterator();
		while (itOut.hasNext()) {
			Map.Entry<String, HashMap<String, ArrayList<String>>> entryOut = itOut
					.next();
			String fileKey = entryOut.getKey();
			isPrimaryKey1(entryOut.getValue(), fileKey);
			isPrimaryKey2(entryOut.getValue(), fileKey);
			itOut.remove(); // OK
		}

	}

	/**
	 * 找出指定表存在的单一主键
	 * 
	 * @param fileKeyValue
	 * @param fileKey
	 * @throws IOException
	 */
	private static void isPrimaryKey1(
			HashMap<String, ArrayList<String>> fileKeyValue, String fileKey)
			throws IOException {
		Iterator<Map.Entry<String, ArrayList<String>>> itIn = fileKeyValue
				.entrySet().iterator();
		ArrayList<String> columnNotInOneKeyList = new ArrayList<String>();
		if (fileKey != null)
			keyBw.append(fileKey + "单一主键有:" + "\n");

		while (itIn.hasNext()) {
			Map.Entry<String, ArrayList<String>> entryIn = itIn.next();
			String columnKey = entryIn.getKey();

			if (compareString(entryIn.getValue(), fileKey, columnKey,
					columnNotInOneKeyList))
				outPutKeyToTxt(columnKey);
		}

		columnNotInOneKeyMap.put(fileKey, columnNotInOneKeyList);

		keyBw.append("\n"
				+ "-------------------------------------------------------------------"
				+ "\n");
		keyBw.flush();
	}

	/**
	 * 找出指定表存在的二主键
	 * 
	 * @param fileKeyValue
	 * @param fileKey
	 * @throws IOException
	 */
	private static void isPrimaryKey2(
			HashMap<String, ArrayList<String>> fileKeyValue, String fileKey)
			throws IOException {
		// Iterator<Map.Entry<String, ArrayList<String>>> itIn = fileKeyValue
		// .entrySet().iterator();
		if (fileKey != null)
			keyBw.append(fileKey + "二主键有:" + "\n");

		String[] columnNameStr = columnMapNotInOneKeyToStr(
				columnNotInOneKeyMap, fileKey);
		int length = columnNameStr.length;
		for (int i = length - 1; i >= 1; i--) {
			ArrayList<String> list1 = fileKeyValue.get(columnNameStr[i]);
			for (int j = i - 1; j >= 0; j--) {
				ArrayList<String> list2 = fileKeyValue.get(columnNameStr[j]);
				ArrayList<String> list = merge2List(list1, list2);
				if (compareMerge2String(list)) {
					String keyWithTwo = "(" + list1.get(0) + "(" + list1.get(1)
							+ ")" + "," + list2.get(0) + "(" + list2.get(1)
							+ ")" + ")";
					outPutKeyToTxt(keyWithTwo);
				}
			}
		}

		keyBw.append("\n"
				+ "-------------------------------------------------------------------"
				+ "\n");

		keyBw.append("====================================================================================================================================================="
				+ "\n");
		keyBw.flush();
	}

	/**
	 * 连接两个ArrayList
	 * 
	 * @param list
	 * @return
	 */
	private static boolean compareMerge2String(ArrayList<String> list) {
		ArrayList<String> columnValueCopy = (ArrayList<String>) list.clone();
		Collections.sort(columnValueCopy);
		for (int i = 4; i < columnValueCopy.size(); i++) {
			String str1 = (String) columnValueCopy.get(i - 1);
			String str2 = (String) columnValueCopy.get(i);
			if (str1.trim().equals(str2.trim())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 连接两个ArrayList
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	private static ArrayList<String> merge2List(ArrayList<String> list1,
			ArrayList<String> list2) {
		ArrayList<String> mergels = new ArrayList<>();
		for (int i = 0; i < list1.size(); i++) {
			String tempStr = list1.get(i) + "," + list2.get(i);
			mergels.add(tempStr);
		}
		return mergels;
	}

	/**
	 * 将Map<String, ArrayList<String>>中的 ArrayList<String>转化String
	 * 
	 * @param columnMapNotInOneKey
	 * @param fileKey
	 * @return
	 */
	private static String[] columnMapNotInOneKeyToStr(
			Map<String, ArrayList<String>> columnMapNotInOneKey, String fileKey) {
		ArrayList<String> list = columnMapNotInOneKey.get(fileKey);
		int size = list.size();
		String[] columnNameStr = new String[size];
		for (int i = 0; i < size; i++) {
			columnNameStr[i] = list.get(i);
		}

		return columnNameStr;
	}

	/**
	 * 判断制定ArrayList中的字符串是否都是不同的
	 * 
	 * @param columnValue
	 * @param fileKey
	 * @param columnKey
	 * @return
	 * @throws IOException
	 */
	private static boolean compareString(ArrayList<String> columnValue,
			String fileKey, String columnKey,
			ArrayList<String> columnNotInOneList) throws IOException {
		String columnType = columnValue.get(2).trim();
		if (columnType.equals("date") || columnType.equals("float")
				|| columnType.equals("integer")) {
			return false;
		}
		ArrayList<String> columnValueCopy = (ArrayList<String>) columnValue
				.clone();
		Collections.sort(columnValueCopy);
		// int a=columnValue.size();
		for (int i = 4; i < columnValueCopy.size(); i++) {
			String str1 = (String) columnValueCopy.get(i - 1);
			String str2 = (String) columnValueCopy.get(i);
			if (str1.trim().equals(str2.trim())) {
				columnNotInOneList.add(columnKey);
				return false;
			}
		}

		return true;
	}

	private static void outPutKeyToTxt(String columnKey) throws IOException {
		if (columnKey != null) {
			keyBw.append(columnKey + "\t");
		}
	}

	/**
	 * 产生指定表的列名=该列的字符串生成ArrayList的Map
	 * 
	 * @param result
	 * @throws IOException
	 */
	private static void dealColumnStringMap(String[][] result)
			throws IOException {
		// TODO Auto-generated method stub
		int rowLength = result.length;
		int columnLength = result[0].length;
		String fileKey = fileName.split("[.]")[0].trim();

		HashMap<String, ArrayList<String>> columnPriKeyMap = new HashMap<String, ArrayList<String>>();
		for (int j = 1; /* ignore first column */j < columnLength; j++) {
			ArrayList<String> columnStrList = new ArrayList<String>();
			String priKey = result[0][j] + "(" + result[1][j] + ")";
			for (int i = 0; i < rowLength; i++) {
				columnStrList.add(result[i][j]);
			}
			columnPriKeyMap.put(priKey, columnStrList);
			// columnStrList.clear();
		}
		columnMap.put(fileKey, columnPriKeyMap);
	}
}
