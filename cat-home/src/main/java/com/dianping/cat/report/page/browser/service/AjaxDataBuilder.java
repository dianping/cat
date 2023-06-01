package com.dianping.cat.report.page.browser.service;

import com.dianping.cat.Cat;
import com.dianping.cat.web.AjaxData;
import com.dianping.cat.web.AjaxDataDao;
import com.dianping.cat.web.AjaxDataEntity;
import org.unidal.lookup.annotation.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AjaxDataBuilder {

	@Inject
	private AjaxDataDao m_dao;

	public List<AjaxData> queryByField(AjaxDataQueryEntity entity, AjaxDataField groupByField) {
		List<AjaxData> datas = new ArrayList<AjaxData>();
		int apiId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int code = entity.getCode();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();
		int network = entity.getNetwork();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperator(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_OPERATOR_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCity(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_CITY_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetwork(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_NETWORK_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public List<AjaxData> queryByFieldCode(AjaxDataQueryEntity entity, AjaxDataField groupByField) {
		List<AjaxData> datas = new ArrayList<AjaxData>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int code = entity.getCode();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperatorCode(commandId, period, city, operator, network, code,
				      AjaxDataEntity.READSET_OPERATOR_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetworkCode(commandId, period, city, operator, network, code,
				      AjaxDataEntity.READSET_NETWORK_CODE_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCityCode(commandId, period, city, operator, network, code,
				      AjaxDataEntity.READSET_CITY_CODE_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_CODE_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public List<AjaxData> queryByMinute(AjaxDataQueryEntity entity, AjaxQueryType type) {
		int apiId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int code = entity.getCode();
		int network = entity.getNetwork();
		int start = entity.getStartMinuteOrder();
		int end = entity.getEndMinuteOrder();
		List<AjaxData> datas = new ArrayList<AjaxData>();

		try {
			switch (type) {
			case SUCCESS:
				datas = m_dao.findDataByMinuteCode(apiId, period, city, operator, code, network, start, end,
				      AjaxDataEntity.READSET_SUCCESS_DATA);
				break;
			case REQUEST:
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network, start, end,
				      AjaxDataEntity.READSET_COUNT_DATA);
				break;
			case DELAY:
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network, start, end,
				      AjaxDataEntity.READSET_AVG_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return datas;
	}

}
