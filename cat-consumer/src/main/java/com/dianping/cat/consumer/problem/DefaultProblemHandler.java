package com.dianping.cat.consumer.problem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultProblemHandler extends ProblemHandler {
	public static final String ID = "default-problem";

	@Inject
	private Set<String> m_errorTypes;

	@Inject
	private Set<String> m_failureTypes;

	@Override
	public void handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String type = message.getType();

			//TODO remove me
			if (!"ABTest".equals(type)) {
				processTransaction(machine, (Transaction) message, tree);
			}
		} else if (message instanceof Event) {
			processEvent(machine, (Event) message, tree);
		} else if (message instanceof Heartbeat) {
			processHeartbeat(machine, (Heartbeat) message, tree);
		}
	}

	private void processEvent(Machine machine, Event message, MessageTree tree) {
		if (!message.getStatus().equals(Message.SUCCESS) && m_errorTypes.contains(message.getType())) {
			String type = ProblemType.ERROR.getName();
			String status = message.getName();

			Entry entry = findOrCreateEntry(machine, type, status);
			updateEntry(tree, entry, 0);
		}
	}

	private void processHeartbeat(Machine machine, Heartbeat heartbeat, MessageTree tree) {
		String type = heartbeat.getType().toLowerCase();

		if ("heartbeat".equals(type)) {
			String status = heartbeat.getName();
			Entry entry = findOrCreateEntry(machine, type, status);

			updateEntry(tree, entry, 0);
		}
	}

	private void processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		String transactionStatus = transaction.getStatus();

		if (!transactionStatus.equals(Transaction.SUCCESS)) {
			String type = transaction.getType();
			String status = "";

			if (m_failureTypes.contains(type)) {
				type = transaction.getType();
				// make it march for alarm
				if (type.equals("PigeonCall") || type.equals("Call")) {
					type = "call";
				}
				status = transaction.getName();
			} else {
				type = ProblemType.FAILURE.getName();
				status = transaction.getType() + ":" + transaction.getName();
			}

			Entry entry = findOrCreateEntry(machine, type, status);
			updateEntry(tree, entry, 0);
		}

		List<Message> children = transaction.getChildren();

		for (Message message : children) {
			if (message instanceof Transaction) {
				processTransaction(machine, (Transaction) message, tree);
			} else if (message instanceof Event) {
				processEvent(machine, (Event) message, tree);
			} else if (message instanceof Heartbeat) {
				processHeartbeat(machine, (Heartbeat) message, tree);
			}
		}
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}

	public void setFailureType(String type) {
		m_failureTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}