package com.dianping.cat.consumer.problem;

import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.MessageTree;

public abstract class ProblemHandler {
	public static final int MAX_LOG_SIZE = 60;

	protected Entity findOrCreateEntity(Machine machine, String type, String status) {
		String id = type + ":" + status;
		Entity entity = machine.findOrCreateEntity(id);
		entity.setType(type).setStatus(status);

		return entity;
	}

	protected int getSegmentByMessage(MessageTree tree) {
		Message message = tree.getMessage();
		long current = message.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));

		return min;
	}

	public abstract void handle(Machine machine, MessageTree tree);

	public void updateEntity(MessageTree tree, Entity entity, int value) {
		Duration duration = entity.findOrCreateDuration(value);
		List<String> messages = duration.getMessages();

		duration.incCount();
		if (messages.size() < MAX_LOG_SIZE) {
			messages.add(tree.getMessageId());
		}

		// make problem thread id = thread group name, make report small
		JavaThread thread = entity.findOrCreateThread(tree.getThreadGroupName());

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
}