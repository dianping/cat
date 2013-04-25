package com.dianping.cat.report;

import java.util.Set;

public interface IReport {
	public String getDomain();

	public Set<String> getDomainNames();

	public java.util.Date getEndTime();

	public Set<String> getIps();

	public java.util.Date getStartTime();

	public IReport setDomain(String domain);

	public IReport setEndTime(java.util.Date endTime);

	public IReport setStartTime(java.util.Date startTime);
}
