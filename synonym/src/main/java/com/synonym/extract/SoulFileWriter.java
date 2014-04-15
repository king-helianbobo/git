package com.synonym.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

	public void writeString(String content) {
		try {
			bw.write(content);
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
		// 必须调用该方法，否则数据只在流中，而没有写入文件
		try {
			bw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
