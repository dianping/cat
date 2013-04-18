package com.dianping.cat.consumer.problem.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

public class DefaultProblemHandler extends Handler {
	@Inject
	private Set<String> m_errorTypes;

	@Inject
	private Set<String> m_failureTypes;

	@Override
	public int handle(Machine machine, MessageTree tree) {
		int count = 0;
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			count += processTransaction(machine, (Transaction) message, tree);
		} else if (message instanceof Event) {
			count += processEvent(machine, (Event) message, tree);
		} else if (message instanceof Heartbeat) {
			count += processHeartbeat(machine, (Heartbeat) message, tree);
		}

		return count;
	}

	private int processEvent(Machine machine, Event message, MessageTree tree) {
		int count = 0;

		if (!message.getStatus().equals(Message.SUCCESS) && m_errorTypes.contains(message.getType())) {
			String type = ProblemType.ERROR.getName();
			String status = message.getName();

			Entry entry = findOrCreateEntry(machine, type, status);
			updateEntry(tree, entry, 0);

			count++;
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
				type = transaction.getType();
				//make it march for alarm
				if (type.equals("PigeonCall")) {
					type = "call";
				}
				status = transaction.getName();
			} else {
				type = ProblemType.FAILURE.getName();
				status = transaction.getType() + ":" + transaction.getName();
			}

			Entry entry = findOrCreateEntry(machine, type, status);
			updateEntry(tree, entry, 0);

			count++;
		}

		List<Message> children = transaction.getChildren();

		for (Message message : children) {
			if (message instanceof Transaction) {
				count += processTransaction(machine, (Transaction) message, tree);
			} else if (message instanceof Event) {
				count += processEvent(machine, (Event) message, tree);
			} else if (message instanceof Heartbeat) {
				count += processHeartbeat(machine, (Heartbeat) message, tree);
			}
		}

		return count;
	}

	private int processHeartbeat(Machine machine, Heartbeat heartbeat, MessageTree tree) {
		String type = ProblemType.HEARTBEAT.getName();
		String status = heartbeat.getName();
		Entry entry = findOrCreateEntry(machine, type, status);

		updateEntry(tree, entry, 0);
		return 1;
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}

	public void setFailureType(String type) {
		m_failureTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}