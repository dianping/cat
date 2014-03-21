package com.dianping.cat.report.page.systemMonitor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	private Gson m_gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

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

		HttpStatus status = new HttpStatus();
		String statusStr = null;
		Set<String> mustPara = new HashSet<String>();
		String domain = payload.getDomain();
		String group = payload.getGroup();
		String key = payload.getKey();
		
		if (domain == null) {
			mustPara.add("domain");
		}
		if (group == null) {
			mustPara.add("group");
		}
		if (key == null) {
			mustPara.add("key");
		}
		if (mustPara.size() > 0) {
			status.setStatusCode(String.valueOf(-1));
			status.setErrorMsg("Unknown [ " + StringUtils.join(mustPara, ",") + " ] name!");
			statusStr = m_gson.toJson(status);
			model.setAction(action);
			model.setStatus(statusStr);
			model.setPage(ReportPage.SYSTEMMONITOR);

			if (!ctx.isProcessStopped()) {
				m_jspViewer.view(ctx, model);
			}
			return;
		}

		long time = payload.getTimestamp() < TimeUtil.getCurrentHour().getTime() ? 
							System.currentTimeMillis() : payload.getTimestamp();

		int count = payload.getCount() == 0 ? 1 : payload.getCount();

		Transaction t = Cat.newTransaction("SystemMetric", action.toString());

		Metric metric = Cat.getProducer().newMetric(group, key);
		DefaultMetric defaultMetric = (DefaultMetric) metric;
		if (defaultMetric != null) {
			defaultMetric.setTimestamp(time);
		}

		switch (action) {
		case COUNT_API:
			defaultMetric.setStatus("C");
			defaultMetric.addData(String.valueOf(count));
			break;
		case AVG_API:
			defaultMetric.setStatus("T");
			defaultMetric.addData(String.format("%.2f", payload.getAvg()));
			break;
		case SUM_API:
			defaultMetric.setStatus("S,C");
			defaultMetric.addData(String.format("%s,%.2f", count, payload.getSum()));
			break;
		default:
			throw new RuntimeException("Unknown action: " + action);
		}

		t.complete();
		
		DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();
		tree.setDomain(domain);
		
		Message message = tree.getMessage();
		if (message instanceof Transaction) {
			((DefaultTransaction) message).setTimestamp(time);
		}

		System.out.println(tree);

		status.setStatusCode(String.valueOf(0));
		statusStr = m_gson.toJson(status);
		model.setStatus(statusStr);
		model.setAction(action);
		model.setPage(ReportPage.SYSTEMMONITOR);
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
