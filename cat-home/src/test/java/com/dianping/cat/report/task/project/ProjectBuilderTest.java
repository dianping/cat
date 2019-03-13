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
package com.dianping.cat.report.task.project;

import java.io.File;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.service.ProjectService;

public class ProjectBuilderTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		ServerConfigManager manager = lookup(ServerConfigManager.class);

		manager.initialize(new File(Cat.getCatHome(),"server.xml"));
	}

	@Test
	public void test() throws ParseException {
		lookup(ProjectService.class);

		ProjectUpdateTask builder = (ProjectUpdateTask) lookup(ProjectUpdateTask.class);

		builder.deleteUnusedDomainInfo();

	}

}
