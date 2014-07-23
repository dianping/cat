package com.dianping.cat.report.task.alert.thirdParty;

import java.util.List;

import com.dianping.cat.home.alert.thirdParty.entity.Domain;

public class ThirdPartyAlertEntity {

	private String m_type;

	private List<Domain> m_domains;

	private String m_details;

	public String getType() {
		return m_type;
	}

	public String getDetails() {
		return m_details;
	}

	public List<Domain> getDomains() {
		return m_domains;
	}

	public ThirdPartyAlertEntity setType(String type) {
		m_type = type;
		return this;
	}

	public ThirdPartyAlertEntity setDetails(String details) {
		m_details = details;
		return this;
	}

	public ThirdPartyAlertEntity setDomains(List<Domain> domains) {
		m_domains = domains;
		return this;
	}

	@Override
	public String toString() {
		return "[type=" + m_type + ", details=" + m_details + "]";
	}

}
