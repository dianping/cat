package com.dianping.cat.report.task.alert.sender.receiver;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.AlertConfigManager;

public class TransactionContactor extends ProjectContactor implements Contactor {

	@Inject
	protected ProjectService m_projectService;

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	public static final String ID = AlertType.Transaction.getName();

	@Override
	public String getId() {
		return ID;
	}

}
