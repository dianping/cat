package com.dianping.cat.report.page.monitor;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("key")
	private String m_key;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("timestamp")
	private long m_timestamp;

	@FieldMeta("count")
	private long m_count = 1;

	@FieldMeta("avg")
	private double m_avg;

	@FieldMeta("sum")
	private double m_sum;

	@FieldMeta("value")
	private double m_value;

	@FieldMeta("batch")
	private String m_batch;

	public Payload() {
		super(ReportPage.MONITOR);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public double getAvg() {
		return m_avg;
	}

	public String getBatch() {
		return m_batch;
	}

	public long getCount() {
		return m_count;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getGroup() {
		return m_group;
	}

	public String getKey() {
		return m_key;
	}

	public double getSum() {
		return m_sum;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public String getType() {
		return m_type;
	}

	public double getValue() {
		return m_value;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.COUNT_API);
	}

	public void setAvg(double avg) {
		m_avg = avg;
	}

	public void setBatch(String batch) {
		m_batch = batch;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public void setKey(String key) {
		m_key = key;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.MONITOR);
	}

	public void setSum(double sum) {
		m_sum = sum;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setValue(double value) {
		m_value = value;
		m_count = (long) value;
		m_avg = value;
		m_sum = value;
	}

	@Override
	public String toString() {
		return "Payload [m_group=" + m_group + ", m_key=" + m_key + ", m_type=" + m_type + ", m_domain=" + m_domain
		      + ", m_timestamp=" + m_timestamp + "]";
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.COUNT_API;
		}
	}
}
