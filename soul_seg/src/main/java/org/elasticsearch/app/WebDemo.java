package org.elasticsearch.app;

import org.soul.splitWord.NlpAnalysis;

public class WebDemo {
	private final static String WARM_UP_WORD = "中文分词";

	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 1) {
			System.err.println("ServerPort by default 8888");
			args = new String[] { "8888" };
		}
		NlpAnalysis.parse(WARM_UP_WORD);
		int serverPort = Integer.valueOf(args[0]);
		new SoulServer().startServer(serverPort);
	}
}
