package com.dianping.cat.message.transport;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;

/**
 * <p>
 * Transport is a broker that listens to message from previous phase and relay
 * (or re-send) it to next phase.
 * </p>
 * 
 * Typically, different environment has different transport configuration
 * <ul>
 * <li>Dev environment: [collector] ====> [in memory transport] ====> [message handler] ====> [consumers]</li>
 * <li>QA environment: [collector] ====> [tcp socket transport] ==network==> [tcp socket transport] ====> [message handler] ====> [consumers]</li>
 * </ul>
 * 
 * @author Frankie Wu
 */
public interface Transport {
   public void onMessage(MessageHandler handler);

   public void send(Message message);

   public void shutdown();
}
