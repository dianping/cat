package com.dianping.cat.notify.dao.ibatisimpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.notify.dao.DailyReportDao;
import com.dianping.cat.notify.model.DailyReport;

public class DailyReportDaoImp implements DailyReportDao {
	private BaseDao baseDao;

	public void setBaseDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<DailyReport> findAllByDomainNameDuration(Date startDate,
			Date endDate, String domain, String name, int type)
			throws Exception {
		Map<String,Object> map=new HashMap<String,Object>();
		if(type != DailyReport.JSON_TYPE && type != DailyReport.XML_TYPE){
			return null;
		}
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("domain", domain);
		map.put("name", name);
		map.put("type", type);
		return (List) baseDao.executeQueryForList("DailyReport.selectReport", map);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<DailyReport> findSendMailReportDomainDuration(Date startDate,
			Date endDate, String domain, int type)
			throws Exception {
		Map<String,Object> map=new HashMap<String,Object>();
		if(type != DailyReport.JSON_TYPE && type != DailyReport.XML_TYPE){
			return null;
		}
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("domain", domain);
		map.put("type", type);
		return (List) baseDao.executeQueryForList("DailyReport.selectSendMailReport", map);
	}

	@Override
   public List<String> findDistinctReportDomain(Date startDate, Date endDate, int type)
         throws Exception {
		Map<String,Object> map=new HashMap<String,Object>();
		if(type != DailyReport.JSON_TYPE && type != DailyReport.XML_TYPE){
			return null;
		}
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("type", type);
		return (List) baseDao.executeQueryForList("DailyReport.selectDistinctDomain", map);
   }
}
