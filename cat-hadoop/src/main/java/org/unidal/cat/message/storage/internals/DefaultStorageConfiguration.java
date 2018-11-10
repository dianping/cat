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
package org.unidal.cat.message.storage.internals;

import java.io.File;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = StorageConfiguration.class)
public class DefaultStorageConfiguration implements Initializable, StorageConfiguration {
	private String m_baseDataDir;

	@Override
	public String getBaseDataDir() {
		return m_baseDataDir;
	}

	@Override
	public void setBaseDataDir(String baseDataDir) {
		m_baseDataDir = baseDataDir;
	}

	@Override
	public void initialize() throws InitializationException {
		setBaseDataDir(new File(Cat.getCatHome(),"bucket"));
	}

	@Override
	public boolean isLocalMode() {
		return true;
	}

	@Override
	public void setBaseDataDir(File baseDataDir) {
		m_baseDataDir = baseDataDir.getAbsolutePath() + '/';
	}
}
