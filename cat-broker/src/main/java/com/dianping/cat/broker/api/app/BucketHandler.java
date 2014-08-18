package com.dianping.cat.broker.api.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

	public final static String SAVE_PATH = "/data/appdatas/cat/app-data-save/";

	public BucketHandler(long startTime, AppDataService appDataService) {
		m_startTime = startTime;
		m_appDataQueue = new AppDataQueue();
		m_datas = new LinkedHashMap<Integer, HashMap<String, AppData>>();
		m_appDataService = appDataService;
	}

	public void end() {
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

	public void save() {
		if (m_datas.size() > 0) {
			try {
				File parentDir = new File(SAVE_PATH);
				boolean success = parentDir.mkdirs();

				if (success) {
					String filePath = SAVE_PATH + String.valueOf(m_startTime);
					BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
					char tab = '\t';
					char enter = '\n';

					for (Entry<Integer, HashMap<String, AppData>> outerEntry : m_datas.entrySet()) {
						HashMap<String, AppData> value = outerEntry.getValue();

						for (Entry<String, AppData> entry : value.entrySet()) {
							AppData appData = entry.getValue();

							StringBuilder sb = new StringBuilder();
							sb.append(appData.getTimestamp()).append(tab);
							sb.append(appData.getCity()).append(tab);
							sb.append(appData.getOperator()).append(tab);
							sb.append(appData.getNetwork()).append(tab);
							sb.append(appData.getVersion()).append(tab);
							sb.append(appData.getConnectType()).append(tab);
							sb.append(appData.getCommand()).append(tab);
							sb.append(appData.getCode()).append(tab);
							sb.append(appData.getPlatform()).append(tab);
							sb.append(appData.getRequestByte()).append(tab);
							sb.append(appData.getResponseByte()).append(tab);
							sb.append(appData.getResponseTime()).append(enter);

							writer.append(sb.toString());
						}
					}
					writer.close();
				} else {
					Cat.logError(new RuntimeException("error when create temp data file " + parentDir.getAbsolutePath()));
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	public void load(File file) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				String[] items = line.split("\t");
				AppData appData = new AppData();

				appData.setTimestamp(Long.parseLong(items[0]));
				appData.setCity(Integer.parseInt(items[1]));
				appData.setOperator(Integer.parseInt(items[2]));
				appData.setNetwork(Integer.parseInt(items[3]));
				appData.setVersion(Integer.parseInt(items[4]));
				appData.setConnectType(Integer.parseInt(items[5]));
				appData.setCommand(Integer.parseInt(items[6]));
				appData.setCode(Integer.parseInt(items[7]));
				appData.setPlatform(Integer.parseInt(items[8]));
				appData.setRequestByte(Integer.parseInt(items[9]));
				appData.setResponseByte(Integer.parseInt(items[10]));
				appData.setResponseTime(Integer.parseInt(items[11]));

				enqueue(appData);
			}
			bufferedReader.close();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}
}
