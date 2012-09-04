package com.dianping.dog.event;

public enum DefaultEventType implements EventType {

	DATA_EVENT, // 生成数据

	RULE_EVENT, // 规则变更事件

	ALARM_EVENT;// 报警事件

}
