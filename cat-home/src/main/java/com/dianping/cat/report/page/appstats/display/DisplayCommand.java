package com.dianping.cat.report.page.appstats.display;

import java.util.LinkedHashMap;
import java.util.Map;

public class DisplayCommand {

	private int m_id;

	private String m_name;

	private long m_count;

	private double m_sum;

	private double m_avg;

	private long m_errors;

	private double m_successRatio;

	private long m_requestSum;

	private double m_requestAvg;

	private long m_responseSum;

	private double m_responseAvg;

	private String m_domain;

	private String m_title;

	private Map<String, DisplayCode> m_codes = new LinkedHashMap<String, DisplayCode>();

	public DisplayCommand(int id) {
		m_id = id;
	}

	public DisplayCommand addCode(DisplayCode code) {
		m_codes.put(code.getId(), code);
		return this;
	}

	public DisplayCode findCode(String id) {
		return m_codes.get(id);
	}

	public DisplayCode findOrCreateCode(String id) {
		DisplayCode code = m_codes.get(id);

		if (code == null) {
			synchronized (m_codes) {
				code = m_codes.get(id);

				if (code == null) {
					code = new DisplayCode(id);
					m_codes.put(id, code);
				}
			}
		}

		return code;
	}

	public double getAvg() {
		return m_avg;
	}

	public Map<String, DisplayCode> getCodes() {
		return m_codes;
	}

	public long getCount() {
		return m_count;
	}

	public String getDomain() {
		return m_domain;
	}

	public long getErrors() {
		return m_errors;
	}

	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public double getRequestAvg() {
		return m_requestAvg;
	}

	public long getRequestSum() {
		return m_requestSum;
	}

	public double getResponseAvg() {
		return m_responseAvg;
	}

	public long getResponseSum() {
		return m_responseSum;
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

	public DisplayCommand incCount() {
		m_count++;
		return this;
	}

	public DisplayCommand incCount(long count) {
		m_count += count;
		return this;
	}

	public DisplayCommand incErrors() {
		m_errors++;
		return this;
	}

	public DisplayCommand incErrors(long errors) {
		m_errors += errors;
		return this;
	}

	public DisplayCommand incRequestSum() {
		m_requestSum++;
		return this;
	}

	public DisplayCommand incRequestSum(long requestSum) {
		m_requestSum += requestSum;
		return this;
	}

	public DisplayCommand incResponseSum() {
		m_responseSum++;
		return this;
	}

	public DisplayCommand incResponseSum(long responseSum) {
		m_responseSum += responseSum;
		return this;
	}

	public DisplayCommand incSum() {
		m_sum++;
		return this;
	}

	public DisplayCommand incSum(double sum) {
		m_sum += sum;
		return this;
	}

	public void setAvg(double avg) {
		m_avg = avg;
	}

	public void setCodes(Map<String, DisplayCode> codes) {
		m_codes = codes;
	}

	public void setCount(long count) {
		m_count = count;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setErrors(long errors) {
		m_errors = errors;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setRequestAvg(double requestAvg) {
		m_requestAvg = requestAvg;
	}

	public void setRequestSum(long requestSum) {
		m_requestSum = requestSum;
	}

	public void setResponseAvg(double responseAvg) {
		m_responseAvg = responseAvg;
	}

	public void setResponseSum(long responseSum) {
		m_responseSum = responseSum;
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
