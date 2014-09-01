package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;

public interface CapacityUpdater {

	public int updateDBCapacity(double capacity) throws DalException;

	public void updateOverloadReport(int updateBStartId, List<OverloadReport> overloadReports) throws DalException;

	public String getId();

}
