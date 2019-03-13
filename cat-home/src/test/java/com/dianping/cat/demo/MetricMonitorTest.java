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
package com.dianping.cat.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class MetricMonitorTest {

	@Test
	public void test() throws Exception {
		String host = "cat.qa.dianpingoa.com";
		String group = "db-mysql-tg01s6.nh[10.1.1.136]";
		for (int i = 0; i < 1000; i++) {
			SendData(host, group, "Load", 100);
			SendData(host, group, "QUESTIONS", 100);
			SendData(host, group, "swapTotal", 100);
			SendData(host, group, "diskAvail", 100);
			SendData(host, group, "Aborted_clients", 100);
			SendData(host, group, "Innodb_buffer_pool_pages_dirty", 100);
			SendData(host, group, "Innodb_deadlocks", 100);
			Thread.sleep(5000);
		}
	}

	public void SendData(String host, String group, String key, int value) throws Exception {
		String url = "http://" + host + "/cat/r/monitor?timestamp=%s&group=%s&domain=cat&key=%s&op=sum&value=%s";
		long timestamp = System.currentTimeMillis();
		url = String.format(url, timestamp, group, key, value);
		String ret = sendGet(url);
		System.out.println(ret);
	}

	public String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
}
