package com.dianping.cat.report.task.alert.exception;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.top.TopMetric;
import com.dianping.cat.report.page.top.TopMetric.Item;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.task.alert.manager.AlertManager;
import com.dianping.cat.report.task.alert.sender.ExceptionPostman;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class ExceptionAlert implements Task, LogEnabled {

	@Inject
	private ExceptionAlertConfig m_alertConfig;

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Inject
	private AlertExceptionBuilder m_alertBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	@Inject
	protected ExceptionPostman m_postman;

	@Inject
	protected AlertManager m_alertManager;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private static final int ALERT_PERIOD = 1;

	private Logger m_logger;

	private TopMetric buildTopMetric(Date date) {
		TopReport topReport = queryTopReport(date);
		TopMetric topMetric = new TopMetric(ALERT_PERIOD, Integer.MAX_VALUE, m_exceptionConfigManager);

		topMetric.setStart(date).setEnd(new Date(date.getTime() + TimeUtil.ONE_MINUTE));
		topMetric.visitTopReport(topReport);
		return topMetric;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getName() {
		return "exception-alert";
	}

	private TopReport queryTopReport(Date start) {
		String domain = Constants.CAT;
		String date = String.valueOf(start.getTime());
		ModelRequest request = new ModelRequest(domain, start.getTime()).setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean active = true;
		while (active) {
			long current = System.currentTimeMillis();
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("ExceptionAlert", "M" + minuteStr);

			try {
				TopMetric topMetric = buildTopMetric(new Date(current - TimeUtil.ONE_MINUTE * 2));
				Collection<List<Item>> items = topMetric.getError().getResult().values();
				List<Item> item = new ArrayList<Item>();

				if (!items.isEmpty()) {
					item = items.iterator().next();
				}
				Map<String, List<AlertException>> alertExceptions = m_alertBuilder.buildAlertExceptions(item);

				for (Entry<String, List<AlertException>> entry : alertExceptions.entrySet()) {
					try {
						String domain = entry.getKey();
						List<AlertException> exceptions = entry.getValue();

						m_postman.sendAlert(m_alertConfig, m_alertBuilder, domain, exceptions);

						String mailTitle = m_alertConfig.buildMailTitle(domain, null);
						String content = m_alertBuilder.buildDBContent(exceptions.toString(), domain);

						for (AlertException exception : exceptions) {
							AlertResultEntity alertResult = new AlertResultEntity(true, content, exception.getType());
							m_alertManager.storeAlert(getName(), domain, exception.getName(), mailTitle, alertResult);
						}
					} catch (Exception e) {
						m_logger.error(e.getMessage());
					}
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				t.complete();
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}
}
