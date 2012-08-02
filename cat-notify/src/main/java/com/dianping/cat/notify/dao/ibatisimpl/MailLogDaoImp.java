package com.dianping.cat.notify.dao.ibatisimpl;

import java.util.List;

import com.dianping.cat.notify.dao.MailLogDao;
import com.dianping.cat.notify.model.MailLog;

public class MailLogDaoImp implements MailLogDao {

	private BaseDao baseDao;

	public void setBaseDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}

	@Override
	public List<MailLog> getAllMailLog() throws Exception {
		return null;
	}

	@Override
	public void insertMailLog(MailLog mailLog) throws Exception {
	     baseDao.insert("MailLog.insert",mailLog);
	}

}
