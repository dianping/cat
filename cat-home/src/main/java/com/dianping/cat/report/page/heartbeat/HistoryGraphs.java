package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.core.dal.Graph;
import com.dianping.cat.consumer.core.dal.GraphDao;
import com.dianping.cat.consumer.core.dal.GraphEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.BaseHistoryGraphs;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.heartbeat.Handler.DetailOrder;
import com.google.gson.Gson;

public class HistoryGraphs extends BaseHistoryGraphs{

	public static final int K = 1024;

	@Inject
	private GraphDao m_graphDao;

	public Map<String, double[]> buildHeartbeatDatas(Date start, Date end, List<Graph> graphs) {
		int size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_HOUR);
		Map<String, String[]> hourlyDate = getHourlyDatas(graphs, start, size);
		return getHeartBeatDatesEveryMinute(hourlyDate, size);
	}

	private void divideByK(Map<String, double[]> result, String[] divideByKDates) {
		for (String name : divideByKDates) {
			double[] data = result.get(name);

			for (int i = 0; i < data.length; i++) {
				data[i] = data[i] / (K * K);
			}
		}
	}

	private void formatHeartBeat(Map<String, double[]> result) {
		double[] totalStartedThread = result.get("TotalStartedThread");
		if (totalStartedThread != null) {
			result.put("StartedThread", getAddedCount(totalStartedThread));
		}
		String[] addedDatas = { "NewGcCount", "OldGcCount", "CatMessageProduced", "CatMessageSize", "CatMessageOverflow" };
		String[] divideByKDates = { "CatMessageSize", "HeapUsage", "NoneHeapUsage", "MemoryFree" };

		organiseAddedData(result, addedDatas);
		divideByK(result, divideByKDates);
	}

	private double[] getAddedCount(double[] source) {
		int size = source.length;
		double[] result = new double[size];

		for (int i = 1; i <= size - 1; i++) {
			if (source[i - 1] > 0) {
				double d = source[i] - source[i - 1];
				if (d < 0) {
					d = source[i];
				}
				result[i] = d;
			}
		}
		return result;
	}

	private ArrayList<LineChart> getDiskInfo(Map<String, double[]> graphData, Date start, int size) {
		ArrayList<LineChart> diskInfo = new ArrayList<LineChart>();

		Iterator<Entry<String, double[]>> iterator = graphData.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, double[]> entry = iterator.next();
			String name = (String) entry.getKey();
			if (name.startsWith("Disk")) {
				double[] data = graphData.get(name);
				for (int i = 0; i < data.length; i++) {
					data[i] = data[i] / (K * K * K);
				}
				String title = name + "[GB]";
				LineChart disk = getGraphItem(title, name, start, size, graphData);
				diskInfo.add(disk);
			}
		}
		return diskInfo;
	}

	private LineChart getGraphItem(String title, String key, Date start, int size, Map<String, double[]> graphData) {
		LineChart item = new LineChart();
		item.setStart(start);
		item.setSize(size);
		item.setTitle(title);
		item.addSubTitle(title);
		item.setStep(TimeUtil.ONE_MINUTE);
		double[] activeThread = graphData.get(key);
		item.addValue(activeThread);
		return item;
	}

	public Map<String, double[]> getHeartBeatData(Model model, Payload payload) {
		Date start = new Date(payload.getDate());
		Date end = payload.getHistoryEndDate();
		String ip = payload.getIpAddress();
		String domain = payload.getDomain();
		List<Graph> graphs = new ArrayList<Graph>();

		try {
			graphs = this.m_graphDao.findByDomainNameIpDuration(start, end, ip, domain, "heartbeat",
			      GraphEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}
		Map<String, double[]> result = buildHeartbeatDatas(start, end, graphs);
		return result;
	}

	public Map<String, double[]> getHeartBeatDatesEveryMinute(Map<String, String[]> heartBeats, final int size) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		final int minutesPerHour = 60;
		int sizeOfHeartBeat = size * minutesPerHour;
		Iterator<Entry<String, String[]>> iterator = heartBeats.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, String[]> entry = iterator.next();
			String name = (String) entry.getKey();
			double[] allDatePerMinutes = new double[sizeOfHeartBeat];
			String[] allPeriods = entry.getValue();
			for (int i = 0; i < allPeriods.length; i++) {
				double[] datePerHour = new double[minutesPerHour];
				String oneHour = allPeriods[i];
				String[] dateInMinutes = oneHour.split(",");
				for (int j = 0; j < dateInMinutes.length; j++) {
					datePerHour[j] = Double.parseDouble(dateInMinutes[j]);
				}
				for (int m = 0; m < minutesPerHour; m++) {
					int index = i * minutesPerHour + m;
					allDatePerMinutes[index] = datePerHour[m];
				}
			}
			result.put(name, allDatePerMinutes);
		}
		formatHeartBeat(result);
		return result;
	}

	private Map<String, String[]> getHourlyDatas(List<Graph> graphs, Date start, int size) {
		Map<String, String[]> heartBeats = initial(size);
		for (Graph graph : graphs) {
			int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeUtil.ONE_HOUR);
			String detailContent = graph.getDetailContent();
			String[] alldates = detailContent.split("\n");

			for (int i = 0; i < alldates.length; i++) {
				String[] records = alldates[i].split("\t");
				String name = records[DetailOrder.NAME.ordinal()];
				String countPerHour = records[DetailOrder.COUNT_IN_MINUTES.ordinal()];
				String[] singlePeriod = heartBeats.get(name);
				if (singlePeriod == null) {
					singlePeriod = initialData(size);
					heartBeats.put(name, singlePeriod);
				}
				singlePeriod[indexOfperiod] = countPerHour;
			}
		}
		return heartBeats;
	}

	private Map<String, String[]> initial(int size) {
		Map<String, String[]> heartBeats = new HashMap<String, String[]>();
		String[] names = { "ActiveThread", "HttpThread", "CatMessageOverflow", "CatMessageProduced", "CatMessageSize",
		      "CatThreadCount", "DaemonThread", "HeapUsage", "MemoryFree", "NewGcCount", "NoneHeapUsage", "OldGcCount",
		      "PigeonStartedThread", "SystemLoadAverage", "TotalStartedThread", "StartedThread" };
		for (String name : names) {
			String[] singlePeriod = initialData(size);
			heartBeats.put(name, singlePeriod);
		}
		return heartBeats;
	}

	private String[] initialData(int size) {
		String[] singlePeriod = new String[size];
		for (int index = 0; index < size; index++) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 60; i++) {
				sb.append("0,");
			}
			String hourData = sb.substring(0, sb.length() - 1);
			singlePeriod[index] = hourData;
		}
		return singlePeriod;
	}

	private void organiseAddedData(Map<String, double[]> result, String[] addedNames) {
		for (String addedName : addedNames) {
			result.put(addedName, getAddedCount(result.get(addedName)));
		}
	}

	// show the graph of heartbeat
	public void showHeartBeatGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		int size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_HOUR * 60);
		Map<String, double[]> graphData = getHeartBeatData(model, payload);
		String queryType = payload.getType();

		if (queryType.equalsIgnoreCase("thread")) {
			model.setActiveThreadGraph(getGraphItem("Thread (Count) ", "ActiveThread", start, size, graphData)
			      .getJsonString());
			model.setDaemonThreadGraph(getGraphItem("Daemon Thread (Count) ", "DaemonThread", start, size, graphData)
			      .getJsonString());
			model.setTotalThreadGraph(getGraphItem("Total Started Thread (Count) ", "TotalStartedThread", start, size,
			      graphData).getJsonString());
			model.setStartedThreadGraph(getGraphItem("Started Thread (Count) ", "StartedThread", start, size, graphData)
			      .getJsonString());
		}else if(queryType.equalsIgnoreCase("frameworkThread")){
			model.setHttpThreadGraph(getGraphItem("Http Thread (Count) ", "HttpThread", start, size, graphData)
			      .getJsonString());
			model.setCatThreadGraph(getGraphItem("Cat Started Thread (Count) ", "CatThreadCount", start, size, graphData)
			      .getJsonString());
			model.setPigeonThreadGraph(getGraphItem("Pigeon Started Thread (Count) ", "PigeonStartedThread", start, size,
			      graphData).getJsonString());
		}
		else if (queryType.equalsIgnoreCase("system")) {
			model.setNewGcCountGraph(getGraphItem("NewGc Count (Count) ", "NewGcCount", start, size, graphData)
			      .getJsonString());
			model.setOldGcCountGraph(getGraphItem("OldGc Count (Count) ", "OldGcCount", start, size, graphData)
			      .getJsonString());
			model.setSystemLoadAverageGraph(getGraphItem("System Load Average ", "SystemLoadAverage", start, size,
			      graphData).getJsonString());
		} else if (queryType.equalsIgnoreCase("memory")) {
			model.setMemoryFreeGraph(getGraphItem("Memory Free (MB) ", "MemoryFree", start, size, graphData)
			      .getJsonString());
			model.setHeapUsageGraph(getGraphItem("Heap Usage (MB) ", "HeapUsage", start, size, graphData).getJsonString());
			model.setNoneHeapUsageGraph(getGraphItem("None Heap Usage (MB) ", "NoneHeapUsage", start, size, graphData)
			      .getJsonString());
		} else if (queryType.equalsIgnoreCase("disk")) {
			List<LineChart> diskInfo = getDiskInfo(graphData, start, size);
		
			model.setDisks(diskInfo.size());
			model.setDiskHistoryGraph(new Gson().toJson(diskInfo));
		} else if (queryType.equalsIgnoreCase("cat")) {
			model.setCatMessageProducedGraph(getGraphItem("Cat Message Produced (Count) / Minute", "CatMessageProduced",
			      start, size, graphData).getJsonString());
			model.setCatMessageOverflowGraph(getGraphItem("Cat Message Overflow (Count) / Minute", "CatMessageOverflow",
			      start, size, graphData).getJsonString());
			model.setCatMessageSizeGraph(getGraphItem("Cat Message Size (MB) / Minute", "CatMessageSize", start, size,
			      graphData).getJsonString());
		}
	}
}
