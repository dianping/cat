/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
