package com.dianping.dog.alarm.rule.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.alarm.problem.AlertEvent;


public class ExceptionMessageCreater implements MessageCreater {

	@Override
	public Message create(AlertEvent event) {
		Message message = new Message();
		RuleEntity entity = event.getEntity();
		StringBuilder sb = new StringBuilder();
		sb.append(entity.getDomain()+ENTER_SPLITER);
		sb.append(formatTime(null,event.getTime())+ENTER_SPLITER);
		sb.append(entity.getConnect().getType()+":"+event.getCount()+ENTER_SPLITER);
		message.setContent(sb.toString());
		return message;
	}
	
	public static String formatTime(String format,long timestamp){
		SimpleDateFormat df = null;
		Date date=new Date(timestamp);
		if(format == null){
			df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		}else{
			df=new SimpleDateFormat(format);
		}
		return df.format(date);
	}
	
}
