package com.dianping.cat.message.consumer.failure;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.failurereport.entity.Entry;
import com.dianping.cat.consumer.failurereport.entity.FailureReport;
import com.dianping.cat.consumer.failurereport.entity.Segment;
import com.dianping.cat.message.Message;

/**
 * The class is used to record the state of the system. The is only record the
 * lastest one hour.
 * 
 * @author yong.you
 * 
 */
public class FailureState {
	private long m_crrentTime;
	private FailureReport m_report;
	private List<FailReportStore> m_outputList;
	private static final long HOUR = 60 * 60 * 1000;
	private static final int LENGTH = 60;
	private static final SimpleDateFormat SDF=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public FailureState() {
		m_crrentTime = 0;
	}

	public String getTimeStr(Message message){
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(message.getTimestamp());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date=new Date();
		date.setTime(cal.getTimeInMillis());
		try {
			return SDF.format(date);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * -1 in last hour, 0 in current hour, 1 in next hour, 2 in next many hours
	 * 
	 * @param message
	 * @return
	 */
	public int isInDuration(Message message) {
		/*long time = message.getTimestamp();
		Date startDate = m_failureReport.getStartTime();
		long startTime = startDate.getTime();
		if (time < startTime) {
			return -1;
		}
		if (time >= startTime && time < startTime + HOUR) {
			return 0;
		}
		if (time < startTime + HOUR * 2 && time >= startTime + HOUR) {
			return 1;
		}*/
		return 2;
	}
	
	/**
	 * 获取到Message的时间段。
	 * 如果是新的时间段，则新建对象，否则更新对象。
	 * 新建对象之前需要将前60分钟的对象，存入历史数据。 
	 * 更新machines信息。
	 * @param message
	 */
	public void addMessage(Message message) {
		Entry entry =convertToEntry(message);
		
	}

	public void registerOutput(FailReportStore output) {
		m_outputList.add(output);
	}

	public Entry convertToEntry(Message message) {
		//TODO
		Entry entry = new Entry();
		entry.setText(message.getName());
		entry.setType(message.getType());
		return entry;
	}
}
