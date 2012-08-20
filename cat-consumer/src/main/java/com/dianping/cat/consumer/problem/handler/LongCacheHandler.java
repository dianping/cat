package com.dianping.cat.consumer.problem.handler;

import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class LongCacheHandler extends Handler {
	@Inject
	private ServerConfigManager m_configManager;

	private int m_defaultCacheThreshold = 10;

	private int processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		int count = 0;

		if (transaction instanceof Transaction && transaction.getType().startsWith("Cache.")) {

			long duration = ((Transaction) transaction).getDurationInMillis();

			if (duration > m_defaultCacheThreshold) {
				String type = ProblemType.LONG_CACHE.getName();
				String status = transaction.getName();

				Entry entry = findOrCreatEntry(machine, type, status);
				updateEntry(tree, entry, 0);
				count++;
			}
		}
		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(machine, temp, tree);
			}
		}
		return count;
	}

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count = processTransaction(machine, transaction, tree);
		}

		return count;
	}
}