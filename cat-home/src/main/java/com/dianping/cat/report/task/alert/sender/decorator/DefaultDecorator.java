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

	private DateFormat m_fromat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	protected String buildContactInfo(String domainName) {
		try {
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

		return null;
	}

	protected String buildExceptionContent(AlertEntity alert) {
		try {
			StringBuilder sb = new StringBuilder();
			String domain = alert.getGroup();
			String date = m_fromat.format(alert.getAlertDate());

			sb.append("[CAT异常告警] [项目: ").append(domain).append("] : ");
			sb.append(alert.getContent()).append("[时间: ").append(date).append("]");
			sb.append(" <a href='").append("http://cat.dianpingoa.com/cat/r/p?domain=").append(domain).append("&date=")
			      .append(date).append("'>点击此处查看详情</a>").append("<br/>");

			return sb.toString();
		} catch (Exception ex) {
			Cat.logError("build exception content error:" + alert.toString(), ex);
			return null;
		}
	}

	public String generateTitle(AlertEntity alert) {
		String type = alert.getType();
		if ("business".equals(type)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[业务告警] [产品线 ").append(alert.getProductline()).append("]");
			sb.append("[业务指标 ").append(alert.getMetric()).append("]");
			return sb.toString();
		}

		if ("network".equals(type)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[网络告警] [产品线 ").append(alert.getProductline()).append("]");
			sb.append("[网络指标 ").append(alert.getMetric()).append("]");
			return sb.toString();
		}

		if ("system".equals(type)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[系统告警] [产品线 ").append(alert.getProductline()).append("]");
			sb.append("[系统指标 ").append(alert.getMetric()).append("]");
			return sb.toString();
		}

		if ("exception".equals(type)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[CAT异常告警] [项目: ").append(alert.getGroup()).append("]");
			return sb.toString();
		}

		return "";
	}

	public abstract String generateContent(AlertEntity alert);

}
