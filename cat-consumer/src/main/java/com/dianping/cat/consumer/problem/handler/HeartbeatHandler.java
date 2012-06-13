package com.dianping.cat.consumer.problem.handler;

import java.util.List;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class HeartbeatHandler implements Handler {

	@Override
	public int handle(Segment segment, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count += processTransaction(segment, transaction, tree);
		} else if (message instanceof Heartbeat) {
			count += processHeartbeat(segment, (Heartbeat) message, tree);
		}
		return count;
	}

	private int processHeartbeat(Segment segment, Heartbeat heartbeat, MessageTree tree) {
		Entry entry = new Entry();
		entry.setMessageId(tree.getMessageId());
		entry.setStatus(heartbeat.getName());
		entry.setType(ProblemType.HEARTBEAT.getName());
		segment.addEntry(entry);
		return 1;
	}

	private int processTransaction(Segment segment, Transaction transaction, MessageTree tree) {
		List<Message> children = transaction.getChildren();
		int count = 0;

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(segment, temp, tree);
			} else if (message instanceof Heartbeat) {
				count += processHeartbeat(segment, (Heartbeat) message, tree);
			}
		}

		return count;
	}
}