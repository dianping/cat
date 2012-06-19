package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
		      .setProperty("date", date);

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

		normalize(model, payload);

		DisplayHeartbeat heartbeat = null;
		switch (payload.getAction()) {
		case VIEW:
			heartbeat = showReport(model, payload);
			setModel(model, heartbeat);
			break;
		case MOBILE:
			heartbeat = showReport(model, payload);
			MobileHeartbeatModel mobileModel = setMobileModel(model, heartbeat);
			Gson gson = new Gson();
			String json = gson.toJson(mobileModel);
			model.setMobileResponse(json);
			break;
		case HISTORY:
			showHeartBeatGraph(model, payload);
			break;
		}
		if (payload.getPeriod().isCurrent()) {
			model.setCreatTime(new Date());
		} else {
			model.setCreatTime(new Date(payload.getDate() + 60 * 60 * 1000 - 1000));
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
		if (action == Action.HISTORY ) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			model.setLongDate(payload.getDate());
		}
	}

	// show the graph of heartbeat
	private void showHeartBeatGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		int size = (int) ((end.getTime() - start.getTime()) / ONE_HOUR);
		GraphItem item = new GraphItem();
		item.setStart(start);
		item.setSize(size);
		Map<String, double[]> graphData = getHeartBeatData(model, payload);
		
		//Active Thread
		item.setTitles("Active Thread");
		double[] activeThread = graphData.get("ActiveThread");
		item.addValue(activeThread);
		model.setActiveThreadGraph(item.getJsonString());

		item.getValues().clear();
		
		//Daemon Thread
		item.setTitles("Daemon Thread");
		double[] daemonThread = graphData.get("DaemonThread");
		item.addValue(daemonThread);
		model.setDaemonThreadGraph(item.getJsonString());

		item.getValues().clear();
		
		//Total Started Thread
		item.setTitles("Total Started Thread");
		double[] totalStartedThread = graphData.get("TotalStartedThread");
		item.addValue(totalStartedThread);
		model.setTotalThreadGraph(item.getJsonString());

		item.getValues().clear();
		
		item.setTitles("Started Thread");
		double[] startedThread = graphData.get("StartedThread");
		item.addValue(startedThread);
		model.setStartedThreadGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Cat Started Thread");
		double[] catThreadCount = graphData.get("CatThreadCount");
		item.addValue(catThreadCount);
		model.setCatThreadGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Pigeon Started Thread");
		double[] pigeonStartedThread = graphData.get("PigeonStartedThread");
		item.addValue(pigeonStartedThread);
		model.setPigeonThreadGraph(item.getJsonString());
		
		
		item.getValues().clear();
		
		item.setTitles("NewGc Count");
		double[] newGcCount = graphData.get("NewGcCount");
		item.addValue(newGcCount);
		model.setNewGcCountGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("OldGc Count");
		double[] oldGcCount = graphData.get("OldGcCount");
		item.addValue(oldGcCount);
		model.setOldGcCountGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("System Load Average");
		double[] systemLoadAverage = graphData.get("SystemLoadAverage");
		item.addValue(systemLoadAverage);
		model.setSystemLoadAverageGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Memory Free");
		double[] memoryFree = graphData.get("MemoryFree");
		item.addValue(memoryFree);
		model.setMemoryFreeGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Heap Usage");
		double[] heapUsage = graphData.get("HeapUsage");
		item.addValue(heapUsage);
		model.setHeapUsageGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("None Heap Usage");
		double[] noneHeapUsage = graphData.get("NoneHeapUsage");
		item.addValue(noneHeapUsage);
		model.setNoneHeapUsageGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Disk /");
		double[] diskRoot = graphData.get("Disk /");
		item.addValue(diskRoot);
		model.setDiskRootGraph(item.getJsonString());
		
		
		item.getValues().clear();
		
		item.setTitles("Disk /data");
		double[] diskData = graphData.get("Disk /data");
		item.addValue(diskData);
		model.setDiskDataGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Cat Message Produced / Minute");
		double[] catMessageProduced = graphData.get("CatMessageProduced");
		item.addValue(catMessageProduced);
		model.setCatMessageProducedGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Cat Message Overflow / Minute");
		double[] catMessageOverflow = graphData.get("CatMessageOverflow");
		item.addValue(catMessageOverflow);
		model.setCatMessageOverflowGraph(item.getJsonString());
		
		item.getValues().clear();
		
		item.setTitles("Cat Message Size / Minute");
		double[] catMessageSize = graphData.get("CatMessageSize");
		item.addValue(catMessageSize);
		model.setCatMessageSizeGraph(item.getJsonString());

	}

	private Map<String, double[]> getHeartBeatData(Model model, Payload payload) {
		Date start = new Date(payload.getDate());
		Date end = payload.getHistoryEndDate();
		String ip = model.getIpAddress();
		String domain = payload.getDomain();
		List<Graph> graphs = new ArrayList<Graph>();
		try {
			graphs = this.graphDao.findByDomainNameIpDuration(start, end, ip, domain, "heartbeat",GraphEntity.READSET_FULL);
		} catch (DalException e) {
			e.printStackTrace();
		}
		Map<String, double[]> result = buildHeartbeatDates(start, end, graphs);
		return result;
	}

	private Map<String, double[]> buildHeartbeatDates(Date start, Date end, List<Graph> graphs) {
		int size = (int) ((end.getTime() - start.getTime()) / ONE_HOUR);
		Map<String, String[]> hourlyDate = gethourlyDate(graphs, start,size);
		return getHeartBeatDatesEveryMinute(hourlyDate, size);
	}

	private Map<String, String[]> gethourlyDate(List<Graph> graphs, Date start,int size) {
		Map<String, String[]> heartBeats = new HashMap<String, String[]>();
		for (Graph graph : graphs) {
			int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / ONE_HOUR);
			String detailContent = graph.getDetailContent();
			String[] alldates = detailContent.split("\n");
			for (int i = 0; i < alldates.length; i++) {
				String[] records = alldates[i].split("\t");
				String name = records[DetailOrder.NAME.ordinal()];
				String countPerHour = records[DetailOrder.COUNT_IN_MINUTES.ordinal()];
				boolean isExist = heartBeats.get(name) == null ? false : true;
				String[] singlePeriod = null;
				if (!isExist) {
					singlePeriod = new String[size];
					for(int index=0;index<size;index++){
						singlePeriod[index]="";
					}
				} else {
					singlePeriod = heartBeats.get(name);
				}
				singlePeriod[indexOfperiod]=countPerHour;
				heartBeats.put(name, singlePeriod);
			}
		}
		return heartBeats;
	}

	@SuppressWarnings("unchecked")
	public Map<String, double[]> getHeartBeatDatesEveryMinute(Map<String, String[]> heartBeats, final int size) {
		if(isEmptyMap(heartBeats)||size<=0){
			return Collections.EMPTY_MAP;
		}
		Map<String, double[]> result = new HashMap<String, double[]>();
		final int minutesPerHour = 60;
		int sizeOfHeartBeat = size * minutesPerHour;
		double[] emptyArray = new double[sizeOfHeartBeat];
		for (int i = 0; i < emptyArray.length; i++) {
			emptyArray[i] = 0;
		}
		Iterator iterator = heartBeats.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String name = (String) entry.getKey();
			double[] allDatePerMinutes = new double[sizeOfHeartBeat];
			String[] allPeriods = (String[]) entry.getValue();
			for (int i = 0; i < allPeriods.length; i++) {
				double[] datePerHour = new double[minutesPerHour];
				String oneHour = allPeriods[i];
				if(!illegalData(oneHour)){
					String[] dateInMinutes = oneHour.split(",");
					for (int j = 0; j < dateInMinutes.length; j++) {
						datePerHour[j] = Double.parseDouble(dateInMinutes[j]);
					}
				}else{
					datePerHour=emptyArray;
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
	
	//illegal
	private boolean illegalData(String oneHourData){
		return isEmpty(oneHourData)||oneHourData.split(",").length!=60;
	}
	
	@SuppressWarnings("unchecked")
   public boolean isEmptyMap(Map map){
		return map==null||map.size()==0;
	}

	private void formatHeartBeat(Map<String, double[]> result) {
		double[] totalStartedThread = result.get("TotalStartedThread");
		if (totalStartedThread != null) {
			result.put("StartedThread", getAddedCount(totalStartedThread));
		}
		double[] newGcCount = result.get("NewGcCount");
		if (totalStartedThread != null) {
			result.put("NewGcCount", getAddedCount(newGcCount));
		}
		double[] oldGcCount = result.get("OldGcCount");
		if (totalStartedThread != null) {
			result.put("OldGcCount", getAddedCount(oldGcCount));
		}
		double[] catMessageProduced = result.get("CatMessageProduced");
		if (totalStartedThread != null) {
			result.put("CatMessageProduced", getAddedCount(catMessageProduced));
		}
		double[] catMessageSize = result.get("CatMessageSize");
		if (totalStartedThread != null) {
			result.put("CatMessageSize", getAddedCount(catMessageSize));
			for (int i = 0; i < catMessageSize.length; i++) {
				catMessageSize[i] = catMessageSize[i] / K / K;
			}
		}
		double[] catMessageOverflow = result.get("CatMessageOverflow");
		if (totalStartedThread != null) {
			result.put("CatMessageOverflow", getAddedCount(catMessageOverflow));
		}
		// CatMessageSize HeapUsage NoneHeapUsage MemoryFree
		double[] heapUsage = result.get("HeapUsage");
		if (heapUsage != null) {
			for (int i = 0; i < heapUsage.length; i++) {
				heapUsage[i] = heapUsage[i] / K / K;
			}
		}
		double[] noneHeapUsage = result.get("NoneHeapUsage");
		if (noneHeapUsage != null) {
			for (int i = 0; i < noneHeapUsage.length; i++) {
				noneHeapUsage[i] = noneHeapUsage[i] / K / K;
			}
		}
		double[] memoryFree = result.get("MemoryFree");
		if (memoryFree != null) {
			for (int i = 0; i < memoryFree.length; i++) {
				memoryFree[i] = memoryFree[i] / K / K;
			}
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

	private boolean isEmpty(String content) {
		return content == null || content.equals("");
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

	private void setModel(Model model, DisplayHeartbeat displayHeartbeat) {
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
