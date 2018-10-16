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
package com.dianping.cat.alarm.spi.decorator;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;

@Named
public class DecoratorManager extends ContainerHolder implements Initializable {

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	public Pair<String, String> generateTitleAndContent(AlertEntity alert) {
		AlertType alertType = alert.getType();
		Decorator decorator = m_decorators.get(alertType.getName());

		if (decorator != null) {
			String title = decorator.generateTitle(alert);
			String content = decorator.generateContent(alert);

			return new Pair<String, String>(title, content);
		} else {
			throw new RuntimeException("error alert type:" + alert.getType());
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_decorators = lookupMap(Decorator.class);
	}

}
