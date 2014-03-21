package com.dianping.cat.report.page.monitor;

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
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "monitor")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "monitor")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		Pair<Boolean,String> result = getStatus(payload);
		
		if(!result.getKey()){
			model.setStatus(result.getValue());
			model.setAction(action);
			model.setPage(ReportPage.MONITOR);
			if (!ctx.isProcessStopped()) {
				m_jspViewer.view(ctx, model);
			}
			return;
		}
		
		String domain = payload.getDomain();
		String group = payload.getGroup();
		String key = payload.getKey();
		
		long tmpTime = payload.getTimestamp();
		long time = tmpTime < TimeUtil.getCurrentHour().getTime() ? System.currentTimeMillis() : tmpTime;
		
		int tmpCount = payload.getCount();
		int count = tmpCount == 0 ? 1 : tmpCount;

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

		model.setStatus(result.getValue());
		model.setAction(action);
		model.setPage(ReportPage.MONITOR);
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
	public Pair<Boolean,String> getStatus(Payload payload) {
		String  status = null;
		StringBuilder sb = null;
		String domain = payload.getDomain();
		String group = payload.getGroup();
		String key = payload.getKey();
		HttpStatus httpStatus = new HttpStatus();
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		
		if(domain == null || group == null || key == null) {
			sb = new StringBuilder("Unknown [ ");
			if (domain == null) {
				sb.append("domain ");
			}
			if (group == null) {
				sb.append("group ");
			}
			if (key == null) {
				sb.append("key ");
			}
			sb.append("] name!");
			httpStatus.setStatusCode(String.valueOf(-1));
			httpStatus.setErrorMsg(sb.toString());
		}else{
			httpStatus.setStatusCode(String.valueOf(0));
			httpStatus.setErrorMsg(null);
		}
		status = gson.toJson(httpStatus);
		return new Pair<Boolean,String>(sb==null,status);
	}
}
