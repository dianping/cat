package com.dianping.cat.report.alert.thirdParty;

public class ThirdPartyAlertEntity {

	private String m_type;

	private String m_domain;

	private String m_details;

	public String getDetails() {
		return m_details;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getType() {
		return m_type;
	}

	public ThirdPartyAlertEntity setDetails(String details) {
		m_details = details;
		return this;
	}

	public ThirdPartyAlertEntity setDomain(String domain) {
		m_domain = domain;
		return this;
	}

	public ThirdPartyAlertEntity setType(String type) {
		m_type = type;
		return this;
	}

	@Override
	public String toString() {
		return "[type=" + m_type + ", details=" + m_details + "]";
	}

}
