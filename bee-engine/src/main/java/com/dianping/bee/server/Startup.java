/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-15
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software s the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.helpers.LogLog;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public final class Startup {
	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

	public static void main(String[] args) {
		try {
			// init
			WhaleServer server = WhaleServer.getInstance();
			server.beforeStart(dateFormat);

			// startup
			server.startup();
		} catch (Throwable e) {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			LogLog.error(sdf.format(new Date()) + " startup error", e);
			System.exit(-1);
		}
	}

}
