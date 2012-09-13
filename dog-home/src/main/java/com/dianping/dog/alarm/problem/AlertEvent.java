package com.dianping.dog.alarm.problem;

import java.util.List;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.alarm.rule.AlarmType;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventType;

public class AlertEvent implements Event{
	
	private RuleEntity entity;

	private long time;

	private List<AlarmType> typeList;

	private int count;

	public RuleEntity getEntity() {
		return entity;
	}

	public void setEntity(RuleEntity entity) {
		this.entity = entity;
	}

	public long getTime() {
   	return time;
   }

	public void setTime(long time) {
   	this.time = time;
   }

	public List<AlarmType> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<AlarmType> typeList) {
		this.typeList = typeList;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
   public EventType getEventType() {
	   return EventType.ProblemAlarmEvent;
   }
	
}
