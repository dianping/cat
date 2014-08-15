package com.dianping.cat.broker.api.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.config.app.AppDataService;

public class BucketHandler implements Task {

	private AppDataQueue m_appDataQueue;

	private AppDataService m_appDataService;

	private boolean m_isActive = true;

	private HashMap<Integer, HashMap<String, AppData>> m_datas;

	private long m_startTime;

	public BucketHandler(long startTime, AppDataService appDataService) {
		m_startTime = startTime;
		m_appDataQueue = new AppDataQueue();
		m_datas = new LinkedHashMap<Integer, HashMap<String, AppData>>();
		m_appDataService = appDataService;
	}

	private void end() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_startTime);

		int minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		minute = minute - minute % 5;

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		Date period = new Date(cal.getTimeInMillis());

		for (Entry<Integer, HashMap<String, AppData>> outerEntry : m_datas.entrySet()) {
			List<AppDataCommand> commands = new ArrayList<AppDataCommand>();
			HashMap<String, AppData> value = outerEntry.getValue();

			for (Entry<String, AppData> entry : value.entrySet()) {
				AppData appData = entry.getValue();
				AppDataCommand proto = new AppDataCommand();

				proto.setPeriod(period);
				proto.setMinuteOrder(minute);
				proto.setCommandId(appData.getCommand());
				proto.setCity(appData.getCity());
				proto.setOperator(appData.getOperator());
				proto.setNetwork(appData.getNetwork());
				proto.setAppVersion(appData.getVersion());
				proto.setConnnectType(appData.getConnectType());
				proto.setCode(appData.getCode());
				proto.setPlatform(appData.getPlatform());
				proto.setAccessNumber(appData.getCount());
				proto.setResponseSumTime(appData.getResponseTime());
				proto.setRequestPackage(appData.getRequestByte());
				proto.setResponsePackage(appData.getResponseByte());
				proto.setCreationDate(new Date());

				commands.add(proto);

				if (commands.size() >= 100) {
					batchInsert(commands);

					commands = new ArrayList<AppDataCommand>();
				}
			}
			batchInsert(commands);
		}
	}

	protected void batchInsert(List<AppDataCommand> appDataCommands) {
		try {
			int length = appDataCommands.size();
			AppDataCommand[] array = new AppDataCommand[length];

			for (int i = 0; i < length; i++) {
				array[i] = appDataCommands.get(i);
			}

			m_appDataService.insert(array);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public boolean enqueue(AppData appData) {
		return m_appDataQueue.offer(appData);
	}

	@Override
	public String getName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		return "BucketHandler-" + sdf.format(new Date(m_startTime));
	}

	public boolean isActive() {
		return m_isActive;
	}

	private void processEntity(AppData appData) {
		int command = appData.getCommand();
		StringBuilder sb = new StringBuilder();
		char split = ':';

		sb.append(appData.getCity()).append(split);
		sb.append(appData.getOperator()).append(split);
		sb.append(appData.getConnectType()).append(split);
		sb.append(appData.getVersion()).append(split);
		sb.append(appData.getNetwork()).append(split);
		sb.append(appData.getCode()).append(split);
		sb.append(appData.getPlatform());

		String key = sb.toString();
		HashMap<String, AppData> secondMap = m_datas.get(command);

		if (secondMap == null) {
			secondMap = new LinkedHashMap<String, AppData>();

			secondMap.put(key, appData);
			m_datas.put(command, secondMap);
		} else {
			AppData mergedAppData = secondMap.get(key);

			if (mergedAppData == null) {
				secondMap.put(key, appData);
			} else {
				mergedAppData.addCount(appData.getCount());
				mergedAppData.addRequestByte(appData.getRequestByte());
				mergedAppData.addResponseByte(appData.getResponseByte());
				mergedAppData.addResponseTime(appData.getResponseTime());
			}
		}
	}

	@Override
	public void run() {
		while (isActive()) {
			AppData appData = m_appDataQueue.poll();

			if (appData != null) {
				try {
					processEntity(appData);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		while (true) {
			AppData appData = m_appDataQueue.poll();

			if (appData != null) {
				processEntity(appData);
			} else {
				break;
			}
		}

		end();
	}

	@Override
	public void shutdown() {
		m_isActive = false;
	}

}
