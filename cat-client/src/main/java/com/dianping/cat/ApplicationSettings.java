package com.dianping.cat;

import java.io.InputStream;
import java.util.Properties;

public class ApplicationSettings {

	private static final String PROPERTIES_FILE = "/META-INF/app.properties";

	private static int s_queue_size = 5000;

	private static int s_tree_length_size = 2000;

	static {
		InputStream in = null;

		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);

			if (in == null) {
				in = Cat.class.getResourceAsStream(PROPERTIES_FILE);
			}
			if (in != null) {
				Properties prop = new Properties();

				prop.load(in);

				String queueLength = prop.getProperty("cat.queue.length");

				if (queueLength != null) {
					s_queue_size = Integer.parseInt(queueLength);
				}

				String treeMaxLength = prop.getProperty("cat.tree.max.length");

				if (treeMaxLength != null) {
					s_tree_length_size = Integer.parseInt(treeMaxLength);
				}
			}
		} catch (Exception e) {
			// ingore
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static int getQueueSize() {
		return s_queue_size;
	}

	public static int getTreeLengthLimit() {
		return s_tree_length_size;
	}
}
