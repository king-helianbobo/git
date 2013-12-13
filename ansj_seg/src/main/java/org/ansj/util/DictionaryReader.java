package org.ansj.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * classes is root directory
 */
public class DictionaryReader {
	public static BufferedReader getReader(String name) {
		InputStream in = DictionaryReader.class.getResourceAsStream("/" + name);
		try {
			return new BufferedReader(new InputStreamReader(in, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream getInputStream(String name) {
		InputStream in = DictionaryReader.class.getResourceAsStream("/" + name);
		return in;
	}
}
