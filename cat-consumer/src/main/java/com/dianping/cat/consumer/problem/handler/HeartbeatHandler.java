package com.dianping.cat.consumer.problem.handler;

import java.util.List;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class HeartbeatHandler extends Handler {

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count += processTransaction(machine, transaction, tree);
		} else if (message instanceof Heartbeat) {
			count += processHeartbeat(machine, (Heartbeat) message, tree);
		}
		return count;
	}

	private int processHeartbeat(Machine machine, Heartbeat heartbeat, MessageTree tree) {
		String type = ProblemType.HEARTBEAT.getName();
		String status = heartbeat.getName();
		Entry entry = findOrCreatEntry(machine, type, status);

		updateEntry(tree, entry, 0);
		return 1;
	}

	private int processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		List<Message> children = transaction.getChildren();
		int count = 0;

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(machine, temp, tree);
			} else if (message instanceof Heartbeat) {
				count += processHeartbeat(machine, (Heartbeat) message, tree);
			}
		}

		return count;
	}
}