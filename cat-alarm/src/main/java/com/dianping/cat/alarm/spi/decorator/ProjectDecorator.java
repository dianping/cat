/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

	private static final String DEFAULT = "Default";

	@Inject
	protected ProjectService m_projectService;

	public String buildContactInfo(String domain) {
		try {
			Project project = m_projectService.findByDomain(domain);
			if (project != null) {
				String owners = project.getOwner();
				String phones = project.getPhone();
				StringBuilder builder = new StringBuilder();

				if (!StringUtils.isEmpty(project.getBu()) && !DEFAULT.equals(project.getBu())) {
					builder.append("<br/>所属部门：").append(project.getBu());
				}
				if (!StringUtils.isEmpty(project.getCmdbProductline()) && !DEFAULT.equals(project.getCmdbProductline())) {
					builder.append("<br/>所属产品：").append(project.getCmdbProductline());
				}
				if (!StringUtils.isEmpty(project.getOwner())) {
					builder.append("<br/>负责人员：").append(project.getOwner());
				}
				if (!StringUtils.isEmpty(project.getPhone())) {
					builder.append("<br/>联系号码：").append(project.getPhone());
				}
				return builder.toString();
			}
		} catch (Exception ex) {
			Cat.logError("build project contact info error for domain: " + domain, ex);
		}

		return "";
	}
}
