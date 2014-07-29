package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.system.config.AlertConfigManager;

public abstract class ProductlineContactor extends DefaultContactor implements Contactor {

	@Inject
	protected ProjectDao m_projectDao;

	@Inject
	protected AlertConfigManager m_configManager;

	protected Project m_project = new Project();

	@Override
	public List<String> queryEmailContactors() {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(split(m_project.getEmail()));

			return mailReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors() {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(split(m_project.getPhone()));

			return smsReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors() {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			weixinReceivers.addAll(split(m_project.getEmail()));

			return weixinReceivers;
		}
	}

	public void setModule(String domainName) {
		try {
			m_project = m_projectDao.findByDomain(domainName, ProjectEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError("query project error:" + domainName, e);
		}
	}

}
