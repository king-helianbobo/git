package org.soul.splitWord.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.soul.analysis.BasicAnalysis;
import org.soul.domain.Term;
import org.soul.treeSplit.*;
import org.soul.utility.DictionaryReader;
import org.soul.wordToVec.VectorLearn;
import org.soul.wordToVec.WordToVec;

public class Word2VecTest {

	private static final File modelFile = new File("/mnt/e/tmp/resultbig.txt");

	private static Log log = LogFactory.getLog(Word2VecTest.class);

	public static void main(String[] args) throws IOException {

		// URL url = Word2VecTest.class.getResource("/taobao");
		//
		// String dirPath = url.getFile();
		// File[] files = new File(dirPath).listFiles();
		// log.info(url.getFile());
		//
		// // InputStream in = Word2VecTest.class.getResourceAsStream("/" +
		// name);
		//
		// // DicReader.getReader("newWord/newWordFilter.dic");
		//
		// for (int i = 0; i < files.length; i++)
		// log.info(files[i].getAbsolutePath());
		//
		// // 构建语料
		// FileOutputStream fos = new FileOutputStream(sportCorpusFile);
		// for (File file : files) {
		// if (file.canRead() && file.getName().endsWith(".txt")) {
		// parserFile(fos, file);
		// }
		// }

		// 进行分词训练

		VectorLearn lean = new VectorLearn();

		lean.learnFile(modelFile);

		lean.saveModel(new File("vector.mod"));
		//
		// // 加载测试
		//
		WordToVec w2v = new WordToVec();

		w2v.loadJavaModel("vector.mod");

		log.info(w2v.distance("范冰冰"));

	}

	private static void parserFile(FileOutputStream fos, File file)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		BufferedReader br = IOUtil.getReader(file.getAbsolutePath(),
				IOUtil.UTF8);

		// BufferedReader br = IOUtil
		// .getReader(file.getAbsolutePath(), IOUtil.GBK);
		String temp = null;
		// JSONObject parse = null;
		while ((temp = br.readLine()) != null) {

			paserStr(fos, StringUtil.rmHtmlTag(temp));
			// parse = JSONObject.parseObject(temp);
			// paserStr(fos, parse.getString("title"));
			// paserStr(fos, StringUtil.rmHtmlTag(parse.getString("content")));
		}
	}

	private static void paserStr(FileOutputStream fos, String str)
			throws IOException {
		List<Term> parse2 = BasicAnalysis.parse(str);
		StringBuilder sb = new StringBuilder();
		for (Term term : parse2) {
			sb.append(term.getName());
			sb.append(" ");
		}
		log.info(sb.toString());
		fos.write(sb.toString().getBytes());
		// fos.write("\n".getBytes());
		fos.write(" ".getBytes());
	}
}