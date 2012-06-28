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

public class FailureHandler extends Handler {
	@Inject
	private Set<String> m_failureTypes;

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count += processTransaction(machine, transaction, tree);
		}

		return count;
	}

	private int processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		int count = 0;
		String transactionStatus = transaction.getStatus();
		if (!transactionStatus.equals(Transaction.SUCCESS)) {
			String type = transaction.getType();
			String status = "";
			if (m_failureTypes.contains(type)) {
				type = transaction.getType().toLowerCase();
				status = transaction.getName();
			} else {
				type = ProblemType.FAILURE.getName();
				status = transaction.getType() + ":" + transaction.getName();
			}

			Entry entry = findOrCreatEntry(machine, type, status);
			updateEntry(tree, entry, 0);

			count++;
		}

		List<Message> children = transaction.getChildren();

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(machine, temp, tree);
			}
		}

		return count;
	}

	public void setFailureType(String type) {
		m_failureTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}