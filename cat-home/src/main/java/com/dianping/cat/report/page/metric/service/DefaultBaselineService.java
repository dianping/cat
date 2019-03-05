/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.metric.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.home.dal.report.BaselineDao;
import com.dianping.cat.home.dal.report.BaselineEntity;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.task.TaskHelper;

@Named(type = BaselineService.class)
public class DefaultBaselineService implements BaselineService {

	@Inject
	private BaselineDao m_baselineDao;

	private Map<String, Baseline> m_baselines = new LinkedHashMap<String, Baseline>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Baseline> eldest) {
			return size() > 50000;
		}
	};

	private Map<String, String> m_empties = new LinkedHashMap<String, String>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, String> eldest) {
			return size() > 50000;
		}
	};

	private double[] decodeBaselines(byte[] datas) throws IOException {
		double[] result;
		ByteArrayInputStream input = new ByteArrayInputStream(datas);
		DataInputStream dataInput = new DataInputStream(input);
		int size = dataInput.readInt();

		result = new double[size];
		for (int i = 0; i < size; i++) {
			result[i] = dataInput.readDouble();
		}
		return result;
	}

	private byte[] encodeBaselines(double[] dataInDoubleArray) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		DataOutputStream output = new DataOutputStream(out);

		output.writeInt(dataInDoubleArray.length);
		for (double dataItem : dataInDoubleArray) {
			output.writeDouble(dataItem);
		}
		return out.toByteArray();
	}

	private Map<String, String> getEmpties() {
		synchronized (this) {
			return m_empties;
		}
	}

	@Override
	public boolean hasDailyBaseline(String reportName, String key, Date reportPeriod) {
		String baselineKey = reportName + ":" + key + ":" + reportPeriod;
		Baseline baseline = m_baselines.get(baselineKey);
		boolean has = false;

		if (baseline != null) {
			has = true;
		} else {
			try {
				baseline = m_baselineDao.findByReportNameKeyTime(reportPeriod, reportName, key, BaselineEntity.READSET_FULL);
				has = true;
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return has;
	}

	@Override
	public void insertBaseline(Baseline baseline) {
		try {
			baseline.setData(encodeBaselines(baseline.getDataInDoubleArray()));
			m_baselineDao.insert(baseline);

			String baselineKey = baseline.getReportName() + ":" + baseline.getIndexKey() + ":"	+ baseline.getReportPeriod();

			getEmpties().remove(baselineKey);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	@Override
	public double[] queryBaseline(int currentMinute, int ruleMinute, String metricKey, String name) {
		double[] baseline = new double[ruleMinute];

		if (currentMinute >= ruleMinute - 1) {
			int start = currentMinute + 1 - ruleMinute;
			int end = currentMinute;

			baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), name);
		} else if (currentMinute < 0) {
			int start = 60 + currentMinute + 1 - (ruleMinute);
			int end = 60 + currentMinute;

			baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), name);
		} else {
			int currentStart = 0, currentEnd = currentMinute;
			double[] currentBaseline = queryBaseLine(currentStart, currentEnd, metricKey,
									new Date(ModelPeriod.CURRENT.getStartTime()), name);

			int lastStart = 60 + 1 - (ruleMinute - currentMinute);
			int lastEnd = 59;
			double[] lastBaseline = queryBaseLine(lastStart, lastEnd, metricKey,	new Date(ModelPeriod.LAST.getStartTime()),
									name);

			baseline = mergerArray(lastBaseline, currentBaseline);
		}

		return baseline;
	}

	private double[] queryBaseLine(int start, int end, String key, Date date, String name) {
		double[] baseline = queryHourlyBaseline(name, key, date);
		int length = end - start + 1;
		double[] result = new double[length];

		if (baseline != null) {
			System.arraycopy(baseline, start, result, 0, length);
		}

		return result;
	}

	@Override
	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
		String baselineKey = reportName + ":" + key + ":" + reportPeriod;
		Baseline baseline = m_baselines.get(baselineKey);

		if (baseline == null) {
			try {
				boolean has = getEmpties().containsKey(baselineKey);

				if (!has) {
					baseline = m_baselineDao.findByReportNameKeyTime(reportPeriod, reportName, key,	BaselineEntity.READSET_FULL);
					m_baselines.put(baselineKey, baseline);
				} else {
					return null;
				}
			} catch (DalNotFoundException e) {
				getEmpties().put(baselineKey, baselineKey);
				return null;
			} catch (Exception e) {
				Cat.logError(e);
				return null;
			}
		}

		try {
			return decodeBaselines(baseline.getData());
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	@Override
	public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) {
		double[] result = new double[60];
		Date today = TaskHelper.todayZero(reportPeriod);
		int hour = (int) ((reportPeriod.getTime() - today.getTime()) / TimeHelper.ONE_HOUR);
		double[] dayResult = queryDailyBaseline(reportName, key, today);

		if (dayResult != null) {
			for (int i = 0; i < 60; i++) {
				result[i] = dayResult[hour * 60 + i];
			}
		}
		return result;
	}
}
