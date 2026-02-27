package com.dianping.cat.message.pipeline;

import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.ConfigureManager;

public interface MessageHandlerContext {
	void fireMessage(Object msg);

	ConfigureManager getConfigureManager();

	Logger getLogger();

	MessageHandler handler();

	MessagePipeline pipeline();

	MessageSource source();
}
