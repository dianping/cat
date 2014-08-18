package com.dianping.cat.report.task.alert.sender.decorator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.site.lookup.util.StringUtils;

public abstract class DefaultDecorator implements Decorator {

	@Inject
	protected ProjectDao m_projectDao;

	protected DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	protected String buildContactInfo(String domainName) {
		try {
			if (domainName.startsWith("f5-")) {
				domainName = domainName.substring(3);
			} else if (domainName.startsWith("switch-")) {
				domainName = domainName.substring(7);
			}

			Project project = m_projectDao.findByDomain(domainName, ProjectEntity.READSET_FULL);
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
		} catch (Exception ex) {
			Cat.logError("build contact info error for doamin: " + domainName, ex);
		}

		return "";
	}

	public String generateContent(AlertEntity alert) {
		return alert.getContent() + buildContactInfo(alert.getGroup());
	}

}
