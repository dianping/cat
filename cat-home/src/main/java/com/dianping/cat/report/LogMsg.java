package com.dianping.cat.report;

import java.util.ArrayList;
import java.util.List;

public class LogMsg implements Comparable<LogMsg> {

	private final int MAX_ID_COUNT = 60;

	private String m_msg;

	private int m_count;

	private List<Integer> m_ids = new ArrayList<Integer>();

	public void addCount() {
		m_count++;
	}

	public void addId(int id) {
		if (m_ids.size() < MAX_ID_COUNT) {
			m_ids.add(id);
		}
	}

	@Override
	public int compareTo(LogMsg o) {
		return o.getCount() - m_count;
	}

	public int getCount() {
		return m_count;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public List<Integer> getIds() {
		return m_ids;
	}

	public void setIds(List<Integer> ids) {
		m_ids = ids;
	}

	public String getMsg() {
		return m_msg;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

}
