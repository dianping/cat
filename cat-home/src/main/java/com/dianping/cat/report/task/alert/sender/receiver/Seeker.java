package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.system.config.AlertConfigManager;
import com.site.helper.Splitters;

public class Seeker {

	@Inject
	protected ProjectDao m_projectDao;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected AlertConfigManager m_configManager;

	public List<String> queryReceivers(String productlineName, AlertChannel channel, String type) {
		if ("exception".equals(type) || "thirdparty".equals(type)) {
			return queryReceiversByDomain(productlineName, channel, type);
		} else {
			return queryReceiversByProductline(productlineName, channel, type);
		}
	}

	public List<String> queryReceiversByProductline(String productlineName, AlertChannel channel, String type) {
		ProductLine productline = m_productLineConfigManager.queryProductLine(productlineName);

		if (channel == AlertChannel.MAIL) {
			return buildMailReceivers(productline, type);
		}

		if (channel == AlertChannel.WEIXIN) {
			return buildWeixinReceivers(productline, type);
		}

		if (channel == AlertChannel.SMS) {
			return buildSMSReceivers(productline, type);
		}

		return new ArrayList<String>();
	}

	public List<String> queryReceiversByDomain(String domainName, AlertChannel channel, String type) {
		try {
			Project project = m_projectDao.findByDomain(domainName, ProjectEntity.READSET_FULL);

			if (channel == AlertChannel.MAIL) {
				return buildMailReceivers(project, type);
			}

			if (channel == AlertChannel.WEIXIN) {
				return buildWeixinReceivers(project, type);
			}

			if (channel == AlertChannel.SMS) {
				return buildSMSReceivers(project, type);
			}

		} catch (DalException e) {
			Cat.logError("query receivers error:" + domainName + " " + channel + " " + type, e);
		}

		return new ArrayList<String>();

	}

	private List<String> buildMailReceivers(ProductLine productLine, String type) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(type);

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(buildProductlineMailReceivers(productLine));

			return mailReceivers;
		}
	}

	private List<String> buildMailReceivers(Project project, String type) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(type);

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(buildProjectMailReceivers(project));

			return mailReceivers;
		}
	}

	private List<String> buildDefaultMailReceivers(Receiver receiver) {
		List<String> mailReceivers = new ArrayList<String>();

		if (receiver != null) {
			mailReceivers.addAll(receiver.getEmails());
		}
		return mailReceivers;
	}

	private List<String> buildProductlineMailReceivers(ProductLine productLine) {
		return split(productLine.getEmail());
	}

	private List<String> buildProjectMailReceivers(Project project) {
		return split(project.getEmail());
	}

	private List<String> buildSMSReceivers(ProductLine productLine, String type) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(type);

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(buildProductlineSMSReceivers(productLine));

			return smsReceivers;
		}
	}

	private List<String> buildSMSReceivers(Project project, String type) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(type);

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(buildProjectSMSReceivers(project));

			return smsReceivers;
		}
	}

	private List<String> buildDefaultSMSReceivers(Receiver receiver) {
		List<String> smsReceivers = new ArrayList<String>();

		if (receiver != null) {
			smsReceivers.addAll(receiver.getPhones());
		}
		return smsReceivers;
	}

	private List<String> buildProductlineSMSReceivers(ProductLine productLine) {
		return split(productLine.getPhone());
	}

	private List<String> buildProjectSMSReceivers(Project project) {
		return split(project.getPhone());
	}

	private List<String> buildWeixinReceivers(Project project, String type) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(type);

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			weixinReceivers.addAll(buildProjectMailReceivers(project));

			return weixinReceivers;
		}
	}

	private List<String> buildWeixinReceivers(ProductLine productLine, String type) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(type);

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			weixinReceivers.addAll(buildProductlineWeixinReceivers(productLine));

			return weixinReceivers;
		}
	}

	private List<String> buildDefaultWeixinReceivers(Receiver receiver) {
		List<String> weixinReceivers = new ArrayList<String>();

		if (receiver != null) {
			weixinReceivers.addAll(receiver.getWeixins());
		}
		return weixinReceivers;
	}

	private List<String> buildProductlineWeixinReceivers(ProductLine productLine) {
		return split(productLine.getEmail());
	}

	private List<String> split(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().split(str));
		}

		return result;
	}

}
