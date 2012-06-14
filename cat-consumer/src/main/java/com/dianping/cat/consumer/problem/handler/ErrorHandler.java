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

public class ErrorHandler implements Handler {
	@Inject
	private Set<String> m_errorTypes;

	@Override
	public int handle(Segment segment, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count += processTransaction(segment, transaction, tree);
		} else {
			count += processMessage(segment, message, tree);
		}

		return count;
	}

	private int processMessage(Segment segment, Message message, MessageTree tree) {
		int count = 0;

		if (!message.getStatus().equals(Message.SUCCESS) && m_errorTypes.contains(message.getType())) {
			Entry entry = new Entry();
			entry.setMessageId(tree.getMessageId());

			entry.setStatus(message.getName());
			entry.setType(ProblemType.ERROR.getName());

			if (message instanceof Transaction) {
				long duration = ((Transaction) message).getDurationInMillis();

				entry.setDuration((int) duration);
			}

			segment.addEntry(entry);

			count++;
		}

		return count;
	}

	private int processTransaction(Segment segment, Transaction transaction, MessageTree tree) {
		List<Message> children = transaction.getChildren();
		int count = 0;

		count += processMessage(segment, transaction, tree);

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(segment, temp, tree);
			} else {
				count += processMessage(segment, message, tree);
			}
		}

		return count;
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}