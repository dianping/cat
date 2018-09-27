package com.dianping.cat.alarm.spi.spliter;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.spi.AlertChannel;

@Named
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
