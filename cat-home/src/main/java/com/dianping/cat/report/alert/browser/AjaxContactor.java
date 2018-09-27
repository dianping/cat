package com.dianping.cat.report.alert.browser;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.PatternItem;

public class AjaxContactor extends ProjectContactor {

	@Inject
	protected UrlPatternConfigManager m_urlPatternConfigManager;

	public static final String ID = AlertType.Ajax.getName();

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
		return super.queryEmailContactors(queryDomainByUrl(id));
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		return super.queryWeiXinContactors(queryDomainByUrl(id));
	}

	@Override
	public List<String> querySmsContactors(String id) {
		return super.querySmsContactors(queryDomainByUrl(id));
	}

	@Override
	public List<String> queryDXContactors(String id) {
		return super.queryDXContactors(queryDomainByUrl(id));
	}
}
