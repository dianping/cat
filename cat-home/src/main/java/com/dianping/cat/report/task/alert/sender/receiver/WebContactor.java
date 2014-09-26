package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.AlertConfigManager;

public class WebContactor extends DefaultContactor implements Contactor {

	@Inject
	protected ProjectService m_projectService;

	@Inject
	protected UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	public static final String ID = AlertType.Web.getName();

	@Override
	public String getId() {
		return ID;
	}

	private String queryDomainByUrl(String url) {
		String domain = "";
		PatternItem patternItem = m_urlPatternConfigManager.queryUrlPattern(url);

		if (patternItem != null) {
			domain = patternItem.getDomain();
		}
		return domain;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();

		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));

			System.out.println(queryDomainByUrl(id));
			Project project = m_projectService.findByDomain(queryDomainByUrl(id));
			if (project != null) {
				mailReceivers.addAll(split(project.getEmail()));
			}

			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));

			Project project = m_projectService.findByDomain(queryDomainByUrl(id));

			if (project != null) {
				weixinReceivers.addAll(split(project.getEmail()));
			}

			return weixinReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			Project project = m_projectService.findByDomain(queryDomainByUrl(id));

			if (project != null) {
				smsReceivers.addAll(split(project.getPhone()));
			}

			return smsReceivers;
		}
	}

}
