package com.dianping.cat.report.page.monitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

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
import com.site.lookup.util.StringUtils;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	private Gson m_gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

	public HttpStatus checkPars(Payload payload) {
		StringBuilder sb = new StringBuilder();
		String domain = payload.getDomain();
		String group = payload.getGroup();
		String key = payload.getKey();
		HttpStatus httpStatus = new HttpStatus();
		boolean error = false;

		if (StringUtils.isEmpty(domain)) {
			sb.append("domain ");
			error = true;
		}
		if (StringUtils.isEmpty(group)) {
			sb.append("group ");
			error = true;
		}
		if (StringUtils.isEmpty(key)) {
			sb.append("key ");
			error = true;
		}
		if (error) {
			httpStatus.setErrorMsg("invalid field: " + sb.toString());
			httpStatus.setStatusCode(String.valueOf(HttpStatus.FAIL));
		} else {
			httpStatus.setStatusCode(String.valueOf(HttpStatus.SUCCESS));
		}

		return httpStatus;
	}

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
		HttpStatus status = checkPars(payload);

		model.setPage(ReportPage.MONITOR);
		model.setStatus(m_gson.toJson(status));
		if (status.getStatusCode().equals(String.valueOf(HttpStatus.SUCCESS))) {
			String domain = payload.getDomain();
			String group = payload.getGroup();
			String key = payload.getKey();
			long time = payload.getTimestamp();
			int count = payload.getCount();
			boolean invalid = time < TimeUtil.getCurrentHour().getTime();

			if (invalid) {
				Cat.logError(new RuntimeException("Error timestamp in metric api, time"
				      + new SimpleDateFormat("yyyy-MM-dd HH:ss").format(new Date(time)) + payload.toString()));

				time = System.currentTimeMillis();
			}

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
			DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();
			tree.setDomain(domain);

			Message message = tree.getMessage();
			if (message instanceof Transaction) {
				((DefaultTransaction) message).setTimestamp(time);
			}
		}
		model.setAction(action);
		model.setPage(ReportPage.MONITOR);
		m_jspViewer.view(ctx, model);
	}

}
