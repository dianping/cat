package com.dianping.cat.configuration2;

import java.util.List;

import com.dianping.cat.configure.client2.entity.Server;

public interface ConfigureManager {
	public String getDomain();

	public List<Server> getServers();
}
