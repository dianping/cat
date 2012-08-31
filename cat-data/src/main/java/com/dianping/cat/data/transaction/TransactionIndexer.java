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
		if (pair == null) {
			pair = new Pair<String, String>("", "Cat");
		}

		TransactionReport report = getHourlyReport(pair.getValue(), pair.getKey());
		Machine machine = report.getMachines().get("All");

		for (TransactionType type : machine.getTypes().values()) {
			if (type.getNames().isEmpty()) {
				applyRow(ctx, type, null);
			} else {
				for (TransactionName name : type.getNames().values()) {
					applyRow(ctx, type, name);
				}
			}
		}
	}

	private void applyRow(RowContext ctx, TransactionType type, TransactionName name) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			TransactionColumn column = ctx.getColumn(i);

			switch (column) {
			case Type:
				ctx.setColumnValue(i, type.getId());
				break;
			case Name:
				ctx.setColumnValue(i, name == null ? null : name.getId());
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
