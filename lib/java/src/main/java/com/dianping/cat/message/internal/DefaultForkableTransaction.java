package com.dianping.cat.message.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultForkableTransaction extends AbstractMessage implements ForkableTransaction {
    private String rootMessageId;
    private String parentMessageId;
    private long durationInMicros;
    private List<Message> children = Collections.synchronizedList(new ArrayList<Message>());

    private static String generateThreadName() {
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("qtp")) {
            int index = threadName.indexOf(" ");
            if (index > -1) {
                threadName = threadName.substring(0, index);
            }
        }

        if (threadName.length() > 80) {
            threadName = "ThreadID-" + String.valueOf(Thread.currentThread().getId());
        }

        return threadName;
    }

    public DefaultForkableTransaction(String rootMessageId, String parentMessageId) {
        super("System", "Forkable");

        this.rootMessageId = rootMessageId;
        this.parentMessageId = parentMessageId;
        durationInMicros = System.nanoTime() / 1000L;

        setStatus(Message.SUCCESS);
        addData("thread-name=" + generateThreadName());
    }

    @Override
    public Transaction addChild(Message message) {
        children.add(message);
        return this;
    }

    @Override
    public synchronized void complete() {
        if (!isCompleted()) {
            super.setCompleted(true);

            durationInMicros = 0;

            int size = children.size();

            for (int i = 0; i < size; i++) {
                Message child = children.get(i);
                @SuppressWarnings("resource")
                ForkedTransaction forked = (ForkedTransaction) child;

                children.set(i, forked.join());
            }
        }
    }

    @Override
    public synchronized ForkedTransaction doFork() {
        DefaultForkedTransaction child = new DefaultForkedTransaction(rootMessageId, parentMessageId);

        if (isCompleted()) {
            // NOTES: if root message has already been serialized & sent out,
            // then the parent will NEVER see this child, but this child can see the parent
            children.add(child.join());
        } else {
            children.add(child);
        }
        Cat.getManager().getContext().attach(child, rootMessageId, parentMessageId);
        return child;
    }

    @Override
    public ForkableTransaction forFork() {
        return this;
    }

    @Override
    public List<Message> getChildren() {
        return children;
    }

    @Override
    public long getDurationInMicros() {
        if (super.isCompleted()) {
            return durationInMicros;
        } else {
            return 0;
        }
    }

    @Override
    public long getDurationInMillis() {
        if (super.isCompleted()) {
            return durationInMicros / 1000L;
        } else {
            return 0;
        }
    }

    @Override
    public long getRawDurationInMicros() {
        return durationInMicros;
    }

    @Override
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public void setDurationInMicros(long durationInMicros) {
        this.durationInMicros = durationInMicros;
    }

    public void setDurationInMillis(long durationInMillis) {
        durationInMicros = durationInMillis * 1000L;
    }

    @Override
    public void setDurationStart(long durationStart) {
        throw new UnsupportedOperationException();
    }

}
