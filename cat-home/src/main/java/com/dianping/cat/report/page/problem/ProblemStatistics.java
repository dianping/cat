package com.dianping.cat.report.page.problem;

public class ProblemStatistics {
	private String m_type;
	
	private int m_count;

	public String getType() {
   	return m_type;
   }

	public ProblemStatistics setType(String type) {
   	m_type = type;
   	return this;
   }

	public int getCount() {
   	return m_count;
   }

	public ProblemStatistics setCount(int count) {
   	m_count = count;
   	return this;
   }
}
