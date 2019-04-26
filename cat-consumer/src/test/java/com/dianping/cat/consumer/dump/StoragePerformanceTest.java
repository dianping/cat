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
package com.dianping.cat.consumer.dump;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;

public class StoragePerformanceTest extends ComponentTestCase {
	private MessageCodec m_codec = new PlainTextMessageCodec();

	@Before
	public void before() {
		File baseDir = new File(Cat.getCatHome(),"bucket/dump/20160415");

		Files.forDir().delete(new File(baseDir, "dump"), true);

		lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
	}

	@Test
	public void testManyDomainIpWrite() throws Exception {
		TreeHelper.init(m_codec);
		long start = System.currentTimeMillis();
		MessageBucketManager manager = lookup(MessageBucketManager.class, "local");
		int hour = 405746;

		for (int i = 0; i < 10000; i++) {
			long interStart = System.currentTimeMillis();

			for (int domainIndex = 0; domainIndex < 300; domainIndex++) {
				String domain = "domain" + domainIndex;

				for (int ipIndex = 0; ipIndex < 10; ipIndex++) {
					String ip = "0a01020" + ipIndex;
					MessageId id = new MessageId(domain, ip, hour, i * 10 + ipIndex);
					MessageTree tree = TreeHelper.cacheTree(m_codec, id);

					((DefaultMessageTree) tree).setMessageId(id.toString());
					tree.setFormatMessageId(id);

					manager.storeMessage(tree, id);
				}
			}

			long duration = System.currentTimeMillis() - interStart;

			if (i % 100 == 0) {
				System.out.println("duration:" + duration + ":qps:" + 3000 * 1000 / duration);
			}
		}

		long duration = System.currentTimeMillis() - start;
		System.out.println("write cost" + duration);
	}
}
