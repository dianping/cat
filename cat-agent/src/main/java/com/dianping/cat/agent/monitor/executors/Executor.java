package com.dianping.cat.agent.monitor.executors;

import java.util.List;

import com.dianping.cat.agent.monitor.DataEntity;

public interface Executor {
	
	public String getId();
	
	public List<DataEntity> execute();
	
}
