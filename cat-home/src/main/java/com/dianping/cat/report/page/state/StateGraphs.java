package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.service.ReportServiceManager;

public class StateGraphs {

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private ServerConfigManager m_configManager;

	public LineChart buildGraph(StateReport report, String domain, Date start, Date end, String reportType, String key,
	      String ip) {
		if (reportType.equalsIgnoreCase("graph")) {
			return getHourlyGraph(report, domain, start, end, key, ip);
		} else {
			return getDailyGraph(domain, start, end, key, ip);
		}
	}

	private LineChart getDailyGraph(String domain, Date start, Date end, String key, String ip) {
		List<StateReport> reports = new ArrayList<StateReport>();

		for (long date = start.getTime(); date < end.getTime(); date = date + TimeHelper.ONE_HOUR) {
			StateReport report = getHourlyReport(date, domain, ip);

			if (report != null) {
				reports.add(report);
			}
		}
		int day = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_HOUR);
		LineChart item = new LineChart();

		item.setStart(start).setSize(day).setTitle(key).setStep(TimeHelper.ONE_HOUR);
		item.addSubTitle(key);
		item.addValue(getDataFromHourlySummary(reports, start.getTime(), day, key, ip));
		return item;
	}

	private Double[] getDataFromHourlyDetail(StateReport report, long start, int maxSize, String key, String ip) {
		Double[] result = new Double[maxSize];
		long minute = (System.currentTimeMillis()) / 1000 / 60 % 60;
		long current = System.currentTimeMillis();
		current -= current % Constants.HOUR;
		long size = (int) minute + 1;

		if (report.getStartTime().getTime() < current) {
			size = maxSize;
		}

		for (int i = 0; i < size; i++) {
			result[i] = 0.0;
		}
		StateDisplay show = new StateDisplay(ip, m_configManager);
		show.visitStateReport(report);
		Map<Long, Detail> datas = null;
		String domain = "";
		int index = key.indexOf(':');
		if (index != -1) {
			domain = key.substring(0, index);
			key = key.substring(index + 1);
			ProcessDomain processDomain = show.getProcessDomainMap().get(domain);
			if (processDomain != null) {
				datas = processDomain.getDetails();
			}
		}

		Map<Long, Message> messages = show.getMessagesMap();
		for (int i = 0; i < size; i++) {
			if (index != -1) {
				if (datas == null) {
					continue;
				}
				Detail detail = datas.get(i * 60 * 1000L + start);
				if (detail == null) {
					continue;
				}
				if (key.equalsIgnoreCase("total")) {
					result[i] = (double) detail.getTotal();
				} else if (key.equalsIgnoreCase("totalLoss")) {
					result[i] = (double) detail.getTotalLoss();
				} else if (key.equalsIgnoreCase("size")) {
					result[i] = (double) detail.getSize() / 1024 / 1024;
				}
				continue;
			}
			Message message = messages.get(i * 60 * 1000L + start);

			if (message != null) {
				if (key.equalsIgnoreCase("total")) {
					result[i] = (double) message.getTotal();
				} else if (key.equalsIgnoreCase("totalLoss")) {
					result[i] = (double) message.getTotalLoss();
				} else if (key.equalsIgnoreCase("avgTps")) {
					result[i] = (double) message.getTotal();
				} else if (key.equalsIgnoreCase("maxTps")) {
					result[i] = (double) message.getTotal();
				} else if (key.equalsIgnoreCase("dump")) {
					result[i] = (double) message.getDump();
				} else if (key.equalsIgnoreCase("dumpLoss")) {
					result[i] = (double) message.getDumpLoss();
				} else if (key.equalsIgnoreCase("pigeonTimeError")) {
					result[i] = (double) message.getPigeonTimeError();
				} else if (key.equalsIgnoreCase("networkTimeError")) {
					result[i] = (double) message.getNetworkTimeError();
				} else if (key.equalsIgnoreCase("blockTotal")) {
					result[i] = (double) message.getBlockTotal();
				} else if (key.equalsIgnoreCase("blockLoss")) {
					result[i] = (double) message.getBlockLoss();
				} else if (key.equalsIgnoreCase("blockTime")) {
					result[i] = (double) message.getBlockTime() * 1.0 / 60 / 1000;
				} else if (key.equalsIgnoreCase("size")) {
					result[i] = (double) message.getSize() / 1024 / 1024;
				} else if (key.equalsIgnoreCase("delayAvg")) {
					if (message.getDelayCount() > 0) {
						result[i] = message.getDelaySum() / message.getDelayCount();
					}
				}
			}
		}
		return result;
	}

	private double[] getDataFromHourlySummary(List<StateReport> reports, long start, int size, String key, String ip) {
		double[] result = new double[size];

		for (StateReport report : reports) {
			Date startTime = report.getStartTime();
			StateDisplay show = new StateDisplay(ip, m_configManager);

			show.visitStateReport(report);
			int i = (int) ((startTime.getTime() - start) / TimeHelper.ONE_HOUR);
			Machine machine = show.getTotal();

			result[i] = queryValue(key, machine);
		}
		return result;
	}

	private double queryValue(String key, Machine machine) {
		double value = 0;
		if (key.equalsIgnoreCase("total")) {
			value = machine.getTotal();
		} else if (key.equalsIgnoreCase("totalLoss")) {
			value = machine.getTotalLoss();
		} else if (key.equalsIgnoreCase("avgTps")) {
			value = machine.getAvgTps();
		} else if (key.equalsIgnoreCase("maxTps")) {
			value = machine.getMaxTps();
		} else if (key.equalsIgnoreCase("dump")) {
			value = machine.getDump();
		} else if (key.equalsIgnoreCase("dumpLoss")) {
			value = machine.getDumpLoss();
		} else if (key.equalsIgnoreCase("pigeonTimeError")) {
			value = machine.getPigeonTimeError();
		} else if (key.equalsIgnoreCase("networkTimeError")) {
			value = machine.getNetworkTimeError();
		} else if (key.equalsIgnoreCase("blockTotal")) {
			value = machine.getBlockTotal();
		} else if (key.equalsIgnoreCase("blockLoss")) {
			value = machine.getBlockLoss();
		} else if (key.equalsIgnoreCase("blockTime")) {
			value = machine.getBlockTime() * 1.0 / 60 / 1000;
		} else if (key.equalsIgnoreCase("size")) {
			value = machine.getSize() / 1024 / 1024;
		} else if (key.equalsIgnoreCase("delayAvg")) {
			if (machine.getDelayCount() > 0) {
				value = machine.getDelaySum() / machine.getDelayCount();
			}
		}
		return value;
	}

	private LineChart getHourlyGraph(StateReport report, String domain, Date start, Date end, String key, String ip) {
		LineChart item = new LineChart();

		item.setStart(start).setSize(60).setTitle(key).setStep(TimeHelper.ONE_MINUTE);
		item.add(key, getDataFromHourlyDetail(report, start.getTime(), 60, key, ip));
		return item;
	}

	private StateReport getHourlyReport(long date, String domain, String ip) {
		return m_reportService.queryStateReport(domain, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
	}

}
