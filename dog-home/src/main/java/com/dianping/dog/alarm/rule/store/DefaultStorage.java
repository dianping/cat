package com.dianping.dog.alarm.rule.store;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DefaultStorage<T extends Data> implements Storage<T> {
	private StoreQueue<T> queue;

	private volatile T currentData;
	
	//private static final long HOUR = 60 * 1000;

	@Override
	public boolean init(int period) {
		queue = new LinkedStoreQueue<T>(period);
		return true;
	}

	@Override
	public synchronized void add(T data) {
		if(currentData == null){
			currentData = data;
			return;
		}
		long timeSpan = compare(data.getTimeStamp(), currentData.getTimeStamp());
		//TODO need to check the hour and date first!
		if (timeSpan == 0) {
			currentData.merge(data);
		} else if (timeSpan > 0) {
			queue.addData(currentData);
			currentData = data;
		}
	}

	@Override
	public List<T> getDataList() {
		return queue.getAll();
	}

	public int compare(long firstStamp, long secondStamp) {
		int firstMinute = getMinuteOfDay(firstStamp);
		int secondMinute = getMinuteOfDay(secondStamp);
		return firstMinute - secondMinute;
	}

	public int getMinuteOfDay(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(timestamp));
		return calendar.get(Calendar.MINUTE);
	}

}
