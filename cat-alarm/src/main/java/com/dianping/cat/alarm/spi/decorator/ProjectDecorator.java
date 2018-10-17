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

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.service.ProjectService;

public abstract class ProjectDecorator extends Decorator {

	@Inject
	protected ProjectService m_projectService;

	public String buildContactInfo(String domainName) {
		try {
			Project project = m_projectService.findByDomain(domainName);

			if (project != null) {
				String owners = project.getOwner();
				String phones = project.getPhone();
				StringBuilder builder = new StringBuilder();

				if (!StringUtils.isEmpty(owners)) {
					builder.append("[业务负责人: ").append(owners).append(" ]");
				}
				if (!StringUtils.isEmpty(phones)) {
					builder.append("[负责人手机号码: ").append(phones).append(" ]");
				}

				return builder.toString();
			}
		} catch (Exception ex) {
			Cat.logError("build project contact info error for domain: " + domainName, ex);
		}

		return "";
	}
}
