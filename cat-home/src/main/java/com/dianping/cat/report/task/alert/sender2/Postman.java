package com.dianping.cat.report.task.alert.sender2;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.home.alert.type.entity.Type;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.BaseAlertConfig;
import com.dianping.cat.system.config.AlertPolicyManager;
import com.site.lookup.util.StringUtils;

public class Postman {

	@Inject
	protected ProjectDao m_projectDao;

	@Inject
	protected MailSender m_mailSender;

	@Inject
	protected SmsSender m_smsSender;

	@Inject
	protected WeixinSender m_weixinSender;

	@Inject
	protected AlertPolicyManager m_alertTypeManager;

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

	protected List<String> queryReceivers() {

		return null;
	}

	public boolean sendAlert(BaseAlertConfig alertConfig, AlertResultEntity alertResult, ProductLine productline,
	      String domain, String mailTitle, String configId) {
		String content = alertResult.getContent();
		String contactInfo = buildContactInfo(domain);
		Type type = m_alertTypeManager.getType(configId, domain, alertResult.getAlertLevel());
		boolean sendResult = true;

		if (type.isSendMail()) {
			String mailContent = content + "<br/>" + contactInfo;
			List<String> receivers = alertConfig.buildMailReceivers(productline);
			if (!m_mailSender.sendAlert(receivers, domain, mailTitle, mailContent)) {
				sendResult = false;
			}
		}

		if (type.isSendWeixin()) {
			String weixinContent = content + "\n" + contactInfo;
			List<String> receivers = alertConfig.buildMailReceivers(productline);
			if (!m_weixinSender.sendAlert(receivers, domain, mailTitle, weixinContent)) {
				sendResult = false;
			}
		}

		if (type.isSendSms()) {
			String smsContent = content + contactInfo;
			List<String> receivers = alertConfig.buildSMSReceivers(productline);
			if (!m_smsSender.sendAlert(receivers, domain, mailTitle, smsContent)) {
				sendResult = false;
			}
		}

		Cat.logEvent(configId, productline.getId(), Event.SUCCESS, mailTitle + "  " + content);
		return sendResult;
	}

}
