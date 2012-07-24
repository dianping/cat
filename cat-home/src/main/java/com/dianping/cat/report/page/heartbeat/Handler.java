package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.GraphEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.trend.GraphItem;
import com.dianping.cat.report.view.StringSortHelper;
import com.google.gson.Gson;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	public static final long ONE_HOUR = 3600 * 1000L;
	
	public static final int K = 1024;

	@Inject
	private GraphDao graphDao;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private GraphBuilder m_builder;

	@Inject(type = ModelService.class, value = "heartbeat")
	private ModelService<HeartbeatReport> m_service;

	private String getIpAddress(HeartbeatReport report, Payload payload) {
		Set<String> ips = report.getIps();
		String ip = payload.getIpAddress();

		if ((ip == null || ip.length() == 0) && !ips.isEmpty()) {
			ip = StringSortHelper.sort(ips).get(0);
		}

		return ip;
	}

	private HeartbeatReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date).setProperty("ip", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);
			HeartbeatReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "h")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "h")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		DisplayHeartbeat heartbeat = null;

		normalize(model, payload);
		switch (payload.getAction()) {
		case VIEW:
			heartbeat = showReport(model, payload);
			setHeartbeatGraphInfo(model, heartbeat);
			break;
		case MOBILE:
			heartbeat = showReport(model, payload);
			MobileHeartbeatModel mobileModel = setMobileModel(model, heartbeat);
			String json = new Gson().toJson(mobileModel);

			model.setMobileResponse(json);
			break;
		case HISTORY:
			if (model.getIpAddress() != null) {
				showHeartBeatGraph(model, payload);
			}
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		model.setAction(payload.getAction());
		model.setPage(ReportPage.HEARTBEAT);
		model.setIpAddress(payload.getIpAddress());

		Action action = payload.getAction();
		if (action == Action.HISTORY) {
			String type = payload.getReportType();

			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			model.setLongDate(payload.getDate());

			HeartbeatReport report = new HeartbeatReport();

			model.setReport(report);
			try {
				Date historyStartDate = payload.getHistoryStartDate();
				Date historyEndDate = payload.getHistoryEndDate();
				List<Graph> domains = graphDao.findDomainByNameDuration(historyStartDate, historyEndDate, "heartbeat",
				      GraphEntity.READSET_DOMAIN);
				String domain = payload.getDomain();
				List<Graph> ips = graphDao.findIpByDomainNameDuration(historyStartDate, historyEndDate, domain,
				      "heartbeat", GraphEntity.READSET_IP);
				Set<String> reportDomains = report.getDomainNames();
				Set<String> reportIps = report.getIps();

				for (Graph graph : domains) {
					reportDomains.add(graph.getDomain());
				}
				for (Graph graph : ips) {
					reportIps.add(graph.getIp());
				}
				report.setDomain(payload.getDomain());
				model.setDisplayDomain(payload.getDomain());

				String ip = payload.getIpAddress();
				if (StringUtils.isEmpty(ip)) {
					List<String> ips2 = model.getIps();
					if (ips2.size() > 0) {
						ip = ips2.get(0);
					}
				}
				model.setIpAddress(ip);
			} catch (DalException e) {
				e.printStackTrace();
			}
		}
	}

	private GraphItem getGraphItem(String title, String key, Date start, int size, Map<String, double[]> graphData) {
		GraphItem item = new GraphItem();
		item.setStart(start);
		item.setSize(size);
		item.setTitles(title);
		double[] activeThread = graphData.get(key);
		item.addValue(activeThread);
		return item;
	}

	// show the graph of heartbeat
	private void showHeartBeatGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		int size = (int) ((end.getTime() - start.getTime()) / ONE_HOUR * 60);
		Map<String, double[]> graphData = getHeartBeatData(model, payload);
		
		model.setActiveThreadGraph(getGraphItem("Thread (Count) ", "ActiveThread", start, size, graphData)
		      .getJsonString());
		model.setDaemonThreadGraph(getGraphItem("Daemon Thread (Count) ", "DaemonThread", start, size, graphData)
		      .getJsonString());
		model.setTotalThreadGraph(getGraphItem("Total Started Thread (Count) ", "TotalStartedThread", start, size,
		      graphData).getJsonString());
		model.setStartedThreadGraph(getGraphItem("Started Thread (Count) ", "StartedThread", start, size, graphData)
		      .getJsonString());
		model.setCatThreadGraph(getGraphItem("Cat Started Thread (Count) ", "CatThreadCount", start, size, graphData)
		      .getJsonString());
		model.setPigeonThreadGraph(getGraphItem("Pigeon Started Thread (Count) ", "PigeonStartedThread", start, size,
		      graphData).getJsonString());
		model.setNewGcCountGraph(getGraphItem("NewGc Count (Count) ", "NewGcCount", start, size, graphData)
		      .getJsonString());
		model.setOldGcCountGraph(getGraphItem("OldGc Count (Count) ", "OldGcCount", start, size, graphData)
		      .getJsonString());
		model.setSystemLoadAverageGraph(getGraphItem("System Load Average ", "SystemLoadAverage", start, size, graphData)
		      .getJsonString());
		model.setMemoryFreeGraph(getGraphItem("Memory Free (MB) ", "MemoryFree", start, size, graphData).getJsonString());
		model.setHeapUsageGraph(getGraphItem("Heap Usage (MB) ", "HeapUsage", start, size, graphData).getJsonString());
		model.setNoneHeapUsageGraph(getGraphItem("None Heap Usage (MB) ", "NoneHeapUsage", start, size, graphData)
		      .getJsonString());
		model.setCatMessageProducedGraph(getGraphItem("Cat Message Produced (Count) / Minute", "CatMessageProduced",
		      start, size, graphData).getJsonString());
		model.setCatMessageOverflowGraph(getGraphItem("Cat Message Overflow (Count) / Minute", "CatMessageOverflow",
		      start, size, graphData).getJsonString());
		model.setCatMessageSizeGraph(getGraphItem("Cat Message Size (MB) / Minute", "CatMessageSize", start, size,
		      graphData).getJsonString());
		List<GraphItem> diskInfo=getDiskInfo(graphData,start,size);
		model.setDisks(diskInfo.size());
		model.setDiskHistoryGraph(new Gson().toJson(diskInfo));
	}
	
	private ArrayList<GraphItem> getDiskInfo(Map<String, double[]> graphData,Date start, int size){
		ArrayList<GraphItem> diskInfo=new ArrayList<GraphItem>();
		
		Iterator<Entry<String, double[]>> iterator = graphData.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, double[]> entry = iterator.next();
			String name = (String) entry.getKey();
			if(name.startsWith("Disk")){
				double[]data=graphData.get(name);
				for (int i = 0; i < data.length; i++) {
	            data[i]=data[i]/(K*K*K);
            }
				String title=name+"[GB]";
				GraphItem disk=getGraphItem(title,name,start,size,graphData);
				diskInfo.add(disk);
			}
		}
		return diskInfo;
	}

	public Map<String, double[]> getHeartBeatData(Model model, Payload payload) {
		Date start = new Date(payload.getDate());
		Date end = payload.getHistoryEndDate();
		String ip = model.getIpAddress();
		String domain = payload.getDomain();
		List<Graph> graphs = new ArrayList<Graph>();

		try {
			graphs = this.graphDao.findByDomainNameIpDuration(start, end, ip, domain, "heartbeat",
			      GraphEntity.READSET_FULL);
		} catch (DalException e) {
			e.printStackTrace();
		}
		Map<String, double[]> result = buildHeartbeatDatas(start, end, graphs);
		return result;
	}

	public Map<String, double[]> buildHeartbeatDatas(Date start, Date end, List<Graph> graphs) {
		int size = (int) ((end.getTime() - start.getTime()) / ONE_HOUR);
		Map<String, String[]> hourlyDate = getHourlyDate(graphs, start, size);
		return getHeartBeatDatesEveryMinute(hourlyDate, size);
	}
	
	private Map<String, String[]> initial(int size){
		Map<String, String[]> heartBeats = new HashMap<String, String[]>();
		String[]names={"ActiveThread","CatMessageOverflow","CatMessageProduced","CatMessageSize","CatThreadCount","DaemonThread"
				,"HeapUsage","MemoryFree","NewGcCount","NoneHeapUsage","OldGcCount","PigeonStartedThread","SystemLoadAverage","TotalStartedThread","StartedThread"};
		for(String name:names){
			String[] singlePeriod = initialData(size);
			heartBeats.put(name, singlePeriod);
		}
		return heartBeats;
	}
	
	private Map<String, String[]> getHourlyDate(List<Graph> graphs, Date start, int size) {
		Map<String, String[]> heartBeats = initial(size);
		for (Graph graph : graphs) {
			int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / ONE_HOUR);
			String detailContent = graph.getDetailContent();
			String[] alldates = detailContent.split("\n");

			for (int i = 0; i < alldates.length; i++) {
				String[] records = alldates[i].split("\t");
				String name = records[DetailOrder.NAME.ordinal()];
				String countPerHour = records[DetailOrder.COUNT_IN_MINUTES.ordinal()];
				String[] singlePeriod = heartBeats.get(name);
				if(singlePeriod==null){
					singlePeriod=initialData(size);
					heartBeats.put(name,singlePeriod);
				}
				singlePeriod[indexOfperiod] = countPerHour;
			}
		}
		return heartBeats;
	}
	private String[]initialData(int size){
		String[] singlePeriod = new String[size];
		for (int index = 0; index < size; index++) {
			StringBuilder sb=new StringBuilder();
			for (int i = 0; i < 60; i++) {
		      sb.append("0,");
	      }
			String hourData=sb.substring(0, sb.length()-1);
			singlePeriod[index] =hourData;
		}
		return singlePeriod;
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

	private void formatHeartBeat(Map<String, double[]> result) {
		double[] totalStartedThread = result.get("TotalStartedThread");
		if (totalStartedThread != null) {
			result.put("StartedThread", getAddedCount(totalStartedThread));
		}
		String[] addedDatas = { "NewGcCount", "OldGcCount", "CatMessageProduced", "CatMessageSize", "CatMessageOverflow" };
		String[] divideByKDates = { "CatMessageSize", "HeapUsage", "NoneHeapUsage", "MemoryFree"};

		organiseAddedData(result, addedDatas);
		divideByK(result, divideByKDates);
	}

	private void divideByK(Map<String, double[]> result, String[] divideByKDates) {

		for (String name : divideByKDates) {
			double[] data = result.get(name);
			for (int i = 0; i < data.length; i++) {
				data[i] = data[i] /(K*K);
			}
		}
		
	}

	private void organiseAddedData(Map<String, double[]> result, String[] addedNames) {
		for (String addedName : addedNames) {
			result.put(addedName, getAddedCount(result.get(addedName)));
		}
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

	private MobileHeartbeatModel setMobileModel(Model model, DisplayHeartbeat heartbeat) {
		MobileHeartbeatModel result = new MobileHeartbeatModel();
		result.display(model, heartbeat);
		return result;
	}

	private DisplayHeartbeat showReport(Model model, Payload payload) {
		try {
			ModelPeriod period = payload.getPeriod();

			if (period.isFuture()) {
				model.setLongDate(payload.getCurrentDate());
			} else {
				model.setLongDate(payload.getDate());
			}
			model.setDisplayDomain(payload.getDomain());

			HeartbeatReport report = getReport(payload);
			if (report == null) {
				return null;
			}
			model.setReport(report);
			String ip = getIpAddress(report, payload);
			model.setIpAddress(ip);

			DisplayHeartbeat displayHeartbeat = new DisplayHeartbeat(m_builder).display(report, ip);
			return displayHeartbeat;
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
			model.setException(e);
		}
		return null;
	}

	private void setHeartbeatGraphInfo(Model model, DisplayHeartbeat displayHeartbeat) {
		if (displayHeartbeat == null) {
			return;
		}
		model.setResult(displayHeartbeat);
		model.setActiveThreadGraph(displayHeartbeat.getActiceThreadGraph());
		model.setDaemonThreadGraph(displayHeartbeat.getDeamonThreadGraph());
		model.setTotalThreadGraph(displayHeartbeat.getTotalThreadGraph());
		model.setStartedThreadGraph(displayHeartbeat.getStartedThreadGraph());
		model.setCatThreadGraph(displayHeartbeat.getCatThreadGraph());
		model.setPigeonThreadGraph(displayHeartbeat.getPigeonTheadGraph());
		model.setHttpThreadGraph(displayHeartbeat.getHttpTheadGraph());
		model.setCatMessageProducedGraph(displayHeartbeat.getCatMessageProducedGraph());
		model.setCatMessageOverflowGraph(displayHeartbeat.getCatMessageOverflowGraph());
		model.setCatMessageSizeGraph(displayHeartbeat.getCatMessageSizeGraph());
		model.setNewGcCountGraph(displayHeartbeat.getNewGcCountGraph());
		model.setOldGcCountGraph(displayHeartbeat.getOldGcCountGraph());
		model.setHeapUsageGraph(displayHeartbeat.getHeapUsageGraph());
		model.setNoneHeapUsageGraph(displayHeartbeat.getNoneHeapUsageGraph());
		model.setDisks(displayHeartbeat.getDisks());
		model.setDisksGraph(displayHeartbeat.getDisksGraph());
		model.setSystemLoadAverageGraph(displayHeartbeat.getSystemLoadAverageGraph());
		model.setMemoryFreeGraph(displayHeartbeat.getMemoryFreeGraph());
	}

	// the detail order of heartbeat is:name min max sum sum2 count_in_minutes
	public enum DetailOrder {
		NAME, MIN, MAX, SUM, SUM2, COUNT_IN_MINUTES
	}

}
