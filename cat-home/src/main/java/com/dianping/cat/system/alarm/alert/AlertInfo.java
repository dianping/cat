package com.dianping.cat.system.alarm.alert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlertInfo {

	public static final String EMAIL = "email";

	public static final int EMAIL_TYPE = 0;

	public static final String EXCEPTION = "exception";

	public static final String SERVICE = "service";

	public static final String SMS = "sms";

	public static final int SMS_TYPE = 1;

	private String m_content;

	private Date m_date;

	private List<String> m_mails;

	private List<String> m_phones;

	private int m_ruleId;

	private String m_title;

	private int m_type;

	public String getContent() {
		return m_content;
	}

	public Date getDate() {
		return m_date;
	}

	public List<String> getMails() {
		if (m_mails == null) {
			return new ArrayList<String>();
		}
		return m_mails;
	}

	public List<String> getPhones() {
		if (m_phones == null) {
			return new ArrayList<String>();
		}
		return m_phones;
	}

	public int getRuleId() {
		return m_ruleId;
	}

	public String getTitle() {
		return m_title;
	}

	public int getType() {
		return m_type;
	}

	public AlertInfo setContent(String content) {
		m_content = content;
		return this;
	}

	public AlertInfo setDate(Date date) {
		m_date = date;
		return this;
	}

	public AlertInfo setMails(List<String> mails) {
		m_mails = mails;
		return this;
	}

	public void setPhones(List<String> phones) {
		m_phones = phones;
	}

	public void setRuleId(int ruleId) {
		m_ruleId = ruleId;
	}

	public AlertInfo setTitle(String title) {
		m_title = title;
		return this;
	}

	public void setType(int type) {
		m_type = type;
	}

}
