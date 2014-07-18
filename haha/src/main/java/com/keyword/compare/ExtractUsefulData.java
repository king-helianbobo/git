package com.keyword.compare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractUsefulData {
	private static final String regEx = "[\u4e00-\u9fa5]";

	public static void main(String[] args) {
		readFile();
	}

	public static void readFile() {
		try {
			File file = new File("data/out.txt");
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				StringBuilder sb = new StringBuilder();
				int i = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if ("--".equals(lineTxt))
						continue;
					while (i < 2) {
						lineTxt += "\n" + bufferedReader.readLine();
						i++;
					}
					Pattern p = Pattern.compile(regEx);
					Matcher m = p.matcher(lineTxt);
					if (m.find()) {
						sb.append(lineTxt.replaceAll("\\t", "、") + "\n");
						sb.append("--");
						sb.append("\n");
					}
					i = 0;
				}

				writeFile(sb.toString());
				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(String string) throws IOException {
		// TODO Auto-generated method stub
		OutputStream out = new FileOutputStream("data/test.txt");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,
				"utf-8"));
		bw.append(string);
		bw.flush();

		out.close();
		bw.close();
	}
}
