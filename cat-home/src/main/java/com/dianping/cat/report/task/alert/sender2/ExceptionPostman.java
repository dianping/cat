package com.dianping.cat.report.task.alert.sender2;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.home.alert.type.entity.Type;
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.task.alert.exception.ExceptionAlertConfig;

public class ExceptionPostman extends Postman {

	private Project queryProjectByDomain(String projectName) {
		Project project = null;
		try {
			project = m_projectDao.findByDomain(projectName, ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return project;
	}

	public boolean sendAlert(ExceptionAlertConfig alertConfig, AlertExceptionBuilder alertBuilder, String domain,
	      List<AlertException> exceptions) {
		boolean sendResult = true;
		Project project = queryProjectByDomain(domain);
		String mailTitle = alertConfig.buildMailTitle(domain, null);
		String contactInfo = buildContactInfo(domain);
		Type type = new Type();

		for (AlertException exception : exceptions) {
			Type tmpType = m_alertTypeManager.getType(alertConfig.getId(), domain, exception.getType());

			if (tmpType.isSendMail()) {
				type.setSendMail(true);
			}
			if (tmpType.isSendWeixin()) {
				type.setSendWeixin(true);
			}
			if (tmpType.isSendSms()) {
				type.setSendSms(true);
			}
		}

		if (type.isSendMail()) {
			List<String> emails = alertConfig.buildMailReceivers(project);
			String mailContent = alertBuilder.buildMailContent(exceptions.toString(), domain, contactInfo);
			if (!m_mailSender.sendAlert(emails, domain, mailTitle, mailContent)) {
				sendResult = false;
			}
		}

		if (type.isSendWeixin()) {
			List<String> weixins = alertConfig.buildWeiXinReceivers(project);
			List<AlertException> errorExceptions = alertBuilder.buildErrorException(exceptions);
			String weixinContent = alertBuilder.buildContent(errorExceptions.toString(), domain, contactInfo);
			if (!m_weixinSender.sendAlert(weixins, domain, mailTitle, weixinContent)) {
				sendResult = false;
			}
		}

		if (type.isSendSms()) {
			List<String> phones = alertConfig.buildSMSReceivers(project);
			List<AlertException> errorAndTriggeredExceptions = alertBuilder.buildErrorAndTriggeredException(exceptions);
			String smsContent = alertBuilder.buildContent(errorAndTriggeredExceptions.toString(), domain, contactInfo);
			if (!m_smsSender.sendAlert(phones, domain, mailTitle, smsContent)) {
				sendResult = false;
			}
		}

		return sendResult;
	}
}
