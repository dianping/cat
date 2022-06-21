package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.Log;
import com.dianping.cat.message.LogSegment;

public class DefaultLogSegment implements LogSegment {
	private List<Log> m_logs = new ArrayList<>();

	@Override
	public List<Log> getLogs() {
		return m_logs;
	}

	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHostName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIpAddress() {
		// TODO Auto-generated method stub
		return null;
	}
}
