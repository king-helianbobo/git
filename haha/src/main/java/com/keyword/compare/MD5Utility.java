package com.keyword.compare;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utility {

	// public static String calFileMD5(String filePath) {
	// File file = new File(filePath);
	//
	// if (!file.isFile()) {
	// return null;
	// }
	// MessageDigest digest = null;
	// FileInputStream in = null;
	// byte buffer[] = new byte[1024];
	// int len;
	// try {
	// digest = MessageDigest.getInstance("MD5");
	// in = new FileInputStream(file);
	// while ((len = in.read(buffer, 0, 1024)) != -1) {
	// digest.update(buffer, 0, len);
	// }
	// in.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	// BigInteger bigInt = new BigInteger(1, digest.digest());
	// return bigInt.toString(16);
	// }

	public static String calculateFileMD5(String filePath) {
		// filePath是想对路径
		MessageDigest digest = null;
		InputStream in = MD5Utility.class.getResourceAsStream("/baipi/"
				+ filePath);
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			// in = new FileInputStream(in);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	public static String calculateStrMD5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] b = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
