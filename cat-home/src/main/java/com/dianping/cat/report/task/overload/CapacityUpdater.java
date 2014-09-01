package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;

public interface CapacityUpdater {

	public static final int HOURLY_TYPE = 1;

	public static final int DAILY_TYPE = 2;

	public static final int WEEKLY_TYPE = 3;

	public static final int MONTHLY_TYPE = 4;

	public int updateDBCapacity(double capacity) throws DalException;

	public void updateOverloadReport(int updateBStartId, List<OverloadReport> overloadReports) throws DalException;

	public String getId();

}
