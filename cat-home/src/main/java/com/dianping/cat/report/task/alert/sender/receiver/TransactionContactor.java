package com.dianping.cat.report.task.alert.sender.receiver;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.service.ProjectService;

public class TransactionContactor extends ProjectContactor {

	@Inject
	protected ProjectService m_projectService;

	public static final String ID = AlertType.Transaction.getName();

	@Override
	public String getId() {
		return ID;
	}

}
