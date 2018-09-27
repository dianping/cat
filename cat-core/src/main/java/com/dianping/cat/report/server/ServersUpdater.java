package com.dianping.cat.report.server;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface ServersUpdater {

	public Map<String, Set<String>> buildServers(Date hour);

}
