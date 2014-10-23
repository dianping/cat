package com.dianping.cat.report.task.alert.summary;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;

public class AlterationDataGenerator extends SummaryDataGenerator {

	public static final String ID = "AlterationDataGenerator";

	@Inject
	private AlterationDao m_alterationDao;

	@Override
	public Map<Object, Object> generateModel(String domain, Date date) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();

		try {
			List<Alteration> alterations = m_alterationDao.findByDomainAndTime(getStartDate(date), date, domain,
			      AlterationEntity.READSET_FULL);

			dataMap.put("count", alterations.size());
			dataMap.put("items", alterations);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return dataMap;
	}

	private Date getStartDate(Date date) {
		return new Date(date.getTime() - AlertSummaryExecutor.SUMMARY_DURATION);
	}

	@Override
	public String getID() {
		return ID;
	}

}
