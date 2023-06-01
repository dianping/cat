package com.dianping.cat.report.page.browser.task;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.web.*;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Calendar;
import java.util.Date;

@Named(type = TaskBuilder.class, value = WebDatabasePruner.ID)
public class WebDatabasePruner implements TaskBuilder {

	public static final String ID = Constants.WEB_DATABASE_PRUNER;

	@Inject
	private WebSpeedDataDao m_webSpeedDataDao;

	@Inject
	private AjaxDataDao m_ajaxDataDao;

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	@Inject
	private JsErrorLogContentDao m_jsErrorLogContentDao;

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	private WebSpeedConfigManager m_webSpeedConfigManager;

	private static final int DURATION = -2;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Threads.forGroup("cat").start(new DeleteTask());
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("WebDatabasePruner  builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("WebDatabasePruner builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("WebDatabasePruner builder don't support weekly task");
	}

	public Date queryPeriod(int months) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

	private boolean pruneDatabase(int months) {
		Date period = queryPeriod(months);
		boolean ajax = pruneAjaxData(period);
		boolean speed = pruneSpeedData(period);
		boolean js = pruneJsLog(period);

		return ajax && speed && js;
	}

	public boolean pruneJsLog(Date period) {
		boolean success = true;
		Transaction t = Cat.newTransaction("DeleteTask", "jsError");

		try {
			JsErrorLog jsErrorLog = m_jsErrorLogDao.createLocal();
			jsErrorLog.setUpdatetime(period);
			m_jsErrorLogDao.deleteBeforePeriod(jsErrorLog);

			JsErrorLogContent jsErrorLogContent = m_jsErrorLogContentDao.createLocal();
			jsErrorLogContent.setUpdatetime(period);
			m_jsErrorLogContentDao.deleteBeforePeriod(jsErrorLogContent);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
			success = false;
		} finally {
			t.complete();
		}

		return success;
	}

	private boolean pruneAjaxData(Date period) {
		boolean success = true;

		for (int id : m_urlPatternConfigManager.getUrlIds()) {
			Transaction t = Cat.newTransaction("DeleteTask", "Ajax");

			try {
				pruneAjaxDataTable(period, id);
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
				success = false;
			} finally {
				t.complete();
			}
		}
		return success;
	}

	public void pruneAjaxDataTable(Date period, int id) throws DalException {
		AjaxData ajaxData = m_ajaxDataDao.createLocal();
		ajaxData.setApiId(id);
		ajaxData.setPeriod(period);
		m_ajaxDataDao.deleteBeforePeriod(ajaxData);
	}

	private boolean pruneSpeedData(Date period) {
		boolean success = true;

		for (int id : m_webSpeedConfigManager.querySpeedIds()) {
			Transaction t = Cat.newTransaction("DeleteTask", "WebSpeed");

			try {
				pruneSpeedDataTable(period, id);
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
				success = false;
			} finally {
				t.complete();
			}
		}
		return success;
	}

	public void pruneSpeedDataTable(Date period, int id) throws DalException {
		WebSpeedData webSpeedData = m_webSpeedDataDao.createLocal();

		webSpeedData.setSpeedId(id);
		webSpeedData.setPeriod(period);
		m_webSpeedDataDao.deleteBeforePeriod(webSpeedData);
	}

	private class DeleteTask implements Task {

		@Override
		public void run() {
			try {
				pruneDatabase(DURATION);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		@Override
		public String getName() {
			return "delete-web-job";
		}

		@Override
		public void shutdown() {
		}
	}

}
