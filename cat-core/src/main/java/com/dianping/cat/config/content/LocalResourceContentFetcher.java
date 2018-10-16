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
package com.dianping.cat.config.content;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ContentFetcher.class)
public class LocalResourceContentFetcher implements ContentFetcher, LogEnabled {
	private final String PATH = "/config/";

	private Logger m_logger;

	@Override
	public String getConfigContent(String configName) {
		String path = PATH + configName + ".xml";
		String content = "";

		try {
			content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
		} catch (Exception e) {
			m_logger.warn("can't find local default config " + configName);
			Cat.logError(configName + " can't find", e);
		}
		return content;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
