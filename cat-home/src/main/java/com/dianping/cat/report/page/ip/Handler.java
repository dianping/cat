package com.dianping.cat.report.page.ip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

			report = analyzer.generate(domain);
		} else {
			report = new IpReport();
		}

		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		List<DisplayModel> models = new ArrayList<DisplayModel>();
		DisplayModelBuilder builder = new DisplayModelBuilder(models, minute);

		report.accept(builder); // prepare display model

		Collections.sort(models, new Comparator<DisplayModel>() {
			@Override
			public int compare(DisplayModel m1, DisplayModel m2) {
				return m2.getLastFifteen() - m1.getLastFifteen(); // desc
			}
		});

		model.setReport(report);
		model.setDisplayModels(models);
		m_jspViewer.view(ctx, model);
	}

	static class DisplayModelBuilder extends BaseVisitor {
		private int m_minute;

		private List<DisplayModel> m_models;

		private DisplayModel m_model;

		public DisplayModelBuilder(List<DisplayModel> models, int minute) {
			m_models = models;
			m_minute = minute;
		}

		@Override
		public void visitIp(Ip ip) {
			m_model = new DisplayModel(ip.getAddress());
			m_models.add(m_model);

			super.visitIp(ip);
		}

		@Override
		public void visitPeriod(Period period) {
			m_model.process(m_minute, period.getMinute(), period.getValue());
		}
	}
}
