package com.dianping.cat.service.app.speed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.app.AppSpeedDataEntity;
import com.dianping.cat.service.app.BaseAppDataService;
import com.dianping.cat.service.app.BaseQueryEntity;

public class AppSpeedService implements BaseAppDataService<AppSpeedData> {

	@Inject
	private AppSpeedDataDao m_dao;

	public static final String ID = AppSpeedData.class.getName();

	@Override
	public int[] insert(AppSpeedData[] proto) throws DalException {
		return m_dao.insert(proto);
	}

	@Override
	public void insertSingle(AppSpeedData proto) throws DalException {
		m_dao.insert(proto);
	}

	public List<AppSpeedData> queryValue(BaseQueryEntity entity) {
		int speedId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int platform = entity.getPlatfrom();
		List<AppSpeedData> datas = new ArrayList<AppSpeedData>();

		try {
			datas = m_dao.findDataByMinute(speedId, period, city, operator, network, appVersion, platform,
			      AppSpeedDataEntity.READSET_AVG_DATA);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}
}
