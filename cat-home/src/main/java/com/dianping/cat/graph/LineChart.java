package com.dianping.cat.graph;

import java.util.Date;

public interface LineChart {

	public boolean pushData(String key, Date date, long step);

	public double[] getData(String key, Date start, Date end, int step);

}
