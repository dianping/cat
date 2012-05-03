package com.dianping.cat.job.url;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class UrlJobMapper extends Mapper<Object, MessageTreeWritable, Text, UrlValue> {

	public static final String DEFAULT_DOMAIN = "NoDomain";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyMMddHH:mm:ss:SSS");

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
		String name = transaction.getName();
		if (type.equals("URL") && (name.equalsIgnoreCase("/deallist.bin"))) {
			UrlValue value = new UrlValue().setValue(transaction.getDurationInMillis()).setDate(
			      converDate(tree.getMessage().getTimestamp()));
			context.write(new Text(name), value);
		}
	}

	private Text converDate(long time) {
		return new Text(sdf.format(new Date(time)));
	}

}