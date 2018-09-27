package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.Level;
import com.dianping.cat.report.LogMsg;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.browser.ModuleManager;
import com.dianping.cat.report.page.browser.display.JsErrorDetailInfo;
import com.dianping.cat.report.page.browser.display.JsErrorDisplayInfo;
import com.dianping.cat.web.JsErrorLog;
import com.dianping.cat.web.JsErrorLogContent;
import com.dianping.cat.web.JsErrorLogContentDao;
import com.dianping.cat.web.JsErrorLogContentEntity;
import com.dianping.cat.web.JsErrorLogDao;
import com.dianping.cat.web.JsErrorLogEntity;

public class JsErrorLogService {

	@Inject
	private JsErrorLogContentDao m_jsErrorLogContentlDao;

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	@Inject
	private ModuleManager m_moduleManager;

	private final int LIMIT = 10000;

	private final String MODULES = "modules";

	private final String BROWSERS = "browsers";

	private void addCount(String item, Map<String, AtomicInteger> distributions) {
		AtomicInteger count = distributions.get(item);

		if (count == null) {
			count = new AtomicInteger(1);
			distributions.put(item, count);
		} else {
			count.incrementAndGet();
		}
	}

	public JsErrorDisplayInfo buildJsErrorDisplayInfo(JsErrorQueryEntity jsErrorQuery) {
		JsErrorDisplayInfo info = new JsErrorDisplayInfo();
		Map<String, Map<String, AtomicInteger>> distributions = new HashMap<String, Map<String, AtomicInteger>>();

		try {
			Map<String, LogMsg> errorMsgs = new HashMap<String, LogMsg>();
			int offset = 0;
			int totalCount = 0;

			while (true) {
				List<JsErrorLog> result = queryJsErrorInfo(jsErrorQuery, offset, LIMIT);

				for (JsErrorLog log : result) {
					processLog(errorMsgs, log);
					buildDistributeions(log, distributions);
				}

				int count = result.size();
				totalCount += count;
				offset += count;

				if (count < LIMIT) {
					break;
				}
			}

			List<LogMsg> errorMsgList = sort(errorMsgs);

			info.setErrors(errorMsgList);
			info.setTotalCount(totalCount);
			info.setLevels(Level.getLevels());
			info.setModules(m_moduleManager.getModules());
			info.setDistributions(buildDistributionChart(distributions));
		} catch (DalException e) {
			Cat.logError(e);
		}

		return info;
	}

	private void buildDistributeions(JsErrorLog log, Map<String, Map<String, AtomicInteger>> distributions) {
		if (distributions.isEmpty()) {
			Map<String, AtomicInteger> modules = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> browsers = new HashMap<String, AtomicInteger>();

			distributions.put(MODULES, modules);
			distributions.put(BROWSERS, browsers);
		}

		addCount(log.getBrowser(), distributions.get(BROWSERS));
		addCount(log.getModule(), distributions.get(MODULES));
	}

	private Map<String, PieChart> buildDistributionChart(Map<String, Map<String, AtomicInteger>> distributions) {
		Map<String, PieChart> charts = new HashMap<String, PieChart>();

		for (Entry<String, Map<String, AtomicInteger>> entrys : distributions.entrySet()) {
			Map<String, AtomicInteger> distribution = entrys.getValue();
			PieChart chart = new PieChart();
			List<Item> items = new ArrayList<Item>();

			for (Entry<String, AtomicInteger> entry : distribution.entrySet()) {
				Item item = new Item();

				item.setNumber(entry.getValue().get()).setTitle(entry.getKey());
				items.add(item);
			}
			chart.addItems(items);
			chart.setTitle(entrys.getKey());
			charts.put(entrys.getKey(), chart);
		}

		return charts;
	}

	private void processLog(Map<String, LogMsg> errorMsgs, JsErrorLog log) {
		String msg = log.getMsg();
		int index = msg.indexOf("?");

		if (index > 0) {
			msg = msg.substring(0, index);
		}
		LogMsg errorMsg = errorMsgs.get(msg);

		if (errorMsg == null) {
			errorMsg = new LogMsg();
			errorMsg.setMsg(msg);
			errorMsgs.put(msg, errorMsg);
		}

		errorMsg.addCount();
		errorMsg.addId(log.getId());
	}

	public JsErrorDetailInfo queryJsErrorInfo(int id) {
		JsErrorDetailInfo info = new JsErrorDetailInfo();

		try {
			JsErrorLog jsErrorLog = m_jsErrorLogDao.findByPK(id, JsErrorLogEntity.READSET_FULL);
			JsErrorLogContent detail = m_jsErrorLogContentlDao.findByPK(id, JsErrorLogContentEntity.READSET_FULL);

			info.setErrorTime(jsErrorLog.getErrorTime());
			info.setLevel(Level.getNameByCode(jsErrorLog.getLevel()));
			info.setModule(jsErrorLog.getModule());
			info.setDetail(new String(detail.getContent(), "UTF-8"));
			info.setAgent(jsErrorLog.getBrowser());
			info.setDpid(jsErrorLog.getDpid());
		} catch (Exception e) {
			Cat.logError(e);
		}

		return info;
	}

	public List<JsErrorLog> queryJsErrorInfo(JsErrorQueryEntity query, int offset, int limit) throws DalException {
		Date startTime = query.buildStartTime();
		Date endTime = query.buildEndTime();
		int levelCode = query.buildLevel();
		String module = query.getModule();
		String dpid = query.getDpid();

		List<JsErrorLog> result = m_jsErrorLogDao.findDataByTimeModuleLevelBrowser(startTime, endTime, module, levelCode,
		      null, dpid, offset, limit, JsErrorLogEntity.READSET_FULL);

		return result;
	}

	private List<LogMsg> sort(Map<String, LogMsg> errorMsgs) {
		List<LogMsg> errorMsgList = new ArrayList<LogMsg>();
		Iterator<Entry<String, LogMsg>> iter = errorMsgs.entrySet().iterator();

		while (iter.hasNext()) {
			errorMsgList.add(iter.next().getValue());
		}

		Collections.sort(errorMsgList);
		return errorMsgList;
	}
}
