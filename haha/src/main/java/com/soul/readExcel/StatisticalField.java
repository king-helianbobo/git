package com.soul.readExcel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class StatisticalField {

	private static Map<String, Integer> fieldMap = new HashMap<String, Integer>();
	private static Map<String,String> fieldNameMap = new HashMap<String,String>();
	private static Map<String,ArrayList<String>> fieldTableMap = new HashMap<String,ArrayList<String>>();

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		
		listDirectory("/home/jacky/文档/开发测试数据");
		
		fieldMap = sortMap(fieldMap);
		
		FileOutputStream tableOut = new FileOutputStream("table/column/statisticalField.txt");
		BufferedWriter tableBw = new BufferedWriter(new OutputStreamWriter(tableOut, "utf-8"));
		
		 for (Entry<String, Integer> entry : fieldMap.entrySet()) {
			 //System.out.println("field: " + entry.getKey() + "(" + fieldNameMap.get(entry.getKey()) + ")" + "'s total sum is " + entry.getValue());
			 
			 tableBw.append("field: " + entry.getKey() + "(" + fieldNameMap.get(entry.getKey()) + ")" + "'s frequency of occurrence is " + entry.getValue() + "\n");
		
			 ArrayList<String> tableList = fieldTableMap.get(entry.getKey());
			 tableBw.append("the field appear in these table: ");
			 if(null != tableList && tableList.size() > 0){
				 for(String tableName : tableList){
					tableBw.append(tableName + "\t\t");
				 }
			 }
			 
			 tableBw.append("\n" + "================================================================================================================" + "\n");
		 }
		 tableBw.flush();
		 tableOut.close();
		 tableBw.close();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map sortMap(Map oldMap) {
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				oldMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<java.lang.String, Integer> arg0,
					Entry<java.lang.String, Integer> arg1) {
				return arg1.getValue() - arg0.getValue();
			}
		});
		Map newMap = new LinkedHashMap();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;
	}

	private static void listDirectory(String path)
			throws FileNotFoundException, IOException {
		File dir = new File(path);
		File file[] = dir.listFiles();
		for (int j = 0; j < file.length; j++) {
			if (file[j].isDirectory()) {
				listDirectory(file[j].getAbsolutePath());
			} else if (file[j].getName().endsWith("xls") && !file[j].getName().trim().endsWith("发票开具信息.xls".trim())) {
				String[][] result = getData(file[j], 0);
				String tableName = file[j].getName().split("[.]")[0].trim();
				int columnLength = result[0].length;
				int rowLength = result.length;
				//for (int r = 0; r < rowLength; r++) {
					for (int n = 1; /*ignore first column*/ n < columnLength/*result[r].length*/; n++) { 
						
						if(!fieldNameMap.containsKey(result[1][n])){
							fieldNameMap.put(result[0][n], result[1][n]);
						}
						
						ArrayList<String> tableList = fieldTableMap.get(result[0][n]);
						if(null == tableList){
							tableList = new ArrayList<String>();
							tableList.add(tableName);
							fieldTableMap.put(result[0][n], tableList);
						}else{
							if(!tableList.contains(tableName))
								tableList.add(tableName);
							
							fieldTableMap.put(result[0][n], tableList);
						}
						
						Object o = fieldMap.get(result[0][n]);
						if(null != o){
							int temp = Integer.valueOf(fieldMap.get(result[0][n]))+1;
							fieldMap.put(result[0][n], temp);
						}else{
							fieldMap.put(result[0][n], 1);
						}
					}
				//}
				
			} else {
				System.out.println("do nothing");
			}
		}
	}

	@SuppressWarnings("deprecation")
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

}
