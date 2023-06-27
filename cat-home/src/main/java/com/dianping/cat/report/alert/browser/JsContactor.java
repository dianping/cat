package com.dianping.cat.report.alert.browser;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import org.unidal.lookup.annotation.Inject;

public class JsContactor extends ProjectContactor implements Contactor {

	public static final String ID = AlertType.JS.getName();

	@Inject
	protected JsRuleConfigManager m_jsRuleConfigManager;

	@Override
	public String getId() {
		return ID;
	}
}
