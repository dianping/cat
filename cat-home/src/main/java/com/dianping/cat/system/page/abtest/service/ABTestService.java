package com.dianping.cat.system.page.abtest.service;

import java.util.List;
import java.util.Map;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.system.page.abtest.AbtestStatus;

public interface ABTestService {

	public Abtest getABTestByRunId(int id);

	public AbtestModel getAbtestModelByStatus(AbtestStatus... status);

	public GroupStrategy getGroupStrategyById(int id);
	
	public AbtestRun getAbtestRunById(int id);
	
	public List<GroupStrategy> getAllGroupStrategies();
	
	public Map<String, List<Project>> getAllProjects();
}
