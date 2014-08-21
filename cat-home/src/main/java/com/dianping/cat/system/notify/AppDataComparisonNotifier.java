package com.dianping.cat.system.notify;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.configuration.appComparison.entity.AppComparison;
import com.dianping.cat.configuration.appComparison.entity.AppComparisonConfig;
import com.dianping.cat.configuration.appComparison.entity.Item;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.dianping.cat.system.notify.render.AppDataComparisonRender;
import com.site.helper.Splitters;
import com.site.lookup.util.StringUtils;

public class AppDataComparisonNotifier {

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppComparisonConfigManager m_appComparisonConfigManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private SenderManager m_sendManager;

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private AppDataComparisonRender m_render;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	public void doNotifying() {
		Date yesterdayDate = TimeUtil.getYesterday();
		String yesterday = m_sdf.format(yesterdayDate);
		Transaction t = Cat.newTransaction("AppDataComparitonInformer", yesterday);

		try {
			Map<String, AppDataComparisonResult> results = buildAppDataComparisonResults(yesterdayDate,
			      m_appComparisonConfigManager.getConfig());
			Map<List<String>, List<AppDataComparisonResult>> results2Receivers = buildReceivers2Results(results);

			for (Entry<List<String>, List<AppDataComparisonResult>> entry : results2Receivers.entrySet()) {
				notify(yesterdayDate, entry.getValue(), entry.getKey());
			}

			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	private Set<String> buildAllUsers(Map<String, AppDataComparisonResult> results) {
		Set<String> allUsers = new HashSet<String>();

		for (Entry<String, AppDataComparisonResult> entry : results.entrySet()) {
			AppDataComparisonResult result = entry.getValue();
			String id = result.getId();

			allUsers.addAll(m_appComparisonConfigManager.queryEmails(id));
		}
		return allUsers;
	}

	private Map<String, String> buildUser2Ids(Set<String> allUsers, Map<String, AppDataComparisonResult> results) {
		Map<String, String> user2Id = new HashMap<String, String>();

		for (String user : allUsers) {

			for (Entry<String, AppDataComparisonResult> entry : results.entrySet()) {
				AppDataComparisonResult result = entry.getValue();
				String id = result.getId();
				String emails = m_appComparisonConfigManager.queryEmailStr(id);

				if (emails.contains(user)) {
					String ids = user2Id.get(user);

					if (StringUtils.isEmpty(ids)) {
						user2Id.put(user, id);
					} else {
						user2Id.put(user, ids + ";" + id);
					}
				}
			}
		}
		return user2Id;
	}

	private Map<String, String> buildid2Users(Map<String, String> user2Id) {
		Map<String, String> id2Users = new HashMap<String, String>();

		for (Entry<String, String> entry : user2Id.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String users = user2Id.get(value);

			if (StringUtils.isEmpty(users)) {
				id2Users.put(value, key);
			} else {
				id2Users.put(value, users + ";" + value);
			}
		}
		return id2Users;
	}

	private Map<List<String>, List<AppDataComparisonResult>> buildReceivers2Results(
	      Map<String, AppDataComparisonResult> results) {
		Set<String> allUsers = buildAllUsers(results);
		Map<String, String> user2Ids = buildUser2Ids(allUsers, results);
		Map<String, String> id2Users = buildid2Users(user2Ids);
		Map<List<String>, List<AppDataComparisonResult>> users2Results = buildUsers2Results(id2Users, results);

		return users2Results;
	}

	private Map<List<String>, List<AppDataComparisonResult>> buildUsers2Results(Map<String, String> id2Users,
	      Map<String, AppDataComparisonResult> results) {
		Map<List<String>, List<AppDataComparisonResult>> users2Results = new HashMap<List<String>, List<AppDataComparisonResult>>();

		for (Entry<String, String> entry : id2Users.entrySet()) {
			List<String> emails = Splitters.by(";").noEmptyItem().split(entry.getValue());
			List<String> ids = Splitters.by(";").noEmptyItem().split(entry.getKey());
			List<AppDataComparisonResult> userResults = new ArrayList<AppDataComparisonResult>();

			for (String id : ids) {
				userResults.add(results.get(id));
			}
			users2Results.put(emails, userResults);
		}
		return users2Results;
	}

	private void notify(Date yesterday, List<AppDataComparisonResult> results, List<String> emails) {
		String title = renderTitle();
		String content = m_render.renderReport(yesterday, results);
		AlertMessageEntity message = new AlertMessageEntity(null, title, "AppDataComparison", content, emails);

		try {
			boolean result = m_sendManager.sendAlert(AlertChannel.MAIL, message);

			insertMailLog(-1, content, title, result, emails);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private void insertMailLog(int reportId, String content, String title, boolean result, List<String> emails)
	      throws DalException {
		MailRecord entity = m_mailRecordDao.createLocal();

		entity.setTitle(title);
		entity.setContent(content);
		entity.setRuleId(reportId);
		entity.setType(1);
		entity.setReceivers(emails.toString());
		if (result) {
			entity.setStatus(0);
		} else {
			entity.setStatus(1);
		}
		m_mailRecordDao.insert(entity);
	}

	private String renderTitle() {
		return "CAT端到端报告";
	}

	private Map<String, AppDataComparisonResult> buildAppDataComparisonResults(Date date, AppComparisonConfig config) {
		Map<String, AppDataComparisonResult> results = new LinkedHashMap<String, AppDataComparisonResult>();

		for (Entry<String, AppComparison> entry : config.getAppComparisons().entrySet()) {
			// try {
			AppDataComparisonResult result = queryDelay4AppComparison(date, entry.getValue());
			// AppDataComparisonResult result = queryDelay4AppComparison(m_sdf.parse("2014-07-18"), entry.getValue());
			results.put(entry.getKey(), result);
			// } catch (ParseException e) {
			// e.printStackTrace();
			// }
		}
		return results;
	}

	private AppDataComparisonResult queryDelay4AppComparison(Date yesterday, AppComparison appComparison) {
		String id = appComparison.getId();
		AppDataComparisonResult result = new AppDataComparisonResult();
		result.setId(id);

		for (Item item : appComparison.getItems()) {
			String itemId = item.getId();
			String command = item.getCommand();
			double delay = queryOneDayDelay4Command(yesterday, command);

			if (delay > -1) {
				result.addItem(itemId, command, delay);
			}
		}
		return result;
	}

	private double queryOneDayDelay4Command(Date yesterday, String url) {
		String yesterdayStr = m_sdf.format(yesterday);
		Integer command = m_appConfigManager.getCommands().get(url);

		if (command != null) {
			QueryEntity entity = new QueryEntity(yesterdayStr + ";" + command + ";;;;;;;");

			return m_appDataService.queryOneDayDelayAvg(entity);
		} else {
			Cat.logError(new RuntimeException("AppComparison config has unrecognized command: " + url));
			return -1;
		}
	}

}
