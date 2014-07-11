package com.elasticsearch.application;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.monitor.jvm.JvmInfo;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

public class SunWebDemo {
	private static final Log log = LogFactory.getLog(SunWebDemo.class);
	// private static String host = "http://61.177.121.182:9200";
	private static String host = "http://192.168.2.249:9200";
	private static String index = "route_mini";
	private static String type = "table";
	private static int port = 8888;

	static public void daemonize() {
		System.out.close();
		System.err.close();
		final String pidFile = System.getProperty("es.pidfile",
				System.getProperty("es-pidfile"));
		log.info(pidFile);
		if (pidFile != null) {
			try {
				File fPidFile = new File(pidFile);
				if (fPidFile.getParentFile() != null) {
					log.info(fPidFile.getParentFile().getAbsolutePath());
					FileSystemUtils.mkdirs(fPidFile.getParentFile());
				}
				FileOutputStream outputStream = new FileOutputStream(fPidFile);
				outputStream.write(Long.toString(JvmInfo.jvmInfo().pid())
						.getBytes());
				outputStream.close();
				fPidFile.deleteOnExit();
			} catch (Exception e) {
				System.exit(3);
			}
		}
	}

	private static void startServer() throws Exception {
		log.info("starting http server ...");
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(
				new InetSocketAddress(port), 100);
		// listen on port 8888, accept 100 requests one time
		httpserver.createContext("/", new SoulHttpHandler(host, index, type));
		httpserver.setExecutor(null);
		httpserver.start();
		log.info("server started, listening on port: " + port + "!");
	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length != 4) {
			args = new String[] { host, index, type, String.valueOf(port) };
		} else {
			host = args[0];
			index = args[1];
			type = args[2];
			port = Integer.valueOf(args[3]);
		}
		// daemonize();
		startServer();
	}
}
