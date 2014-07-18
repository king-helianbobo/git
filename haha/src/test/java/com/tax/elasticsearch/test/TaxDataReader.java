package com.tax.elasticsearch.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import com.splitword.soul.utility.StringUtil;

public class TaxDataReader {
	private static ObjectMapper mapper = new ObjectMapper();

	public static Map<String, List<Map<String, String>>> readTableData(
			String path) throws JsonParseException, JsonMappingException,
			IOException {
		Map<String, List<Map<String, String>>> tableMap = new HashMap<String, List<Map<String, String>>>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), "UTF-8"));
		String temp = null;
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			if (StringUtil.isBlank(temp))
				continue;
			JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
					temp);
			@SuppressWarnings("unchecked")
			Map<String, String> entry = mapper.readValue(jsonParser, Map.class);
			String tableName = entry.get("tableName");
			List<Map<String, String>> list = tableMap.get(tableName);
			if (list == null)
				list = new LinkedList<Map<String, String>>();
			list.add(entry);
			tableMap.put(tableName, list);
		}
		reader.close();
		return tableMap;
	}

	public static Map<String, Map<String, String>> readDefData(String path)
			throws JsonParseException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), "UTF-8"));
		String temp = null;
		Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			if (StringUtil.isBlank(temp))
				continue;
			JsonParser jsonParser = mapper.getJsonFactory().createJsonParser(
					temp);
			@SuppressWarnings("unchecked")
			Map<String, Map<String, Object>> entry = mapper.readValue(
					jsonParser, Map.class);
			int i = 0;
			for (String tableName : entry.keySet()) {
				Map<String, Object> valueMap = entry.get(tableName);
				for (String field : valueMap.keySet()) {
					if (field.equals("chineseTableName")
							|| field.equals("lineNumber"))
						continue;
					String str1 = tableName + "#" + field;
					@SuppressWarnings("unchecked")
					Map<String, String> map = (Map<String, String>) valueMap
							.get(field);
					resultMap.put(str1, map);
				}
				i++;
			}
			Assert.assertEquals(i, 1);
		}
		reader.close();
		return resultMap;
	}
}
