package com.dianping.cat.system.alarm.threshold;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.logger.LoggerFactory;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.template.entity.Duration;
import com.dianping.cat.home.template.entity.Param;
import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;

public class ThresholdRule {

	private String m_connectionUrl;

	private List<ThresholdDataEntity> m_datas = new ArrayList<ThresholdDataEntity>();

	private String m_domain;

	private Map<String, Long> m_lastAlarmTime = new HashMap<String, Long>();

	private ThresholdDataEntity m_lastData;

	private int m_ruleId;

	private ThresholdTemplate m_template;

	public ThresholdRule(int ruleId, String domain, ThresholdTemplate template) {
		m_ruleId = ruleId;
		m_domain = domain;
		resetTemplate(template);
	}

	public ThresholdAlarmMeta addData(ThresholdDataEntity entity, String type) {
		if (validateData(entity)) {
			m_datas.add(entity);
			m_lastData = entity;

			List<Duration> durations = getDurations();
			int length = durations.size();
			Date date = entity.getDate();

			for (int i = length - 1; i >= 0; i--) {
				Duration duration = durations.get(i);
				String strategy = duration.getAlarm();

				if (strategy != null && strategy.length() > 0) {
					int interval = duration.getInterval();
					long count = getCount(interval, date);

					if (count >= duration.getMin() && count <= duration.getMax()) {
						ThresholdAlarmMeta meta = new ThresholdAlarmMeta();

						meta.setDuration(duration).setRealCount(count).setType(type);
						meta.setRuleId(m_ruleId).setDomain(m_domain).setDate(date);
						meta.setBaseUrl(m_template.getConnection().getBaseUrl());

						if (needAlarm(entity, duration)) {
							m_lastAlarmTime.put(duration.getId(), date.getTime());
							return meta;
						}
						return null;
					}
				}
			}
			cleanData(getMaxInterval(), date.getTime());
		} else {

		}
		return null;
	}

	public void cleanData(int maxInterval, long time) {
		long start = time - (maxInterval + 1) * TimeUtil.ONE_MINUTE;

		List<ThresholdDataEntity> removes = new ArrayList<ThresholdDataEntity>();
		for (ThresholdDataEntity entity : m_datas) {
			if (entity.getDate().getTime() < start) {
				removes.add(entity);
			}
		}

		for (ThresholdDataEntity entity : removes) {
			m_datas.remove(entity);
		}
	}

	public String getConnectUrl() {
		return m_connectionUrl;
	}

	public long getCount(int interval, Date date) {
		long start = date.getTime() - interval * 60 * 1000;
		long totalCount = 0;

		ThresholdDataEntity last = null;
		ThresholdDataEntity first = null;

		int length = m_datas.size();
		for (int i = 0; i < length; i++) {
			ThresholdDataEntity entity = m_datas.get(i);

			if (entity.getDate().getTime() > start) {
				if (last == null) {
					last = entity;
					first = entity;
				} else {
					if (entity.getCount() >= last.getCount() && i == length - 1) {
						totalCount += entity.getCount() - first.getCount();
					} else if (entity.getCount() < last.getCount()) {
						totalCount += last.getCount() - first.getCount();
						totalCount += entity.getCount();
						first = entity;
						last = entity;
					} else {
						last = entity;
					}
				}
			}
		}

		return totalCount;
	}

	public List<ThresholdDataEntity> getDatas() {
		return m_datas;
	}

	public String getDomain() {
		return m_domain;
	}

	public List<Duration> getDurations() {
		return new ArrayList<Duration>(m_template.getDurations().values());
	}

	public Map<String, Long> getLastAlarmTime() {
		return m_lastAlarmTime;
	}

	public int getMaxInterval() {
		List<Duration> durations = getDurations();
		int max = 0;

		for (Duration duration : durations) {
			Integer interval = duration.getInterval();

			if (interval > max) {
				max = interval;
			}
		}
		return max;
	}

	public int getRuleId() {
		return m_ruleId;
	}

	private boolean needAlarm(ThresholdDataEntity entity, Duration duration) {
		Long currentTime = entity.getDate().getTime();
		Long lastAlarmTime = m_lastAlarmTime.get(duration.getId());

		if (lastAlarmTime == null || (currentTime - lastAlarmTime) > duration.getAlarmInterval() * TimeUtil.ONE_MINUTE) {
			return true;
		}
		return false;
	}

	public void resetTemplate(ThresholdTemplate template) {
		m_template = template;

		StringBuilder sb = new StringBuilder();
		sb.append(m_template.getConnection().getBaseUrl());
		sb.append("?");

		Collection<Param> pars = m_template.getConnection().getParams().values();

		for (Param par : pars) {
			sb.append(par.getType()).append("=").append(par.getValue()).append("&");
		}
		sb.append("domain=").append(m_domain);
		m_connectionUrl = sb.toString();
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setTemplate(ThresholdTemplate template) {
		m_template = template;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(500);

		sb.append("[Domain:").append(m_domain).append(";");
		sb.append("[Template:").append(m_template.toString()).append(";").append("]");

		return sb.toString();
	}

	private boolean validateData(ThresholdDataEntity entity) {
		Date entityDate = entity.getDate();
		long now = System.currentTimeMillis() + TimeUtil.ONE_MINUTE;
		if (entityDate.getTime() > now) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			LoggerFactory.getLogger(ThresholdRule.class).error(
			      "date is invalidate!" + sdf.format(entity.getDate()) + " Now:" + sdf.format(new Date()));
			return false;
		}
		if (m_lastData == null) {
			return true;
		} else {
			long newCount = entity.getCount();
			long lastCount = m_lastData.getCount();

			if (newCount > lastCount) {
				return true;
			} else {
				long current = entityDate.getTime() / 1000 / 60;
				int minute = (int) (current % (60));

				if (minute == 0) {
					return true;
				}
			}
		}
		return false;
	}

}
