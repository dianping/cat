package com.dianping.cat.report.page.model;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.transaction.model.IEntity;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.event.LocalEventService;
import com.dianping.cat.report.page.model.heartbeat.LocalHeartbeatService;
import com.dianping.cat.report.page.model.ip.LocalIpService;
import com.dianping.cat.report.page.model.logview.LocalLogViewService;
import com.dianping.cat.report.page.model.matrix.LocalMatrixService;
import com.dianping.cat.report.page.model.problem.LocalProblemService;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.report.view.StringSortHelper;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler extends ContainerHolder implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "transaction-local")
	private LocalTransactionService m_transactionService;

	@Inject(type = ModelService.class, value = "event-local")
	private LocalEventService m_eventService;

	@Inject(type = ModelService.class, value = "problem-local")
	private LocalProblemService m_problemService;

	@Inject(type = ModelService.class, value = "logview-local")
	private LocalLogViewService m_logviewService;

	@Inject(type = ModelService.class, value = "ip-local")
	private LocalIpService m_ipService;

	@Inject(type = ModelService.class, value = "heartbeat-local")
	private LocalHeartbeatService m_heartbeatService;

	@Inject(type = ModelService.class, value = "matrix-local")
	private LocalMatrixService m_matrixService;

	private String doFilter(Payload payload, Object dataModel) {
		String report = payload.getReport();
		String ipAddress = payload.getIpAddress();
		if ("transaction".equals(report)) {
			TransactionReportFilter filter = new TransactionReportFilter(payload.getType(), payload.getName(), ipAddress);

			return filter.buildXml((IEntity<?>) dataModel);
		} else if ("event".equals(report)) {
			EventReportFilter filter = new EventReportFilter(payload.getType(), payload.getName(), ipAddress);

			return filter.buildXml((com.dianping.cat.consumer.event.model.IEntity<?>) dataModel);
		} else if ("problem".equals(report)) {
			ProblemReportFilter filter = new ProblemReportFilter(ipAddress, payload.getThreadId(), payload.getType());

			return filter.buildXml((com.dianping.cat.consumer.problem.model.IEntity<?>) dataModel);
		} else if ("heartbeat".equals(report)) {
			if (StringUtils.isEmpty(ipAddress)) {
				HeartbeatReport reportModel = (HeartbeatReport) dataModel;
				Set<String> ips = reportModel.getIps();
				if (ips.size() > 0) {
					ipAddress = StringSortHelper.sort(ips).get(0);
				}
			}
			HeartBeatReportFilter filter = new HeartBeatReportFilter(ipAddress);

			return filter.buildXml((com.dianping.cat.consumer.heartbeat.model.IEntity<?>) dataModel);
		} else {
			return String.valueOf(dataModel);
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "model")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "model")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.XML);
		model.setPage(ReportPage.MODEL);

		try {
			String report = payload.getReport();
			String domain = payload.getDomain();
			ModelRequest request = new ModelRequest(domain, payload.getPeriod());
			ModelResponse<?> response = null;

			if ("transaction".equals(report)) {
				response = m_transactionService.invoke(request);
			} else if ("event".equals(report)) {
				response = m_eventService.invoke(request);
			} else if ("problem".equals(report)) {
				response = m_problemService.invoke(request);
			} else if ("logview".equals(report)) {
				request.setProperty("messageId", payload.getMessageId());
				if (!StringUtils.isEmpty(payload.getDirection())) {
					request.setProperty("direction", payload.getDirection());
				}
				if (!StringUtils.isEmpty(payload.getTag())) {
					request.setProperty("tag", payload.getTag());
				}
				response = m_logviewService.invoke(request);
			} else if ("ip".equals(report)) {
				response = m_ipService.invoke(request);
			} else if ("heartbeat".equals(report)) {
				response = m_heartbeatService.invoke(request);
			} else if ("matrix".equals(report)) {
				response = m_matrixService.invoke(request);
			} else {
				throw new RuntimeException("Unsupported report: " + report + "!");
			}

			Object dataModel = response.getModel();

			model.setModel(dataModel);
			model.setModelInXml(dataModel == null ? "" : doFilter(payload, dataModel));
		} catch (Throwable e) {
			model.setException(e);
			Cat.getProducer().logError(e);
		}

		m_jspViewer.view(ctx, model);
	}

	static class EventReportFilter extends com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder {
		private String m_type;

		private String m_name;

		private String m_ipAddress;

		public EventReportFilter(String type, String name, String ip) {
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.event.model.entity.Machine machine) {
			if (m_ipAddress == null || m_ipAddress.equals(CatString.ALL_IP)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			} else {
				// skip it
			}
		}

		@Override
		public void visitName(EventName name) {
			if (m_type == null) {
				// skip it
			} else if (m_name != null && name.getId().equals(m_name)) {
				super.visitName(name);
			} else if ("*".equals(m_name)) {
				super.visitName(name);
			} else {
				super.visitName(name);
			}
		}

		@Override
		public void visitRange(com.dianping.cat.consumer.event.model.entity.Range range) {
			if (m_type != null && m_name != null) {
				super.visitRange(range);
			}
		}

		@Override
		public void visitType(EventType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (m_type != null && type.getId().equals(m_type)) {
				type.setSuccessMessageUrl(null);
				type.setFailMessageUrl(null);

				super.visitType(type);
			} else {
				// skip it
			}
		}
	}

	static class ProblemReportFilter extends com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		// view is show the summary,detail show the thread info
		private String m_type;

		public ProblemReportFilter(String ipAddress, String threadId, String type) {
			m_ipAddress = ipAddress;
			m_type = type;
		}

		@Override
		public void visitMachine(Machine machine) {
			if (m_ipAddress == null) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			} else {
				// skip it
			}
		}

		@Override
		public void visitDuration(com.dianping.cat.consumer.problem.model.entity.Duration duration) {
			if ("view".equals(m_type)) {
				super.visitDuration(duration);
			} else if ("graph".equals(m_type)) {
				// skip it
			} else {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitSegment(Segment segment) {
			super.visitSegment(segment);
		}

		@Override
		public void visitThread(JavaThread thread) {
			if ("graph".equals(m_type)) {
				super.visitThread(thread);
			} else if ("view".equals(m_type)) {
				// skip it
			} else {
				super.visitThread(thread);
			}
		}
	}

	static class TransactionReportFilter extends com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
		private String m_type;

		private String m_name;

		private String m_ipAddress;

		public TransactionReportFilter(String type, String name, String ip) {
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.transaction.model.entity.Machine machine) {
			if (m_ipAddress == null || m_ipAddress.equals(CatString.ALL_IP)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			} else {
				// skip it
			}
		}

		@Override
		public void visitDuration(Duration duration) {
			if (m_type != null && m_name != null) {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitAllDuration(AllDuration duration) {

		}

		@Override
		public void visitName(TransactionName name) {
			if (m_type == null) {
				// skip it
			} else if (m_name != null && name.getId().equals(m_name)) {
				visitTransactionName(name);
			} else if ("*".equals(m_name)) {
				visitTransactionName(name);
			} else {
				visitTransactionName(name);
			}
		}

		private void visitTransactionName(TransactionName name) {
			super.visitName(name);
		}

		@Override
		public void visitRange(Range range) {
			if (m_type != null && m_name != null) {
				super.visitRange(range);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type == null) {
				super.visitType(type);

			} else if (m_type != null && type.getId().equals(m_type)) {
				type.setSuccessMessageUrl(null);
				type.setFailMessageUrl(null);
				super.visitType(type);
			} else {
				// skip it
			}
		}
	}

	static class HeartBeatReportFilter extends com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder {
		private String m_ip;

		public HeartBeatReportFilter(String ip) {
			m_ip = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.heartbeat.model.entity.Machine machine) {
			if (machine.getIp().equals(m_ip)) {
				super.visitMachine(machine);
			}
		}
	}
}
