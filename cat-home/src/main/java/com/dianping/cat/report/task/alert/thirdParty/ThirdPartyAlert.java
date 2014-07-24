package com.dianping.cat.report.task.alert.thirdParty;

import java.text.SimpleDateFormat;
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

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.sender2.MailSender;
import com.site.helper.Splitters;

public class ThirdPartyAlert implements Task, LogEnabled {

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	protected MailSender m_mailSender;

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
					sendAlert(entry.getKey(), entry.getValue());
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

	private Project queryProjectByDomain(String projectName) {
		Project project = null;
		try {
			project = m_projectDao.findByDomain(projectName, ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return project;
	}

	public String buildMailContent(String exceptions, String domain) {
		StringBuilder sb = new StringBuilder();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

		sb.append("[CAT第三方告警] [项目: ").append(domain).append("] : ");
		sb.append(exceptions).append("[时间: ").append(time).append("]");

		return sb.toString();
	}

	private void sendAlert(String domain, List<ThirdPartyAlertEntity> entities) {
		Project project = queryProjectByDomain(domain);
		List<String> emails = buildMailReceivers(project);
		String mailTitle = buildMailTitle(domain);
		String mailContent = buildMailContent(entities.toString(), domain);

		m_mailSender.sendAlert(emails, domain, mailTitle, mailContent);
		m_logger.info(mailTitle + " " + mailContent + " " + emails);
		Cat.logEvent("ExceptionAlert", domain, Event.SUCCESS, "[邮件告警] " + mailTitle + "  " + mailContent);
	}

	private String buildMailTitle(String domain) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT第三方告警] [项目: ").append(domain).append("]");
		return sb.toString();
	}

	private List<String> buildMailReceivers(Project project) {
		List<String> mailReceivers = new ArrayList<String>();

		mailReceivers.addAll(split(project.getEmail()));

		return mailReceivers;
	}

	private List<String> split(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().split(str));
		}

		return result;
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
