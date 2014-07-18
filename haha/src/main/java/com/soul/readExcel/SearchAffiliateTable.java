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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.splitword.lionsoul.jcseg.util.ChineseHelper;

public class SearchAffiliateTable {
	
	private static Map<String,String> attachedTable = new HashMap<String,String>();
	// the value is the relationship between main table and attached table
	private static Map<String,String> mainTable = new HashMap<String,String>(); 
	private static Map<String, ArrayList<String>> tableMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> tableMap_AB = new HashMap<String, ArrayList<String>>();
	private static Map<String,String> tableSignatureMap = new HashMap<String,String>();
	private static Map<String,HashMap<String,String>> columnSignatureMap = new HashMap<String,HashMap<String,String>>();
	private static Map<String,HashMap<String,ArrayList<String>>> fieldData = new HashMap<String,HashMap<String,ArrayList<String>>>();
	private static final String strType = "string";
	private static List<String> synonymyFieldList = new ArrayList<String>();
	private static Map<String,String> tableKey = new HashMap<String,String>();
	private static FileOutputStream relateOut;
	private static BufferedWriter relateBw;
	
	
	private static String fileName = "";
	
	static{
		try {
			
			relateOut = new FileOutputStream("加工测试数据3/relate3.txt");
			relateBw = new BufferedWriter(new OutputStreamWriter(relateOut, "utf-8"));
			
			FileInputStream tableIn = new FileInputStream("table/key.txt");
			BufferedReader tableBr = new BufferedReader(new InputStreamReader(tableIn, "utf-8"));
			String lineTxt = null;

			while ((lineTxt = tableBr.readLine()) != null) {
				if(!"".equals(lineTxt)){
					String[] temp = lineTxt.split("[=]");
					tableKey.put(temp[0], temp[1]);
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
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		listDirectory("加工测试数据3");
		
		streamlineTableColumn();
		
		
		
		//checkClomunSize();
		
		//fillMainTable();
		
		checkRelationshipBetweenTable();
		
		checkRetionshipBetween_A();
		
		findSynonymyField();
		
		
		/*for(String s : synonymyFieldList){
			relateBw.append(s + "\n");
			relateBw.flush();
		}*/
		
		/*System.out.println("main table: ");
		
		for(Entry<String, String> entry : mainTable.entrySet()){
			System.out.print(entry.getKey() + "\t" + "(" + entry.getValue() + ")" + "\n");
		}
		
		System.out.println();
		
		System.out.println("attached table: ");
		
		for(Entry<String, String> entry : attachedTable.entrySet()){
			System.out.print(entry.getKey() + "\t");
		}*/
		
		close();
	}
	
	private static void checkRetionshipBetween_A() {
		// TODO Auto-generated method stub
		tableMap_AB.remove("A02"); //special
		
		
		/*for(Entry<String, ArrayList<String>> entry : tableMap_AB.entrySet()){
			tableMap.remove(entry.getKey().trim());
			
		}*/
		Map<String, ArrayList<String>> tempTableMap = new HashMap<String,ArrayList<String>>();
		tempTableMap.putAll(tableMap);
		for(Entry<String, ArrayList<String>> entry : tableMap.entrySet()){
			String tableSignature = entry.getKey();
			//System.out.println(entry.getKey());
			checkRetionshipBetweenInternal_A(tableSignature,tempTableMap);
		}

		for(Entry<String, ArrayList<String>> entry : tempTableMap.entrySet()){
			String tableSignature = entry.getKey();
			if(null != tableMap_AB.get(entry.getKey()))
				System.out.println(tableSignature + "(" + tableSignatureMap.get(tableSignature) + ")" + " has relationship with B");
			else
				System.out.println("no relationship: " + tableSignature + "(" + tableSignatureMap.get(tableSignature) + ")");
		}
	}

	private static void checkRetionshipBetweenInternal_A(String tableSignature,Map<String, ArrayList<String>> tempTableMap) {
		// TODO Auto-generated method stub
		ArrayList<String> columnList = tableMap.get(tableSignature);
		
		for(Entry<String, ArrayList<String>> entry : tableMap.entrySet()){
			String tableSignature_1 = entry.getKey();
			ArrayList<String> columnList_1 = entry.getValue();
			if(!tableSignature.trim().equals(tableSignature_1.trim())){
				List<String> sameEles = Utilitys.sameElement(columnList, columnList_1);

				if( null != sameEles && sameEles.size() > 0){
					
					System.out.println(tableSignature + "(" + tableSignatureMap.get(tableSignature) + ")"
															+ " has relationship with " + tableSignature_1 + "(" + tableSignatureMap.get(tableSignature_1) + ")");
					System.out.println(" by these fields : ");
					for(String s : sameEles){
						System.out.print(s + "\t");
					}
					System.out.println();
					
					tempTableMap.remove(tableSignature);
				}else{
					//System.out.println("no relationship : " + tableSignature);
					continue;
				}
			}
		}
	}

	/**
	 * find the synonymy field
	 */
	private static void findSynonymyField() {
		// TODO Auto-generated method stub
		for(Entry<String,HashMap<String,ArrayList<String>>> entry : fieldData.entrySet()){
			String tableName = entry.getKey();
			findSynonymyFieldInternal(tableName);
		}
	}

	private static void findSynonymyFieldInternal(String tableName) {
		// TODO Auto-generated method stub
		HashMap<String,ArrayList<String>> columnMap = fieldData.get(tableName);
		for(Entry<String,HashMap<String,ArrayList<String>>> entry : fieldData.entrySet()){
			String tableName_1 = entry.getKey();
			HashMap<String,ArrayList<String>> columnMap_1 = fieldData.get(tableName_1);
			if(!tableName.trim().equals(tableName_1.trim()))
				synonymyInvoke(tableName,columnMap,tableName_1,columnMap_1);
		}
	}

	private static void synonymyInvoke(String tableName,
			HashMap<String, ArrayList<String>> columnMap, String tableName_1,
			HashMap<String, ArrayList<String>> columnMap_1) {
		// TODO Auto-generated method stub
		
		for(Entry<String,ArrayList<String>> entry : columnMap.entrySet()){
			String column = entry.getKey();
			ArrayList<String> dataList = entry.getValue();
			synonymyInvoke_1(tableName,column,dataList,tableName_1,columnMap_1);
		}
	}

	private static void synonymyInvoke_1(String tableName,String column,ArrayList<String> dataList,
			String tableName_1,HashMap<String,ArrayList<String>> columnMap_1) {
		// TODO Auto-generated method stub
		for(Entry<String,ArrayList<String>> entry : columnMap_1.entrySet()){
			String column_1 = entry.getKey();
			ArrayList<String> dataList_1 = entry.getValue();
			
			if(Utilitys.isSubsetOf(dataList, dataList_1)){
				if(!column.trim().equals(column_1.trim()))
					synonymyFieldList.add(tableName + ": " + column + "(" + columnSignatureMap.get(tableName).get(column) + ")" + "=" + tableName_1 + ": " + column_1 + "(" + columnSignatureMap.get(tableName_1).get(column_1) + ")");
			}else if(Utilitys.isSubsetOf(dataList_1, dataList)){
				if(!column.trim().equals(column_1.trim()))
					synonymyFieldList.add(tableName + ": " + column + "(" + columnSignatureMap.get(tableName).get(column) + ")" + "=" + tableName_1 + ": " + column_1 + "(" + columnSignatureMap.get(tableName_1).get(column_1) + ")");
			}
		}
	}

	private static void checkRelationshipBetweenTable() throws IOException {
		// TODO Auto-generated method stub
		for(Entry<String, String> entry : tableKey.entrySet()){
			String tableName = entry.getKey();
			
			tableMap.remove(tableName);
		}
		
		for(Entry<String, ArrayList<String>> entry : tableMap.entrySet()){
			String tableName_A = entry.getKey();
			HashMap<String,ArrayList<String>> columnDataList = fieldData.get(tableName_A);
			
			checkRelationshipBetweenTable_1(columnDataList,tableName_A);
		}
	}


	private static void checkRelationshipBetweenTable_1(
			HashMap<String, ArrayList<String>> columnDataList,String tableName_A) throws IOException {
		// TODO Auto-generated method stub
		for(Entry<String, String> entry : tableKey.entrySet()){
			String tableName_B = entry.getKey();
			ArrayList<String> dataList = fieldData.get(tableName_B).get(entry.getValue().trim());
			
			if(doDeal(tableName_B,dataList,tableName_A,columnDataList)){
				tableMap_AB.put(tableName_A, tableMap.get(tableName_A));
				//tableMap.remove(tableName_A);
				
				return;
			}
		}
	}

	private static boolean doDeal(String tableName_B, ArrayList<String> dataList,
			String tableName_A,
			HashMap<String, ArrayList<String>> columnDataList) throws IOException {
		// TODO Auto-generated method stub
		for(Entry<String, ArrayList<String>> entry : columnDataList.entrySet()){
			String columnName_A = entry.getKey();
			ArrayList<String> dataList_A = entry.getValue();
			String columnName_B = tableKey.get(tableName_B);
			
			if(Utilitys.isSubsetOf(dataList_A, dataList)){
				/*relateBw.append(tableName_A + "(" + tableSignatureMap.get(tableName_A) + ")" + " # " + columnName_A + "(" + columnSignatureMap.get(tableName_A).get(columnName_A) + ")" 
									+ " , " + tableName_B + "(" + tableSignatureMap.get(tableName_B) + ")" + columnName_B + "(" + columnSignatureMap.get(tableName_B).get(columnName_B) + ")");
				relateBw.append("\n");
				
				relateBw.flush();*/
				return true;
			}
		}
		return false;
	}

	/**
	 * According the current attached table find the main table(if the table have the column 
	 * that in some attached table,then think the table is main table); 
	 */
	private static void fillMainTable() {
		// TODO Auto-generated method stub
		for(Entry<String,String> entry : attachedTable.entrySet()){
			String tableName = entry.getKey();
			String columnName = entry.getValue();
			
			fillMainTableInternal(tableName,columnName);
		}
	}

	private static void fillMainTableInternal(String tableName,
			String columnName) {
		// TODO Auto-generated method stub
		
		Iterator<Map.Entry<String, ArrayList<String>>> it = tableMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = it.next();
			String mainTableName = entry.getKey();
			ArrayList<String> columnList = entry.getValue();
			if (null != columnList && columnList.contains(columnName)) {
				mainTable.put(mainTableName, mainTableName + "#" + columnName + " , " + tableName + "#" + columnName);
				//it.remove(); //remove
			}
		}
	}

	/**
	 * if the table column size is one,then think this table is attached table and remove from the tableMap
	 */
	private static void checkClomunSize() {
		// TODO Auto-generated method stub
		Iterator<Map.Entry<String, ArrayList<String>>> it = tableMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = it.next();
			String tableName = entry.getKey();
			ArrayList<String> columnList = entry.getValue();
			if (columnList.size() == 1) {
				attachedTable.put(tableName, columnList.get(0));
				it.remove(); //remove
			}
		}
	}

	private static void streamlineTableColumn() {
		// TODO Auto-generated method stub
		for (Entry<String,HashMap<String,ArrayList<String>>> entry : fieldData.entrySet()) {
			String tableName = entry.getKey();
			HashMap<String,ArrayList<String>> dataMap = entry.getValue();
			
			streamlineTableColumn_1(tableName,dataMap);
		}
	}

	private static void streamlineTableColumn_1(String tableName,
			HashMap<String, ArrayList<String>> dataMap) {
		// TODO Auto-generated method stub
		for (Entry<String, ArrayList<String>> entry : dataMap.entrySet()) {
			String columnName = entry.getKey();
			ArrayList<String> columnDataList = entry.getValue();
			
			if(!isLegal(columnDataList)){
				tableMap.get(tableName).remove(columnName);
			}
		}
	}

	private static boolean isLegal(ArrayList<String> columnDataList) {
		// TODO Auto-generated method stub
		if(null == columnDataList || columnDataList.size() < 0)
			return false;
		for(String data : columnDataList){
			if(ChineseHelper.containChineseChar(data))
				return false;
			else
				continue;
		}
		
		return true;
	}

	private static void listDirectory(String path)
			throws FileNotFoundException, IOException {
		File dir = new File(path);
		File file[] = dir.listFiles();
		for (int j = 0; j < file.length; j++) {
			if (file[j].isDirectory()) {
				listDirectory(file[j].getAbsolutePath());
			} else if (file[j].getName().endsWith("xls")) {
				String[][] result = Utilitys.getData(file[j], 0);
				fileName = file[j].getName();
            	
            	dealResult(result);
				
			} 
		}
	}

	private static void dealResult(String[][] result) {
		int rowLength = result.length;
		int columnLength = result[0].length;
		String tableName = fileName.split("[.]")[1].trim();
		String tableSingnature = fileName.split("[.]")[0].trim();
		
		tableSignatureMap.put(tableSingnature, tableName);
		
		ArrayList<String> columnList = new ArrayList<String>();
		ArrayList<String> dataList = null;
		HashMap<String,ArrayList<String>> dataMap = new HashMap<String,ArrayList<String>>();
		HashMap<String,String> columnMap = new HashMap<String,String>();
		for (int i = 0; i < rowLength; i++) {
			for (int j = 1; /*ignore first column*/ j < columnLength/*result[i].length*/; j++) {
				if(i ==0 ){
					if(null != result[2][j] && strType.equals(result[2][j]))
						columnList.add(result[0][j]);
				}
				
				if(i == 1){
					columnMap.put(result[0][j], result[1][j]);
				}
				
				if(i >= 3){
					if(null != result[2][j] && strType.equals(result[2][j])){
						if(null == dataMap.get(result[0][j])){
							dataList = new ArrayList<String>();
							dataList.add(result[i][j]);
							dataMap.put(result[0][j], dataList);
						}else{
							dataList = dataMap.get(result[0][j]);
							dataList.add(result[i][j]);
							dataMap.put(result[0][j], dataList);
						}
					}
				}
			}
	}
		tableMap.put(tableSingnature, columnList);
		fieldData.put(tableSingnature,dataMap);
		columnSignatureMap.put(tableSingnature, columnMap);
}

	private static boolean isExclude(String column) {
		// TODO Auto-generated method stub
		return Utilitys.excludeFields.contains(column);
	}
	
	private static void close() throws IOException{
		if(relateOut != null)
			relateOut.close();
		if(relateBw != null)
			relateBw.close();
	}
}
