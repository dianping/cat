package com.dianping.cat.data.transaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.SAXException;

import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionIndexer implements Index {
	private void applyRow(RowContext ctx, TransactionReport report, Machine machine, TransactionType type, TransactionName name) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			TransactionColumn column = ctx.getColumn(i);

			switch (column) {
			case Ip:
				ctx.setColumnValue(i, machine.getIp());
				break;
			case Type:
				ctx.setColumnValue(i, type.getId());
				break;
			case Name:
				ctx.setColumnValue(i, name == null ? null : name.getId());
				break;
			case Domain:
				ctx.setColumnValue(i, report.getDomain());
				break;
			case StartTime:
				ctx.setColumnValue(i, report.getStartTime());
				break;
			case MinDuration:
				ctx.setColumnValue(i, name != null ? name.getMin() : type.getMin());
				break;
			case MaxDuration:
				ctx.setColumnValue(i, name != null ? name.getMax() : type.getMax());
				break;
			case SampleMessage:
				String url;

				if (name != null) {
					if (name.getFailMessageUrl() != null) {
						url = name.getFailMessageUrl();
					} else {
						url = name.getSuccessMessageUrl();
					}
				} else {
					if (type.getFailMessageUrl() != null) {
						url = type.getFailMessageUrl();
					} else {
						url = type.getSuccessMessageUrl();
					}
				}
				ctx.setColumnValue(i, url);
				break;
			case TotalCount:
				ctx.setColumnValue(i, name != null ? name.getTotalCount() : type.getTotalCount());
				break;
			case FailCount:
				ctx.setColumnValue(i, name != null ? name.getFailCount() : type.getFailCount());
				break;
			case AvgDuration:
				ctx.setColumnValue(i, name != null ? name.getAvg() : type.getAvg());
				break;
			case StdDuration:
				ctx.setColumnValue(i, name != null ? name.getStd() : type.getStd());
				break;
			case Line95:
				ctx.setColumnValue(i, name != null ? name.getLine95Value() : type.getLine95Value());
				break;
			case TPS:
				ctx.setColumnValue(i, name != null ? name.getTps() : type.getTps());
				break;
			default:
				// TODO more here
			}
		}

		ctx.apply();
	}

	private TransactionReport getHourlyReport(RowContext ctx) throws IOException, SAXException {
		String domain = ctx.getFirstAttribute("domain", "Cat");
		String date = ctx.getFirstAttribute("starttime", "");
		String ip = ctx.getFirstAttribute("ip", "All");
		URL url = new URL(String.format("http://localhost:2281/cat/r/t?domain=%s&date=%s&ip=%s&xml=true", domain, date, ip));
		InputStream in = url.openStream();

		try {
			TransactionReport report = DefaultSaxParser.parse(in);

			return report;
		} finally {
			in.close();
		}
	}

	@Override
	public void query(RowContext ctx) throws Exception {
		TransactionReport report = getHourlyReport(ctx);
		Machine machine = report.getMachines().values().iterator().next();

		for (TransactionType type : machine.getTypes().values()) {
			if (type.getNames().isEmpty()) {
				applyRow(ctx, report, machine, type, null);
			} else {
				for (TransactionName name : type.getNames().values()) {
					applyRow(ctx, report, machine, type, name);
				}
			}
		}
	}
}
