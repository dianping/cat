package com.dianping.cat.alarm.app.crash;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.crash.entity.ExceptionLimit;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertLevel;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.app.crash.CrashLog;
import com.dianping.cat.app.crash.CrashLogDao;
import com.dianping.cat.app.crash.CrashLogEntity;
import com.dianping.cat.config.app.CrashLogConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;

@Named
public class CrashAlert implements Task {

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	@Inject
	private CrashLogDao m_crashLogDao;

	@Inject
	private CrashRuleConfigManager m_crashRuleConfigManager;

	@Inject
	private CrashLogConfigManager m_crashLogConfigManager;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Inject
	protected AlertManager m_sendManager;

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("AlertCrash", TimeHelper.getMinuteStr());

			try {
				Date startTime = new Date(current - TimeHelper.ONE_MINUTE * 2);
				Date endTime = new Date(current - TimeHelper.ONE_MINUTE);
				List<ExceptionLimit> limits = m_crashRuleConfigManager.queryAllExceptionLimits();

				for (ExceptionLimit limit : limits) {
					int appId = limit.getAppId();
					String platformStr = limit.getPlatform();
					String module = limit.getModule();
					int platform = m_mobileConfigManager.getPlatformId(platformStr);

					CrashLog result = m_crashLogDao.findCountByConditions(startTime, endTime, String.valueOf(appId),
					      platform, module, CrashLogEntity.READSET_COUNT_DATA);
					int count = result.getCount();

					if (count >= limit.getWarnings()) {
						AlertEntity entity = new AlertEntity();
						String appName = m_mobileConfigManager.getAppName(appId);

						entity.setDate(startTime).setContent(buildContent(appName, module, count));
						entity.setMetric(limit.getId()).setType(getName()).setGroup(module).setDomain(appName);
						entity.setContactGroup(limit.getId());

						if (count >= limit.getErrors()) {
							entity.setLevel(AlertLevel.ERROR);
						} else {
							entity.setLevel(AlertLevel.WARNING);
						}

						Map<String, Object> paras = new HashMap<String, Object>();

						paras.put("end", endTime);
						paras.put("warning", limit.getWarnings());
						paras.put("error", limit.getErrors());
						paras.put("count", count);
						paras.put("appId", appId);
						paras.put("platform", platform);
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

	private String buildContent(String appName, String module, long count) {
		StringBuilder sb = new StringBuilder();

		sb.append("[AppName: ").append(appName).append(" 模块: ").append(module).append(" 数量: ").append(count).append("]");
		return sb.toString();
	}

	@Override
	public String getName() {
		return AlertType.CRASH.getName();
	}

	@Override
	public void shutdown() {
	}

}
