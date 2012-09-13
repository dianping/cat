/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-11
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.jdbc;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.bee.engine.AllTests;
import com.dianping.bee.engine.TestEnvConfig;
import com.dianping.bee.mysql.InformationSchemaTest;
import com.dianping.bee.server.SimpleServer;
import com.site.lookup.ContainerLoader;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(Suite.class)
@SuiteClasses({

StatementTest.class,

PreparedStatementTest.class,

InformationSchemaTest.class,

DCLtatementTest.class,

AllTests.class })
public class AllInOneJDBCTests {
	private static SimpleServer server;

	@AfterClass
	public static void after() throws Exception {
		server.shutdown();
	}

	@BeforeClass
	public static void before() throws Exception {
		ContainerConfiguration configuration = getConfiguration();
		PlexusContainer container = ContainerLoader.getDefaultContainer(configuration);

		server = container.lookup(SimpleServer.class);
		server.startup();
	}

	private static ContainerConfiguration getConfiguration() {
		ContainerConfiguration configuration = new DefaultContainerConfiguration().setName("test");
		String resource = TestEnvConfig.class.getName().replace('.', '/') + ".xml";

		configuration.setContainerConfiguration(resource);
		return configuration;
	}
}