package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.StatisticsComputer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.AbstractGraphPayload;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

/**
 * @author sean.wang
 * @since Feb 6, 2012
 */
public class Handler implements PageHandler<Context>, Initializable {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_service;

	@Inject
	private GraphBuilder m_builder;

	private Map<Integer, Integer> m_map = new HashMap<Integer, Integer>();

	private StatisticsComputer m_computer = new StatisticsComputer();

	private EventName getEventName(Payload payload) {
		String domain = payload.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType())//
		      .setProperty("name", payload.getName());
		ModelResponse<EventReport> response = m_service.invoke(request);
		EventReport report = response.getModel();
		EventType t = report.findType(type);

		if (t != null) {
			EventName n = t.findName(name);

			if (n != null) {
				n.accept(m_computer);
			}

			return n;
		}

		Cat.getManager().getThreadLocalMessageTree();

		return null;
	}

	private EventReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType());

		if (m_service.isEligable(request)) {
			ModelResponse<EventReport> response = m_service.invoke(request);
			EventReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "e")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "e")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		// init session
		HttpSession session = ctx.getHttpServletRequest().getSession();
		String sessionDomain = (String) session.getAttribute("domain");
		String sessionDate = (String) session.getAttribute("date");

		if (StringUtils.isEmpty(payload.getDomain()) && sessionDomain != null) {
			payload.setDomain(sessionDomain);
		}
		if (payload.getRealDate() == 0 && sessionDate != null) {
			payload.setDate(sessionDate);
		}

		model.setAction(payload.getAction());
		model.setPage(ReportPage.EVENT);
		model.setDisplayDomain(payload.getDomain());

		switch (payload.getAction()) {
		case VIEW:
			showReport(model, payload);
			break;
		case GRAPHS:
			showGraphs(model, payload);
			break;
		}
		// reset session
		session.setAttribute("domain", model.getDomain());
		session.setAttribute("date", model.getDate());

		m_jspViewer.view(ctx, model);
	}

	@Override
	public void initialize() throws InitializationException {
		int k = 1;

		m_map.put(0, 0);

		for (int i = 0; i < 17; i++) {
			m_map.put(k, i);
			k <<= 1;
		}
	}

	private void showGraphs(Model model, Payload payload) {
		final EventName name = getEventName(payload);

		if (name == null) {
			return;
		}

		String graph1 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", name));
		String graph2 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", name));

		model.setGraph1(graph1);
		model.setGraph2(graph2);
	}

	private void showReport(Model model, Payload payload) {
		try {
			EventReport report = getReport(payload);
			if (report == null) {
				return;
			}
			
			if (payload.getPeriod().isFuture()) {
				model.setLongDate(payload.getCurrentDate());
			} else {
				model.setLongDate(payload.getDate());
			}

			report.accept(m_computer);
			model.setReport(report);
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
			model.setException(e);
		}
	}

	abstract class AbstractPayload extends AbstractGraphPayload {
		private final EventName m_name;

		public AbstractPayload(String title, String axisXLabel, String axisYLabel, EventName name) {
			super(title, axisXLabel, axisYLabel);

			m_name = name;
		}

		@Override
		public String getAxisXLabel(int index) {
			return String.valueOf(index * 5);
		}

		@Override
		public int getDisplayHeight() {
			return (int) (super.getDisplayHeight() * 0.7);
		}

		@Override
		public int getDisplayWidth() {
			return (int) (super.getDisplayWidth() * 0.7);
		}

		@Override
		public String getIdPrefix() {
			return m_name.getId() + "-" + super.getIdPrefix();
		}

		protected EventName getEventName() {
			return m_name;
		}

		@Override
		public int getWidth() {
			return super.getWidth() + 120;
		}

		@Override
		public boolean isStandalone() {
			return false;
		}
	}

	final class FailurePayload extends AbstractPayload {
		public FailurePayload(String title, String axisXLabel, String axisYLabel, EventName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getEventName().getRanges()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getFails();
			}

			return values;
		}
	}

	final class HitPayload extends AbstractPayload {
		public HitPayload(String title, String axisXLabel, String axisYLabel, EventName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getEventName().getRanges()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getCount();
			}

			return values;
		}
	}
}
