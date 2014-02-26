package org.elasticsearch.app;

import org.splitword.soul.analysis.NlpAnalysis;

public class WebDemo {
	private final static String welcomeWord = "中文分词";

	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 1) {
			// System.err.println("ServerPort by default 8888");
			args = new String[]{"8888"};
		}
		SoulHttpClient client = new SoulHttpClient();
		// client.suggestSearch("tai");
		String queryStr = "Google中国";
		client.simpleQuerySearch(queryStr);
		client.scrollSearchWithSort(queryStr);
		client.scrollSearchWithoutSort(queryStr);
		// client.anotherSuggestSearch("天呀");
		// NlpAnalysis.parse(welcomeWord);
		// int serverPort = Integer.valueOf(args[0]);
		// new SoulServer().startServer(serverPort);
	}
}
