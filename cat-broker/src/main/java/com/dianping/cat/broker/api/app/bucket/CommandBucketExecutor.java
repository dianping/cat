package com.dianping.cat.broker.api.app.bucket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.broker.api.app.AppCommandData;
import com.dianping.cat.broker.api.app.BaseData;
import com.dianping.cat.service.app.BaseAppDataService;

public class CommandBucketExecutor implements BucketExecutor {

	private BaseAppDataService<AppDataCommand> m_appDataService;

	private HashMap<Integer, HashMap<String, AppCommandData>> m_datas = new LinkedHashMap<Integer, HashMap<String, AppCommandData>>();

	private long m_startTime;

	private CountDownLatch m_flushCountDownLatch = new CountDownLatch(0);

	private CountDownLatch m_saveCountDownLatch = new CountDownLatch(0);

	public CommandBucketExecutor(long startTime, BaseAppDataService<AppDataCommand> appDataService) {
		m_startTime = startTime;
		m_appDataService = appDataService;
	}

	protected void batchInsert(List<AppDataCommand> appDataCommands, List<AppCommandData> datas) {
		int[] ret = null;
		try {
			int length = appDataCommands.size();
			AppDataCommand[] array = new AppDataCommand[length];

			for (int i = 0; i < length; i++) {
				array[i] = appDataCommands.get(i);
			}
			ret = m_appDataService.insert(array);
		} catch (Exception e) {
			Cat.logError(e);
		}

		if (ret != null) {
			int length = datas.size();

			for (int i = 0; i < length; i++) {
				datas.get(i).setFlushed();
			}
		}
	}

	@Override
	public void flush() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_startTime);

		int minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		minute = minute - minute % 5;

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		Date period = new Date(cal.getTimeInMillis());

		for (Entry<Integer, HashMap<String, AppCommandData>> outerEntry : m_datas.entrySet()) {
			try {
				List<AppDataCommand> commands = new ArrayList<AppDataCommand>();
				List<AppCommandData> datas = new ArrayList<AppCommandData>();
				HashMap<String, AppCommandData> value = outerEntry.getValue();

				for (Entry<String, AppCommandData> entry : value.entrySet()) {
					m_saveCountDownLatch.await();

					m_flushCountDownLatch = new CountDownLatch(1);
					try {
						AppCommandData appData = entry.getValue();

						if (appData.notFlushed()) {
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
							datas.add(appData);

							if (commands.size() >= 100) {
								batchInsert(commands, datas);

								commands = new ArrayList<AppDataCommand>();
							}
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				batchInsert(commands, datas);
				m_flushCountDownLatch.countDown();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		m_datas.clear();
	}

	@Override
	public BaseData loadRecord(String[] items) {
		AppCommandData appData = new AppCommandData();

		appData.setCommand(Integer.parseInt(items[1]));
		appData.setTimestamp(Long.parseLong(items[2]));
		appData.setCity(Integer.parseInt(items[3]));
		appData.setOperator(Integer.parseInt(items[4]));
		appData.setNetwork(Integer.parseInt(items[5]));
		appData.setVersion(Integer.parseInt(items[6]));
		appData.setConnectType(Integer.parseInt(items[7]));
		appData.setCode(Integer.parseInt(items[8]));
		appData.setPlatform(Integer.parseInt(items[9]));
		appData.setCount(Integer.parseInt(items[10]));
		appData.setResponseTime(Integer.parseInt(items[11]));
		appData.setRequestByte(Integer.parseInt(items[12]));
		appData.setResponseByte(Integer.parseInt(items[13]));
		return appData;
	}

	public BaseAppDataService<?> getAppDataService() {
		return m_appDataService;
	}

	public HashMap<Integer, HashMap<String, AppCommandData>> getDatas() {
		return m_datas;
	}

	public long getStartTime() {
		return m_startTime;
	}

	@Override
	public void processEntity(BaseData appData) {
		try {
			AppCommandData appCommandData = (AppCommandData) appData;
			int command = appCommandData.getCommand();
			StringBuilder sb = new StringBuilder();
			char split = ':';

			sb.append(appCommandData.getCity()).append(split);
			sb.append(appCommandData.getOperator()).append(split);
			sb.append(appCommandData.getConnectType()).append(split);
			sb.append(appCommandData.getVersion()).append(split);
			sb.append(appCommandData.getNetwork()).append(split);
			sb.append(appCommandData.getCode()).append(split);
			sb.append(appCommandData.getPlatform());

			String key = sb.toString();
			HashMap<String, AppCommandData> secondMap = m_datas.get(command);

			if (secondMap == null) {
				secondMap = new LinkedHashMap<String, AppCommandData>();

				secondMap.put(key, appCommandData);
				m_datas.put(command, secondMap);
			} else {
				AppCommandData mergedAppData = secondMap.get(key);

				if (mergedAppData == null) {
					secondMap.put(key, appCommandData);
				} else {
					mergedAppData.addCount(appCommandData.getCount());
					mergedAppData.addRequestByte(appCommandData.getRequestByte());
					mergedAppData.addResponseByte(appCommandData.getResponseByte());
					mergedAppData.addResponseTime(appCommandData.getResponseTime());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public void save(File file) {
		if (m_datas.size() > 0) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
				char tab = '\t';
				char enter = '\n';

				for (Entry<Integer, HashMap<String, AppCommandData>> outerEntry : m_datas.entrySet()) {
					HashMap<String, AppCommandData> value = outerEntry.getValue();

					for (Entry<String, AppCommandData> entry : value.entrySet()) {
						m_flushCountDownLatch.await();

						m_saveCountDownLatch = new CountDownLatch(1);
						AppCommandData appData = entry.getValue();

						if (appData.notFlushed()) {
							StringBuilder sb = new StringBuilder();
							sb.append(appData.getClass().getName()).append(tab);
							sb.append(appData.getCommand()).append(tab);
							sb.append(appData.getTimestamp()).append(tab);
							sb.append(appData.getCity()).append(tab);
							sb.append(appData.getOperator()).append(tab);
							sb.append(appData.getNetwork()).append(tab);
							sb.append(appData.getVersion()).append(tab);
							sb.append(appData.getConnectType()).append(tab);
							sb.append(appData.getCode()).append(tab);
							sb.append(appData.getPlatform()).append(tab);
							sb.append(appData.getCount()).append(tab);
							sb.append(appData.getResponseTime()).append(tab);
							sb.append(appData.getRequestByte()).append(tab);
							sb.append(appData.getResponseByte()).append(enter);

							writer.append(sb.toString());
							appData.setSaved();
						}
						m_saveCountDownLatch.countDown();
					}
				}
				writer.close();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}
}
