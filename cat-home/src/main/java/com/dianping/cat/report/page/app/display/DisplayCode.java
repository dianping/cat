package com.dianping.cat.report.page.app.display;

public class DisplayCode {

	private String m_id;

	private long m_count;

	private double m_sum;

	private double m_avg;

	private long m_errors;

	private double m_successRatio;

	private String m_title;

	public DisplayCode(String id) {
		m_id = id;
	}

	public double getAvg() {
		return m_avg;
	}

	public long getCount() {
		return m_count;
	}

	public long getErrors() {
		return m_errors;
	}

	public String getId() {
		return m_id;
	}

	public double getSuccessRatio() {
		return m_successRatio;
	}

	public double getSum() {
		return m_sum;
	}

	public String getTitle() {
		return m_title;
	}

	public DisplayCode incCount() {
		m_count++;
		return this;
	}

	public DisplayCode incCount(long count) {
		m_count += count;
		return this;
	}

	public DisplayCode incErrors() {
		m_errors++;
		return this;
	}

	public DisplayCode incErrors(long errors) {
		m_errors += errors;
		return this;
	}

	public DisplayCode incSum() {
		m_sum++;
		return this;
	}

	public DisplayCode incSum(double sum) {
		m_sum += sum;
		return this;
	}

	public void setAvg(double avg) {
		m_avg = avg;
	}

	public void setCount(long count) {
		m_count = count;
	}

	public void setErrors(long errors) {
		m_errors = errors;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setSuccessRatio(double successRatio) {
		m_successRatio = successRatio;
	}

	public void setSum(double sum) {
		m_sum = sum;
	}

	public void setTitle(String title) {
		m_title = title;
	}

}
