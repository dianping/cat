package com.dianping.cat.consumer.problem.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public class ErrorHandler extends Handler {
	@Inject
	private Set<String> m_errorTypes;

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count += processTransaction(machine, transaction, tree);
		} else {
			count += processMessage(machine, message, tree);
		}

		return count;
	}

	private int processMessage(Machine machine, Message message, MessageTree tree) {
		int count = 0;

		if (!message.getStatus().equals(Message.SUCCESS) && m_errorTypes.contains(message.getType())) {
			String type = ProblemType.ERROR.getName();
			String status = message.getName();

			Entry entry = findOrCreatEntry(machine, type, status);
			updateEntry(tree, entry, 0);

			count++;
		}

		return count;
	}

	private int processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		List<Message> children = transaction.getChildren();
		int count = 0;

		count += processMessage(machine, transaction, tree);

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(machine, temp, tree);
			} else {
				count += processMessage(machine, message, tree);
			}
		}

		return count;
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}