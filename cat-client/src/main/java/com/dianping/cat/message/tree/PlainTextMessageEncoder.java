package com.dianping.cat.message.tree;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;

import io.netty.buffer.ByteBuf;

// Component
public class PlainTextMessageEncoder implements MessageEncoder {
   public static final String ID = "PT1"; // plain text version 1

   private static final byte TAB = '\t'; // tab character

   private static final byte LF = '\n'; // line feed character

   private BufferHelper m_bufferHelper = new BufferHelper();

   private DateHelper m_dateHelper = new DateHelper();

   @Override
   public void encode(MessageTree tree, ByteBuf buf) {
      encodeHeader(tree, buf);

      if (tree.getMessage() != null) {
         encodeMessage(tree.getMessage(), buf);
      }
   }

   private void encodeHeader(MessageTree tree, ByteBuf buf) {
      BufferHelper helper = m_bufferHelper;

      helper.write(buf, ID);
      helper.write(buf, TAB);
      helper.write(buf, tree.getDomain());
      helper.write(buf, TAB);
      helper.write(buf, tree.getHostName());
      helper.write(buf, TAB);
      helper.write(buf, tree.getIpAddress());
      helper.write(buf, TAB);
      helper.write(buf, tree.getThreadGroupName());
      helper.write(buf, TAB);
      helper.write(buf, tree.getThreadId());
      helper.write(buf, TAB);
      helper.write(buf, tree.getThreadName());
      helper.write(buf, TAB);
      helper.write(buf, tree.getMessageId());
      helper.write(buf, TAB);
      helper.write(buf, tree.getParentMessageId());
      helper.write(buf, TAB);
      helper.write(buf, tree.getRootMessageId());
      helper.write(buf, TAB);
      helper.write(buf, tree.getSessionToken());
      helper.write(buf, LF);
   }

   private void encodeLine(Message message, ByteBuf buf, char type, Policy policy) {
      BufferHelper helper = m_bufferHelper;

      helper.write(buf, (byte) type);

      if (type == 'T' && message instanceof Transaction) {
         long duration = ((Transaction) message).getDurationInMillis();

         helper.write(buf, m_dateHelper.format(message.getTimestamp() + duration));
      } else {
         helper.write(buf, m_dateHelper.format(message.getTimestamp()));
      }

      helper.write(buf, TAB);
      helper.writeRaw(buf, message.getType());
      helper.write(buf, TAB);
      helper.writeRaw(buf, message.getName());
      helper.write(buf, TAB);

      if (policy != Policy.WITHOUT_STATUS) {
         helper.writeRaw(buf, message.getStatus());
         helper.write(buf, TAB);

         Object data = message.getData();

         if (policy == Policy.WITH_DURATION && message instanceof Transaction) {
            long duration = ((Transaction) message).getDurationInMicros();

            helper.write(buf, String.valueOf(duration));
            helper.write(buf, "us");
            helper.write(buf, TAB);
         }

         helper.writeRaw(buf, String.valueOf(data));
         helper.write(buf, TAB);
      }

      helper.write(buf, LF);
   }

   public void encodeMessage(Message message, ByteBuf buf) {
      if (message instanceof Transaction) {
         Transaction transaction = (Transaction) message;
         List<Message> children = transaction.getChildren();

         if (children.isEmpty()) {
            encodeLine(transaction, buf, 'A', Policy.WITH_DURATION);
         } else {
            int len = children.size();

            encodeLine(transaction, buf, 't', Policy.WITHOUT_STATUS);

            for (int i = 0; i < len; i++) {
               Message child = children.get(i);

               if (child != null) {
                  encodeMessage(child, buf);
               }
            }

            encodeLine(transaction, buf, 'T', Policy.WITH_DURATION);
         }
      } else if (message instanceof Event) {
         encodeLine(message, buf, 'E', Policy.DEFAULT);
      } else if (message instanceof Trace) {
         encodeLine(message, buf, 'L', Policy.DEFAULT);
      } else if (message instanceof Metric) {
         encodeLine(message, buf, 'M', Policy.DEFAULT);
      } else if (message instanceof Heartbeat) {
         encodeLine(message, buf, 'H', Policy.DEFAULT);
      } else {
         throw new RuntimeException(String.format("Unsupported message type: %s.", message));
      }
   }

   private static class BufferHelper {
      private void escape(ByteBuf buf, byte[] data) {
         int len = data.length;
         int offset = 0;

         for (int i = 0; i < len; i++) {
            byte b = data[i];

            if (b == '\t' || b == '\r' || b == '\n' || b == '\\') {
               buf.writeBytes(data, offset, i - offset);
               buf.writeByte('\\');

               if (b == '\t') {
                  buf.writeByte('t');
               } else if (b == '\r') {
                  buf.writeByte('r');
               } else if (b == '\n') {
                  buf.writeByte('n');
               } else {
                  buf.writeByte(b);
               }

               offset = i + 1;
            }
         }

         if (len > offset) {
            buf.writeBytes(data, offset, len - offset);
         }
      }

      public void write(ByteBuf buf, byte b) {
         buf.writeByte(b);
      }

      public void write(ByteBuf buf, String str) {
         if (str == null) {
            str = "null";
         }

         byte[] data = str.getBytes();

         buf.writeBytes(data);
      }

      public void writeRaw(ByteBuf buf, String str) {
         if (str == null) {
            str = "null";
         }

         byte[] data;

         try {
            data = str.getBytes("utf-8");
         } catch (UnsupportedEncodingException e) {
            data = str.getBytes();
         }

         escape(buf, data);
      }
   }

   /**
    * Thread safe date helper class. DateFormat is NOT thread safe.
    */
   private static class DateHelper {
      private BlockingQueue<SimpleDateFormat> m_formats = new ArrayBlockingQueue<SimpleDateFormat>(20);

      public String format(long timestamp) {
         SimpleDateFormat format = m_formats.poll();

         if (format == null) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
         }

         try {
            return format.format(new Date(timestamp));
         } finally {
            if (m_formats.remainingCapacity() > 0) {
               m_formats.offer(format);
            }
         }
      }

   }

   private static enum Policy {
      DEFAULT,

      WITHOUT_STATUS,

      WITH_DURATION;
   }
}