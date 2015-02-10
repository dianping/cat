package com.dianping.cat.report.alert.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.dependency.TopMetric.Item;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.report.alert.sender.AlertEntity;

public class FrontEndExceptionAlert extends ExceptionAlert {

	public String getName() {
		return AlertType.FrontEndException.getName();
	}

	private void handleFrontEndException(Item frontEndItem) {
		List<AlertException> alertExceptions = m_alertBuilder.buildFrontEndAlertExceptions(frontEndItem);

		for (AlertException exception : alertExceptions) {
			try {
				String metricName = exception.getName();
				AlertEntity entity = new AlertEntity();

				entity.setDate(new Date()).setContent(exception.toString()).setLevel(exception.getType());
				entity.setMetric(metricName).setType(getName()).setGroup(metricName);
				m_sendManager.addAlert(entity);
				System.out.println(entity);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("AlertFrontEnd", TimeHelper.getMinuteStr());

			try {
				TopMetric topMetric = buildTopMetric(new Date(current - TimeHelper.ONE_MINUTE * 2));
				Collection<List<Item>> itemLists = topMetric.getError().getResult().values();
				List<Item> itemList = new ArrayList<Item>();

				if (!itemLists.isEmpty()) {
					itemList = itemLists.iterator().next();
				}
				for (Item item : itemList) {
					if (Constants.FRONT_END.equals(item.getDomain())) {
						handleFrontEndException(item);
						break;
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
