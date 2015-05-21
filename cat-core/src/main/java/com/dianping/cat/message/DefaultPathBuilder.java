package com.dianping.cat.message;

import java.text.MessageFormat;
import java.util.Date;

public class DefaultPathBuilder implements PathBuilder {

	@Override
	public String getLogviewPath(Date timestamp, String name) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}");

		return format.format(new Object[] { timestamp, name });
	}

	@Override
	public String getReportPath(String name, Date timestamp, int index) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}/report-{2}");

		return format.format(new Object[] { timestamp, index, name });
	}
}
