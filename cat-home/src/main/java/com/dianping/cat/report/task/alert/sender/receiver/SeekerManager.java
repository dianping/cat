package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class SeekerManager extends ContainerHolder implements Initializable {

	private Map<String, Seeker> m_seekers = new HashMap<String, Seeker>();

	public List<String> queryReceivers(String type, String productlineName, AlertChannel channel) {
		Seeker seeker = m_seekers.get(type);

		if (seeker != null) {
			return seeker.queryReceivers(productlineName, channel);
		}

		return new ArrayList<String>();
	}

	@Override
	public void initialize() throws InitializationException {
		m_seekers = lookupMap(Seeker.class);
	}
}
