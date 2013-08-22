package com.dianping.cat.abtest.repository;

import java.util.Map;
import java.util.Set;

import javax.script.Invocable;

import com.dianping.cat.abtest.spi.ABTestEntity;

public interface ABTestEntityRepository {
	public Map<String, ABTestEntity> getCurrentEntities();
	
	public Set<String> getActiveRuns(); 
	
	public Invocable getInvocable(int runID);
}
