package com.dianping.cat.report.task.alert.sender.decorator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.site.lookup.util.StringUtils;

public abstract class Decorator {

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
		StringBuilder sb = new StringBuilder();
		String domain = alert.getGroup();
		String date = m_fromat.format(alert.getAlertDate());
		AlertException exception = (AlertException) alert.getParas().get("exception");

		sb.append("[CAT异常告警] [项目: ").append(domain).append("] : ");
		sb.append(exception.toString()).append("[时间: ").append(date).append("]");
		sb.append(" <a href='").append("http://cat.dianpingoa.com/cat/r/p?domain=").append(domain).append("&date=")
		      .append(date).append("'>点击此处查看详情</a>");

		return sb.toString();
	}

	public String generateTitle(AlertEntity alert) {
		if ("business".equals(alert.getType())) {
			StringBuilder sb = new StringBuilder();
			sb.append("[业务告警] [产品线 ").append(alert.getProductline()).append("]");
			sb.append("[业务指标 ").append(alert.getMetricName()).append("]");
			return sb.toString();
		}

		if ("network".equals(alert.getType())) {
			StringBuilder sb = new StringBuilder();
			sb.append("[网络告警] [产品线 ").append(alert.getProductline()).append("]");
			sb.append("[网络指标 ").append(alert.getMetricName()).append("]");
			return sb.toString();
		}

		if ("system".equals(alert.getType())) {
			StringBuilder sb = new StringBuilder();
			sb.append("[系统告警] [产品线 ").append(alert.getProductline()).append("]");
			sb.append("[系统指标 ").append(alert.getMetricName()).append("]");
			return sb.toString();
		}

		if ("exception".equals(alert.getType())) {
			StringBuilder sb = new StringBuilder();
			sb.append("[CAT异常告警] [项目: ").append(alert.getGroup()).append("]");
			return sb.toString();
		}

		return "";
	}

	public abstract String generateContent(AlertEntity alert);

}
