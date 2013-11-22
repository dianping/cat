package com.dianping.cat.message.spi.core;

import java.util.Date;

public interface MessagePathBuilder {
	public String getPath(Date timestamp, String name);

	public String getReportPath(String name, Date timestamp);
}
