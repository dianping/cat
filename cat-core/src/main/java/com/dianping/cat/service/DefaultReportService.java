package com.dianping.cat.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.service.RemoteModelService.HttpServiceCallback;

/**
 * Report service to get timed (hourly, daily, weekly, monthly etc.) reports from various medias (memory, database
 * etc.).
 */
public class DefaultReportService<T> extends ContainerHolder implements ReportService<T>, Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Inject
	private RemoteModelService m_hourlyService;

	private List<Pair<String, Integer>> m_endpoints;

	@Override
	public T getDailyReport(ModelRequest request) {
		return getHouylyReportFromDatabase(request);
	}

	@SuppressWarnings("unchecked")
	protected T getDailyReportFromDatabase(ModelRequest request) {
		String domain = request.getDomain();
		long startTime = request.getStartTime();
		String name = request.getReportName();
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);

		try {
			DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
			      DailyReportEntity.READSET_FULL);
			String xml = report.getContent();

			return delegate.parseXml(xml);
		} catch (Exception e) {
			Cat.logError(e);
		}

		return delegate.makeReport(domain, startTime, ReportConstants.HOUR);
	}

	@SuppressWarnings("unchecked")
	protected T getHourlyReportFromRemote(final ModelRequest request) {
		String domain = request.getDomain();
		long startTime = request.getStartTime();
		String name = request.getReportName();
		final ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);
		final T result = delegate.makeReport(domain, startTime, ReportConstants.HOUR);
		final Semaphore semaphore = new Semaphore(0);
		final Transaction t = Cat.getProducer().newTransaction("ModelService", name);
		int count = 0;

		t.setStatus(Message.SUCCESS);
		t.addData("domain", domain);

		try {
			for (Pair<String, Integer> endpoint : m_endpoints) {
				m_hourlyService.invoke(endpoint, t, name, request, new HttpServiceCallback() {
					@Override
					public void onComplete(String content) {
						semaphore.release();

						try {
							T model = delegate.parseXml(content);

							delegate.mergeReport(result, model);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}

					@Override
					public void onException(Exception e, boolean timeout) {
						semaphore.release();
						Cat.logError(e);
					}
				});
				count++;
			}

			semaphore.tryAcquire(count, 5000, TimeUnit.MILLISECONDS); // 5 seconds timeout
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			t.complete();
		}

		return result;
	}

	@Override
	public T getHouylyReport(ModelRequest request) {
		switch (request.getPeriod()) {
		case CURRENT:
		case LAST:
			return getHourlyReportFromRemote(request);
		case HISTORICAL:
			if (m_configManager.isLocalMode()) {
				return getHourlyReportFromRemote(request);
			} else {
				return getHouylyReportFromDatabase(request);
			}
		default:
			break;
		}

		throw new UnsupportedOperationException(String.format("Not future report available for %s!", request.getPeriod()));
	}

	@SuppressWarnings("unchecked")
	protected T getHouylyReportFromDatabase(ModelRequest request) {
		String domain = request.getDomain();
		long startTime = request.getStartTime();
		String name = request.getReportName();
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);
		T result = delegate.makeReport(domain, startTime, ReportConstants.HOUR);

		try {
			List<HourlyReport> reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
			      HourlyReportEntity.READSET_CONTENT);

			for (HourlyReport report : reports) {
				try {
					String xml = report.getContent();
					T model = delegate.parseXml(xml);

					result = delegate.mergeReport(result, model);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return result;
	}

	@Override
	public T getMonthlyReport(ModelRequest request) {
		return getMonthlyReportFromDatabase(request);
	}

	@SuppressWarnings("unchecked")
	protected T getMonthlyReportFromDatabase(ModelRequest request) {
		String domain = request.getDomain();
		long startTime = request.getStartTime();
		String name = request.getReportName();
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);

		try {
			MonthlyReport report = m_monthlyReportDao.findReportByDomainNamePeriod(new Date(startTime), domain, name,
			      MonthlyReportEntity.READSET_FULL);

			String xml = report.getContent();
			return delegate.parseXml(xml);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return delegate.makeReport(domain, startTime, ReportConstants.DAY * 30);
	}

	@Override
	public T getWeeklyReport(ModelRequest request) {
		return getWeeklyReportFromDatabase(request);
	}

	@SuppressWarnings("unchecked")
	protected T getWeeklyReportFromDatabase(ModelRequest request) {
		String domain = request.getDomain();
		long startTime = request.getStartTime();
		String name = request.getReportName();
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);

		try {
			WeeklyReport report = m_weeklyReportDao.findReportByDomainNamePeriod(new Date(startTime), domain, name,
			      WeeklyReportEntity.READSET_FULL);
			String xml = report.getContent();

			return delegate.parseXml(xml);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return delegate.makeReport(domain, startTime, ReportConstants.WEEK);
	}

	@Override
	public void initialize() throws InitializationException {
		m_endpoints = m_configManager.getConsoleEndpoints();
	}
}
