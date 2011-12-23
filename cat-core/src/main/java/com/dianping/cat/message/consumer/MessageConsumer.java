package com.dianping.cat.message.consumer;

import com.dianping.cat.message.Message;

public interface MessageConsumer {
   public void consume(Message message);
}
