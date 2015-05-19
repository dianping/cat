package com.dianping.cat.message;

import java.util.Date;

public interface PathBuilder {
	public String getLogviewPath(Date timestamp, String name);

	public String getReportPath(String name, Date timestamp,int index);
}
