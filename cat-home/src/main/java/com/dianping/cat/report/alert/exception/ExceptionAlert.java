package com.dianping.cat.report.alert.exception;

import java.util.ArrayList;
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
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.dependency.TopMetric.Item;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.sender.AlertManager;

public class ExceptionAlert implements Task {

	@Inject
	protected ExceptionRuleConfigManager m_exceptionConfigManager;

	@Inject
	protected AlertExceptionBuilder m_alertBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	protected ModelService<TopReport> m_topService;

	@Inject
	protected AlertManager m_sendManager;

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	protected static final int ALERT_PERIOD = 1;

	protected TopMetric buildTopMetric(Date date) {
		TopReport topReport = queryTopReport(date);
		TopMetric topMetric = new TopMetric(ALERT_PERIOD, Integer.MAX_VALUE, m_exceptionConfigManager);

		topMetric.setStart(date).setEnd(new Date(date.getTime() + TimeHelper.ONE_MINUTE));
		topMetric.visitTopReport(topReport);
		return topMetric;
	}

	public String getName() {
		return AlertType.Exception.getName();
	}

	private void handleExceptions(List<Item> itemList) {
		Map<String, List<AlertException>> alertExceptions = m_alertBuilder.buildAlertExceptions(itemList);

		for (Entry<String, List<AlertException>> entry : alertExceptions.entrySet()) {
			try {
				String domain = entry.getKey();
				List<AlertException> exceptions = entry.getValue();

				for (AlertException exception : exceptions) {
					String metricName = exception.getName();
					AlertEntity entity = new AlertEntity();

					entity.setDate(new Date()).setContent(exception.toString()).setLevel(exception.getType());
					entity.setMetric(metricName).setType(getName()).setGroup(domain);
					m_sendManager.addAlert(entity);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	protected TopReport queryTopReport(Date start) {
		String domain = Constants.CAT;
		String date = String.valueOf(start.getTime());
		ModelRequest request = new ModelRequest(domain, start.getTime()).setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			report.accept(new TopExceptionExclude(m_exceptionConfigManager));
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("AlertException", TimeHelper.getMinuteStr());

			try {
				TopMetric topMetric = buildTopMetric(new Date(current - TimeHelper.ONE_MINUTE * 2));
				Collection<List<Item>> itemLists = topMetric.getError().getResult().values();
				List<Item> itemList = new ArrayList<Item>();

				if (!itemLists.isEmpty()) {
					itemList = itemLists.iterator().next();
				}
				List<Item> items = new ArrayList<Item>();

				for (Item item : itemList) {
					if (!Constants.FRONT_END.equals(item.getDomain())) {
						items.add(item);
					}
				}
				handleExceptions(items);

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
