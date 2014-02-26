package org.splitword.lionsoul.jcseg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Jcseg dictionary simplified/traditional convert class.
 * 
 */
public class DicConverter {

	public static final int SIMPLIFIED_TO_TRADITIONAL = 0;
	public static final int TRADITIONAL_TO_SIMPLIFIED = 1;

	/**
	 * convert the srcFile to dstFile to with the specified convert rule.
	 * (SIMPLIFIED_TO_TRADITIONAL or TRADITIONAL_TO_SIMPLIFIED).
	 * 
	 * @param srcfile
	 * @param dstfile
	 * @param _cvt
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean convert(File srcFile, File dstFile, int _cvt)
			throws IOException {
		if (srcFile.equals(dstFile))
			return false;
		BufferedReader reader = new BufferedReader(new FileReader(srcFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));
		String line;
		IStringBuffer isb = new IStringBuffer();
		switch (_cvt) {
			case SIMPLIFIED_TO_TRADITIONAL :
				while ((line = reader.readLine()) != null) {
					isb.clear();
					STConverter.SimToTraditional(line, isb);
					writer.write(isb.buffer(), 0, isb.length());
					writer.write('\n');
				}
				break;
			case TRADITIONAL_TO_SIMPLIFIED :
				while ((line = reader.readLine()) != null) {
					isb.clear();
					STConverter.TraToSimplified(line, isb);
					writer.write(isb.buffer(), 0, isb.length());
					writer.write('\n');
				}
				break;
			default :
				reader.close();
				writer.close();
				return false;
		}

		reader.close();
		writer.close();

		return true;
	}

}
