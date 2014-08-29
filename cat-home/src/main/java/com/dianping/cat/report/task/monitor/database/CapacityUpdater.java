package com.dianping.cat.report.task.monitor.database;

import java.util.List;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.home.OverloadReport.entity.OverloadReport;

public interface CapacityUpdater {

	public int updateDBCapacity(double capacity) throws DalException;

	public void updateOverloadReport(int updateBStartId, List<OverloadReport> overloadReports) throws DalException;

	public String getId();

}
