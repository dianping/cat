package com.dianping.cat.consumer.problem.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public class FailureHandler implements Handler {
	@Inject
	private Set<String> m_failureTypes;

	@Override
	public int handle(Segment segment, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count += processTransaction(segment, transaction, tree);
		}

		return count;
	}

	private int processTransaction(Segment segment, Transaction transaction, MessageTree tree) {
		int count = 0;
		String status = transaction.getStatus();
		if (!status.equals(Transaction.SUCCESS)) {
			Entry entry = new Entry();
			entry.setMessageId(tree.getMessageId());
			
			String type = transaction.getType();
			if (m_failureTypes.contains(type)) {
				entry.setType(transaction.getType().toLowerCase());
				entry.setStatus(transaction.getName());
			} else {
				entry.setType(ProblemType.FAILURE.getName());
				entry.setStatus(transaction.getType() + ":" + transaction.getName());
			}

			entry.setDuration((int) transaction.getDurationInMillis());
			segment.addEntry(entry);

			count++;
		}

		List<Message> children = transaction.getChildren();

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(segment, temp, tree);
			}
		}

		return count;
	}

	public void setFailureType(String type) {
		m_failureTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}