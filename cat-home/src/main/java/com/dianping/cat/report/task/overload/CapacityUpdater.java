package com.dianping.cat.report.task.overload;

import org.unidal.dal.jdbc.DalException;

public interface CapacityUpdater {

	public static final int HOURLY_TYPE = 1;

	public static final int DAILY_TYPE = 2;

	public static final int WEEKLY_TYPE = 3;

	public static final int MONTHLY_TYPE = 4;

	public void updateDBCapacity(double capacity) throws DalException;

	public String getId();

}
