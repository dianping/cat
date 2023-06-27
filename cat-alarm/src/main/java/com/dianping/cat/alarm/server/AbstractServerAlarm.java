package com.dianping.cat.alarm.server;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.alarm.server.ServerAlarmTask.AlarmParameter;
import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.transform.DefaultSaxParser;
import com.dianping.cat.alarm.service.ServerAlarmRuleService;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.server.MetricType;
import com.dianping.cat.server.QueryParameter;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public abstract class AbstractServerAlarm extends ContainerHolder implements ServerAlarm {

	@Inject
	private ServerAlarmRuleService m_ruleService;

//	@Inject(InfluxDB.ID)
	private MetricService m_metricService;

	private Map<Integer, Long> m_times = new ConcurrentHashMap<Integer, Long>();

	private final static long DURATION = TimeHelper.ONE_SECOND;

	private final static int MAX_THREADS = 100;

	private final static int QUEUE_SIZE = 50;

	private static ThreadPoolExecutor s_threadPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 10,
	      TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(QUEUE_SIZE), new RejectedExecutionHandler() {

		      @Override
		      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			      Cat.logEvent("AlarmDiscards", this.getClass().getSimpleName());
		      }
	      });

	private List<ServerAlarmTask> buildAlarmTasks() {
		long current = System.currentTimeMillis();
		List<ServerAlarmTask> tasks = new ArrayList<ServerAlarmTask>();
		List<ServerAlarmRule> rules = m_ruleService.queryRules(getCategory());

		for (ServerAlarmRule rule : rules) {
			int ruleId = rule.getId();
			Long meta = m_times.get(ruleId);

			if (meta != null) {
				if (meta <= current) {
					try {
						ServerAlarmRuleConfig ruleConfig = DefaultSaxParser.parse(rule.getContent());
						Pair<Long, List<Rule>> pair = buildDuration(ruleConfig);
						ServerAlarmTask task = lookup(ServerAlarmTask.class);

						task.setCategory(getCategory());
						task.setAlarmId(getID());

						buildQueries(rule, task, pair.getValue());
						tasks.add(task);
						m_times.put(ruleId, current + pair.getKey());
					} catch (Exception e) {
						Cat.logError(rule.getContent(), e);
					}
				}
			} else {
				m_times.put(ruleId, current);
			}
		}

		return tasks;
	}

	private Pair<Long, List<Rule>> buildDuration(ServerAlarmRuleConfig ruleConfig) {
		List<Rule> rules = ruleConfig.getRules();
		List<Rule> rets = new ArrayList<Rule>();
		long sleeptime = Long.MAX_VALUE;

		for (Rule r : rules) {
			if (checkTime(r)) {
				for (Condition c : r.getConditions()) {
					try {
						String intval = c.getInterval();
						long time = queryInterval(intval);

						if (time < sleeptime) {
							sleeptime = time;
						}
					} catch (Exception e) {
						Cat.logError(c.toString(), e);
					}
				}
				rets.add(r);
			}
		}
		return new Pair<Long, List<Rule>>(sleeptime, rets);
	}

	private Date buildEndDate(String intval, int duration) {
		if (intval.endsWith("s")) {
			return TimeHelper.getStepSecond(duration);
		} else {
			return TimeHelper.getCurrentMinute();
		}
	}

	private String buildGroupByField(String originalTags) {
		StringBuilder sb = new StringBuilder();

		sb.append("endPoint, ");

		if (StringUtils.isNotEmpty(originalTags)) {
			String[] fields = originalTags.split(";");
			List<String> groups = new LinkedList<String>();

			for (int i = 0; i < fields.length; i++) {
				try {
					String f = fields[i].trim();
					String symbol = "=";

					if (f.contains("!~")) {
						symbol = "!~";
					}

					String field = f.split(symbol)[0].trim();

					groups.add(field);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			for (String g : groups) {
				sb.append(g).append(", ");
			}
		}
		return sb.toString();
	}

	private void buildQueries(ServerAlarmRule rule, ServerAlarmTask task, List<Rule> rules) {
		for (Rule r : rules) {
			Map<Long, List<Condition>> sameIntervalRules = buildSameIntervalRules(r);

			for (Entry<Long, List<Condition>> entry : sameIntervalRules.entrySet()) {
				List<Condition> cs = entry.getValue();
				long interval = entry.getKey();
				String intval = cs.iterator().next().getInterval();

				int duration = queryMaxDuration(cs);
				Date end = buildEndDate(intval, duration);
				Date start = new Date(end.getTime() - interval * duration);

				MetricType metricType = MetricType.getByName(rule.getType(), MetricType.AVG);
				QueryParameter parameter = new QueryParameter();
				String originalTags = rule.getTags();
				String tags = "endPoint " + rule.getEndPoint() + ";" + originalTags;
				String groupBy = buildGroupByField(originalTags);

				parameter.setCategory(rule.getCategory()).setType(metricType).setTags(tags).setInterval(intval)
				      .setFillValue("none").setStart(start).setEnd(end).setMeasurement(rule.getMeasurement())
				      .setGroupBy(groupBy);

				AlarmParameter alarmParameter = new AlarmParameter(parameter, cs);

				task.addParameter(alarmParameter);
			}
		}
	}

	private Map<Long, List<Condition>> buildSameIntervalRules(Rule rule) {
		Map<Long, List<Condition>> results = new HashMap<Long, List<Condition>>();

		for (Condition condition : rule.getConditions()) {
			long interval = queryInterval(condition.getInterval());
			List<Condition> rets = results.get(interval);

			if (rets == null) {
				rets = new ArrayList<Condition>();

				results.put(interval, rets);
			}
			rets.add(condition);
		}
		return results;
	}

	private boolean checkTime(Rule r) {
		try {
			Pair<Integer, Integer> startTime = parseHourMinute(r.getStartTime());
			Pair<Integer, Integer> endTime = parseHourMinute(r.getEndTime());
			long current = System.currentTimeMillis();
			long day = TimeHelper.getCurrentDay().getTime();
			long start = day + startTime.getKey() * TimeHelper.ONE_HOUR + endTime.getValue() * TimeHelper.ONE_MINUTE;
			long end = day + endTime.getKey() * TimeHelper.ONE_HOUR + endTime.getValue() * TimeHelper.ONE_MINUTE;

			return current >= start && current <= end;
		} catch (Exception e) {
			Cat.logError(r.toString(), e);
			return false;
		}
	}

	@Override
	public String getName() {
		return getID() + "-Alarm";
	}

	private Pair<Integer, Integer> parseHourMinute(String startTime) {
		String[] times = startTime.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);

		return new Pair<Integer, Integer>(hour, minute);
	}

	private long queryInterval(String interval) {
		Interval intval = Interval.findByInterval(interval);

		if (intval != null) {
			int n = Integer.valueOf(interval.substring(0, interval.length() - 1));

			return n * intval.getTime();
		} else {
			throw new RuntimeException("Unrecognized interval: " + interval);
		}
	}

	private int queryMaxDuration(List<Condition> conditions) {
		int max = 0;

		for (Condition condition : conditions) {
			int duration = condition.getDuration();

			if (max < duration) {
				max = duration;
			}
		}

		return max;
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();

			try {
				List<ServerAlarmTask> tasks = buildAlarmTasks();

				for (ServerAlarmTask task : tasks) {
					Transaction t = Cat.newTransaction("AlertServer", task.getCategory());

					try {
						s_threadPool.submit(task);
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					} finally {
						t.complete();
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
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
