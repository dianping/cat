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
package org.unidal.cat.message.storage.local;

import java.text.MessageFormat;
import java.util.Date;

import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = PathBuilder.class, value = "local")
public class LocalFileBuilder implements PathBuilder {
	@Inject
	private StorageConfiguration m_config;

	@Override
	public String getPath(String domain, Date startTime, String consumerId, FileType type) {
		MessageFormat format;
		String path;

		switch (type) {
		case TOKEN:
			format = new MessageFormat("dump/{0,date,yyyyMMdd}/{0,date,HH}/{2}.{3}");
			path = format.format(new Object[] { startTime, null, consumerId, type.getExtension() });
			break;
		default:
			format = new MessageFormat("dump/{0,date,yyyyMMdd}/{0,date,HH}/{1}-{2}.{3}");
			path = format.format(new Object[] { startTime, domain, consumerId, type.getExtension() });
			break;
		}

		String baseDir = m_config.getBaseDataDir();

		return baseDir + path;
	}
}
