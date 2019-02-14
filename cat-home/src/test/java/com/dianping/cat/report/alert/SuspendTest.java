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
package com.dianping.cat.report.alert;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;

public class SuspendTest extends ComponentTestCase {

	@Test
	public void test() {
		AlertManager manager = lookup(AlertManager.class);
		AlertEntity entity = new AlertEntity();
		entity.setDate(new Date()).setContent("test").setLevel("error");
		entity.setMetric("testMetric").setType(AlertType.Transaction.getName()).setGroup("testGroup");

		try {
			manager.addAlert(entity);
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception ex) {

		}

		Assert.assertTrue(manager.isSuspend(entity.getKey(), 1));
		try {
			TimeUnit.SECONDS.sleep(65);
		} catch (InterruptedException e) {
		}

		Assert.assertFalse(manager.isSuspend(entity.getKey(), 1));
	}

}
