package com.tax.elasticsearch.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

public class TaxDataWriter {
	private static final Log log = LogFactory.getLog(TaxDataWriter.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String splitTag = "[.]";

	public static List<String> tableData(String fileName, List<String[]> result)
			throws IOException {
		String[] parts = fileName.split(splitTag);
		log.info(fileName);
		List<String> list = new ArrayList<String>();
		String[] values0 = result.get(0);
		String[] values1 = result.get(1);
		String[] values2 = result.get(2);
		for (int j = 0; j < values0.length; j++) {
			if (values0[j].equals("")) {
				Assert.assertEquals(values1[j], "");
				Assert.assertEquals(values2[j], "");
			}
			if (values1[j].equals("")) {
				Assert.assertEquals(values0[j], "");
				Assert.assertEquals(values2[j], "");
			}
		}
		for (int i = 3; i < result.size(); i++) {
			Map<String, String> tmp = new HashMap<String, String>();
			String[] valueII = result.get(i);
			for (int j = 0; j < valueII.length; j++) {
				String fieldName = values0[j];
				if (fieldName.equals(""))
					continue;
				String value = valueII[j];
				// if (values2[j].equals("date"))
				// log.info(values0[j] + "," + values1[j] + "," + value);
				// if (value.endsWith(".00") && !values2[j].equals("string")) {
				// log.info(values0[j] + "," + values1[j] + "," + value);
				// if (!values2[j].equals("integer")
				// && !values2[j].equals("float"))
				// Assert.assertEquals(1, 0);
				// }
				if (value.contains(".00") && values2[j].equals("string")) {
					if (!values1[j].equals("税务管理码")
							&& !values1[j].equals("身份证号码")
							&& !values1[j].equals("证件号")
							&& !values1[j].equals("付款方管理码")
							&& !values1[j].equals("收款方管理码"))
						log.info(values0[j] + "," + values1[j] + "," + value);
					Assert.assertEquals(true, value.endsWith(".00"));
					value = value.substring(0, value.length() - 3);
				}
				tmp.put(fieldName, value);
			}
			tmp.put("tableName", parts[0]);
			tmp.put("lineNumber", String.valueOf(i - 2));
			String json = mapper.writeValueAsString(tmp);
			list.add(json);
		}
		return list;
	}

	public static String tableDefinition(String fileName, List<String[]> result)
			throws IOException {
		String[] parts = fileName.split(splitTag);
		String[] values0 = result.get(0);
		String[] values1 = result.get(1);
		String[] values2 = result.get(2);
		Map<String, Object> map2 = new HashMap<String, Object>();
		for (int i = 0; i < values0.length; i++) {
			String fieldName = values0[i];
			if (fieldName.equals(""))
				continue;
			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put("chineseFieldName", values1[i]);
			tmp.put("dataType", values2[i]);
			map2.put(fieldName, tmp);
		}
		map2.put("chineseTableName", parts[1]);
		// map2.put("column", list);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(parts[0], map2);
		String json = mapper.writeValueAsString(map);
		return json;
	}
}
