package com.dianping.cat.report;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dainping.cat.consumer.core.dal.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class DefaultReportService<T> extends ContainerHolder implements ReportService<T>, Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Logger m_logger;

	private List<Pair<String, Integer>> m_endpoints;

	@Override
	@SuppressWarnings("unchecked")
	public T createReport(String name, String domain, long startTime, long duration) {
		ReportDelegate<T> maker = lookup(ReportDelegate.class, name);

		return maker.make(domain, startTime, duration);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public T getDailyReport(String name, String domain, Date start) {
		return null;
	}

	@Override
	public T getDailyReportByPeriod(String name, String domain, Date start, Date end) {
		return null;
	}

	public T getHourlyReportFromRemote(String name, String domain, long startTime) {

		// TODO
		return null;
	}

	public URL buildUrl(Pair<String, Integer> endpoint, ModelRequest request, String name) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(64);

		for (Entry<String, String> e : request.getProperties().entrySet()) {
			if (e.getValue() != null) {
				sb.append('&');
				sb.append(e.getKey()).append('=').append(e.getValue());
			}
		}

		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", endpoint.getKey(), endpoint.getValue(), m_serviceUri, name, request.getDomain(),
		      request.getPeriod(), sb.toString());

		return new URL(url);
	}

	public ModelResponse<T> invoke(Transaction parent, ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
		Transaction t = cat.newTransaction(parent, "ModelService", getClass().getSimpleName());

		try {
			URL url = buildUrl(request);

			t.addData(url.toString());

			String xml = Files.forIO().readFrom(url.openStream(), "utf-8");
			int len = xml == null ? 0 : xml.length();

			t.addData("length", len);

			if (len > 0) {
				T report = buildModel(xml);

				response.setModel(report);
				t.setStatus(Message.SUCCESS);
			} else {
				t.setStatus("NoReport");
			}

		} catch (Exception e) {
			logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}

		return response;
	}

	public Map<String, T> getHourlyReportsFromBucket(String name, long startTime) {
		Map<String, T> reports = new HashMap<String, T>();
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(startTime, name);

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				T report = parseReport(name, xml);

				reports.put(id, report);
			}
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(String.format("Error when loading transacion reports of %s!", new Date(startTime)), e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}

		return reports;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getHouylyReport(String name, String domain, long startTime) {
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);
		T old = delegate.make(domain, startTime, ReportConstants.HOUR);

		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(new Date(startTime), domain, name,
			      ReportEntity.READSET_CONTENT);

			for (Report report : reports) {
				String xml = report.getContent();

				try {
					T model = delegate.parse(xml);

					old = delegate.merge(old, model);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.logEvent("HourlyReport.Error", name, Event.SUCCESS,
					      "domain=" + report.getDomain() + "&period=" + report.getPeriod() + "&id=" + report.getId());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return old;
	}

	@SuppressWarnings("unchecked")
	public T getHouylyReportFromDatabase(String name, String domain, long startTime) {
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);
		T old = delegate.make(domain, startTime, ReportConstants.HOUR);

		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(new Date(startTime), domain, name,
			      ReportEntity.READSET_CONTENT);

			for (Report report : reports) {
				String xml = report.getContent();

				try {
					T model = delegate.parse(xml);

					old = delegate.merge(old, model);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.logEvent("HourlyReport.Error", name, Event.SUCCESS,
					      "domain=" + report.getDomain() + "&period=" + report.getPeriod() + "&id=" + report.getId());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return old;
	}

	@Override
	public T getMonthlyReport(String name, String domain, Date start) {
		return null;
	}

	@Override
	public T getWeeklyReport(String name, String domain, Date start) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T parseReport(String name, String xml) throws Exception {
		ReportDelegate<T> delegate = lookup(ReportDelegate.class, name);

		return delegate.parse(xml);
	}

	@Override
	public void initialize() throws InitializationException {
		m_endpoints = m_configManager.getConsoleEndpoints();
	}
}
