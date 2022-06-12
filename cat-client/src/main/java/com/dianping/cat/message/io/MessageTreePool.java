package com.dianping.cat.message.io;

import com.dianping.cat.message.tree.MessageTree;

public interface MessageTreePool {
   public void feed(MessageTree tree);

   public MessageTree poll() throws InterruptedException;

   public int size();
}
