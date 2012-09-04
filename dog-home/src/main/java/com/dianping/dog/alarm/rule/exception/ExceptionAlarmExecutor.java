package com.dianping.dog.alarm.rule.exception;

import java.util.List;

import com.dianping.dog.connector.RowData;
import com.dianping.dog.event.AlarmEvent;
import com.dianping.dog.event.DataEvent;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.site.lookup.annotation.Inject;

public class ExceptionAlarmExecutor implements EventListener<DataEvent> {

	@Inject
	private EventDispatcher m_dispatch;

	@Inject
	private ExceptionRowDataManager m_rowDatamanager;

	@Inject
	private ExceptionRuleManager m_manager;
	
	@Override
	public boolean isEligible(DataEvent event) {
		return false;
	}

	@Override
	public void onEvent(DataEvent event) {
		m_rowDatamanager.addData(event.getRowData());

		List<ExceptionRule> rules = m_manager.getRules();
		
		if (rules != null) {
			for (ExceptionRule rule : rules) {
				List<RowData> datas = m_rowDatamanager.fetchRowData(rule);

				AlarmEvent alarm = rule.apply(datas);

				if (alarm != null) {
					m_dispatch.dispatch(alarm);
				}
			}
		}
	}
}
