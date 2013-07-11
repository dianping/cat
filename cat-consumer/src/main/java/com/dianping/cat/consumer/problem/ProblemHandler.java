package com.dianping.cat.consumer.problem;

import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.MessageTree;

public abstract class ProblemHandler {
	public static final int MAX_LOG_SIZE = 60;

	public abstract int handle(Machine machine, MessageTree tree);

	// TODO fix performance issue
	protected Entry findOrCreateEntry(Machine machine, String type, String status) {
		List<Entry> entries = machine.getEntries();

		for (Entry entry : entries) {
			if (entry.getType().equals(type) && entry.getStatus().equals(status)) {
				return entry;
			}
		}

		Entry entry = new Entry();

		entry.setStatus(status);
		entry.setType(type);
		entries.add(entry);
		return entry;
	}

	public void updateEntry(MessageTree tree, Entry entry, int value) {
		Duration duration = entry.findOrCreateDuration(value);
		List<String> messages = duration.getMessages();

		duration.incCount();
		if (messages.size() < MAX_LOG_SIZE) {
			messages.add(tree.getMessageId());
		}

		//make problem thread id = thread group name, make report small
		JavaThread thread = entry.findOrCreateThread(tree.getThreadGroupName());

		if (thread.getGroupName() == null) {
			thread.setGroupName(tree.getThreadGroupName());
		}
		if (thread.getName() == null) {
			thread.setName(tree.getThreadName());
		}

		Segment segment = thread.findOrCreateSegment(getSegmentByMessage(tree));
		List<String> segmentMessages = segment.getMessages();

		segment.incCount();
		if (segmentMessages.size() < MAX_LOG_SIZE) {
			segmentMessages.add(tree.getMessageId());
		}
	}

	protected int getSegmentByMessage(MessageTree tree) {
		Message message = tree.getMessage();
		long current = message.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));

		return min;
	}
}