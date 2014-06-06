package com.dianping.cat.report.page.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.IEntity;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
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
import com.dianping.cat.report.page.model.state.LocalStateService;
import com.dianping.cat.report.page.model.top.LocalTopService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.report.page.system.graph.SystemReportConvertor;
import com.dianping.cat.report.page.userMonitor.UserMonitorConvert;
import com.dianping.cat.report.view.StringSortHelper;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class Handler extends ContainerHolder implements PageHandler<Context> {

	@Inject(type = ModelService.class, value = "cross-local")
	private LocalCrossService m_crossService;

	@Inject(type = ModelService.class, value = "dependency-local")
	private LocalDependencyService m_dependencyService;

	@Inject(type = ModelService.class, value = "event-local")
	private LocalEventService m_eventService;

	@Inject(type = ModelService.class, value = "heartbeat-local")
	private LocalHeartbeatService m_heartbeatService;

	@Inject(type = ModelService.class, value = "matrix-local")
	private LocalMatrixService m_matrixService;

	@Inject(type = ModelService.class, value = "message-local")
	private LocalMessageService m_messageService;

	@Inject(type = ModelService.class, value = "metric-local")
	private LocalMetricService m_metricService;

	@Inject(type = ModelService.class, value = "problem-local")
	private LocalProblemService m_problemService;

	@Inject(type = ModelService.class, value = "state-local")
	private LocalStateService m_stateService;

	@Inject(type = ModelService.class, value = "top-local")
	private LocalTopService m_topService;

	@Inject(type = ModelService.class, value = "transaction-local")
	private LocalTransactionService m_transactionService;

	private static final int DEFAULT_SIZE = 32 * 1024;

	private byte[] compress(String str) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 32);
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.toByteArray();
	}

	private String doFilter(Payload payload, Object dataModel) {
		String report = payload.getReport();
		String ipAddress = payload.getIpAddress();
		if (TransactionAnalyzer.ID.equals(report)) {
			try {
				TransactionReportFilter filter = new TransactionReportFilter(payload.getType(), payload.getName(),
				      ipAddress);

				return filter.buildXml((IEntity<?>) dataModel);
			} catch (Exception e) {
				TransactionReportFilter filter = new TransactionReportFilter(payload.getType(), payload.getName(),
				      ipAddress);

				return filter.buildXml((IEntity<?>) dataModel);
			}
		} else if (EventAnalyzer.ID.equals(report)) {
			EventReportFilter filter = new EventReportFilter(payload.getType(), payload.getName(), ipAddress);

			return filter.buildXml((com.dianping.cat.consumer.event.model.IEntity<?>) dataModel);
		} else if (ProblemAnalyzer.ID.equals(report)) {
			ProblemReportFilter filter = new ProblemReportFilter(ipAddress, payload.getThreadId(), payload.getType());

			return filter.buildXml((com.dianping.cat.consumer.problem.model.IEntity<?>) dataModel);
		} else if (HeartbeatAnalyzer.ID.equals(report)) {
			if (StringUtils.isEmpty(ipAddress)) {
				HeartbeatReport reportModel = (HeartbeatReport) dataModel;
				Set<String> ips = reportModel.getIps();
				if (ips.size() > 0) {
					ipAddress = StringSortHelper.sort(ips).get(0);
				}
			}
			HeartBeatReportFilter filter = new HeartBeatReportFilter(ipAddress);

			return filter.buildXml((com.dianping.cat.consumer.heartbeat.model.IEntity<?>) dataModel);
		} else if (MatrixAnalyzer.ID.equals(report)) {
			return new MatrixReportFilter().buildXml((com.dianping.cat.consumer.matrix.model.IEntity<?>) dataModel);
		} else if (CrossAnalyzer.ID.equals(report)) {
			return new CrossReportFilter().buildXml((com.dianping.cat.consumer.cross.model.IEntity<?>) dataModel);
		} else if (StateAnalyzer.ID.equals(report)) {
			return new StateReportFilter().buildXml((com.dianping.cat.consumer.state.model.IEntity<?>) dataModel);
		} else if (TopAnalyzer.ID.equals(report)) {
			return new TopReportFilter().buildXml((com.dianping.cat.consumer.top.model.IEntity<?>) dataModel);
		} else if (MetricAnalyzer.ID.equals(report)) {
			return new MetricReportFilter().buildXml((com.dianping.cat.consumer.metric.model.IEntity<?>) dataModel);
		} else if (DependencyAnalyzer.ID.equals(report)) {
			return new DependencyReportFilter()
			      .buildXml((com.dianping.cat.consumer.dependency.model.IEntity<?>) dataModel);
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

	@SuppressWarnings("unchecked")
	@Override
	@OutboundActionMeta(name = "model")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		HttpServletResponse httpResponse = ctx.getHttpServletResponse();

		model.setAction(Action.XML);
		model.setPage(ReportPage.MODEL);

		try {
			String report = payload.getReport();
			String domain = payload.getDomain();
			ModelPeriod period = payload.getPeriod();
			ModelRequest request = null;

			if ("logview".equals(report)) {
				request = new ModelRequest(domain, MessageId.parse(payload.getMessageId()).getTimestamp());
			} else {
				request = new ModelRequest(domain, period.getStartTime());
			}
			ModelResponse<?> response = null;

			if (TransactionAnalyzer.ID.equals(report)) {
				response = m_transactionService.invoke(request);
			} else if (EventAnalyzer.ID.equals(report)) {
				response = m_eventService.invoke(request);
			} else if (ProblemAnalyzer.ID.equals(report)) {
				response = m_problemService.invoke(request);
			} else if ("logview".equals(report)) {
				String messageId = payload.getMessageId();
				MessageId id = MessageId.parse(messageId);

				request.setProperty("messageId", messageId);
				request.setProperty("waterfall", String.valueOf(payload.isWaterfall()));

				if (id.getVersion() != 1) {
					response = m_messageService.invoke(request);
				}
			} else if (HeartbeatAnalyzer.ID.equals(report)) {
				response = m_heartbeatService.invoke(request);
			} else if (MatrixAnalyzer.ID.equals(report)) {
				response = m_matrixService.invoke(request);
			} else if (CrossAnalyzer.ID.equals(report)) {
				response = m_crossService.invoke(request);
			} else if (StateAnalyzer.ID.equals(report)) {
				response = m_stateService.invoke(request);
			} else if (TopAnalyzer.ID.equals(report)) {
				response = m_topService.invoke(request);
			} else if (MetricAnalyzer.ID.equals(report)) {
				response = m_metricService.invoke(request);

				String metricType = payload.getMetricType();
				String type = payload.getType();

				if (Constants.METRIC_USER_MONITOR.equals(metricType)) {
					String city = payload.getCity();
					String channel = payload.getChannel();
					UserMonitorConvert convert = new UserMonitorConvert(type, city, channel);
					MetricReport metricReport = (MetricReport) response.getModel();

					convert.visitMetricReport(metricReport);
					((ModelResponse<MetricReport>) response).setModel(convert.getReport());

				} else if (Constants.METRIC_SYSTEM_MONITOR.equals(metricType)) {
					String ipAddrsStr = payload.getIpAddress();
					Set<String> ipAddrs = null;
					
					if (!Constants.ALL.equalsIgnoreCase(ipAddrsStr)) {
						String[] ipAddrsArray = ipAddrsStr.split("_");
						ipAddrs = new HashSet<String>(Arrays.asList(ipAddrsArray));
					}
					
					SystemReportConvertor convert = new SystemReportConvertor(type, ipAddrs);
					MetricReport metricReport = (MetricReport) response.getModel();
					
					convert.visitMetricReport(metricReport);
					((ModelResponse<MetricReport>) response).setModel(convert.getReport());

				}

			} else if (DependencyAnalyzer.ID.equals(report)) {
				response = m_dependencyService.invoke(request);
			} else {
				throw new RuntimeException("Unsupported report: " + report + "!");
			}

			if (response != null) {
				Object dataModel = response.getModel();
				String xml = "";

				if (dataModel != null) {
					xml = doFilter(payload, dataModel);
				}
				ServletOutputStream outputStream = httpResponse.getOutputStream();
				byte[] compress = compress(xml);

				httpResponse.setContentType("application/xml;charset=utf-8");
				httpResponse.addHeader("Content-Encoding", "gzip");
				outputStream.write(compress);
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}
	}

	public static class CrossReportFilter extends com.dianping.cat.consumer.cross.model.transform.DefaultXmlBuilder {
		public CrossReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

	public static class DependencyReportFilter extends
	      com.dianping.cat.consumer.dependency.model.transform.DefaultXmlBuilder {
		public DependencyReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

	public static class EventReportFilter extends com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public EventReportFilter(String type, String name, String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.event.model.entity.Machine machine) {
			if (m_ipAddress == null || m_ipAddress.equals(Constants.ALL)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(EventName name) {
			if (m_type != null) {
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
			} else if (type.getId().equals(m_type)) {
				type.setSuccessMessageUrl(null);
				type.setFailMessageUrl(null);

				super.visitType(type);
			}
		}
	}

	static class HeartBeatReportFilter extends com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder {
		private String m_ip;

		public HeartBeatReportFilter(String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_ip = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.heartbeat.model.entity.Machine machine) {
			if (machine.getIp().equals(m_ip) || StringUtils.isEmpty(m_ip) || Constants.ALL.equals(m_ip)) {
				super.visitMachine(machine);
			}
		}
	}

	public static class MatrixReportFilter extends com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder {
		public MatrixReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

	public static class MetricReportFilter extends com.dianping.cat.consumer.metric.model.transform.DefaultXmlBuilder {
		public MetricReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

	public static class ProblemReportFilter extends com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		// view is show the summary,detail show the thread info
		private String m_type;

		public ProblemReportFilter(String ipAddress, String threadId, String type) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_ipAddress = ipAddress;
			m_type = type;
		}

		@Override
		public void visitDuration(com.dianping.cat.consumer.problem.model.entity.Duration duration) {
			if ("view".equals(m_type)) {
				super.visitDuration(duration);
			} else if (!"graph".equals(m_type)) {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			if (m_ipAddress == null) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
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
			} else if (!"view".equals(m_type)) {
				super.visitThread(thread);
			}
		}
	}

	public static class StateReportFilter extends com.dianping.cat.consumer.state.model.transform.DefaultXmlBuilder {
		public StateReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

	public static class TopReportFilter extends com.dianping.cat.consumer.top.model.transform.DefaultXmlBuilder {
		public TopReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

	public static class TransactionReportFilter extends
	      com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public TransactionReportFilter(String type, String name, String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
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
				if (m_ipAddress == null || m_ipAddress.equals(Constants.ALL)) {
					super.visitMachine(machine);
				} else if (machine.getIp().equals(m_ipAddress)) {
					super.visitMachine(machine);
				}
			}
		}

		@Override
		public void visitName(TransactionName name) {
			if (m_type != null) {
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
			} else if (type.getId().equals(m_type)) {
				super.visitType(type);
			}
		}
	}

}
