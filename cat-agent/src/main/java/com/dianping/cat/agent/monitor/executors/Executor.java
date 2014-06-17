package com.dianping.cat.agent.monitor.executors;

import java.util.List;

public interface Executor {
	
	public String getId();
	
	public List<DataEntity> execute();
	
}
