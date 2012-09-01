package com.dianping.cat.data.transaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.SAXException;

import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.index.Pair;
import com.dianping.bee.engine.spi.index.RangeType;
import com.dianping.bee.engine.spi.row.RowContext;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionIndexer implements Index<Pair<String, String>> {
	@Override
	public void queryById(RowContext ctx, Pair<String, String> pair) throws Exception {
		pair = new Pair<String, String>("", "Cat");

		TransactionReport report = getHourlyReport(pair.getValue(), pair.getKey());
		Machine machine = report.getMachines().get("All");

		for (TransactionType type : machine.getTypes().values()) {
			if (type.getNames().isEmpty()) {
				applyRow(ctx, report, type, null);
			} else {
				for (TransactionName name : type.getNames().values()) {
					applyRow(ctx, report, type, name);
				}
			}
		}
	}

	private void applyRow(RowContext ctx, TransactionReport report, TransactionType type, TransactionName name) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			TransactionColumn column = ctx.getColumn(i);

			switch (column) {
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
			case Failures:
				ctx.setColumnValue(i, name != null ? name.getFailCount() : type.getFailCount());
				break;
			case SumDuration:
				ctx.setColumnValue(i, name != null ? name.getSum() : type.getSum());
				break;
			case Line95:
				ctx.setColumnValue(i, name != null ? name.getLine95Value() : type.getLine95Value());
				break;
			default:
				// TODO more here
			}
		}

		ctx.apply();
	}

	@Override
	public void queryByIds(RowContext ctx, Pair<String, String>[] pairs) throws Exception {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public void queryByRange(RowContext ctx, Pair<String, String> start, Pair<String, String> end, RangeType rangeType)
	      throws Exception {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	private TransactionReport getHourlyReport(String domain, String date) throws IOException, SAXException {
		URL url = new URL(String.format("http://localhost:2281/cat/r/t?domain=%s&date=%s&xml=true", domain, date));
		InputStream in = url.openStream();

		try {
			TransactionReport report = DefaultSaxParser.parse(in);

			return report;
		} finally {
			in.close();
		}
	}
}
