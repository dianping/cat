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
package com.dianping.cat.status;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class HeartbeatExtenstion implements StatusExtension, Initializable {

	@Override
	public String getId() {
		return "MyTestId";
	}

	@Override
	public String getDescription() {
		return "MyDescription";
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> maps = new HashMap<String, String>();

		maps.put("key1", String.valueOf(1));
		maps.put("key2", String.valueOf(2));
		maps.put("key3", String.valueOf(3));

		return maps;
	}

	@Override
	public void initialize() throws InitializationException {
		StatusExtensionRegister.getInstance().register(this);
	}

}
