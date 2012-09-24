package com.dianping.dog.alarm.parser;

import org.codehaus.plexus.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.dianping.dog.alarm.connector.RowData;
import com.dianping.dog.alarm.entity.ConnectEntity;

public class ProblemDataParser implements DataParser {

	private Logger m_logger;

	@SuppressWarnings("hiding")
	@Override
	public <String> RowData parse(ConnectEntity conEntity, String context) {
		RowData data = new RowData();
		try {
			JSONObject jsonObject = new JSONObject(context.toString().trim());
			long timeStamp = Long.parseLong(jsonObject.get("timestamp").toString());
			long totalCount = 0;
			try {
				totalCount = Long.parseLong(jsonObject.get("Count").toString());
			} catch (Exception e) {
				m_logger.error(ProblemDataParser.class.getName() + "fail to parser data from "+ context);
				return null;
			}
			data.setTimeStamp(timeStamp);
			data.setId(conEntity.getConId());
			data.addData("domain", conEntity.getDomain());
			data.addData("type", conEntity.getType());
			data.addData("report", conEntity.getReport());
			data.addData("name", conEntity.getName());
			data.addData("totalCount", totalCount);
			return data;
		} catch (JSONException e) {
			m_logger.error(ProblemDataParser.class.getName() + " fail to parser data from " + e);
		}
		return null;
	}

	@Override
	public RowData mergeRowData(RowData newData, RowData oldData) {
		RowData rowData = newData.copy();
		rowData.setTimeStamp(newData.getTimeStamp());
		long nTotalCount = (Long) newData.getData("totalCount");
		long oTotalCount = (Long) oldData.getData("totalCount");
		rowData.addData("totalCount", nTotalCount - oTotalCount);
		return rowData;
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

}
