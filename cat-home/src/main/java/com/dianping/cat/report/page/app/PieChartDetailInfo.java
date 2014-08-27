package com.dianping.cat.report.page.app;

public class PieChartDetailInfo {

	private int m_id;

	private String m_title;

	private double m_requestSum;

	private double m_successRatio;

	public int getId() {
		return m_id;
	}

	public PieChartDetailInfo setId(int id) {
		m_id = id;
		return this;
	}

	public String getTitle() {
		return m_title;
	}

	public PieChartDetailInfo setTitle(String title) {
		m_title = title;
		return this;
	}

	public double getRequestSum() {
		return m_requestSum;
	}

	public PieChartDetailInfo setRequestSum(double requestSum) {
		m_requestSum = requestSum;
		return this;
	}

	public double getSuccessRatio() {
		return m_successRatio;
	}

	public void setSuccessRatio(double successRatio) {
		m_successRatio = successRatio;
	}

	@Override
   public String toString() {
	   return "PieChartDetailInfo [m_id=" + m_id + ", m_title=" + m_title + ", m_requestSum=" + m_requestSum
	         + ", m_successRatio=" + m_successRatio + "]";
   }
}
