package com.dianping.cat.message.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultForkedTransaction extends AbstractMessage implements ForkedTransaction {
    private String rootMessageId;
    private String parentMessageId;
    private String messageId;
    private long durationInMicros;
    private List<Message> children;
    private AtomicBoolean joined = new AtomicBoolean();

    public DefaultForkedTransaction(String rootMessageId, String parentMessageId) {
        super("System", "ChildThread");

        this.rootMessageId = rootMessageId;
        this.parentMessageId = parentMessageId;

        durationInMicros = System.nanoTime() / 1000L;
        addData(Thread.currentThread().getName());
        setStatus(Message.SUCCESS);
    }

    @Override
    public Transaction addChild(Message message) {
        if (children == null) {
            children = new ArrayList<Message>();
        }

        children.add(message);
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

            durationInMicros = end / 1000L - durationInMicros;
            super.setCompleted(true);

            List<ForkableTransaction> forkables = Cat.getManager().getThreadLocalMessageTree().getForkableTransactions();

            if (forkables != null) {
                for (ForkableTransaction forkable : forkables) {
                    forkable.complete();
                }
            }

            if (joined.get()) {
                setName("Async.Detached." + getName());
                Cat.getManager().getContext().detach(rootMessageId, parentMessageId);
            } else {
                setName("Sync.Embedded." + getName());
                Cat.getManager().getContext().detach(null, null); // make stack pop
            }
        }
    }

    @Override
    public ForkableTransaction forFork() {
        MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
        String rootMessageId = tree.getRootMessageId();

        if (tree.getMessageId() == null) {
            tree.setMessageId(Cat.createMessageId());
        }

        ForkableTransaction forkable = new DefaultForkableTransaction(rootMessageId, tree.getMessageId());
        addChild(forkable);

        Cat.getManager().getContext().addForkableTransaction(forkable);

        return forkable;
    }

    @Override
    public List<Message> getChildren() {
        if (children == null) {
            return new ArrayList<Message>();
        } else {
            return children;
        }
    }

    @Override
    public long getDurationInMicros() {
        if (super.isCompleted()) {
            return durationInMicros;
        } else {
            return -1;
        }
    }

    @Override
    public long getDurationInMillis() {
        if (super.isCompleted()) {
            return durationInMicros / 1000L;
        } else {
            return -1;
        }
    }

    @Override
    public String getMessageId() {
        return messageId;
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
    public synchronized Message join() {
        joined.set(true);

        if (isCompleted()) {
            return this;
        } else {
            if (messageId == null) {
                String messageId = Cat.createMessageId();
                setMessageId(messageId);
            }

            DefaultEvent event = new DefaultEvent("RemoteCall", "RunAway");

            event.addData(messageId, getType() + ":" + getName());
            event.setTimestamp(getTimestamp());
            event.setStatus(Message.SUCCESS);
            return event;
        }
    }

    public void setDurationInMicros(long duration) {
        durationInMicros = duration;
    }

    @Override
    public void setDurationInMillis(long duration) {
        durationInMicros = duration;
    }

    @Override
    public void setDurationStart(long durationStart) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
