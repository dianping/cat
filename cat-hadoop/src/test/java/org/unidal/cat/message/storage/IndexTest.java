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
package org.unidal.cat.message.storage;

import java.io.File;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.internal.MessageId;

public class IndexTest extends ComponentTestCase {

	private String m_ip;

	@Before
	public void before() throws Exception {
		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(new File("target"));
		File baseDir = new File("target");
		Files.forDir().delete(new File(baseDir, "dump"), true);
		m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	@Test
	public void testMapAndLookups() throws Exception {
		int total = 15000;
		IndexManager manager = lookup(IndexManager.class, "local");
		Index index = manager.getIndex("from", m_ip, 403899, true);

		for (int i = 1; i < total; i++) {
			MessageId from = MessageId.parse("from-0a260014-403899-" + i);
			MessageId to = MessageId.parse("to-0a260015-403899-" + i);

			index.map(from, to);
		}

		index = manager.getIndex("from", m_ip, 403899, true);
		for (int i = 1; i < total; i++) {
			MessageId from = MessageId.parse("from-0a260014-403899-" + i);
			MessageId expected = MessageId.parse("to-0a260015-403899-" + i);

			MessageId actual = index.find(from);
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testMapAndLookupManyIps() throws Exception {
		IndexManager manager = lookup(IndexManager.class, "local");
		Index index = manager.getIndex("from", m_ip, 403899, true);

		for (int i = 1; i < 150000; i++) {
			for (int ip = 0; ip < 10; ip++) {
				MessageId from = MessageId.parse("from-0a26000" + ip + "-403899-" + i);
				MessageId to = MessageId.parse("from-0a25000" + ip + "-403899-" + i);

				index.map(from, to);
			}
		}

		index = manager.getIndex("from", m_ip, 403899, true);
		for (int i = 1; i < 150000; i++) {

			for (int ip = 0; ip < 10; ip++) {
				MessageId from = MessageId.parse("from-0a26000" + ip + "-403899-" + i);
				MessageId expected = MessageId.parse("from-0a25000" + ip + "-403899-" + i);

				MessageId actual = index.find(from);

				Assert.assertEquals(expected, actual);
			}
		}
	}

}
