package com.dianping.cat.report.task.alert.summary;

import java.util.Date;
import java.util.Map;

public abstract class SummaryDataGenerator {

	public abstract Map<Object, Object> generateModel(String domain, Date date);

	public abstract String getID();

}
