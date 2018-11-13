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
package com.dianping.cat.consumer.storage.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named
public class StorageBuilderManager extends ContainerHolder implements Initializable {

	private Map<String, StorageBuilder> m_storageBuilders;

	public List<String> getDefaultMethods(String type) {
		StorageBuilder storageBuilder = m_storageBuilders.get(type);

		if (storageBuilder != null) {
			return storageBuilder.getDefaultMethods();
		} else {
			return Collections.emptyList();
		}
	}

	public StorageBuilder getStorageBuilder(String type) {
		return m_storageBuilders.get(type);
	}

	@Override
	public void initialize() throws InitializationException {
		m_storageBuilders = lookupMap(StorageBuilder.class);
	}

}
