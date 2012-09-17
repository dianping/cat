package com.dianping.dog.alarm.problem;

import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventType;

public class AlertEvent implements Event{
	
	private RuleEntity entity;

	private long time;

	private Duration duration;

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

	public Duration getDuration() {
   	return duration;
   }

	public void setDuration(Duration duration) {
   	this.duration = duration;
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
