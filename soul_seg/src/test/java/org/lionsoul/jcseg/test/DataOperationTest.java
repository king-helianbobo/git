package org.lionsoul.jcseg.test;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class DataOperationTest {
	@Ignore("Convert Sogou Txt data to hdfs friendly format")
	@Test
	public void convertDataToHdfsFormat() {
		SogouDataReader reader = new SogouDataReader("/mnt/f/Sogou-mini/");
		try {
			reader.convertToHdfsFormat("/mnt/f/hdfs3/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
