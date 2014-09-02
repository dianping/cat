package com.dianping.cat.report.baseline.impl;

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

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.home.dal.report.BaselineDao;
import com.dianping.cat.home.dal.report.BaselineEntity;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.task.TaskHelper;

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

	@Override
	public void insertBaseline(Baseline baseline) {
		try {
			baseline.setData(encodeBaselines(baseline.getDataInDoubleArray()));
			m_baselineDao.insert(baseline);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
		String baselineKey = reportName + ":" + key + ":" + reportPeriod;
		Baseline baseline = m_baselines.get(baselineKey);

		if (baseline == null) {
			try {
				String str = m_empties.get(baselineKey);

				if (str == null) {
					baseline = m_baselineDao.findByReportNameKeyTime(reportPeriod, reportName, key,
					      BaselineEntity.READSET_FULL);
					m_baselines.put(baselineKey, baseline);
				} else {
					return null;
				}
			} catch (DalNotFoundException e) {
				m_empties.put(baselineKey, baselineKey);
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
		int hour = (int) ((reportPeriod.getTime() - today.getTime()) / TimeUtil.ONE_HOUR);
		double[] dayResult = queryDailyBaseline(reportName, key, today);

		if (dayResult != null) {
			for (int i = 0; i < 60; i++) {
				result[i] = dayResult[hour * 60 + i];
			}
		}
		return result;
	}
}
