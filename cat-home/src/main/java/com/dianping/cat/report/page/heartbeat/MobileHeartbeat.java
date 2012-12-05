package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.MobileGraphItem;
import com.dianping.cat.report.graph.ValueTranslater;

public class MobileHeartbeat {
	private MobileGraphItem m_activeThreads = new MobileGraphItem();

	private MobileGraphItem m_addCatMessageOverflow = new MobileGraphItem();

	private MobileGraphItem m_addCatMessageProduced = new MobileGraphItem();

	private MobileGraphItem m_addCatMessageSize = new MobileGraphItem();

	private MobileGraphItem m_addNewGcCount = new MobileGraphItem();

	private MobileGraphItem m_addOldGcCount = new MobileGraphItem();

	private MobileGraphItem m_catThreads = new MobileGraphItem();

	private String m_currentIp;

	private MobileGraphItem m_daemonThreads = new MobileGraphItem();

	private List<MobileGraphItem> m_disks = new ArrayList<MobileGraphItem>();

	private MobileGraphItem m_heapUsage = new MobileGraphItem();

	private MobileGraphItem m_httpThreads = new MobileGraphItem();

	private List<String> m_ips = new ArrayList<String>();

	private MobileGraphItem m_memoryFree = new MobileGraphItem();

	private MobileGraphItem m_newThreads = new MobileGraphItem();

	private MobileGraphItem m_noneHeapUsage = new MobileGraphItem();

	private MobileGraphItem m_pigeonThreads = new MobileGraphItem();

	private MobileGraphItem m_systemLoadAverage = new MobileGraphItem();

	private transient ValueTranslater m_tansalater = new DefaultValueTranslater();

	private MobileGraphItem m_totalThreads = new MobileGraphItem();

	private void creatGraph(MobileGraphItem graph, double[] values, String title) {
		graph.setTitle(title);
		graph.setValue(values);
		graph.setMaxValue(m_tansalater.getMaxValue(values));
	}

	public MobileHeartbeat display(Model model, DisplayHeartbeat heartbeat) {
		if (heartbeat == null) {
			return this;
		}
		m_currentIp = model.getIpAddress();
		m_ips = model.getIps();
		creatGraph(m_activeThreads, heartbeat.getActiveThreads(), "Active Thread");
		creatGraph(m_daemonThreads, heartbeat.getDaemonThreads(), "Daemon Thread");
		creatGraph(m_totalThreads, heartbeat.getTotalThreads(), "Total Thread");
		creatGraph(m_newThreads, heartbeat.getNewThreads(), "New Thread");
		creatGraph(m_catThreads, heartbeat.getCatThreads(), "Cat Thread");
		creatGraph(m_pigeonThreads, heartbeat.getPigeonThreads(), "Pigeon Thread");
		creatGraph(m_httpThreads, heartbeat.getHttpThreads(), "HTTP Thread");
		creatGraph(m_addCatMessageProduced, heartbeat.getAddCatMessageProduced(), "Cat Message Produced / Minute");
		creatGraph(m_addCatMessageOverflow, heartbeat.getAddCatMessageOverflow(), "Cat Message Overflow / Minute");
		creatGraph(m_addCatMessageSize, heartbeat.getAddCatMessageSize(), "Cat Message Size / Minute");
		creatGraph(m_addNewGcCount, heartbeat.getNewGcCount(), "NewGc Count");
		creatGraph(m_addOldGcCount, heartbeat.getOldGcCount(), "OldGc Count");
		creatGraph(m_heapUsage, heartbeat.getHeapUsage(), "Heap Usage");
		creatGraph(m_noneHeapUsage, heartbeat.getNoneHeapUsage(), "None Heap Usage");
		creatGraph(m_memoryFree, heartbeat.getMemoryFree(), "Memory Free");
		creatGraph(m_systemLoadAverage, heartbeat.getSystemLoadAverage(), "System Load");

		List<String> diskNames = heartbeat.getDiskNames();
		List<double[]> diskValues = heartbeat.getDiskValues();
		for (int i = 0; i < diskNames.size(); i++) {
			MobileGraphItem item = new MobileGraphItem();
			creatGraph(item, diskValues.get(i), "Disk Free ( " + diskNames.get(i) + " )");
			m_disks.add(item);
		}
		return this;
	}

