package com.dianping.cat.report.page.monitor;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.config.ProductLineConfig;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.alert.MetricType;

public class Handler implements PageHandler<Context> {

	@Inject
	private JsonBuilder m_builder;

	private void buildBatchMetric(String content) {
		if (content != null) {
			String[] lines = content.split("\n");

			// group, domain, key, type, time, value
			for (String line : lines) {
				if (StringUtils.isNotEmpty(line)) {
					String[] tabs = line.split("\t");

					if (tabs.length == 6) {
						try {
							String group = tabs[0];
							String domain = tabs[1];
							String key = tabs[2];
							String type = tabs[3];
							long time = Long.parseLong(tabs[4]);
							double value = Double.parseDouble(tabs[5]);
							buildMetric(group, domain, key, type, time, value);
						} catch (Exception e) {
							Cat.logError("Unrecognized batch data: " + line, e);
						}
					} else {
						Cat.logError(new RuntimeException("Unrecognized batch data: " + line));
					}
				}
			}
		}
	}

	private Metric buildMetric(Payload payload, String type, double value) {
		String group = payload.getGroup();
		String domain = payload.getDomain();
		String key = payload.getKey();
		long time = payload.getTimestamp();

		if (time <= 0) {
			time = System.currentTimeMillis();
		}
		return buildMetric(group, domain, key, type, time, value);
	}

	private Metric buildMetric(String group, String domain, String key, String type, long time, double value) {
		Metric metric = Cat.getProducer().newMetric(group, key);
		DefaultMetric defaultMetric = (DefaultMetric) metric;

		defaultMetric.setTimestamp(time);
		if (MetricType.SUM.name().equalsIgnoreCase(type)) {
			defaultMetric.setStatus("S,C");
			defaultMetric.addData(String.format("%s,%.2f", 1, value));
		} else if (MetricType.AVG.name().equalsIgnoreCase(type)) {
			defaultMetric.setStatus("T");
			defaultMetric.addData(String.format("%.2f", value));
		} else if (MetricType.COUNT.name().equalsIgnoreCase(type)) {
			defaultMetric.setStatus("C");
			defaultMetric.addData(String.valueOf(value));
		} else {
			throw new RuntimeException("Error type in metric api, type: " + type);
		}

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			((DefaultTransaction) message).setTimestamp(time);
		}
		if (isGenericMetric(group)) {
			tree.setDomain(domain);
		}
		metric.complete();
		return defaultMetric;
	}

	public HttpStatus checkPars(Payload payload) {
		StringBuilder sb = new StringBuilder();
		String domain = payload.getDomain();
		String group = payload.getGroup();
		String key = payload.getKey();
		Action action = payload.getAction();
		HttpStatus httpStatus = new HttpStatus();
		boolean error = false;

		if (!Action.BATCH_API.equals(action)) {
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
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		HttpStatus status = checkPars(payload);

		if (status.getStatusCode().equals(String.valueOf(HttpStatus.SUCCESS))) {
			switch (action) {
			case COUNT_API:
				buildMetric(payload, MetricType.COUNT.name(), payload.getCount());
				break;
			case AVG_API:
				buildMetric(payload, MetricType.AVG.name(), payload.getAvg());
				break;
			case SUM_API:
				buildMetric(payload, MetricType.SUM.name(), payload.getSum());
				break;
			case BATCH_API:
				buildBatchMetric(payload.getBatch());
				break;
			}
		}
		ctx.getHttpServletResponse().getWriter().write(m_builder.toJson(status));
	}

	private boolean isDatabase(String group) {
		return ProductLineConfig.DATABASE.isTypeOf(group);
	}

	private boolean isGenericMetric(String group) {
		return !isNetwork(group) && !isSystem(group) && !isDatabase(group);
	}

	private boolean isNetwork(String group) {
		return ProductLineConfig.NETWORK.isTypeOf(group);
	}

	private boolean isSystem(String group) {
		return ProductLineConfig.SYSTEM.isTypeOf(group);
	}

}
