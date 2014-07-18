package com.tax.elasticsearch.test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class ExcelDataExtractor {
	private static final Log log = LogFactory.getLog(ExcelDataExtractor.class);
	private static BufferedWriter relateBw;
	private static Map<String, HashMap<String, String>> columnNameMap = new HashMap<String, HashMap<String, String>>();

	@SuppressWarnings("deprecation")
	public static List<String[]> getData(File path, int ignoreRows)
			throws FileNotFoundException, IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				path));
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet st = wb.getSheetAt(0); // read first sheet
		String[] fieldNames = null;
		String[] chineseFieldNames = null;
		int columnSize = 0;
		List<String[]> valuesList = new ArrayList<String[]>();
		for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
			HSSFRow row = st.getRow(rowIndex);
			if (row == null)
				continue;
			int tempRowSize = row.getLastCellNum();
			if (tempRowSize > columnSize) {
				columnSize = tempRowSize;
			}
			String[] values = new String[columnSize];
			Arrays.fill(values, "");
			boolean hasValue = false;
			for (short columnIndex = 0; columnIndex <= tempRowSize; columnIndex++) {
				String value = "";
				HSSFCell cell = row.getCell(columnIndex);
				if (cell == null)
					continue;
				cell.setEncoding(HSSFCell.ENCODING_UTF_16);
				switch (cell.getCellType()) {
				case HSSFCell.CELL_TYPE_STRING:
					value = cell.getStringCellValue().trim();
					// log.info(value);
					break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					if (HSSFDateUtil.isCellDateFormatted(cell)) {
						Date date = cell.getDateCellValue();
						if (date != null) {
							value = new SimpleDateFormat("yyyy-MM-dd")
									.format(date);
						}
					} else {
						value = new DecimalFormat("0.00").format(cell
								.getNumericCellValue());
						String field = fieldNames[columnIndex];
						// HSSFRichTextString aa = cell
						// .getRichStringCellValue();
						// String doubleAa = String.valueOf(cell
						// .getNumericCellValue());
						// double doubleAa = cell.getNumericCellValue();
						// log.info(value);
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
				if (value.trim().equals("")) {
					continue;
				}
				values[columnIndex] = rightTrim(value);
				hasValue = true;
			}
			if (hasValue) {
				// List<String> tmpVlaues = new ArrayList<String>();
				if (rowIndex == 0) {
					fieldNames = new String[columnSize];
					for (int i = 0; i < values.length; i++) {
						// log.info(values[i]);
						fieldNames[i] = values[i];
					}
				}
				if (rowIndex == 1) {
					chineseFieldNames = new String[columnSize];
					for (int i = 0; i < values.length; i++) {
						// log.info(values[i]);
						chineseFieldNames[i] = values[i];
					}
				}
				valuesList.add(values);
			}
		}
		in.close();
		Assert.assertEquals(true, valuesList.size() > 0);
		return valuesList;
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