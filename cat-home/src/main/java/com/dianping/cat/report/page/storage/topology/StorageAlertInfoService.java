package com.dianping.cat.report.page.storage.topology;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertEntity;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.report.alert.AlertType;

public class StorageAlertInfoService {

	@Inject
	private AlertDao m_alertDao;

	@Inject
	private StorageGraphBuilder m_builder;

	@Inject
	private StorageAlertInfoRTContainer m_alertInfoRTContainer;

	public StorageAlertInfo queryAlertInfo(long time, int minute) {
		StorageAlertInfo alertInfo = m_alertInfoRTContainer.find(time, minute);

		if (alertInfo == null) {
			alertInfo = buildFromDatabase(time, minute);
		}
		return alertInfo;
	}

	private StorageAlertInfo buildFromDatabase(long time, int minute) {
		Date start = new Date(time + minute * TimeHelper.ONE_MINUTE);
		Date end = new Date(start.getTime() + TimeHelper.ONE_MINUTE - 1000);
		System.out.println(start + " " + end);
		StorageAlertInfo alertInfo = new StorageAlertInfo("SQL");

		try {
			List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(start, end, AlertType.StorageDatabase.getName(),
			      AlertEntity.READSET_FULL);

			for (Alert alert : alerts) {
				m_builder.parseAlertEntity(alert, alertInfo);
			}
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return alertInfo;
	}
}
