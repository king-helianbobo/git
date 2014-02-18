package org.elasticsearch.app;

import org.soul.analysis.NlpAnalysis;

public class WebDemo {
	private final static String welcomeWord = "中文分词";

	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 1) {
			// System.err.println("ServerPort by default 8888");
			args = new String[]{"8888"};
		}
		NlpAnalysis.parse(welcomeWord);
		int serverPort = Integer.valueOf(args[0]);
		new SoulServer().startServer(serverPort);
	}
}
