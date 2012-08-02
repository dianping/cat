package com.dianping.cat.notify.server;

public interface Processor {
	
	void init(ContainerHolder holder);
	
	void process(final Request req, final Response rep);

}
