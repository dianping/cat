/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-23
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
package com.dianping.bee.engine.spi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.bee.server.SimpleServer;
import com.site.lookup.ComponentTestCase;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(JUnit4.class)
public class ServerTest extends ComponentTestCase {

	@Test
	public void runServer() throws Exception {
		SimpleServer server = lookup(SimpleServer.class);

		server.startup();

		System.out.println("Press any key to continue ...");
		System.in.read();
	}
}
