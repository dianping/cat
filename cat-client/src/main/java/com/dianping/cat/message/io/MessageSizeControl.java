package com.dianping.cat.message.io;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultForkedTransaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.tree.MessageTree;

// Component
public class MessageSizeControl {
   private AtomicInteger m_maxLines = new AtomicInteger();

   private AtomicInteger m_lines = new AtomicInteger();

   public MessageSizeControl() {
      this(1000);
   }

   public MessageSizeControl(int maxLines) {
      m_maxLines.set(maxLines);
   }

   public int getLines() {
      return m_lines.get();
   }

   private void migrate(Stack<Transaction> stack, Transaction source, Transaction target, int level) {
      Transaction current = level < stack.size() ? stack.get(level) : null;
      final List<Message> children = source.getChildren();
      boolean shouldKeep = false;

      for (Message child : children) {
         if (child == current && current instanceof DefaultTransaction) {
            DefaultTransaction copy = ((DefaultTransaction) current).shallowCopy();

            migrate(stack, current, copy, level + 1);

            target.addChild(copy);
            copy.setCompleted();
            copy.setDurationInMicros(System.nanoTime() / 1000L - copy.getDurationInMicros());
            copy.setStatus(Message.SUCCESS); // status in progress
            shouldKeep = true;
         } else {
            target.addChild(child);

            if (!child.isCompleted()) {
               child.complete();
            }
         }
      }

      children.clear();

      if (shouldKeep) { // add it back
         source.addChild(current);
      }
   }

   public void onMessage(Message message) {
      m_lines.incrementAndGet();
   }

   public void onTransactionEnd(Transaction transaction) {
   }

   public void onTransactionStart(Transaction transaction) {
      m_lines.incrementAndGet();
   }

   public void reset() {
      m_lines.set(0);
   }

   public boolean shouldTruncate() {
      return m_lines.get() >= m_maxLines.get();
   }

   public void truncate(Stack<Transaction> stack, MessageTree child, MessageTree parent, long timestamp) {
      Message message = child.getMessage();

      if (message instanceof DefaultTransaction) {
         DefaultTransaction source = (DefaultTransaction) message;
         DefaultTransaction target = source.shallowCopy();

         migrate(stack, source, target, 1);

         for (int i = stack.size() - 1; i >= 0; i--) {
            DefaultTransaction t = (DefaultTransaction) stack.get(i);

            t.setTimestamp(timestamp);
            t.setDurationInMicros(System.nanoTime() / 1000L);
         }

         m_lines.set(stack.size());

         // add link from parent to child
         DefaultForkedTransaction link = new DefaultForkedTransaction(parent.getRootMessageId(), parent.getMessageId());

         link.setType("Truncated");
         link.setMessageId(child.getMessageId());

         target.addChild(link);
         target.setCompleted();
         target.setDurationInMicros(System.nanoTime() / 1000L - target.getDurationInMicros());
         target.setStatus(Message.SUCCESS);
         parent.setMessage(target);

         child.setParentMessageId(parent.getMessageId());
         child.setRootMessageId(parent.getRootMessageId());
      }
   }
}
