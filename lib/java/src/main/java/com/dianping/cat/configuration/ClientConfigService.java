package com.dianping.cat.configuration;

import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.property.entity.PropertyConfig;
import com.dianping.cat.message.spi.MessageTree;

import java.util.List;

public interface ClientConfigService {

	int getClientConnectTimeout();

	String getDomain();

	int getLongConfigThreshold(String key);

	int getLongThresholdByDuration(String key, int duration);

	String getRouters();

	double getSamplingRate();

	List<Server> getServers();

	boolean isMessageBlock();

	MessageType parseMessageType(MessageTree tree);

	void refreshConfig(PropertyConfig config);

	void refreshConfig();

}