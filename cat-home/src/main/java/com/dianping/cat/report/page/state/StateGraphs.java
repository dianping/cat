package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.HistoryGraphItem;
import com.dianping.cat.report.service.ReportService;

public class StateGraphs {

	@Inject
	private ReportService m_reportService;

	public HistoryGraphItem buildGraph(StateReport report, String domain, Date start, Date end,
	      String reportType, String key, String ip) {
		if (reportType.equalsIgnoreCase("graph")) {
			return getHourlyGraph(report, domain, start, end, key, ip);
		} else {
			return getDailyGraph(domain, start, end, key, ip);
		}
	}

	private HistoryGraphItem getDailyGraph(String domain, Date start, Date end, String key, String ip) {
		List<StateReport> reports = new ArrayList<StateReport>();

		for (long date = start.getTime(); date < end.getTime(); date = date + TimeUtil.ONE_HOUR) {
			StateReport report = getHourlyReport(date, domain, ip);

			if (report != null) {
				reports.add(report);
			}
		}
		int day = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_HOUR);
		HistoryGraphItem item = new HistoryGraphItem();

		item.setStart(start).setSize(day).setTitles(key).setStep(TimeUtil.ONE_HOUR);
		item.addValue(getDataFromHourlySummary(reports, start.getTime(), day, key, ip));
		return item;
	}

	private double[] getDataFromHourlyDetail(StateReport report, long start, int size, String key, String ip) {
		double[] result = new double[size];
		StateShow show = new StateShow(ip);
		show.visitStateReport(report);
		
		Map<Long, Message> messages = show.getMessagesMap();
		for (int i = 0; i < size; i++) {
			Message message = messages.get(i * 60 * 1000 + start);

			if (message != null) {
				if (key.equalsIgnoreCase("total")) {
					result[i] = message.getTotal();
				} else if (key.equalsIgnoreCase("totalLoss")) {
					result[i] = message.getTotalLoss();
				} else if (key.equalsIgnoreCase("avgTps")) {
					result[i] = message.getTotal();
				} else if (key.equalsIgnoreCase("maxTps")) {
					result[i] = message.getTotal();
				} else if (key.equalsIgnoreCase("dump")) {
					result[i] = message.getDump();
				} else if (key.equalsIgnoreCase("dumpLoss")) {
					result[i] = message.getDumpLoss();
				} else if (key.equalsIgnoreCase("pigeonTimeError")) {
					result[i] = message.getPigeonTimeError();
				} else if (key.equalsIgnoreCase("networkTimeError")) {
					result[i] = message.getNetworkTimeError();
				} else if (key.equalsIgnoreCase("blockTotal")) {
					result[i] = message.getBlockTotal();
				} else if (key.equalsIgnoreCase("blockLoss")) {
					result[i] = message.getBlockLoss();
				} else if (key.equalsIgnoreCase("blockTime")) {
					result[i] = message.getBlockTime() * 1.0 / 60 / 1000;
				} else if (key.equalsIgnoreCase("size")) {
					result[i] = message.getSize() / 1024 / 1024;
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
			StateShow show = new StateShow(ip);

			show.visitStateReport(report);
			int i = (int) ((startTime.getTime() - start) / TimeUtil.ONE_HOUR);

			if (key.equalsIgnoreCase("total")) {
				result[i] = show.getTotal().getTotal();
			} else if (key.equalsIgnoreCase("totalLoss")) {
				result[i] = show.getTotal().getTotalLoss();
			} else if (key.equalsIgnoreCase("avgTps")) {
				result[i] = show.getTotal().getAvgTps();
			} else if (key.equalsIgnoreCase("maxTps")) {
				result[i] = show.getTotal().getMaxTps();
			} else if (key.equalsIgnoreCase("dump")) {
				result[i] = show.getTotal().getDump();
			} else if (key.equalsIgnoreCase("dumpLoss")) {
				result[i] = show.getTotal().getDumpLoss();
			} else if (key.equalsIgnoreCase("pigeonTimeError")) {
				result[i] = show.getTotal().getPigeonTimeError();
			} else if (key.equalsIgnoreCase("networkTimeError")) {
				result[i] = show.getTotal().getNetworkTimeError();
			} else if (key.equalsIgnoreCase("blockTotal")) {
				result[i] = show.getTotal().getBlockTotal();
			} else if (key.equalsIgnoreCase("blockLoss")) {
				result[i] = show.getTotal().getBlockLoss();
			} else if (key.equalsIgnoreCase("blockTime")) {
				result[i] = show.getTotal().getBlockTime() * 1.0 / 60 / 1000;
			} else if (key.equalsIgnoreCase("size")) {
				result[i] = show.getTotal().getSize() / 1024 / 1024;
			} else if (key.equalsIgnoreCase("delayAvg")) {
				if (show.getTotal().getDelayCount() > 0) {
					result[i] = show.getTotal().getDelaySum() / show.getTotal().getDelayCount();
				}
			}
		}
		return result;
	}

	private HistoryGraphItem getHourlyGraph(StateReport report, String domain, Date start, Date end, String key,
	      String ip) {
		HistoryGraphItem item = new HistoryGraphItem();
		
		item.setStart(start).setSize(60).setTitles(key).setStep(TimeUtil.ONE_MINUTE);
		item.addValue(getDataFromHourlyDetail(report, start.getTime(), 60, key, ip));
		return item;
	}

	private StateReport getHourlyReport(long date, String domain, String ip) {
		return m_reportService.queryStateReport(domain, new Date(date), new Date(date + TimeUtil.ONE_HOUR));
	}
}
