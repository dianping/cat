package com.dianping.cat.notify.dao;

import java.util.List;

import com.dianping.cat.notify.model.MailLog;

public interface MailLogDao {

	public List<MailLog> getAllMailLog() throws Exception;
	
	public void insertMailLog(MailLog mailLog) throws Exception;

}
