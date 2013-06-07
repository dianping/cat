package com.dianping.cat.helper;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Parse {

	public double[] compute(Map<Date, List<double[]>> datas);
	
	public void setDateNoUse(Date start,Date end);
}
