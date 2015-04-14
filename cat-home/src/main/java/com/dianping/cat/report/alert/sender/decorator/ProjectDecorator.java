package com.dianping.cat.report.alert.sender.decorator;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.service.ProjectService;

import org.unidal.lookup.util.StringUtils;

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
