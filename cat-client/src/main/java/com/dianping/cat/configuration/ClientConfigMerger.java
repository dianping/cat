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
package com.dianping.cat.configuration;

import java.util.Stack;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.transform.DefaultMerger;

public class ClientConfigMerger extends DefaultMerger {
	public ClientConfigMerger(ClientConfig config) {
		super(config);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		if (domain.getIp() != null) {
			old.setIp(domain.getIp());
		}

		if (domain.getEnabled() != null) {
			old.setEnabled(domain.getEnabled());
		}

		if (domain.getMaxMessageSize() > 0) {
			old.setMaxMessageSize(domain.getMaxMessageSize());
		}
	}

	@Override
	protected void visitConfigChildren(ClientConfig to, ClientConfig from) {
		if (to != null) {
			Stack<Object> objs = getObjects();

			// if servers is configured, then override it instead of merge
			if (!from.getServers().isEmpty()) {
				to.getServers().clear();
				to.getServers().addAll(from.getServers());
			}

			// only configured domain in client configure will be merged
			for (Domain source : from.getDomains().values()) {
				Domain target = to.findDomain(source.getId());

				if (target == null) {
					target = new Domain(source.getId());
					to.addDomain(target);
				}

				if (to.getDomains().containsKey(source.getId())) {
					objs.push(target);
					source.accept(this);
					objs.pop();
				}
			}

			for (Property source : from.getProperties().values()) {
				Property target = to.findProperty(source.getName());

				if (target == null) {
					target = new Property(source.getName());
					to.addProperty(target);
				}

				objs.push(target);
				source.accept(this);
				objs.pop();
			}
		}
	}
}
