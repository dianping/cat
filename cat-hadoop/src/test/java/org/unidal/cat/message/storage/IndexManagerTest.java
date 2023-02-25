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

import java.io.IOException;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.internal.MessageId;

public class IndexManagerTest extends ComponentTestCase {
	@Test
	public void test() throws IOException {
		IndexManager manager = lookup(IndexManager.class, "local");
		MessageId id = MessageId.parse("mock-0a260014-403890-12345");
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		Assert.assertNotNull(manager.getIndex(id.getDomain(), ip, id.getHour(), true));
	}
}
