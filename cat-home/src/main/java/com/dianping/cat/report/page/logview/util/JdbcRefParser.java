package com.dianping.cat.report.page.logview.util;

import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;

public class JdbcRefParser {

	public static void hideJdbcRef(MessageTree tree) {
		List<Event> events = tree.getEvents();

		if (events != null && events.size() > 0) {
			for (Event event : events) {
				final String type = event.getType();

				if (type.equals("SQL.Database")) {
					((DefaultEvent) (event)).setData("");
				}
			}
		}

		List<Transaction> transactions = tree.getTransactions();

		if (transactions != null && transactions.size() > 0) {
			for (Transaction t : transactions) {
				final String type = t.getType();

				if ("SQL.Conn".equals(type)) {
					((DefaultTransaction) (t)).setName("${SQL.Conn}");
				}
			}
		}
	}

}
