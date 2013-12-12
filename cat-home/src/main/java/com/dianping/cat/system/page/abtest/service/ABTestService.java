package com.dianping.cat.system.page.abtest.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestReport;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public interface ABTestService {
	
	public void insertAbtest(Abtest abtest) throws DalException;
	
	public Abtest getABTestByCaseId(int caseId);

	public void insertAbtestRun(AbtestRun run) throws DalException;
	
	public void updateAbtestRun(AbtestRun run) throws DalException;
	
	public AbtestRun getAbTestRunById(int id);
	
	public List<AbtestRun> getAllAbtestRun();
	
	public AbtestModel getABTestModelByStatus(AbtestStatus... status);

	public List<AbtestRun> getAbtestRunByStatus(AbtestStatus status);

	public void insertGroupStrategy(GroupStrategy groupStrategy) throws DalException;
	
	public List<GroupStrategy> getGroupStrategyByName(String name);

	public List<GroupStrategy> getAllGroupStrategies();

	public Map<String, List<Project>> getAllProjects();
	
	public List<AbtestReport> getReports(int runId, Date startTime, Date endTime);

	public long getModifiedTime();
}
