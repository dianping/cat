package com.dianping.cat.report.task.alert.thirdParty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertEntity.AlertEntityBuilder;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;

public class ThirdPartyAlert implements Task {

	@Inject
	private SenderManager m_dispatcherManager;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private BlockingQueue<ThirdPartyAlertEntity> m_entities = new ArrayBlockingQueue<ThirdPartyAlertEntity>(5000);

	public boolean put(ThirdPartyAlertEntity entity) {
		boolean result = true;

		try {
			boolean temp = m_entities.offer(entity, 5, TimeUnit.MILLISECONDS);

			if (!temp) {
				result = temp;
			}
			return result;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
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
			Transaction t = Cat.newTransaction("ThirdPartyAlert", "M" + minuteStr);

			try {
				List<ThirdPartyAlertEntity> alertEntities = new ArrayList<ThirdPartyAlertEntity>();

				while (m_entities.size() > 0) {
					ThirdPartyAlertEntity entity = m_entities.poll(5, TimeUnit.MILLISECONDS);

					alertEntities.add(entity);
				}
				Map<String, List<ThirdPartyAlertEntity>> domain2AlertMap = buildDomain2AlertMap(alertEntities);

				for (Entry<String, List<ThirdPartyAlertEntity>> entry : domain2AlertMap.entrySet()) {
					String domain = entry.getKey();
					List<ThirdPartyAlertEntity> thirdPartyAlerts = entry.getValue();

					AlertEntityBuilder builder = new AlertEntity().new AlertEntityBuilder();
					builder.buildDate(new Date()).buildLevel("warning").buildContent(thirdPartyAlerts.toString());
					builder.buildMetric(getName()).buildProductline(domain).buildType(getName()).buildGroup(domain);
					AlertEntity alertEntity = builder.getAlertEntity();

					m_dispatcherManager.send(alertEntity);
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
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

	private Map<String, List<ThirdPartyAlertEntity>> buildDomain2AlertMap(List<ThirdPartyAlertEntity> alertEntities) {
		Map<String, List<ThirdPartyAlertEntity>> domain2AlertMap = new HashMap<String, List<ThirdPartyAlertEntity>>();

		for (ThirdPartyAlertEntity entity : alertEntities) {
			String domain = entity.getDomain();
			List<ThirdPartyAlertEntity> alertList = domain2AlertMap.get(domain);

			if (alertList == null) {
				alertList = new ArrayList<ThirdPartyAlertEntity>();

				domain2AlertMap.put(domain, alertList);
			}
			alertList.add(entity);
		}
		return domain2AlertMap;
	}

	@Override
	public String getName() {
		return "thirdParty";
	}

	@Override
	public void shutdown() {
	}

}
