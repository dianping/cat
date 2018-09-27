package com.dianping.cat.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.task.TaskManager.TaskProlicy;


public class TaskManagerTest {
	private static final long HOUR = 60 * 60 * 1000L;

	@Test
	public void testAll() throws Exception{
		MockTaskManager analyzer = new MockTaskManager();
		Date start = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2013-04-23 00:00");
		Date end = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2013-06-23 00:00");
		long dateLong = start.getTime();

		for (; dateLong < end.getTime(); dateLong = dateLong + HOUR) {
			Date date = new Date(dateLong);


			analyzer.createTask(date,"cat","trasnaction",TaskProlicy.ALL);
		}
		Map<Integer, Set<String>> result = analyzer.getResults();

		Assert.assertEquals(2, result.get(TaskManager.REPORT_MONTH).size());
		Assert.assertEquals(9, result.get(TaskManager.REPORT_WEEK).size());
		Assert.assertEquals(61, result.get(TaskManager.REPORT_DAILY).size());
		Assert.assertEquals(61 * 24, result.get(TaskManager.REPORT_HOUR).size());
	}

	@Test
	public void testExcluedHourly() throws Exception{
		MockTaskManager analyzer = new MockTaskManager();
		Date start = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2013-04-23 00:00");
		Date end = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2013-06-23 00:00");
		long dateLong = start.getTime();

		for (; dateLong < end.getTime(); dateLong = dateLong + HOUR) {
			Date date = new Date(dateLong);


			analyzer.createTask(date,"cat","trasnaction",TaskProlicy.ALL_EXCLUED_HOURLY);
		}
		Map<Integer, Set<String>> result = analyzer.getResults();
		Assert.assertEquals(2, result.get(TaskManager.REPORT_MONTH).size());
		Assert.assertEquals(9, result.get(TaskManager.REPORT_WEEK).size());
		Assert.assertEquals(61, result.get(TaskManager.REPORT_DAILY).size());
		Assert.assertEquals(null, result.get(TaskManager.REPORT_HOUR));
	}

	
	public static class MockTaskManager extends TaskManager{
		private Map<Integer, Set<String>> m_results = new HashMap<Integer, Set<String>>();

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		@Override
		protected void insertToDatabase(Date period, String ip, String domain, int reportType) throws DalException {
			Set<String> lists = m_results.get(reportType);

			if (lists == null) {
				lists = new HashSet<String>();
				m_results.put(reportType, lists);
			}

			lists.add(sdf.format(period));
		}

		public Map<Integer, Set<String>> getResults() {
			return m_results;
		}

	}
}
