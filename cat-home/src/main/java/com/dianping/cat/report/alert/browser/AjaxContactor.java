package com.dianping.cat.report.alert.browser;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import org.unidal.lookup.annotation.Inject;

public class AjaxContactor extends ProjectContactor {

	@Inject
	protected UrlPatternConfigManager m_urlPatternConfigManager;

	public static final String ID = AlertType.Ajax.getName();

	@Override
	public String getId() {
		return ID;
	}
}
