package com.soul.readExcel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class Query {
	
	private static Map<String,Object> invoiceMap = new HashMap<String,Object>();
	private static Map<String,Object> taxMap = new HashMap<String,Object>();
	
	static{
		try {
			File invoiceFile = new File("/home/jacky/文档/test/发票开具信息.xls");
			File taxFile = new File("/home/jacky/文档/test/机构纳税人主表.xls");
			String[][] invoice = getData(invoiceFile, 0);
			String[][] tax = getData(taxFile, 0);
			
			generateJavaFile(invoice,"/home/workspace/soul-client/src/main/java/com/soul/readExcel","Invoice");
			generateJavaFile(tax,"/home/workspace/soul-client/src/main/java/com/soul/readExcel","Tax");
			
		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String str = "";
		while(str != null && !str.equals("q")){
			Scanner sc = new Scanner(System.in);
			System.out.println("please input: ");
			 str = sc.next();
			 
			 for (Entry<String, Object> entry : invoiceMap.entrySet()) {
				 Object o = entry.getValue();
				 Method getSum = o.getClass().getMethod("get金额");
				 Method getManag = o.getClass().getDeclaredMethod("get收款方管理码");
				 Object manag = getManag.invoke(o);
				 Object sum = getSum.invoke(o);
				 
				 if(null != sum && sum.toString().trim().equals(str.trim())){
					 outPutInfo(manag,str);
				 }
			}
		}
	}
	
	private static void outPutInfo(Object key,String str) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		if(key == null)
			return;
		 Object o = taxMap.get(key);
		 Method logMethod = o.getClass().getMethod("getZCDLXDH");
		 Object phone = logMethod.invoke(o);
		 if(null != phone)
			 System.out.println("sum is str's phone is: " + phone.toString());
	}

	private static void generateJavaFile(String[][] invoice,String path,String className) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		FileOutputStream out = new FileOutputStream(path + "/" + className + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
		StringBuilder sb = new StringBuilder();
		sb.append("package com.soul.readExcel;" + "\n");
		sb.append("import java.io.Serializable;" + "\n");
		sb.append("public class " + className + " implements Serializable" + "\n");
		sb.append("{" + "\n");
		
		int rowLength = invoice.length;
		int columnLength = invoice[0].length;
		
		for (int i = 0; i < rowLength; i++) {
			for (int j = 1; /*ignore first column*/ j < columnLength/*result[i].length*/; j++) {
				if (i == 0) {
					sb.append("private String " + invoice[i][j] + ";\n");
					generateSetAndGetMethod(sb,invoice[i][j]);
				}
			}
		}
		
		sb.append("}");
		bw.append(sb.toString());
		bw.flush();
		out.close();
		bw.close();
		
		compile(path,className);
		
		ArrayList<Object> objList = new ArrayList<Object>();
		for (int i = 2; i < rowLength; i++) {
			Object obj = Class.forName("com.soul.readExcel." + className).newInstance();;
			for (int j = 1; /*ignore first column*/ j < columnLength/*result[i].length*/; j++) {				
					Method logMethod = obj.getClass().getMethod("set" + invoice[0][j],new Class[]{java.lang.String.class});
					logMethod.invoke(obj, invoice[i][j]);
			}
			if(null != obj)
				objList.add(obj);
		}
		
		if(className.endsWith("Invoice")){
			for(Object o : objList){
				//Object obj = Class.forName("com.soul.readExcel." + className).newInstance();
				Method logMethod = o.getClass().getMethod("get发票号码");
				invoiceMap.put(logMethod.invoke(o) == null?null:logMethod.invoke(o).toString(), o);
			}
		}else if(className.equals("Tax")){
			for(Object o : objList){
				//Object obj = Class.forName("com.soul.readExcel." + className).newInstance();
				Method logMethod = o.getClass().getMethod("getSWGLM");
				taxMap.put(logMethod.invoke(o) == null?null:logMethod.invoke(o).toString(), o);
			}
		}
	}
	
	private static void compile(String filePath,String fileName){
		  String path = "/home/workspace/soul-client/target/classes";
	        com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
	          
	        String[] args = new String[] { 
	                "-d",
	                path,
	                "-classpath",
	                "%classpath%;"
	                        + path
	                        + ";",
	                "-encoding", "utf-8", filePath + "/" + fileName +".java" };
	        int status = javac.compile(args);
	        if(status != 0) {
	            System.out.println(fileName + " build fialure! ");
	        }
	}

	private static void generateSetAndGetMethod(StringBuilder sb,String str) {
		// TODO Auto-generated method stub
		//set
		sb.append("public void set" + str + "(String " + str + ")" + "{\n");
		sb.append("this." + str + " = " + str + ";" + "\n");
		sb.append("}\n");
		
		//get
		sb.append("public String get" + str + "(){\n");
		sb.append("return " + str +";\n");
		sb.append("}\n");
	}

	private static void listDirectory(String path) throws FileNotFoundException, IOException{
		File dir = new File(path);
		File file[] = dir.listFiles();
		int total = 0;
        for (int j = 0; j < file.length;j++) {
            if (file[j].isDirectory()){
            	listDirectory(file[j].getAbsolutePath());
            }
            else if(file[j].getName().endsWith("xls")){
            	String[][] result =  getData(file[j], 0);
            }else{            	
            	System.out.println("do nothing");
            }
        }
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
		//for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			HSSFSheet st = wb.getSheetAt(/*sheetIndex*/0); //read only first sheet
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
							value = (cell.getBooleanCellValue() == true ? "Y"
									: "N");
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
		//}

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
