package com.dianping.log;

import java.util.Date;
import java.util.List;

public interface LogService {

	public void insert(String type, LogItem item);

	public void batchInsert(String type, List<LogItem> item);

	public List<LogItem> query(String type, Date start, Date end, String id);
}
