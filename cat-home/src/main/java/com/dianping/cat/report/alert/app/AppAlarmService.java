package com.dianping.cat.report.alert.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppCommandDataEntity;
import com.dianping.cat.app.AppDataField;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;

public class AppAlarmService {

	@Inject
	private AppCommandDataDao m_dao;

	public List<AppCommandData> queryByFieldCode(CommandQueryEntity entity, AppDataField groupByField) {
		List<AppCommandData> datas = new ArrayList<AppCommandData>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int source = entity.getSource();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperatorCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_OPERATOR_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetworkCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_NETWORK_CODE_DATA);
				break;
			case APP_VERSION:
				datas = m_dao.findDataByAppVersionCode(commandId, period, city, operator, network, appVersion,
				      connnectType, code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_APP_VERSION_CODE_DATA);
				break;
			case CONNECT_TYPE:
				datas = m_dao.findDataByConnectTypeCode(commandId, period, city, operator, network, appVersion,
				      connnectType, code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_CONNECT_TYPE_CODE_DATA);
				break;
			case PLATFORM:
				datas = m_dao.findDataByPlatformCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_PLATFORM_CODE_DATA);
				break;
			case SOURCE:
				datas = m_dao.findDataBySourceCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_SOURCE_CODE_DATA);
				break;
			case CITY:
				datas = m_dao
				      .findDataByCityCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				            platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_CITY_CODE_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_CODE_DATA);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}
}
