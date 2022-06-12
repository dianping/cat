package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.MessageContextHelper;

public class DefaultForkableTransaction extends AbstractMessage implements ForkableTransaction {
	private String m_rootMessageId;

	private String m_parentMessageId;

	private long m_durationInMicros;

	private List<Message> m_children = Collections.synchronizedList(new ArrayList<Message>());

	public DefaultForkableTransaction(String rootMessageId, String parentMessageId) {
		super("Forkable", Thread.currentThread().getName());

		m_rootMessageId = rootMessageId;
		m_parentMessageId = parentMessageId;

		m_durationInMicros = System.nanoTime() / 1000L;
		setStatus(Message.SUCCESS);
	}

	@Override
	public Transaction addChild(Message message) {
		m_children.add(message);
		return this;
	}

	@Override
	public synchronized void complete() {
		if (!isCompleted()) {
			long end = System.nanoTime();

			m_durationInMicros = end / 1000L - m_durationInMicros;
			super.setCompleted();

			int size = m_children.size();

			for (int i = 0; i < size; i++) {
				Message child = m_children.get(i);
				@SuppressWarnings("resource")
				ForkedTransaction forked = (ForkedTransaction) child;

				m_children.set(i, forked.join());
			}
		}
	}

	@Override
	public void complete(long startInMillis, long endInMillis) {
		setTimestamp(startInMillis);
		setDurationInMillis(endInMillis - startInMillis);

		super.setCompleted();

		if (m_children != null) {
			for (Message child : m_children) {
				if (!child.isCompleted() && child instanceof ForkableTransaction) {
					child.complete();
				}
			}
		}

		MessageContextHelper.getThreadLocal().end(this);
	}

	@Override
	public ForkableTransaction forFork() {
		return this;
	}

	@Override
	public synchronized ForkedTransaction doFork() {
		DefaultForkedTransaction child = new DefaultForkedTransaction(m_rootMessageId, m_parentMessageId);

		if (isCompleted()) {
			// NOTES: if root message has already been serialized & sent out,
			// then the parent will NEVER see this child, but this child can see the parent
			m_children.add(child.join());
		} else {
			m_children.add(child);
		}

		MessageContextHelper.getThreadLocal().attach(child);

		return child;
	}

	@Override
	public List<Message> getChildren() {
		return m_children;
	}

	@Override
	public long getDurationInMicros() {
		if (super.isCompleted()) {
			return m_durationInMicros;
		} else {
			return 0;
		}
	}

	@Override
	public long getDurationInMillis() {
		if (super.isCompleted()) {
			return m_durationInMicros / 1000L;
		} else {
			return 0;
		}
	}

	@Override
	public boolean hasChildren() {
		return m_children != null && m_children.size() > 0;
	}

	@Override
	public boolean isStandalone() {
		return false;
	}

	@Override
	public void setDurationInMillis(long durationInMillis) {
		m_durationInMicros = durationInMillis * 1000L;
	}
}
