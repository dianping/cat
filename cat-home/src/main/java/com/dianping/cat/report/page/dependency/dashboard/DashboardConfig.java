package com.dianping.cat.report.page.dependency.dashboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class DashboardConfig implements Initializable {

	private Map<String, Group> m_groups = new LinkedHashMap<String, Group>();

	private Set<String> m_allDomains = new HashSet<String>();
	
	public boolean contains(String domain){
		return m_allDomains.contains(domain);
	}
	public Map<String, Group> getGroups() {
   	return m_groups;
   }

	@Override
	public void initialize() throws InitializationException {
		addDomain("TuanGou", "TuanGouWeb");
		addDomain("TuanGou", "TuanGouApi");
		addDomain("TuanGou", "TuanGouRemote");
		addDomain("TuanGou", "TuanGouApiMobile");
		addDomain("TuanGou", "DealService");

		addDomain("TuanGou2", "tuangou-paygate");
		addDomain("TuanGou2", "TuanGouWap");
		addDomain("TuanGou2", "BCTuangouWeb");
		addDomain("TuanGou2", "Unipay");

		addDomain("TuanGou3", "PayChannel");
		addDomain("TuanGou3", "PayEngine");
		addDomain("TuanGou3", "PayOrder");
		addDomain("TuanGou3", "BCTuangouServer");

		addDomain("TuanGou4", "TuanGouMT");
		addDomain("TuanGou4", "TuanGouTracking");
		addDomain("TuanGou4", "TuanGouOperation");
	}

	private void addDomain(String group, String domain) {
		Group g = m_groups.get(group);

		if (g == null) {
			g = new Group();
			m_groups.put(group, g);
		}

		g.addDomain(domain);
		m_allDomains.add(domain);
	}

	public static class Group {
		private String m_name;

		private List<String> m_domains = new ArrayList<String>();

		public Group addDomain(String domain) {
			m_domains.add(domain);
			return this;
		}

		public String getName() {
			return m_name;
		}

		public void setName(String name) {
			m_name = name;
		}

		public List<String> getDomains() {
			return m_domains;
		}

		public void setDomains(List<String> domains) {
			m_domains = domains;
		}
	}

}
