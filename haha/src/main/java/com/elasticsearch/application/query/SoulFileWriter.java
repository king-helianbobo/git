package com.elasticsearch.application.query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.elasticsearch.application.search.ExtractUtil;

public class SoulFileWriter {
	String filePath = null;
	FileWriter fw = null;
	BufferedWriter bw = null;

	public SoulFileWriter(String path) {
		filePath = path;
		File file = new File(filePath);
		if (file.exists())
			file.delete();
		try {
			fw = new FileWriter(filePath, true);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeStr(String content) {
		try {
			bw.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeSet(SoulFileWriter writer, List<Set<String>> setList) {
		for (int i = 0; i < setList.size(); i++) {
			Set<String> set = setList.get(i);
			String result = ExtractUtil.setToString(set);
			writer.writeStr(result);
		}
		writer.close();
	}

	public void writeWithNewLine(String content) {
		try {
			bw.write(content + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void flush() {
		try {
			bw.flush();// 必须调用flush，否则数据只在流中，而没有写入文件
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		// this method must be called ,otherwise data would not persist
		try {
			bw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
