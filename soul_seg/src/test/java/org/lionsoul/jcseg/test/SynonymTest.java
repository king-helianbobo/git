package org.lionsoul.jcseg.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SynonymTest {
	TransportClient client;
	private final Log log = LogFactory.getLog(SynonymTest.class);
	private String indexName = "synonym-test";
	private String typeName = "test1";
	private int port = 9300;

	@Before
	public void startClient() throws Exception {
		client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", port));
	}
	@After
	public void closeClient() {
		client.close();
	}
	@Ignore
	@Test
	public void createIndexTestWithMapping() {
		try {
			IndicesExistsResponse existsResponse = client.admin().indices()
					.prepareExists(indexName).execute().actionGet();
			if (existsResponse.isExists()) { // if index exist, delete it
				client.admin().indices().prepareDelete(indexName).execute()
						.actionGet();
			}
			XContentBuilder builder1 = (XContentBuilder) jsonBuilder()
					.startObject().startObject("analysis")
					.startObject("analyzer").startObject("synonym")
					.field("tokenizer", "whitespace").startArray("filter")
					.value("synonym").endArray().endObject().endObject()
					.startObject("filter").startObject("synonym")
					.field("type", "file_watcher_synonym")
					.field("synonyms_path", "library/synonym.txt")
					.field("interval", "2s").endObject().endObject()
					.endObject();
			String mappings = builder1.string();
			log.info(mappings);

			XContentBuilder builder2 = (XContentBuilder) jsonBuilder()
					.startObject().startObject(typeName)
					.startObject("properties").startObject("content")
					.field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("search_analyzer", "synonym").endObject()
					.endObject().endObject().endObject();

			log.info(builder2.string());

			CreateIndexResponse createIndexResponse = client.admin().indices()
					.prepareCreate(indexName).setSettings(builder1)
					.addMapping(typeName, builder2).execute().actionGet();
			assertThat(createIndexResponse.isAcknowledged(), is(true));
			client.admin().cluster().prepareHealth(indexName)
					.setWaitForGreenStatus().execute().actionGet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void extractTmp5() {
		String paths[] = {"/mnt/f/seg/1-1.txt", "/mnt/f/seg/2-1.txt",
				"/mnt/f/seg/3-1.txt", "/mnt/f/seg/4-1.txt",
				"/mnt/f/seg/5-1.txt", "/mnt/f/seg/8-1.txt"};
		List<Set<String>> tree = new ArrayList<Set<String>>();
		InputStream in;
		BufferedReader reader;
		FileWriter fw;
		BufferedWriter bw;
		String temp = null;
		int num = 0;
		try {
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("\\s+");
					String str1 = strs[0].trim();
					String str2 = strs[1].trim();
					log.info(str1 + " " + str2);
					// bw.write(str1 + " " + str2 + "\n");
					boolean contained = false;
					for (int i = 0; i < tree.size(); i++) {
						Set<String> set = tree.get(i);
						if (set.contains(str1) && set.contains(str2)) {
							log.info("this line has contained!");
							contained = true;
						} else if (set.contains(str1)) {
							set.add(str2);
							contained = true;
						} else if (set.contains(str2)) {
							set.add(str1);
							contained = true;
						} else {
						}
					}
					if (!contained) {
						Set<String> set = new HashSet<String>();
						set.add(str1);
						set.add(str2);
						tree.add(set);
					}
					num++;
				}
			}
			log.info("Total Word count is " + num);
			log.info("Total Word Line Numer is " + tree.size());
			fw = new FileWriter("/tmp/a", false);
			bw = new BufferedWriter(fw);
			for (int i = 0; i < tree.size(); i++) {
				Set<String> set = tree.get(i);
				Iterator<String> it = set.iterator();
				boolean firstWord = true;
				while (it.hasNext()) {
					if (!firstWord)
						bw.write(",");
					else
						firstWord = false;
					String str = it.next();
					bw.write(str);
				}
				bw.newLine();
			}
			bw.close();
			fw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractOtherTxt() {
		String paths[] = {"/mnt/f/tmp/1.txt", "/mnt/f/tmp/2.txt",
				"/mnt/f/tmp/3.txt"};
		String outPaths[] = {"/mnt/f/tmp/1-1.txt", "/mnt/f/tmp/2-1.txt",
				"/mnt/f/tmp/3-1.txt"};
		FileWriter fw;
		BufferedWriter bw;
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			for (int j = 0; j < paths.length; j++) {
				in = new FileInputStream(paths[j]);
				fw = new FileWriter(outPaths[j], true);
				bw = new BufferedWriter(fw);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("	");
					String str1 = strs[0].trim();
					String str2 = strs[1].trim();
					log.info(str1 + " " + str2);
					bw.write(str1 + " " + str2 + "\n");
					num++;
				}
				bw.close();
				fw.close();
			}
			log.info("Total Word count is " + num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractTmp7() {
		String paths[] = {"/mnt/f/tmp/7.txt"};
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			FileWriter fw = new FileWriter("/mnt/f/tmp/7-2.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split(" ");
					for (int i = 0; i < strs.length; i++) {
						String str = strs[i];
						String[] _strs = str.split("——");
						String str1 = _strs[0].trim();
						String str2 = _strs[1].trim();
						log.info(str1 + " " + str2);
						bw.write(str1 + " " + str2 + "\n");
						num++;
						// bw.write(str1 + " " + str2 + "\n");
					}
				}
			}
			bw.close();
			fw.close();
			log.info("Total Word count is " + num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void extractTmp4() {
		String paths[] = {"/mnt/f/tmp/4.txt"};
		String outPath = "/mnt/f/tmp/4-1.txt";
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			FileWriter fw = new FileWriter(outPath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("=>");
					int len1 = strs[0].trim().length();
					int len2 = strs[1].trim().length();
					String str1 = strs[0].trim().substring(1, len1 - 1);
					String str2 = strs[1].trim().substring(1, len2 - 2);
					log.info(str1 + " " + str2);
					bw.write(str1 + " " + str2 + "\n");
					num++;
				}
			}
			log.info("Total Word count is " + num);
			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Ignore
	@Test
	public void extractTmp8() {
		String paths[] = {"/mnt/f/tmp/8.txt"};
		InputStream in;
		BufferedReader reader;
		String temp = null;
		int num = 0;
		try {
			FileWriter fw = new FileWriter("/mnt/f/tmp/8-1.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String path : paths) {
				in = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split("\\s+");
					String str1 = "";
					String str2 = "";
					for (int i = 0; i < strs.length; i++) {
						if (i % 3 == 0) {
							assertNotNull(Integer.valueOf(strs[i].trim()));
						} else if (i % 3 == 1) {
							str1 = strs[i].trim();
						} else {
							str2 = strs[i].trim();
							log.info(str1 + " " + str2);
							bw.write(str1 + " " + str2 + "\n");
							num++;
						}
					}
				}
			}
			bw.close();
			fw.close();
			log.info("Total Word count is " + num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
