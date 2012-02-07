package com.dianping.cat.report.page.ip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.Period;
import com.dianping.cat.consumer.ip.model.transform.BaseVisitor;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "ip")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "ip")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.IP);

		IpAnalyzer analyzer = (IpAnalyzer) m_consumer.getCurrentAnalyzer("ip");
		IpReport report;

		if (analyzer != null) {
			Payload payload = ctx.getPayload();
			String domain = payload.getDomain();
			List<String> domains = analyzer.getDomains();

			if (domain == null && domains.size() > 0) {
				domain = domains.get(0);
				payload.setDomain(domain);
			}

			report = analyzer.generate(domain);
			model.setDomains(domains);
		} else {
			report = new IpReport();
		}

		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		Map<String, DisplayModel> models = new HashMap<String, DisplayModel>();
		DisplayModelBuilder builder = new DisplayModelBuilder(models, minute);

		report.accept(builder); // prepare display model

		List<DisplayModel> displayModels = new ArrayList<DisplayModel>(models.values());

		Collections.sort(displayModels, new Comparator<DisplayModel>() {
			@Override
			public int compare(DisplayModel m1, DisplayModel m2) {
				return m2.getLastFifteen() - m1.getLastFifteen(); // desc
			}
		});

		model.setReport(report);
		model.setDisplayModels(displayModels);
		m_jspViewer.view(ctx, model);
	}

	static class DisplayModelBuilder extends BaseVisitor {
		private int m_minute;

		private Map<String, DisplayModel> m_models;

		private Period m_period;

		public DisplayModelBuilder(Map<String, DisplayModel> models, int minute) {
			m_models = models;
			m_minute = minute;
		}

		@Override
		public void visitIp(Ip ip) {
			String address = ip.getAddress();
			DisplayModel model = m_models.get(address);

			if (model == null) {
				model = new DisplayModel(address);
				m_models.put(address, model);
			}

			model.process(m_minute, m_period.getMinute(), ip.getCount());
		}

		@Override
		public void visitPeriod(Period period) {
			m_period = period;
			super.visitPeriod(period);
		}
	}
}
