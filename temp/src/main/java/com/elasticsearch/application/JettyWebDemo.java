package com.elasticsearch.application;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.webapp.WebApp;
import org.apache.hadoop.yarn.webapp.WebApps;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.monitor.jvm.JvmInfo;

public class JettyWebDemo {
	private static final Log log = LogFactory.getLog(JettyWebDemo.class);
	private static String host = "http://192.168.2.249:9200";
	// private static String index = "official_mini";
	private static String index = "route_mini";
	private static String type = "table";
	private static int port = 9999;

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

	public static void main(String[] args) throws Exception {
		if (args == null || args.length != 4) {
			args = new String[] { host, index, type, "9999" };
		}
		host = args[0];
		index = args[1];
		type = args[2];
		port = Integer.valueOf(args[3]);
		// WebApps.$for("", new PageHttpHandler(host, index, type)).at(port)
		// .inDevMode().start().joinThread();
		// WebApps.$for("", new PageHttpHandler(host, index, type)).at(port)
		// .start().joinThread();

		WebApp app = WebApps
				.$for("", PageHttpMvc.class, new PageHttpMvc(host, index, type))
				.at(port).start(new WebApp() {
					@Override
					public void setup() {
						route("/page",
								com.elasticsearch.application.PageHttpMvc.FooController.class);
					}
				});
		app.joinThread();
	}
}
