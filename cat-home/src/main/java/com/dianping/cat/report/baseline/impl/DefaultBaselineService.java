package com.dianping.cat.report.baseline.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.home.dal.report.BaselineDao;
import com.dianping.cat.home.dal.report.BaselineEntity;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.task.TaskHelper;

public class DefaultBaselineService implements BaselineService, LogEnabled {

	@Inject
	private BaselineDao m_baselineDao;

	private Map<String, Map<String, Baseline>> m_baselines = new HashMap<String, Map<String, Baseline>>();

	private Map<String, Baseline> m_baselineMaps = new LinkedHashMap<String, Baseline>(1000) {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Baseline> eldest) {
			return true;
		}
	};
	
	private Logger m_logger;

	@Override
	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
		double[] result = new double[24 * 60];
		Baseline baseline = queryFromMap(reportName, key, reportPeriod);
		if (baseline == null) {
			try {
				baseline = m_baselineDao
				      .findByReportNameKeyTime(reportPeriod, reportName, key, BaselineEntity.READSET_FULL);
				addBaselineToMap(baseline, reportName, key);
				result = parse(baseline.getData());
				return result;
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return result;
	}

	private void addBaselineToMap(Baseline baseline, String reportName, String key) {
		Map<String, Baseline> baselineMap = m_baselines.get(reportName);
		if (baselineMap == null) {
			baselineMap = new HashMap<String, Baseline>();
			m_baselines.put(reportName, baselineMap);
		}
		baselineMap.put(key, baseline);
	}

	private Baseline queryFromMap(String reportName, String key, Date reportPeriod) {
		Map<String, Baseline> baselineMap = m_baselines.get(reportName);
		if (baselineMap == null) {
			return null;
		}
		Baseline result = baselineMap.get(key);
		if (result != null && result.getReportPeriod().equals(reportPeriod)) {
			return result;
		}
		return null;
	}

	@Override
	public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) {
		double[] result = new double[60];
		Date today = TaskHelper.todayZero(reportPeriod);
		int hour = (int) ((reportPeriod.getTime() - today.getTime()) / TimeUtil.ONE_HOUR);
		try {
			Baseline baseline = m_baselineDao.findByReportNameKeyTime(today, reportName, key, BaselineEntity.READSET_FULL);
			double[] dayResult = parse(baseline.getData());
			for (int i = 0; i < 60; i++) {
				result[i] = dayResult[hour * 60 + i];
			}
			return result;
		} catch (DalNotFoundException e) {
			m_logger.warn("base line cant't be fonund!" + reportName + " " + key + " " + reportPeriod);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public void insertBaseline(Baseline baseline) {
		try {
			baseline.setData(build(baseline.getDataInDoubleArray()));
			m_baselineDao.insert(baseline);
		} catch (DalException e) {
			Cat.logError(e);
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	private double[] parse(byte[] datas) throws IOException {
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

	private byte[] build(double[] dataInDoubleArray) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		DataOutputStream output = new DataOutputStream(out);

		output.writeInt(dataInDoubleArray.length);
		for (double dataItem : dataInDoubleArray) {
			output.writeDouble(dataItem);
		}
		return out.toByteArray();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
