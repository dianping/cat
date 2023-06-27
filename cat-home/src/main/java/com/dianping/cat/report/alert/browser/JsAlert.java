package com.dianping.cat.report.alert.browser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.Level;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.js.entity.ExceptionLimit;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.web.JsErrorLog;
import com.dianping.cat.web.JsErrorLogDao;
import com.dianping.cat.web.JsErrorLogEntity;

@Named
public class JsAlert implements Task {

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	@Inject
	private JsRuleConfigManager m_jsRuleConfigManager;

	@Inject
	protected AlertManager m_sendManager;

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("AlertJs", TimeHelper.getMinuteStr());

			try {
				Date startTime = new Date(current - TimeHelper.ONE_MINUTE * 2);
				Date endTime = new Date(current - TimeHelper.ONE_MINUTE);
				List<JsErrorLog> results = m_jsErrorLogDao.findCountByTimeModuleLevel(startTime, endTime,
				      JsErrorLogEntity.READSET_COUNT_DATA);

				for (JsErrorLog jsErrorLog : results) {
					int levelCode = jsErrorLog.getLevel();
					String module = jsErrorLog.getModule();
					long count = jsErrorLog.getCount();
					String level = Level.getNameByCode(levelCode);
					ExceptionLimit limit = m_jsRuleConfigManager.queryExceptionLimit(module, level);

					if (limit != null && count >= limit.getLimit()) {
						AlertEntity entity = new AlertEntity();
						entity.setDate(startTime).setContent(buildContent(module, level, count))
						      .setLevel(level.toLowerCase());
						entity.setMetric(limit.getId()).setType(getName()).setGroup(module);
						entity.setContactGroup(limit.getId());

						Map<String, Object> paras = new HashMap<String, Object>();

						paras.put("end", endTime);
						paras.put("limit", limit.getLimit());
						paras.put("count", count);
						paras.put("level", level);
						entity.setParas(paras);

						m_sendManager.addAlert(entity);
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

	private String buildContent(String module, String level, long count) {
		StringBuilder sb = new StringBuilder();

		sb.append("[ JS报错模块: ").append(module).append(" level: ").append(level).append(" count: ").append(count)
		      .append("]");
		return sb.toString();
	}

	@Override
	public String getName() {
		return AlertType.JS.getName();
	}

	@Override
	public void shutdown() {
	}

}
