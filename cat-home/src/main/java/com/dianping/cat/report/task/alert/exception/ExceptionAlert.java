package com.dianping.cat.report.task.alert.exception;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertEntity.AlertEntityBuilder;
import com.dianping.cat.report.task.alert.sender.dispatcher.DispatcherManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class ExceptionAlert implements Task {

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Inject
	private AlertExceptionBuilder m_alertBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	@Inject
	protected DispatcherManager m_dispatcherManager;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private static final int ALERT_PERIOD = 1;

	private TopMetric buildTopMetric(Date date) {
		TopReport topReport = queryTopReport(date);
		TopMetric topMetric = new TopMetric(ALERT_PERIOD, Integer.MAX_VALUE, m_exceptionConfigManager);

		topMetric.setStart(date).setEnd(new Date(date.getTime() + TimeUtil.ONE_MINUTE));
		topMetric.visitTopReport(topReport);
		return topMetric;
	}

	public String getName() {
		return "exception";
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
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
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

						for (AlertException exception : exceptions) {
							String metricName = exception.getName();

							AlertEntityBuilder builder = new AlertEntity().new AlertEntityBuilder();
							builder.buildDate(new Date()).buildLevel(exception.getType()).buildContent(exception.toString());
							builder.buildMetric(metricName).buildProductline(domain).buildType(getName()).buildGroup(domain);
							AlertEntity alertEntity = builder.getAlertEntity();

							m_dispatcherManager.send(alertEntity);
						}
					} catch (Exception e) {
						Cat.logError(e);
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
