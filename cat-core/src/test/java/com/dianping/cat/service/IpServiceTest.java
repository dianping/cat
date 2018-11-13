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
package com.dianping.cat.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.service.IpService.IpInfo;

public class IpServiceTest extends ComponentTestCase {
	IpService2 service;

	List<Long> result = new ArrayList<Long>();

	@Test
	public void getSystemTime() throws Exception {
		System.err.println(System.currentTimeMillis() - 60 * 1000 * 2);
	}

	@Test
	public void test() throws Exception {
		IpService service = (IpService) lookup(IpService.class);

		for (int i = 0; i < 10000; i++) {
			String ip = i % 255 + "." + i % 255 + "." + i % 255 + "." + i % 255;
			IpInfo info = service.findIpInfoByString(ip);

			if (info != null) {
				System.out.print(ip + " " + info.getChannel());
				System.out.print(" " + info.getCity());
				System.out.println(" " + info.getProvince());
			}
		}
	}

	@Test
	public void test2() throws InterruptedException {
		service = (IpService2) lookup(IpService2.class);
		int count = 1000;

		for (int j = 0; j < count; j++) {
			Threads.forGroup().start(new Runnable() {

				@Override
				public void run() {
					long start = System.currentTimeMillis();

					for (int i = 0; i < 10000; i++) {
						String ip =
												(int) (Math.random() * 255) + "." + (int) (Math.random() * 255) + "."	+ (int) (Math.random() * 255) + "."
																		+ (int) (Math.random() * 255);
						IpInfo info = service.findIpInfoByString(ip);

						if (info == null) {
							System.out.println("error");
						}
					}
					long end = System.currentTimeMillis();
					System.out.println("TIME: " + (end - start));
					result.add(end - start);
				}

			});

		}
		while (result.size() < count) {
			Thread.sleep(1000);
		}

		long sum = 0;
		for (int i = 0; i < count; i++) {
			sum += result.get(i);
		}
		System.out.println(sum / count);
	}
}
