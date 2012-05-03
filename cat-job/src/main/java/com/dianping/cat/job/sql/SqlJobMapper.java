package com.dianping.cat.job.sql;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class SqlJobMapper extends Mapper<Object, MessageTreeWritable, UrlStatementKey, UrlStatementValue> {

	public static final String DEFAULT_DOMAIN = "NoDomain";

	private void handle(Context context, MessageTree tree) throws IOException, InterruptedException {
		Message message = tree.getMessage();
		String domain = tree.getDomain();
		if (domain == null || domain.length() == 0) {
			domain = DEFAULT_DOMAIN;
		}

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			processTransaction(context, transaction, tree, domain);
		}
	}

	public void map(Object key, MessageTreeWritable value, Context context) throws IOException, InterruptedException {
		MessageTree message = value.get();
		handle(context, message);
	}

	private void processTransaction(Context context, Transaction transaction, MessageTree tree, String domain)
	      throws IOException, InterruptedException {
		String type = transaction.getType();

		if (type.equals("SQL")) {
			UrlStatementKey statementKey = new UrlStatementKey();
			String name = transaction.getName();
			String statement = transaction.getData().toString();
			long duration = transaction.getDurationInMillis();
			int flag = 0;

			statementKey.setDomain(new Text(domain)).setName(new Text(name)).setStatement(new Text(statement));
			if (!transaction.getStatus().equals(Transaction.SUCCESS)) {
				flag = 1;
			}
			long transactionTime = transaction.getTimestamp();
			long hour = transactionTime - transactionTime % (60 * 60 * 1000);
			int minute = (int) Math.floor((double) (transactionTime - hour) /(60* 1000.0));
			UrlStatementValue result = new UrlStatementValue(flag, duration, tree.getMessageId(), minute);
			context.write(statementKey, result);
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				processTransaction(context, temp, tree, domain);
			}
		}
	}
}