package com.dianping.cat.report.page.overload.task;

import org.unidal.dal.jdbc.DalException;

public interface CapacityUpdater {

	public static final int HOURLY_TYPE = 1;

	public static final int DAILY_TYPE = 2;

	public static final int WEEKLY_TYPE = 3;

	public static final int MONTHLY_TYPE = 4;

	public static final double CAPACITY = 15.0;

	public void updateDBCapacity() throws DalException;

	public String getId();

}
