package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.TraceContext;
import com.dianping.cat.message.context.TraceContextHelper;

public class DefaultForkedTransaction extends AbstractMessage implements ForkedTransaction {
	private TraceContext m_ctx;

	private String m_rootMessageId;

	private String m_parentMessageId;

	private long m_durationInMicros;

	private List<Message> m_children;

	private AtomicBoolean m_joined = new AtomicBoolean();

	private String m_messageId;

	public DefaultForkedTransaction(String rootMessageId, String parentMessageId) {
		super(FORKED, Thread.currentThread().getName());

		m_ctx = TraceContextHelper.threadLocal();
		m_rootMessageId = rootMessageId != null ? rootMessageId : parentMessageId;
		m_parentMessageId = parentMessageId;

		m_durationInMicros = System.nanoTime() / 1000L;
		setStatus(Message.SUCCESS);
	}

	@Override
	public Transaction addChild(Message message) {
		if (m_children == null) {
			m_children = new ArrayList<Message>();
		}

		m_children.add(message);
		return this;
	}

	@Override
	public void close() {
		complete();
	}

	@Override
	public synchronized void complete() {
		if (!isCompleted()) {
			long end = System.nanoTime();

			m_durationInMicros = end / 1000L - m_durationInMicros;
			super.setCompleted();

			if (m_joined.get()) {
				setType(DETACHED);
				m_ctx.detach(m_rootMessageId, m_parentMessageId);
			} else {
				setType(EMBEDDED);
				m_ctx.detach(null, null); // make stack pop
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

		m_ctx.end(this);
	}

	@Override
	public ForkableTransaction forFork() {
		MessageTree tree = m_ctx.getMessageTree();
		String rootMessageId = tree.getRootMessageId();
		String messageId = tree.getMessageId();
		ForkableTransaction forkable = new DefaultForkableTransaction(rootMessageId, messageId);

		addChild(forkable);
		return forkable;
	}

	@Override
	public List<Message> getChildren() {
		if (m_children == null) {
			return Collections.emptyList();
		} else {
			return m_children;
		}
	}

	@Override
	public CharSequence getData() {
		addData("#", m_messageId);

		return super.getData();
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
	public String getMessageId() {
		return m_messageId;
	}

	@Override
	public String getParentMessageId() {
		return m_parentMessageId;
	}

	@Override
	public String getRootMessageId() {
		return m_rootMessageId;
	}

	@Override
	public boolean hasChildren() {
		return m_children != null && m_children.size() > 0;
	}

	@Override
	public synchronized ForkedTransaction join() {
		m_joined.set(true);

		if (!isCompleted() && getMessageId() == null) {
			DefaultForkedTransaction forked = new DefaultForkedTransaction(m_rootMessageId, m_parentMessageId);
			String messageId = TraceContextHelper.createMessageId();

			setMessageId(messageId);

			forked.setType(DETACHED);
			forked.setName(getName());
			forked.setStatus(Message.SUCCESS);
			forked.setMessageId(messageId);

			return forked;
		} else {
			return this;
		}
	}

	@Override
	public void setDurationInMillis(long durationInMillis) {
		m_durationInMicros = durationInMillis * 1000L;
	}

	@Override
	public void setMessageId(String messageId) {
		m_messageId = messageId;
	}
}
