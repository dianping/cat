package com.dianping.cat.report.page.dashboard;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.StatusStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.TypeStatistics;
import com.google.gson.Gson;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_eventService;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_transactionService;

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_problemService;

	private NumberFormat m_format = new DecimalFormat("#0.00");

	private static final String COUNT = "Count";

	private static final String FAILURE_COUNT = "FailureCount";

	private static final String TIME = "ResponseTime";

	private void buildEventReportResult(EventReport eventReport, String ip, String type, String name,
	      Map<String, String> data) {
		com.dianping.cat.consumer.event.model.entity.Machine eventMachine = eventReport.getMachines().get(ip);

		if (eventMachine != null) {
			if (StringUtils.isEmpty(name) && StringUtils.isEmpty(type)) {
				if (eventMachine != null) {
					Collection<EventType> types = eventMachine.getTypes().values();

					for (EventType eventType : types) {
						String id = eventType.getId();
						data.put(id + COUNT, String.valueOf(eventType.getTotalCount()));
						data.put(id + FAILURE_COUNT, String.valueOf(eventType.getFailCount()));
					}
				}
			} else if (StringUtils.isEmpty(name) && !StringUtils.isEmpty(type)) {
				EventType eventType = eventMachine.findType(type);

				if (eventType != null) {
					data.put(COUNT, String.valueOf(eventType.getTotalCount()));
					for (EventName eventName : eventType.getNames().values()) {
						data.put(eventName.getId() + COUNT, String.valueOf(eventName.getTotalCount()));
						data.put(eventName.getId() + FAILURE_COUNT, String.valueOf(eventName.getFailCount()));
					}
				}
			} else if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(type)) {
				EventType eventType = eventMachine.findType(type);

				if (eventType != null) {
					EventName eventName = eventType.findName(name);

					if (eventName != null) {
						data.put(COUNT, String.valueOf(eventName.getTotalCount()));
						data.put(FAILURE_COUNT, String.valueOf(eventName.getFailCount()));
					}
				}
			}
		}
	}

	private void buildProblemReportResult(ProblemReport problemReport, String ip, String type, String name,
	      Map<String, String> data) {
		ProblemStatistics problemStatistics = new ProblemStatistics();

		if (ip.equalsIgnoreCase(Payload.ALL)) {
			problemStatistics.setAllIp(true);
		} else {
			problemStatistics.setIp(ip);
		}
		problemStatistics.visitProblemReport(problemReport);

		if (StringUtils.isEmpty(name) && StringUtils.isEmpty(type)) {
			Map<String, TypeStatistics> status = problemStatistics.getStatus();

			for (Entry<String, TypeStatistics> temp : status.entrySet()) {
				String key = temp.getKey();
				TypeStatistics value = temp.getValue();
				data.put(key + COUNT, String.valueOf(value.getCount()));
			}
		} else if (StringUtils.isEmpty(name) && !StringUtils.isEmpty(type)) {
			Map<String, TypeStatistics> status = problemStatistics.getStatus();
			TypeStatistics value = status.get(type);

			if (value != null) {
				data.put(COUNT, String.valueOf(value.getCount()));
				for (Entry<String, StatusStatistics> temp : value.getStatus().entrySet()) {
					data.put(temp.getKey() + COUNT, String.valueOf(temp.getValue().getCount()));
				}
			}
		} else if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(type)) {
			Map<String, TypeStatistics> status = problemStatistics.getStatus();
			TypeStatistics value = status.get(type);

			if (value != null) {
				StatusStatistics nameValue = value.getStatus().get(name);
				if (nameValue != null) {
					data.put(COUNT, String.valueOf(nameValue.getCount()));
				}
			}
		}
	}

	private void buildTransactionReportResult(TransactionReport transactionReport, String ip, String type, String name,
	      Map<String, String> data) {
		Machine transactionMachine = transactionReport.getMachines().get(ip);
		if (transactionMachine != null) {
			if (StringUtils.isEmpty(name) && StringUtils.isEmpty(type)) {
				if (transactionMachine != null) {
					Collection<TransactionType> types = transactionMachine.getTypes().values();

					for (TransactionType transactionType : types) {
						String id = transactionType.getId();

						data.put(id + TIME, m_format.format(transactionType.getAvg()));
						data.put(id + COUNT, String.valueOf(transactionType.getTotalCount()));
						data.put(id + FAILURE_COUNT, String.valueOf(transactionType.getFailCount()));
					}
				}
			} else if (StringUtils.isEmpty(name) && !StringUtils.isEmpty(type)) {
				TransactionType transactionType = transactionMachine.findType(type);

				if (transactionType != null) {
					data.put(TIME, m_format.format(transactionType.getAvg()));
					data.put(COUNT, String.valueOf(transactionType.getTotalCount()));
					data.put(FAILURE_COUNT, String.valueOf(transactionType.getFailCount()));

					for (TransactionName transactionName : transactionType.getNames().values()) {
						String id = transactionName.getId();
						data.put(id + TIME, m_format.format(transactionName.getAvg()));
						data.put(id + COUNT, String.valueOf(transactionName.getTotalCount()));
						data.put(id + FAILURE_COUNT, String.valueOf(transactionName.getFailCount()));
					}
				}
			} else if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(type)) {
				TransactionType transactionType = transactionMachine.findType(type);

				if (transactionType != null) {
					TransactionName transactionName = transactionType.findName(name);

					if (transactionName != null) {
						data.put(TIME, m_format.format(transactionName.getAvg()));
						data.put(COUNT, String.valueOf(transactionName.getTotalCount()));
						data.put(FAILURE_COUNT, String.valueOf(transactionName.getFailCount()));
					}
				}
			}
		}
	}

	private Map<String, String> getBaseInfoByDomianAndIp(String domain, String ip) {
		Map<String, String> data = new HashMap<String, String>();

		TransactionReport transactionReport = getTransactionHourlyReport(domain, ip, null);

		Machine transactionMachine = transactionReport.getMachines().get(ip);
		if (transactionMachine != null) {
			Collection<TransactionType> types = transactionMachine.getTypes().values();
			for (TransactionType type : types) {
				String name = type.getId();
				data.put(name + TIME, m_format.format(type.getAvg()));
				data.put(name + COUNT, m_format.format(type.getTotalCount()));
			}
		}
		EventReport eventReport = getEventHourlyReport(domain, ip, null);

		com.dianping.cat.consumer.event.model.entity.Machine eventMachine = eventReport.getMachines().get(ip);
		if (eventMachine != null) {
			long exceptionCount = 0;
			EventType exception = eventMachine.findType("Exception");
			EventType runtimeException = eventMachine.findType("RuntimeException");

			if (exception != null) {
				exceptionCount += exception.getTotalCount();
			}
			if (runtimeException != null) {
				exceptionCount += runtimeException.getTotalCount();
			}
			data.put("Exception", String.valueOf(exceptionCount));
		}

		return data;
	}

	private EventReport getEventHourlyReport(String domain, String ip, String type) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT) //
		      .setProperty("ip", ip);
		if (!StringUtils.isEmpty(type)) {
			request.setProperty("type", type);
		}

		if (m_transactionService.isEligable(request)) {
			ModelResponse<EventReport> response = m_eventService.invoke(request);
			EventReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	private ProblemReport getProblemHourlyReport(String domain, String ip) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT) //
		      .setProperty("type", "view");
		if (!ip.equalsIgnoreCase(Payload.ALL)) {
			request.setProperty("ip", ip);
		}

		if (m_transactionService.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemService.invoke(request);
			ProblemReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	private TransactionReport getTransactionHourlyReport(String domain, String ip, String type) {
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT) //
		      .setProperty("ip", ip);
		if (!StringUtils.isEmpty(type)) {
			request.setProperty("type", type);
		}

		if (m_transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			TransactionReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "dashboard")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "dashboard")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Map<String, String> data = new HashMap<String, String>();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.DASHBOARD);
		data.put("timestamp", String.valueOf(new Date().getTime()));
		String domain = payload.getDomain();

		if (!StringUtils.isEmpty(domain)) {
			String report = payload.getReport();
			String type = payload.getType();
			String name = payload.getName();
			String ip = payload.getIp();

			if (!StringUtils.isEmpty(report)) {
				if ("transaction".equalsIgnoreCase(report)) {
					TransactionReport transactionReport = getTransactionHourlyReport(domain, ip, type);

					buildTransactionReportResult(transactionReport, ip, type, name, data);
				} else if ("event".equalsIgnoreCase(report)) {
					EventReport eventReport = getEventHourlyReport(domain, ip, type);

					buildEventReportResult(eventReport, ip, type, name, data);
				} else if ("problem".equalsIgnoreCase(report)) {
					ProblemReport problemReport = getProblemHourlyReport(domain, ip);

					buildProblemReportResult(problemReport, ip, type, name, data);
				}
			} else {
				Map<String, String> temp = getBaseInfoByDomianAndIp(domain, ip);
				data.putAll(temp);
			}
		}

		model.setData(new Gson().toJson(data));
		m_jspViewer.view(ctx, model);
	}

}