	public MobileGraphItem getActiveThreads() {
		return m_activeThreads;
	}

	public MobileGraphItem getAddCatMessageOverflow() {
		return m_addCatMessageOverflow;
	}

	public MobileGraphItem getAddCatMessageProduced() {
		return m_addCatMessageProduced;
	}

	public MobileGraphItem getAddCatMessageSize() {
		return m_addCatMessageSize;
	}

	public MobileGraphItem getAddNewGcCount() {
		return m_addNewGcCount;
	}

	public MobileGraphItem getAddOldGcCount() {
		return m_addOldGcCount;
	}

	public MobileGraphItem getCatThreads() {
		return m_catThreads;
	}

	public String getCurrentIp() {
		return m_currentIp;
	}

	public MobileGraphItem getDaemonThreads() {
		return m_daemonThreads;
	}

	public List<MobileGraphItem> getDisks() {
		return m_disks;
	}

	public MobileGraphItem getHeapUsage() {
		return m_heapUsage;
	}

	public List<String> getIps() {
		return m_ips;
	}

	public MobileGraphItem getMemoryFree() {
		return m_memoryFree;
	}

	public MobileGraphItem getNewThreads() {
		return m_newThreads;
	}

	public MobileGraphItem getNoneHeapUsage() {
		return m_noneHeapUsage;
	}

	public MobileGraphItem getPigeonTheads() {
		return m_pigeonThreads;
	}

	public MobileGraphItem getSystemLoadAverage() {
		return m_systemLoadAverage;
	}

	public MobileGraphItem getTotalThreads() {
		return m_totalThreads;
	}

	public void setActiveThreads(MobileGraphItem activeThreads) {
		m_activeThreads = activeThreads;
	}

	public void setAddCatMessageOverflow(MobileGraphItem addCatMessageOverflow) {
		m_addCatMessageOverflow = addCatMessageOverflow;
	}

	public void setAddCatMessageProduced(MobileGraphItem addCatMessageProduced) {
		m_addCatMessageProduced = addCatMessageProduced;
	}

	public void setAddCatMessageSize(MobileGraphItem addCatMessageSize) {
		m_addCatMessageSize = addCatMessageSize;
	}

	public void setAddNewGcCount(MobileGraphItem addNewGcCount) {
		m_addNewGcCount = addNewGcCount;
	}

	public void setAddOldGcCount(MobileGraphItem addOldGcCount) {
		m_addOldGcCount = addOldGcCount;
	}

	public void setCatThreads(MobileGraphItem catThreads) {
		m_catThreads = catThreads;
	}

	public void setCurrentIp(String currentIp) {
		m_currentIp = currentIp;
	}

	public void setDaemonThreads(MobileGraphItem daemonThreads) {
		m_daemonThreads = daemonThreads;
	}

	public void setDisks(List<MobileGraphItem> disks) {
		m_disks = disks;
	}

	public void setHeapUsage(MobileGraphItem heapUsage) {
		m_heapUsage = heapUsage;
	}

	public void setHttpTheads(MobileGraphItem httpTheads) {
		m_httpThreads = httpTheads;
	}

	public void setIps(List<String> ips) {
		m_ips = ips;
	}

	public void setMemoryFree(MobileGraphItem memoryFree) {
		m_memoryFree = memoryFree;
	}

	public void setNewThreads(MobileGraphItem newThreads) {
		m_newThreads = newThreads;
	}

	public void setNoneHeapUsage(MobileGraphItem noneHeapUsage) {
		m_noneHeapUsage = noneHeapUsage;
	}

	public void setPigeonTheads(MobileGraphItem pigeonTheads) {
		m_pigeonThreads = pigeonTheads;
	}

	public void setSystemLoadAverage(MobileGraphItem systemLoadAverage) {
		m_systemLoadAverage = systemLoadAverage;
	}

	public void setTotalThreads(MobileGraphItem totalThreads) {
		m_totalThreads = totalThreads;
	}

}
