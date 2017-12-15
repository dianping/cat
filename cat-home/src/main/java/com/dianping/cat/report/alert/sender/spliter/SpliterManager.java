package com.dianping.cat.report.alert.sender.spliter;

import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.report.alert.sender.AlertChannel;

public class SpliterManager extends ContainerHolder implements Initializable {

	private Map<String, Spliter> m_spliters = new HashMap<String, Spliter>();

	@Override
	public void initialize() throws InitializationException {
		m_spliters = lookupMap(Spliter.class);
	}

	public String process(String content, AlertChannel channel) {
		String channelName = channel.getName();
		Spliter splitter = m_spliters.get(channelName);
		
		return splitter.process(content);
	}

}
