package com.tax.elasticsearch.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.elasticsearch.application.query.SoulFileWriter;
import com.elasticsearch.application.search.ExtractUtil;
import com.splitword.soul.utility.StringUtil;

public class TaxDataTest {
	private static Log log = LogFactory.getLog(TaxDataTest.class);
	private static String dataSourceDir = "加工测试数据2";

	@BeforeClass
	public void startNode() throws Exception {
	}

	@AfterClass
	public void closeResources() {
	}

	@Test(enabled = true)
	public void testMethod1() throws Exception {
		File dir = new File(dataSourceDir);
		SoulFileWriter bufWriter = new SoulFileWriter("excel/result.txt"); // 行数据文件
		SoulFileWriter defWriter = new SoulFileWriter("excel/def.txt");// 表定义文件
		File files[] = dir.listFiles();
		Map<String, File> map = new TreeMap<String, File>();
		for (int j = 0; j < files.length; j++) {
			if (files[j].getName().endsWith("xls")) {
				String fileName = files[j].getName();
				map.put(fileName, files[j]);
			}
		}
		for (String fileName : map.keySet()) {
			log.info(fileName);
			File xlsFile = map.get(fileName);
			List<String[]> result = ExcelDataExtractor.getData(xlsFile, 0);
			List<String> jsonList = TaxDataWriter.tableData(fileName, result);
			for (int i = 0; i < jsonList.size(); i++) {
				bufWriter.writeWithNewLine(jsonList.get(i));
			}
			String json2 = TaxDataWriter.tableDefinition(fileName, result);
			defWriter.writeWithNewLine(json2);
			bufWriter.flush();
			defWriter.flush();
		}
		bufWriter.close();
		defWriter.close();
	}

	// @Test(enabled = true)
	public void testMethod2() throws Exception {
		String path = "excel/tableResult.txt";
		Map<String, List<Map<String, String>>> tableMap = TaxDataReader
				.readTableData(path);
		Map<String, Map<String, String>> defMap = TaxDataReader
				.readDefData("excel/tableDef.txt");
		SoulFileWriter writer = new SoulFileWriter("/tmp/1.txt");
		for (String tableName : tableMap.keySet()) {
			List<Map<String, String>> list = tableMap.get(tableName);
			checkList(writer, tableName, list, defMap);
			writer.flush();
		}
		writer.close();
	}

	// @Test(enabled = true)
	public void testMethod3() throws Exception {
		Map<String, List<Map<String, String>>> tableMap = new HashMap<String, List<Map<String, String>>>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream("/tmp/1.txt"), "UTF-8"));
		String temp = null;
		Map<String, Integer> map = new HashMap<String, Integer>();
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			if (StringUtil.isBlank(temp))
				continue;
			Integer number = map.get(temp);
			if (number == null)
				number = 0;
			map.put(temp, number + 1);
		}
		for (String key : map.keySet()) {
			Integer value = map.get(key);
			if (value > 1) {
				log.info(key);
				log.info(value);
			}
		}
		reader.close();

	}

	private boolean checkThisString(String value) {
		if (StringUtil.isBlank(value))
			return false; // not pass
		boolean bResult = false;
		if (value.contains("月") && value.contains("上午")) {
			bResult = true;
			String result = value.replace("上午", "");
			result = result.replace("-", " ");
			String[] strs = result.split("\\s+");
			Assert.assertEquals(4, strs.length);
			String[] str11s = strs[3].split("[.]");
			Assert.assertEquals(4, str11s.length);
			// Assert.assertEquals(strs[3], "上午");
		}
		if (value.contains("月") && value.contains("下午")) {
			bResult = true;
			String result = value.replace("下午", "");
			result = result.replace("-", " ");
			String[] strs = result.split("\\s+");
			Assert.assertEquals(4, strs.length);
			String[] str11s = strs[3].split("[.]");
			Assert.assertEquals(4, str11s.length);
		}
		if (bResult)
			return false;
		else
			return true;
	}

	private void checkList(SoulFileWriter writer, String tableName,
			List<Map<String, String>> list,
			Map<String, Map<String, String>> typeMaps) {
		log.info(tableName + "," + list.size());
		int totalNumber = list.size();
		Map<String, Set<Integer>> resultMap = new HashMap<String, Set<Integer>>();
		for (int i = 0; i < totalNumber; i++) {
			Map<String, String> entry = list.get(i);
			for (String field : entry.keySet()) {
				if (!field.equalsIgnoreCase("lineNumber")
						&& !field.equalsIgnoreCase("tableName")) {
					String typeKey = tableName + "#" + field;
					Map<String, String> typeMap = typeMaps.get(typeKey);
					if (typeMap.get("dataType").equals("float"))
						continue;
					else if (typeMap.get("dataType").equals("date"))
						continue;
					else if (typeMap.get("dataType").equals("integer"))
						continue;
					String value = entry.get(field).trim();
					if (checkThisString(value)) {
						// String convertedValue =
						// typeMap.get("chineseFieldName")
						// + "#" + value;
						String convertedValue = value;
						Set<Integer> lineSet = resultMap.get(convertedValue);
						if (lineSet == null)
							lineSet = new HashSet<Integer>();
						int lineNumber = Integer.valueOf(entry
								.get("lineNumber"));
						lineSet.add(lineNumber);
						resultMap.put(convertedValue, lineSet);
					}
				}
			}

		}

		List<String> valueList = new LinkedList<String>();
		for (String str1 : resultMap.keySet()) {
			valueList.add(str1);
		}
		for (int i = 0; i < valueList.size(); i++) {
			String str1 = valueList.get(i);
			Set<Integer> set1 = resultMap.get(str1);
			int size1 = set1.size();
			for (int j = i + 1; j < valueList.size(); j++) {
				String str2 = valueList.get(j);
				Set<Integer> set2 = resultMap.get(str2);
				int size2 = set2.size();
				if (Math.abs(size1 - size2) <= 2) {
					if (set1.equals(set2)) {
						Set<String> tmpSet = new TreeSet<String>();
						// log.info(str1 + " , " + str2);
						tmpSet.add(str1);
						tmpSet.add(str2);
						StringBuilder builder = new StringBuilder();
						Iterator<Integer> iter = set1.iterator();
						while (iter.hasNext()) {
							builder.append(iter.next() + " ");
						}
						// log.info(builder.toString());
						writer.writeStr(ExtractUtil.setToString(tmpSet));
					}

				}
			}
		}
	}

}
