package com.dianping.cat.report.page.transaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.StatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
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

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_service;

	@Inject
	private GraphBuilder m_builder;

	private Map<Integer, Integer> m_map = new HashMap<Integer, Integer>();

	private StatisticsComputer m_computer = new StatisticsComputer();

	private TransactionName getTransactionName(Payload payload) {
		String domain = payload.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType())//
		      .setProperty("name", payload.getName());
		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();
		TransactionType t = report.findType(type);

		if (t != null) {
			TransactionName n = t.findName(name);

			if (n != null) {
				n.accept(m_computer);
			}

			return n;
		}

		Cat.getManager().getThreadLocalMessageTree();
		
		return null;
	}

	private TransactionReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType());

		if (m_service.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_service.invoke(request);
			TransactionReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "t")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "t")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.TRANSACTION);
		model.setDisplayDomain(payload.getDomain());
		
		switch (payload.getAction()) {
		case VIEW:
			showReport(model, payload);
			break;
		case GRAPHS:
			showGraphs(model, payload);
			break;
		}

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
		final TransactionName name = getTransactionName(payload);

		if (name == null) {
			return;
		}

		String graph1 = m_builder.build(new DurationPayload("Duration Distribution", "Duration (ms)", "Count", name));
		String graph2 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", name));
		String graph3 = m_builder.build(new AverageTimePayload("Average Duration Over Time", "Time (min)",
		      "Average Duration (ms)", name));
		String graph4 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", name));

		model.setGraph1(graph1);
		model.setGraph2(graph2);
		model.setGraph3(graph3);
		model.setGraph4(graph4);
	}

	private void showReport(Model model, Payload payload) {
		try {
			TransactionReport report = getReport(payload);

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
		private final TransactionName m_name;

		public AbstractPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
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

		protected TransactionName getTransactionName() {
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

	final class AverageTimePayload extends AbstractPayload {
		public AverageTimePayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetY() {
			return getDisplayHeight() + 20;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getTransactionName().getRanges()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getAvg();
			}

			return values;
		}
	}

	final class DurationPayload extends AbstractPayload {
		public DurationPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public String getAxisXLabel(int index) {
			if (index == 0) {
				return "0";
			}

			int k = 1;

			for (int i = 1; i < index; i++) {
				k <<= 1;
			}

			return String.valueOf(k);
		}

		@Override
		public boolean isAxisXLabelRotated() {
			return true;
		}

		@Override
		public boolean isAxisXLabelSkipped() {
			return false;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[17];

			for (Duration duration : getTransactionName().getDurations()) {
				int d = duration.getValue();
				Integer k = m_map.get(d);

				if (k != null) {
					values[k] += duration.getCount();
				}
			}

			return values;
		}
	}

	final class FailurePayload extends AbstractPayload {
		public FailurePayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		public int getOffsetY() {
			return getDisplayHeight() + 20;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getTransactionName().getRanges()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getFails();
			}

			return values;
		}
	}

	final class HitPayload extends AbstractPayload {
		public HitPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getTransactionName().getRanges()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getCount();
			}

			return values;
		}
	}
}
