package com.dianping.cat.report;

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
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.core.dal.DailyReport;
import com.dianping.cat.consumer.core.dal.DailyReportDao;
import com.dianping.cat.consumer.core.dal.DailyReportEntity;
import com.dianping.cat.consumer.core.dal.MonthlyReport;
import com.dianping.cat.consumer.core.dal.MonthlyReportDao;
import com.dianping.cat.consumer.core.dal.MonthlyReportEntity;
import com.dianping.cat.consumer.core.dal.Report;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.ReportEntity;
import com.dianping.cat.consumer.core.dal.WeeklyReport;
import com.dianping.cat.consumer.core.dal.WeeklyReportDao;
import com.dianping.cat.consumer.core.dal.WeeklyReportEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.RemoteModelService.HttpServiceCallback;
import com.dianping.cat.report.model.ModelRequest;

/**
 * Report service to get timed (hourly, daily, weekly, monthly etc.) reports from various medias (memory, database
 * etc.).
 */
public class DefaultReportService<T> extends ContainerHolder implements ReportService<T>, Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private ReportDao m_hourlyReportDao;

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
		T result = delegate.makeReport(domain, startTime, ReportConstants.HOUR);

		try {
			List<DailyReport> reports = m_dailyReportDao.findAllByPeriodDomainName(new Date(startTime), domain, name,
			      DailyReportEntity.READSET_FULL);

			for (DailyReport report : reports) {
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
			List<Report> reports = m_hourlyReportDao.findAllByPeriodDomainName(new Date(startTime), domain, name,
			      ReportEntity.READSET_CONTENT);

			for (Report report : reports) {
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
		T result = delegate.makeReport(domain, startTime, ReportConstants.HOUR);

		try {
			List<MonthlyReport> reports = m_monthlyReportDao.findAllByPeriodDomainName(new Date(startTime), domain, name,
			      MonthlyReportEntity.READSET_FULL);

			for (MonthlyReport report : reports) {
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
	public T getWeeklyReport(ModelRequest request) {
		return getWeeklyReportFromDatabase(request);
	}

	@SuppressWarnings("unchecked")
	protected T getWeeklyReportFromDatabase(ModelRequest request) {
		String domain = request.getDomain();
		long startTime = request.getStartTime();
		String name = request.getReportName();
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);
		T result = delegate.makeReport(domain, startTime, ReportConstants.HOUR);

		try {
			List<WeeklyReport> reports = m_weeklyReportDao.findAllByPeriodDomainName(new Date(startTime), domain, name,
			      WeeklyReportEntity.READSET_FULL);

			for (WeeklyReport report : reports) {
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
	public void initialize() throws InitializationException {
		m_endpoints = m_configManager.getConsoleEndpoints();
	}
}
