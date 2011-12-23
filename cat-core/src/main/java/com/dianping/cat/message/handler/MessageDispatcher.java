package com.dianping.cat.message.handler;

import java.util.List;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.consumer.MessageConsumer;

public class MessageDispatcher implements MessageHandler {
   private List<MessageConsumer> m_comsumers;

   @Override
   public void handle(Message message) {
   }
}
