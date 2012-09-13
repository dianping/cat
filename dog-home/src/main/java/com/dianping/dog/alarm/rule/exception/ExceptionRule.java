package com.dianping.dog.alarm.rule.exception;

import java.util.List;

import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.alarm.problem.AlertEvent;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.store.Data;
import com.dianping.dog.alarm.rule.store.DefaultStorage;
import com.dianping.dog.alarm.rule.store.ExceptionData;
import com.dianping.dog.alarm.rule.store.Storage;
import com.dianping.dog.event.EventDispatcher;

public class ExceptionRule implements Rule {
	private Storage<ExceptionData> storage = null;
	
	private EventDispatcher dispatcher;
	
	private static long MINUTE = 60*1000L;
	
	private long millisPeriod = 0;

	private RuleEntity m_entity;

	@Override
	public boolean init(RuleEntity entity) {
		m_entity = entity;
		storage = new DefaultStorage<ExceptionData>();
		storage.init(m_entity.getPeriod());
		millisPeriod = entity.getPeriod() * MINUTE;
		return true;
	}

	@Override
	public String getName() {
		return "Exception rule";
	}

	@Override
	public boolean isEligible(DataEvent event) {

		return false;
	}

	@Override
	public boolean apply(DataEvent event) {
		saveData(event);
		long currentTime = getCurrentTime();
		List<ExceptionData>  dataList = storage.getDataList();
		int totalCount = 0;
		for(ExceptionData data:dataList){
			if(!isIn(data,currentTime)){
				continue;
			}
			totalCount += data.getTotalCount();
		}
		List<Duration> durationList = m_entity.getDurations();
		for(Duration duration : durationList){
			if(duration.isIn(totalCount)){
				AlertEvent alarmEvent = new AlertEvent();
				alarmEvent.setEntity(m_entity);
				alarmEvent.setTime(currentTime);
				alarmEvent.setTypeList(duration.getAlarmType());
				alarmEvent.setCount(totalCount);
				dispatcher.dispatch(alarmEvent);
				return true;
			}
		}
		return true;
	}

	public void saveData(DataEvent event) {
		ExceptionData data = new ExceptionData();
		data.setTimeStamp(event.getTimestamp().getTime());
		data.setTotalCount(event.getTotalCount());
		storage.add(data);
	}
	
	
	private boolean isIn(Data data,long currentTime){
		long beginTime = currentTime - millisPeriod;
		if(data.getTimeStamp() >= beginTime && data.getTimeStamp() <=currentTime){
			return true;
		}
		return false;
	}
	
	private long getCurrentTime(){
		return System.currentTimeMillis();
	}

	@Override
   public void setDispatcher(EventDispatcher dispatcher) {
		this.dispatcher = dispatcher;
   }
}
