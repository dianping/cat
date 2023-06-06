package com.dianping.cat.report.alert.app;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import com.dianping.cat.config.app.AppCommandConfigManager;
import org.unidal.lookup.annotation.Inject;

public class AppContactor extends ProjectContactor {

	@Inject
	protected AppCommandConfigManager m_appConfigManager;

	public static final String ID = AlertType.App.getName();

	@Override
	public String getId() {
		return ID;
	}
}
