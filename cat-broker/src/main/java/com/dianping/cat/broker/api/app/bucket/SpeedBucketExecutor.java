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
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.broker.api.app.BaseData;
import com.dianping.cat.broker.api.app.RawAppSpeedData;
import com.dianping.cat.service.app.BaseAppDataService;

public class SpeedBucketExecutor implements BucketExecutor {

	private BaseAppDataService<AppSpeedData> m_appDataService;

	private HashMap<Integer, HashMap<String, RawAppSpeedData>> m_datas = new LinkedHashMap<Integer, HashMap<String, RawAppSpeedData>>();

	private long m_startTime;

	private CountDownLatch m_flushCountDownLatch = new CountDownLatch(0);

	private CountDownLatch m_saveCountDownLatch = new CountDownLatch(0);

	public SpeedBucketExecutor(long startTime, BaseAppDataService<AppSpeedData> appSpeedDataService) {
		m_startTime = startTime;
		m_appDataService = appSpeedDataService;
	}

	@Override
	public void processEntity(BaseData appData) {
		try {
			RawAppSpeedData appCommandData = (RawAppSpeedData) appData;
			int speedId = appCommandData.getSpeedId();
			StringBuilder sb = new StringBuilder();
			char split = ':';

			sb.append(appCommandData.getCity()).append(split);
			sb.append(appCommandData.getOperator()).append(split);
			sb.append(appCommandData.getVersion()).append(split);
			sb.append(appCommandData.getNetwork()).append(split);
			sb.append(appCommandData.getPlatform());

			String key = sb.toString();
			HashMap<String, RawAppSpeedData> secondMap = m_datas.get(speedId);

			if (secondMap == null) {
				secondMap = new LinkedHashMap<String, RawAppSpeedData>();

				secondMap.put(key, appCommandData);
				m_datas.put(speedId, secondMap);
			} else {
				RawAppSpeedData mergedAppData = secondMap.get(key);

				if (mergedAppData == null) {
					secondMap.put(key, appCommandData);
				} else {
					mergedAppData.addCount(appCommandData.getCount());
					mergedAppData.addResponseTime(appCommandData.getResponseTime());
					mergedAppData.addSlowCount(appCommandData.getSlowCount());
					mergedAppData.addSlowResponseTime(appCommandData.getSlowResponseTime());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

	}

	protected void batchInsert(List<AppSpeedData> eneities, List<RawAppSpeedData> datas) {
		int[] ret = null;
		try {
			int length = eneities.size();
			AppSpeedData[] array = new AppSpeedData[length];

			for (int i = 0; i < length; i++) {
				array[i] = eneities.get(i);
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

		for (Entry<Integer, HashMap<String, RawAppSpeedData>> outerEntry : m_datas.entrySet()) {
			try {
				List<AppSpeedData> commands = new ArrayList<AppSpeedData>();
				List<RawAppSpeedData> datas = new ArrayList<RawAppSpeedData>();
				HashMap<String, RawAppSpeedData> value = outerEntry.getValue();

				for (Entry<String, RawAppSpeedData> entry : value.entrySet()) {
					m_saveCountDownLatch.await();

					m_flushCountDownLatch = new CountDownLatch(1);
					try {
						RawAppSpeedData appData = entry.getValue();

						if (appData.notFlushed()) {
							AppSpeedData proto = new AppSpeedData();

							proto.setSpeedId(appData.getSpeedId());
							proto.setPeriod(period);
							proto.setMinuteOrder(minute);
							proto.setCity(appData.getCity());
							proto.setOperator(appData.getOperator());
							proto.setNetwork(appData.getNetwork());
							proto.setAppVersion(appData.getVersion());
							proto.setPlatform(appData.getPlatform());
							proto.setAccessNumber(appData.getCount());
							proto.setResponseSumTime(appData.getResponseTime());
							proto.setSlowAccessNumber(appData.getSlowCount());
							proto.setSlowResponseSumTime(appData.getSlowResponseTime());
							proto.setCreationDate(new Date());

							commands.add(proto);
							datas.add(appData);

							if (commands.size() >= 100) {
								batchInsert(commands, datas);

								commands = new ArrayList<AppSpeedData>();
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
	public void save(File file) {
		if (m_datas.size() > 0) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
				char tab = '\t';
				char enter = '\n';

				for (Entry<Integer, HashMap<String, RawAppSpeedData>> outerEntry : m_datas.entrySet()) {
					HashMap<String, RawAppSpeedData> value = outerEntry.getValue();

					for (Entry<String, RawAppSpeedData> entry : value.entrySet()) {
						m_flushCountDownLatch.await();

						m_saveCountDownLatch = new CountDownLatch(1);
						RawAppSpeedData appData = entry.getValue();

						if (appData.notFlushed()) {
							StringBuilder sb = new StringBuilder();
							sb.append(appData.getClass().getName()).append(tab);
							sb.append(appData.getSpeedId()).append(tab);
							sb.append(appData.getTimestamp()).append(tab);
							sb.append(appData.getCity()).append(tab);
							sb.append(appData.getOperator()).append(tab);
							sb.append(appData.getNetwork()).append(tab);
							sb.append(appData.getVersion()).append(tab);
							sb.append(appData.getPlatform()).append(tab);
							sb.append(appData.getCount()).append(tab);
							sb.append(appData.getResponseTime()).append(tab);
							sb.append(appData.getSlowCount()).append(tab);
							sb.append(appData.getSlowResponseTime()).append(enter);

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

	@Override
	public BaseData loadRecord(String[] items) {
		RawAppSpeedData appData = new RawAppSpeedData();

		appData.setSpeedId(Integer.parseInt(items[1]));
		appData.setTimestamp(Long.parseLong(items[2]));
		appData.setCity(Integer.parseInt(items[3]));
		appData.setOperator(Integer.parseInt(items[4]));
		appData.setNetwork(Integer.parseInt(items[5]));
		appData.setVersion(Integer.parseInt(items[6]));
		appData.setPlatform(Integer.parseInt(items[9]));
		appData.setCount(Integer.parseInt(items[10]));
		appData.setResponseTime(Integer.parseInt(items[11]));
		appData.setSlowCount(Integer.parseInt(items[12]));
		appData.setSlowResponseTime(Integer.parseInt(items[13]));
		return appData;
	}

}
