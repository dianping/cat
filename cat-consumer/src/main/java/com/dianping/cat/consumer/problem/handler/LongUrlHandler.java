package com.dianping.cat.consumer.problem.handler;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class LongUrlHandler implements Handler {
	@Inject
	private long m_threshold;

	@Override
	public int handle(Segment segment, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction t = (Transaction) message;
			long duration = t.getDuration();

			if (duration > m_threshold) {
				String messageId = tree.getMessageId();

				if (segment.findEntry(messageId) == null) {
					Entry entry = new Entry(messageId);

					entry.setStatus(message.getType() + ":" + message.getName());
					entry.setType(ProblemType.LONG_URL.getName());
					entry.setDuration((int) duration);

					segment.addEntry(entry);
				}

				count++;
			}
		}

		return count;
	}

	public void setThreshold(long threshold) {
		m_threshold = threshold;
	}
}