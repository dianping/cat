package com.dianping.cat.broker.api.app.bucket.impl;

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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppConnectionData;
import com.dianping.cat.broker.api.app.bucket.BucketExecutor;
import com.dianping.cat.broker.api.app.proto.AppConnectionProto;
import com.dianping.cat.broker.api.app.proto.ProtoData;
import com.dianping.cat.broker.api.app.service.AppService;
import com.dianping.cat.config.app.AppConfigManager;

public class ConnectionBucketExecutor implements BucketExecutor {

	private AppService<AppConnectionData> m_appDataService;

	private HashMap<Integer, HashMap<String, AppConnectionProto>> m_datas = new LinkedHashMap<Integer, HashMap<String, AppConnectionProto>>();

	private AppConfigManager m_appConfigManager;

	private long m_startTime;

	private static Semaphore m_semaphore = new Semaphore(1);

	public ConnectionBucketExecutor(long startTime, AppService<AppConnectionData> appDataService,
	      AppConfigManager appConfigManager) {
		m_startTime = startTime;
		m_appDataService = appDataService;
		m_appConfigManager = appConfigManager;
	}

	protected void batchInsert(List<AppConnectionData> appConnectionDatas, List<AppConnectionProto> datas) {
		int[] ret = null;
		int id = 0;

		try {
			int length = appConnectionDatas.size();
			AppConnectionData[] array = new AppConnectionData[length];
			AppConnectionData[] all = new AppConnectionData[length];

			for (int i = 0; i < length; i++) {
				AppConnectionData appConnectionData = appConnectionDatas.get(i);
				array[i] = appConnectionData;
				id = appConnectionData.getCommandId();
			}
			ret = m_appDataService.insert(array);

			if (m_appConfigManager.shouldAdd2AllCommands(id)) {
				for (int i = 0; i < length; i++) {
					AppConnectionData appConnectionData = appConnectionDatas.get(i);
					AppConnectionData copyData = copyAppConnectionData(appConnectionData);
					all[i] = copyData;
				}
				m_appDataService.insert(all);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		if (ret != null) {
			for (int i = 0; i < datas.size(); i++) {
				datas.get(i).setFlushed();
			}
		}
	}

	private AppConnectionData copyAppConnectionData(AppConnectionData appConnectionData) {
		AppConnectionData data = new AppConnectionData();

		data.setAccessNumber(appConnectionData.getAccessNumber());
		data.setAppVersion(appConnectionData.getAppVersion());
		data.setCity(appConnectionData.getCity());
		data.setCode(appConnectionData.getCode());
		data.setConnectType(appConnectionData.getConnectType());
		data.setCreationDate(appConnectionData.getCreationDate());
		data.setId(appConnectionData.getId());
		data.setKeyId(appConnectionData.getKeyId());
		data.setMinuteOrder(appConnectionData.getMinuteOrder());
		data.setNetwork(appConnectionData.getNetwork());
		data.setOperator(appConnectionData.getOperator());
		data.setPeriod(appConnectionData.getPeriod());
		data.setPlatform(appConnectionData.getPlatform());
		data.setRequestPackage(appConnectionData.getRequestPackage());
		data.setResponsePackage(appConnectionData.getResponsePackage());
		data.setResponseSumTime(appConnectionData.getResponseSumTime());
		data.setStatus(appConnectionData.getStatus());
		data.setCommandId(AppConfigManager.ALL_COMMAND_ID);
		return data;
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

		for (Entry<Integer, HashMap<String, AppConnectionProto>> outerEntry : m_datas.entrySet()) {
			try {
				List<AppConnectionData> commands = new ArrayList<AppConnectionData>();
				List<AppConnectionProto> datas = new ArrayList<AppConnectionProto>();
				HashMap<String, AppConnectionProto> value = outerEntry.getValue();

				m_semaphore.tryAcquire(500, TimeUnit.MILLISECONDS);

				for (Entry<String, AppConnectionProto> entry : value.entrySet()) {
					try {
						AppConnectionProto appData = entry.getValue();

						if (appData.notFlushed()) {
							AppConnectionData proto = new AppConnectionData();

							proto.setPeriod(period);
							proto.setMinuteOrder(minute);
							proto.setCommandId(appData.getCommand());
							proto.setCity(appData.getCity());
							proto.setOperator(appData.getOperator());
							proto.setNetwork(appData.getNetwork());
							proto.setAppVersion(appData.getVersion());
							proto.setConnectType(appData.getConnectType());
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

								commands = new ArrayList<AppConnectionData>();
								datas = new ArrayList<AppConnectionProto>();
							}
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				if (commands.size() > 0 && datas.size() > 0) {
					batchInsert(commands, datas);
				}
			} catch (Exception e) {
				Cat.logError(e);
			} finally {
				m_semaphore.release();
			}
		}
		m_datas.clear();
	}

	@Override
	public ProtoData loadRecord(String[] items) {
		AppConnectionProto appData = new AppConnectionProto();

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

	public AppService<?> getAppDataService() {
		return m_appDataService;
	}

	public HashMap<Integer, HashMap<String, AppConnectionProto>> getDatas() {
		return m_datas;
	}

	public long getStartTime() {
		return m_startTime;
	}

	@Override
	public void processEntity(ProtoData appData) {
		try {
			AppConnectionProto appConnectionData = (AppConnectionProto) appData;
			int command = appConnectionData.getCommand();
			StringBuilder sb = new StringBuilder();
			char split = ':';

			sb.append(appConnectionData.getCity()).append(split);
			sb.append(appConnectionData.getOperator()).append(split);
			sb.append(appConnectionData.getConnectType()).append(split);
			sb.append(appConnectionData.getVersion()).append(split);
			sb.append(appConnectionData.getNetwork()).append(split);
			sb.append(appConnectionData.getCode()).append(split);
			sb.append(appConnectionData.getPlatform());

			String key = sb.toString();
			HashMap<String, AppConnectionProto> secondMap = m_datas.get(command);

			if (secondMap == null) {
				secondMap = new LinkedHashMap<String, AppConnectionProto>();

				secondMap.put(key, appConnectionData);
				m_datas.put(command, secondMap);
			} else {
				AppConnectionProto mergedAppData = secondMap.get(key);

				if (mergedAppData == null) {
					secondMap.put(key, appConnectionData);
				} else {
					mergedAppData.addCount(appConnectionData.getCount());
					mergedAppData.addRequestByte(appConnectionData.getRequestByte());
					mergedAppData.addResponseByte(appConnectionData.getResponseByte());
					mergedAppData.addResponseTime(appConnectionData.getResponseTime());
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

				for (Entry<Integer, HashMap<String, AppConnectionProto>> outerEntry : m_datas.entrySet()) {
					HashMap<String, AppConnectionProto> value = outerEntry.getValue();

					for (Entry<String, AppConnectionProto> entry : value.entrySet()) {
						try {
							AppConnectionProto appData = entry.getValue();

							m_semaphore.tryAcquire(500, TimeUnit.MILLISECONDS);

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
						} catch (Exception e) {
							Cat.logError(e);
						} finally {
							m_semaphore.release();
						}
					}
				}
				writer.close();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}
}
