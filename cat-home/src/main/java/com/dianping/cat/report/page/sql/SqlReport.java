package com.dianping.cat.report.page.sql;

import java.util.Date;
import java.util.List;

public class SqlReport {
	private Date m_startTime;

	private Date m_endTime;

	private List<String> m_domains;

	private String m_domain;

	private List<SqlReportModel> m_reportRecords;

	public Date getStartTime() {
   	return m_startTime;
   }

	public SqlReport setStartTime(Date startTime) {
   	m_startTime = startTime;
   	return this;
   }

	public Date getEndTime() {
   	return m_endTime;
   }

	public SqlReport setEndTime(Date endTime) {
   	m_endTime = endTime;
   	return this;
   }

	public List<String> getDomains() {
   	return m_domains;
   }

	public SqlReport setDomains(List<String> domains) {
   	m_domains = domains;
   	return this;
   }

	public String getDomain() {
   	return m_domain;
   }

	public SqlReport setDomain(String domain) {
   	m_domain = domain;
   	return this;
   }

	public List<SqlReportModel> getReportRecords() {
   	return m_reportRecords;
   }

	public void setReportRecords(List<SqlReportModel> reportRecords) {
   	m_reportRecords = reportRecords;
   }
	
	
}
