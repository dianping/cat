package com.dianping.cat.broker.api.page.batch;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class UrlParser implements Initializable {

	private Set<String> m_domains = new HashSet<String>();

	public String parse(String url) {
		for (String str : m_domains) {
			if (url.indexOf(str) > -1) {
				return str;
			}
		}
		return null;
	}

	@Override
	public void initialize() throws InitializationException {
		m_domains.add("m.dianping.com");
		m_domains.add("mm.dianping.com");
		m_domains.add("www.dianping.com");
		m_domains.add("tgapp.dianping.com");
		m_domains.add("evt.dianping.com");
	}

}
