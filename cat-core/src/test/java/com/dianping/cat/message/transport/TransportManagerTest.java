package com.dianping.cat.message.transport;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;

public class TransportManagerTest {
   @Test
   public void testInitailized() {
      new TransportManager().setTransport(new MockTransport());

      Assert.assertNotNull(TransportManager.getTransport());

      new TransportManager().setTransport(null);
   }

   @Test
   public void testNotInitailized() {
      try {
         Assert.assertNotNull(TransportManager.getTransport());

         Assert.fail("TransportManager should be initialized first before call getTransport()!");
      } catch (RuntimeException e) {
         // expected
      }
   }

   @Test
   public void testDoubleInitailization() {
      new TransportManager().setTransport(new MockTransport());

      try {
         new TransportManager().setTransport(new MockTransport());

         Assert.fail("Double initailization of TransportManager should not be allowed!");
      } catch (RuntimeException e) {
         // expected
      }
   }

   static class MockTransport implements Transport {
      @Override
      public void onMessage(MessageHandler handler) {
      }

      @Override
      public void send(Message message) {
      }

      @Override
      public void shutdown() {
      }
   }
}
