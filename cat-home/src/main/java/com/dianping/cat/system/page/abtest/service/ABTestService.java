package com.dianping.cat.system.page.abtest.service;

import java.util.List;
import java.util.Map;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public interface ABTestService {

	public Abtest getABTestByRunId(int id);

	public AbtestModel getABTestModelByStatus(AbtestStatus... status);

	public AbtestModel getABTestModelByRunID(int runId);

	public GroupStrategy getGroupStrategyById(int id);

	public AbtestRun getAbTestRunById(int id);

	public List<AbtestRun> getAbtestRunByStatus(AbtestStatus status);

	public List<GroupStrategy> getAllGroupStrategies();

	public Map<String, List<Project>> getAllProjects();

	public void refresh();

	public void setModified();

	public long getModifiedTime();
}
