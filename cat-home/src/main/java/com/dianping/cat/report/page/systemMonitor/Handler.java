package com.dianping.cat.report.page.systemMonitor;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportPage;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "systemMonitor")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "systemMonitor")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		System.out.println(System.currentTimeMillis());
		switch (action) {
		case SYSTEM_API:
			String domain = payload.getDomain();
			String group = payload.getGroup();
			String type = payload.getType();
			String key = payload.getKey();
			long time = payload.getTimestamp();

			if (time < TimeUtil.getCurrentHour().getTime()) {
				time = System.currentTimeMillis();
			}
			DefaultMetric defaultMetric = null;
			int count = payload.getCount();

			Transaction t = Cat.newTransaction("test", "test");
			if (count == 0) {
				count = 1;
			}
			if ("count".equalsIgnoreCase(type)) {
				Metric metric = Cat.getProducer().newMetric(group, key);

				defaultMetric = (DefaultMetric) metric;
				defaultMetric.setStatus("C");
				defaultMetric.addData(String.valueOf(count));
			} else if ("avg".equalsIgnoreCase(type)) {
				Metric metric = Cat.getProducer().newMetric(group, key);

				defaultMetric = (DefaultMetric) metric;
				defaultMetric.setStatus("T");
				defaultMetric.addData(String.format("%.2f", payload.getAvg()));
			} else if ("sum".equalsIgnoreCase(type)) {
				Metric metric = Cat.getProducer().newMetric(group, key);

				defaultMetric = (DefaultMetric) metric;
				defaultMetric.setStatus("S,C");
				defaultMetric.addData(String.format("%.2f,%s", payload.getSum(), count));
			}
			if (defaultMetric != null) {
				defaultMetric.setTimestamp(time);
			}
			t.complete();
			DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();

			tree.setDomain(domain);
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				((DefaultTransaction) message).setTimestamp(time);
			}

			System.out.println(tree);
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.SYSTEMMONITOR);
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
