package com.dianping.cat.report.page.app.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.report.LogMsg;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class LogService {

	protected String APP_VERSIONS = "appVersions";

	protected String LEVELS = "levels";

	protected String PLATFORM_VERSIONS = "platformVersions";

	protected String DEVICES = "devices";

	private final int BUFFER = 1024;

	protected final int LIMIT = 10000;

	@Inject
	protected MobileConfigManager m_mobileConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	protected void addCount(String item, Map<String, AtomicInteger> distributions) {
		AtomicInteger count = distributions.get(item);

		if (count == null) {
			count = new AtomicInteger(1);
			distributions.put(item, count);
		} else {
			count.incrementAndGet();
		}
	}

	protected String buildContent(byte[] content) {
		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPInputStream gis = null;

		try {
			gis = new GZIPInputStream(bais);
		} catch (IOException ex) {
			try {
				baos.close();
				bais.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
			return m_configHtmlParser.parse(new String(content)).replace("\n", "<br/>");
		}

		try {
			int count;
			byte data[] = new byte[BUFFER];

			while ((count = gis.read(data, 0, BUFFER)) != -1) {
				baos.write(data, 0, count);
			}

			byte[] result = baos.toByteArray();

			baos.flush();
			return m_configHtmlParser.parse(new String(result)).replace("\n", "<br/>");
		} catch (IOException e) {
			Cat.logError(e);
			return m_configHtmlParser.parse(new String(content)).replace("\n", "<br/>");
		} finally {
			try {
				gis.close();
				baos.close();
				bais.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
		}
	}

	protected Map<String, PieChart> buildDistributionChart(Map<String, Map<String, AtomicInteger>> distributions) {
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

	protected void buildLogMsg(Map<String, LogMsg> logMsgs, String msg, int id) {
		if (msg != null) {
			msg = m_configHtmlParser.parse(msg);
		}

		LogMsg logMsg = logMsgs.get(msg);

		if (logMsg == null) {
			logMsg = new LogMsg();
			logMsg.setMsg(msg);
			logMsgs.put(msg, logMsg);
		}

		logMsg.addCount();
		logMsg.addId(id);
	}

	protected List<LogMsg> buildLogMsgList(Map<String, LogMsg> errorMsgs) {
		List<LogMsg> errorMsgList = new ArrayList<LogMsg>();
		Iterator<Entry<String, LogMsg>> iter = errorMsgs.entrySet().iterator();

		while (iter.hasNext()) {
			errorMsgList.add(iter.next().getValue());
		}

		Collections.sort(errorMsgList);
		return errorMsgList;
	}

	protected Set<String> findOrCreate(String key, Map<String, Set<String>> map) {
		Set<String> value = map.get(key);

		if (value == null) {
			value = new HashSet<String>();
			map.put(key, value);
		}
		return value;
	}
}
