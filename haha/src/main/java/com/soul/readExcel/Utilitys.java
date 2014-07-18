package com.soul.readExcel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class Utilitys {
	public static final ArrayList<String> excludeFields = new ArrayList<String>();
	
	static{
		excludeFields.add("MC");
		excludeFields.add("MC_J");
		excludeFields.add("LR_SJ");
		excludeFields.add("XG_SJ");
		excludeFields.add("XY_BJ");
	}
	
	
	/**
	 * remove list duplicate elements  
	 * @param list
	 * @return
	 */
	public static ArrayList<String> removeDuplicate4List(ArrayList<String> list) {
		HashSet<String> set = new HashSet<String>(list);
		list.clear();
		list.addAll(set);

		return list;
	}
	
	/**
	 * obtain same elements from list1 and list2
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static List<String> sameElement(ArrayList<String> list1,
			ArrayList<String> list2) {
		// TODO Auto-generated method stub
		
		if(null == list1 || list1.size() < 0 || null == list2 || list2.size() < 0)
			return null;
		
		List<String> temp = new ArrayList<String>(list1);
		temp.retainAll(list2);
		
		return temp;
	}
	
	/**
	 * check the list1 is subset of list2
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static boolean isSubsetOf(ArrayList<String> list1,ArrayList<String> list2){
		List<String> sameEles = sameElement(list1,list2);
		if(null == sameEles || sameEles.size() <0)
			return false;
		
		ArrayList<String> removeDup1 = removeEmptyElement(removeDuplicate4List(list1));
		ArrayList<String> removeDup2 = removeEmptyElement(removeDuplicate4List(list2));
		
		if(removeDup1.size() > removeDup2.size())
			return false;
		
		for(String s : removeDup1){
			if(removeDup2.contains(s))
				continue;
			else
				return false;
		}
		return true;
	}
	
	/**
	 * read data from excel(the excel version is 2003)
	 * @param file
	 * @param ignoreRows
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static String[][] getData(File file, int ignoreRows)
			throws FileNotFoundException, IOException {
		List<String[]> result = new ArrayList<String[]>();
		int columnSize = 0;
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));
		Map<Integer,String> columnType = new HashMap<Integer,String>();
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
			for (int columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
				String value = "";
				String type = columnType.get(columnIndex);
				
				cell = row.getCell(columnIndex);
				if (cell != null) {
					cell.setEncoding(HSSFCell.ENCODING_UTF_16);
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_STRING:
						if(null != type && "date".equals(type.trim())){
							String tempValue = cell.getStringCellValue();
							if(tempValue.contains("月") && (tempValue.contains("上午") || tempValue.contains("下午"))){
								String[] temp = tempValue.split("\\s+");
								if(temp.length == 4){
									String[] dayAndMonth = temp[0].split("[-]");
									String year = "20" + temp[1].replace("-", "").trim();
									String[] tempHour = temp[2].split("[.]");
									String hour = tempHour[0] + ":" + tempHour[1] + ":" + tempHour[2];
									String tag = getTag(temp[3].trim());
									
									value = year + "-" + dayAndMonth[1].replace("月", "") + "-" + dayAndMonth[0] + " " + hour + " " + tag;
								}
							}
							
							value = cell.getStringCellValue();
						}else
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
							if(null != type && "float".equals(type.trim())){
								value = new DecimalFormat("0.00").format(cell
										.getNumericCellValue());
							}else if(null != type && "date".equals(type.trim())){
								String tempValue = cell.getDateCellValue().toLocaleString();
								DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd");            
					            Date date;
								try {
									date = fmt.parse(tempValue);
									value = fmt.format(date);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}else {
								value = new DecimalFormat("0").format(cell
										.getNumericCellValue());
							}
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
				
				if(rowIndex == 2){
					columnType.put(columnIndex, value);
				}
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
	
	/**
	 * remove empty element
	 * @param list
	 * @return
	 */
	public static ArrayList<String> removeEmptyElement(ArrayList<String> list){
	    Iterator<String> chk_it = list.iterator();  
	    while(chk_it.hasNext()){  
	       String str = chk_it.next();  
	       if(null == str || "".equals(str.trim()))
	    	   chk_it.remove();
	    }
	    return list;
	}
	
	private static String getTag(String str){
		if("上午".equals(str.trim()))
			return "AM";
		else if("下午".equals(str.trim()))
			return "PM";
		
		return "";
	}
}
