package com.dianping.cat.report.page.model;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.sql.model.entity.Database;
import com.dianping.cat.consumer.transaction.model.IEntity;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.cross.LocalCrossService;
import com.dianping.cat.report.page.model.dependency.LocalDependencyService;
import com.dianping.cat.report.page.model.event.LocalEventService;
import com.dianping.cat.report.page.model.heartbeat.LocalHeartbeatService;
import com.dianping.cat.report.page.model.logview.LocalMessageService;
import com.dianping.cat.report.page.model.matrix.LocalMatrixService;
import com.dianping.cat.report.page.model.metric.LocalMetricService;
import com.dianping.cat.report.page.model.problem.LocalProblemService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.sql.LocalSqlService;
import com.dianping.cat.report.page.model.state.LocalStateService;
import com.dianping.cat.report.page.model.top.LocalTopService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.report.view.StringSortHelper;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class Handler extends ContainerHolder implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "event-local")
	private LocalEventService m_eventService;

	@Inject(type = ModelService.class, value = "heartbeat-local")
	private LocalHeartbeatService m_heartbeatService;

	@Inject(type = ModelService.class, value = "message-local")
	private LocalMessageService m_messageService;

	@Inject(type = ModelService.class, value = "matrix-local")
	private LocalMatrixService m_matrixService;

	@Inject(type = ModelService.class, value = "problem-local")
	private LocalProblemService m_problemService;

	@Inject(type = ModelService.class, value = "transaction-local")
	private LocalTransactionService m_transactionService;

	@Inject(type = ModelService.class, value = "cross-local")
	private LocalCrossService m_crossService;

	@Inject(type = ModelService.class, value = "sql-local")
	private LocalSqlService m_sqlService;

	@Inject(type = ModelService.class, value = "state-local")
	private LocalStateService m_stateService;

	@Inject(type = ModelService.class, value = "top-local")
	private LocalTopService m_topService;

	@Inject(type = ModelService.class, value = "metric-local")
	private LocalMetricService m_metricService;

	@Inject(type = ModelService.class, value = "dependency-local")
	private LocalDependencyService m_dependencyService;

	private String doFilter(Payload payload, Object dataModel) {
		String report = payload.getReport();
		String ipAddress = payload.getIpAddress();
		if ("transaction".equals(report)) {
			try {
				TransactionReportFilter filter = new TransactionReportFilter(payload.getType(), payload.getName(),
				      ipAddress);

				return filter.buildXml((IEntity<?>) dataModel);
			} catch (Exception e) {
				TransactionReportFilter filter = new TransactionReportFilter(payload.getType(), payload.getName(),
				      ipAddress);

				return filter.buildXml((IEntity<?>) dataModel);
			}

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
		} else if ("sql".equals(report)) {
			String database = payload.getDatabase();
			SqlReportFilter filter = new SqlReportFilter(database);

			return filter.buildXml((com.dianping.cat.consumer.sql.model.IEntity<?>) dataModel);
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
			ModelPeriod period = payload.getPeriod();
			ModelRequest request = new ModelRequest(domain, period.getStartTime());
			ModelResponse<?> response = null;

			if ("transaction".equals(report)) {
				response = m_transactionService.invoke(request);
			} else if ("event".equals(report)) {
				response = m_eventService.invoke(request);
			} else if ("problem".equals(report)) {
				response = m_problemService.invoke(request);
			} else if ("logview".equals(report)) {
				String messageId = payload.getMessageId();
				MessageId id = MessageId.parse(messageId);

				request.setProperty("messageId", messageId);
				request.setProperty("waterfall", String.valueOf(payload.isWaterfall()));

				if (id.getVersion() == 1) {
				} else {
					response = m_messageService.invoke(request);
				}
			} else if ("heartbeat".equals(report)) {
				response = m_heartbeatService.invoke(request);
			} else if ("matrix".equals(report)) {
				response = m_matrixService.invoke(request);
			} else if ("cross".equals(report)) {
				response = m_crossService.invoke(request);
			} else if ("sql".equals(report)) {
				response = m_sqlService.invoke(request);
			} else if ("state".equals(report)) {
				response = m_stateService.invoke(request);
			} else if ("top".equals(report)) {
				response = m_topService.invoke(request);
			} else if ("metric".equals(report)) {
				response = m_metricService.invoke(request);
			} else if ("dependency".equals(report)) {
				response = m_dependencyService.invoke(request);
			} else {
				throw new RuntimeException("Unsupported report: " + report + "!");
			}

			if (response != null) {
				Object dataModel = response.getModel();

				model.setModel(dataModel);
				model.setModelInXml(dataModel == null ? "" : doFilter(payload, dataModel));
			}
		} catch (Throwable e) {
			model.setException(e);
			Cat.logError(e);
		}

		m_jspViewer.view(ctx, model);
	}

	static class EventReportFilter extends com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public EventReportFilter(String type, String name, String ip) {
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.event.model.entity.Machine machine) {
			if (m_ipAddress == null || m_ipAddress.equals(CatString.ALL)) {
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

	static class HeartBeatReportFilter extends com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder {
		private String m_ip;

		public HeartBeatReportFilter(String ip) {
			m_ip = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.heartbeat.model.entity.Machine machine) {
			if (machine.getIp().equals(m_ip) || StringUtils.isEmpty(m_ip) || CatString.ALL.equals(m_ip)) {
				super.visitMachine(machine);
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

	static class SqlReportFilter extends com.dianping.cat.consumer.sql.model.transform.DefaultXmlBuilder {

		private String m_database;

		public SqlReportFilter(String database) {
			m_database = database;
		}

		@Override
		public void visitDatabase(Database database) {
			if ("All".equals(m_database) || database.getId().equals(m_database)) {
				super.visitDatabase(database);
			}
		}
	}

	static class TransactionReportFilter extends com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public TransactionReportFilter(String type, String name, String ip) {
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitAllDuration(AllDuration duration) {
		}

		@Override
		public void visitDuration(Duration duration) {
			if (m_type != null && m_name != null) {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.transaction.model.entity.Machine machine) {
			synchronized (machine) {
				if (m_ipAddress == null || m_ipAddress.equals(CatString.ALL)) {
					super.visitMachine(machine);
				} else if (machine.getIp().equals(m_ipAddress)) {
					super.visitMachine(machine);
				} else {
					// skip it
				}
			}
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

		@Override
		public void visitRange(Range range) {
			if (m_type != null && m_name != null) {
				super.visitRange(range);
			}
		}

		private void visitTransactionName(TransactionName name) {
			super.visitName(name);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			synchronized (transactionReport) {
				super.visitTransactionReport(transactionReport);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (m_type != null && type.getId().equals(m_type)) {
				super.visitType(type);
			} else {
				// skip it
			}
		}
	}

}
