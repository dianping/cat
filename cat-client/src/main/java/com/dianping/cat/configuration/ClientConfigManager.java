package com.dianping.cat.configuration;

import java.io.File;
import java.util.List;

import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;

public interface ClientConfigManager {

	public Domain getDomain();

	public int getMaxMessageLength();

	public String getServerConfigUrl();

	public List<Server> getServers();

	public int getTaggedTransactionCacheSize();

	public void initialize(File configFile) throws Exception;

	public boolean isCatEnabled();

	public boolean isDumpLocked();

}