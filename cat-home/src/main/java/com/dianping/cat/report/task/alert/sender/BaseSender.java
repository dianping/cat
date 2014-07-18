package com.dianping.cat.report.task.alert.sender;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.task.alert.BaseAlertConfig;
import com.dianping.cat.system.tool.MailSMS;

public enum BaseSender {

	MailSender {
		@Override
		protected void sendLog(String title, String content, List<String> receivers) {
			StringBuilder builder = new StringBuilder();

			builder.append(title).append(",").append(content).append(",");
			for (String receiver : receivers) {
				builder.append(receiver).append(" ");
			}

			Cat.logEvent("SendMail", builder.toString());
		}

		@Override
		protected List<String> queryReceivers(ProductLine productLine, String configId) {
			return m_alertConfig.buildMailReceivers(productLine, configId);
		}

		@Override
		public boolean sendAlert(List<String> receivers, String domain, String title, String content, String alertType,
		      String configId) {
			try {
				m_mailSms.sendEmail(title, content, receivers);
				sendLog(title, content, receivers);
				return true;
			} catch (Exception ex) {
				Cat.logError("send mail error" + " " + title + " " + content, ex);
				return false;
			}
		}
	},

	WeixinSender {
		@Override
		protected void sendLog(String title, String content, List<String> receivers) {
			StringBuilder builder = new StringBuilder();

			builder.append(title).append(" ").append(content).append(" ");
			for (String receiver : receivers) {
				builder.append(receiver).append(" ");
			}

			Cat.logEvent("SendWeixin", builder.toString());
		}

		@Override
		protected List<String> queryReceivers(ProductLine productLine, String configId) {
			return m_alertConfig.buildMailReceivers(productLine, configId);
		}

		@Override
		public boolean sendAlert(List<String> receivers, String domain, String title, String content, String alertType,
		      String configId) {
			if (alertType == null || !alertType.equals("error")) {
				return true;
			}

			try {
				m_mailSms.sendWeiXin(title, content, domain, mergeList(receivers));
				sendLog(title, content, receivers);
				return true;
			} catch (Exception ex) {
				Cat.logError("send weixin error" + " " + title + " " + content, ex);
				return false;
			}
		}

		private String mergeList(List<String> receivers) {
			StringBuilder builder = new StringBuilder();

			for (String receiver : receivers) {
				builder.append(receiver).append(",");
			}

			String tmpResult = builder.toString();
			if (tmpResult.endsWith(",")) {
				return tmpResult.substring(0, tmpResult.length() - 1);
			} else {
				return tmpResult;
			}
		}
	},

	SmsSender {
		@Override
		protected void sendLog(String title, String content, List<String> receivers) {
			StringBuilder builder = new StringBuilder();

			builder.append(title).append(" ").append(content).append(" ");
			for (String receiver : receivers) {
				builder.append(receiver).append(" ");
			}

			Cat.logEvent("SendSms", builder.toString());
		}

		@Override
		protected List<String> queryReceivers(ProductLine productLine, String configId) {
			return m_alertConfig.buildSMSReceivers(productLine, configId);
		}

		@Override
		public boolean sendAlert(List<String> receivers, String domain, String title, String content, String alertType,
		      String configId) {
			if (alertType == null || !alertType.equals("error")) {
				return true;
			}

			try {
				m_mailSms.sendSms(title, content, receivers);
				sendLog(title, content, receivers);
				return true;
			} catch (Exception ex) {
				Cat.logError("send sms error" + " " + title + " " + content, ex);
				return false;
			}
		}
	};

	@Inject
	protected BaseAlertConfig m_alertConfig;

	@Inject
	protected MailSMS m_mailSms;

	public boolean sendAllAlert(ProductLine productLine, String domain, String title, String content, String alertType,
	      String configId) {
		boolean sendResult = true;

		for (BaseSender sender : BaseSender.values()) {
			List<String> receivers = sender.queryReceivers(productLine, configId);
			if (!sender.sendAlert(receivers, domain, title, content, alertType, configId)) {
				sendResult = false;
			}
		}
		return sendResult;
	}

	protected abstract List<String> queryReceivers(ProductLine productLine, String configId);

	protected abstract void sendLog(String title, String content, List<String> receivers);

	public abstract boolean sendAlert(List<String> receivers, String domain, String title, String content,
	      String alertType, String configId);

}
