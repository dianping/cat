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
package com.dianping.cat.message;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.After;
import org.junit.Before;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;

public abstract class CatTestCase extends ComponentTestCase {

	protected File getConfigurationFile() {
		if (isCatServerAlive()) {
			try {
				ClientConfig config = new ClientConfig();

				config.setMode("client");
				config.addDomain(new Domain("cat"));
				config.addServer(new Server("localhost").setPort(2280));

				File file = new File("target/cat-config.xml");

				Files.forIO().writeTo(file, config.toString());
				return file;
			} catch (IOException e) {
				return null;
			}
		}

		return null;
	}

	protected boolean isCatServerAlive() {
		// detect if a CAT server listens on localhost:2280
		try {
			SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 2280));

			channel.close();
			return true;
		} catch (Exception e) {
			// ignore it
		}

		return false;
	}

	@Before
	public void setup() throws Exception {
		Cat.initialize(getConfigurationFile());
	}

	@After
	public void teardown() throws Exception {
	}
}