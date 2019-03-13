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
package com.dianping.cat.report.analyzer;

import java.io.File;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;

public class TopologyGraphTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		build("2014-07-06 18:00");
		build("2014-07-06 18:01");
		build("2014-07-06 18:02");
		build("2014-07-06 18:03");
		build("2014-07-06 18:04");
		build("2014-07-06 18:05");
		build("2014-07-06 18:06");
		build("2014-07-06 18:07");
		build("2014-07-06 18:08");
		build("2014-07-06 18:09");
	}

	public void build(String date) throws Exception {
		TopologyGraphManager manager = lookup(TopologyGraphManager.class);
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			TopologyGraph graph = manager.queryGraphFromDB(formate.parse(date).getTime());

			if (graph != null) {
				File file = new File("/tmp/" + date + ".txt");

				if (!file.exists()) {
					file.createNewFile();
				}
				Files.forIO().writeTo(file, graph.toString());
			} else {
				System.err.println(date + " is null1");
			}
		} catch (Exception e) {
			System.err.println(date + " is null1");
		}
	}

}
