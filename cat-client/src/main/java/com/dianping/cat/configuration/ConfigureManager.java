package com.dianping.cat.configuration;

import java.util.List;

import com.dianping.cat.configuration.model.entity.Server;

public interface ConfigureManager {
	public boolean getBooleanProperty(String name, boolean defaultValue);

	public String getDomain();

	public double getDoubleProperty(String name, double defaultValue);

	public int getIntProperty(String name, int defaultValue);
	
	public long getLongProperty(String name, long defaultValue);

	public String getProperty(String name, String defaultValue);

	public List<Server> getServers();

	public boolean isEnabled();
}
