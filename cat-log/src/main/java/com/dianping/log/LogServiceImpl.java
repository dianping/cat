package com.dianping.log;

import java.util.Date;
import java.util.List;

public class LogServiceImpl implements LogService{

	@Override
   public void insert(String type, LogItem item) {
		// get kafka config from router manager
		// insert to kafka storage
   }

	@Override
   public void batchInsert(String type, List<LogItem> item) {
	   // TODO Auto-generated method stub
   }

	@Override
   public List<LogItem> query(String type, Date start, Date end, String id) {
		// get es config from router manager
		// query user es api
		return null;
   }

}
