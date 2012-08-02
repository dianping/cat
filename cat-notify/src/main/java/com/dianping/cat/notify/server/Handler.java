package com.dianping.cat.notify.server;

import java.util.Map;

public interface Handler {

	void init();

	void setProcessors(Map<String, Processor> processorMap);

	Response process(Request req);

}
