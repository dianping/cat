package com.dianping.cat.message.spi;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public interface MessageManager {
	public void add(Message message);

	public void end(Transaction transaction);

	public Config getClientConfig();

	public Config getServerConfig();

	public void initializeClient(Config config);

	public void initializeServer(Config config);

	public void reset();

	public void setup(String sessionToken, String requestToken);

	public void start(Transaction transaction);

}