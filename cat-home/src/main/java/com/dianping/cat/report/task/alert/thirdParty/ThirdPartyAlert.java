package com.dianping.cat.report.task.alert.thirdParty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;

public class ThirdPartyAlert implements Task, LogEnabled {

	private Logger m_logger;

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
			m_logger.error(e.getMessage());
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
				for (ThirdPartyAlertEntity entity : alertEntities) {
					System.out.println(entity.toString());
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
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "thirdParty-alert";
	}

	@Override
	public void shutdown() {
	}

}
