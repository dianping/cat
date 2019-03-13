/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.message.spi.codec;

import com.dianping.cat.message.*;
import com.dianping.cat.message.internal.*;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;

public class NativeMessageCodec implements MessageCodec {

    private static final String ID = "NT1";

    @Override
    public MessageTree decode(ByteBuf buf) {
        buf.readInt(); // read the length of the message tree

        DefaultMessageTree tree = new DefaultMessageTree();
        Context ctx = new Context(tree);
        Codec.HEADER.decode(ctx, buf);
        Message msg = decodeMessage(ctx, buf);

        tree.setMessage(msg);
        tree.setBuffer(buf);

        return tree;
    }

    private Message decodeMessage(Context ctx, ByteBuf buf) {
        Message msg = null;

        while (buf.readableBytes() > 0) {
            char ch = ctx.readId(buf);

            switch (ch) {
                case 't':
                    Codec.TRANSACTION_START.decode(ctx, buf);
                    break;
                case 'T':
                    msg = Codec.TRANSACTION_END.decode(ctx, buf);
                    break;
                case 'E':
                    Message e = Codec.EVENT.decode(ctx, buf);

                    ctx.addChild(e);
                    break;
                case 'M':
                    Message m = Codec.METRIC.decode(ctx, buf);

                    ctx.addChild(m);
                    break;
                case 'H':
                    Message h = Codec.HEARTBEAT.decode(ctx, buf);

                    ctx.addChild(h);
                    break;
                case 'L':
                    Message l = Codec.TRACE.decode(ctx, buf);

                    ctx.addChild(l);
                    break;
                default:
                    throw new RuntimeException(String.format("Unsupported message type(%s).", ch));
            }
        }

        if (msg == null) {
            msg = ctx.getMessageTree().getMessage();
        }

        return msg;
    }

    @Override
    public ByteBuf encode(MessageTree tree) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(4 * 1024);

        try {
            Context ctx = new Context(tree);

            buf.writeInt(0); // place-holder

            Codec.HEADER.encode(ctx, buf, null);

            Message msg = tree.getMessage();

            if (msg != null) {
                encodeMessage(ctx, buf, msg);
            }
            int readableBytes = buf.readableBytes();

            buf.setInt(0, readableBytes - 4); // reset the message size

            return buf;
        } catch (RuntimeException e) {
            buf.release();

            throw e;
        }
    }

    private void encodeMessage(Context ctx, ByteBuf buf, Message msg) {
        if (msg instanceof Transaction) {
            Transaction transaction = (Transaction) msg;
            List<Message> children = transaction.getChildren();

            Codec.TRANSACTION_START.encode(ctx, buf, msg);

            for (Message child : children) {
                if (child != null) {
                    encodeMessage(ctx, buf, child);
                }
            }

            Codec.TRANSACTION_END.encode(ctx, buf, msg);
        } else if (msg instanceof Event) {
            Codec.EVENT.encode(ctx, buf, msg);
        } else if (msg instanceof Metric) {
            Codec.METRIC.encode(ctx, buf, msg);
        } else if (msg instanceof Heartbeat) {
            Codec.HEARTBEAT.encode(ctx, buf, msg);
        } else if (msg instanceof Trace) {
            Codec.TRACE.encode(ctx, buf, msg);
        } else {
            throw new RuntimeException(String.format("Unsupported message(%s).", msg));
        }
    }

    @Override
    public void reset() {
    }

    static enum Codec {
        HEADER {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                MessageTree tree = ctx.getMessageTree();
                String version = ctx.getVersion(buf);

                if (ID.equals(version)) {
                    tree.setDomain(ctx.readString(buf));
                    tree.setHostName(ctx.readString(buf));
                    tree.setIpAddress(ctx.readString(buf));
                    tree.setThreadGroupName(ctx.readString(buf));
                    tree.setThreadId(ctx.readString(buf));
                    tree.setThreadName(ctx.readString(buf));
                    tree.setMessageId(ctx.readString(buf));
                    tree.setParentMessageId(ctx.readString(buf));
                    tree.setRootMessageId(ctx.readString(buf));
                    tree.setSessionToken(ctx.readString(buf));
                } else {
                    throw new RuntimeException(String.format("Unrecognized version(%s) for binary message codec!", version));
                }

                return null;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                MessageTree tree = ctx.getMessageTree();

                ctx.writeVersion(buf, ID);
                ctx.writeString(buf, tree.getDomain());
                ctx.writeString(buf, tree.getHostName());
                ctx.writeString(buf, tree.getIpAddress());
                ctx.writeString(buf, tree.getThreadGroupName());
                ctx.writeString(buf, tree.getThreadId());
                ctx.writeString(buf, tree.getThreadName());
                ctx.writeString(buf, tree.getMessageId());
                ctx.writeString(buf, tree.getParentMessageId());
                ctx.writeString(buf, tree.getRootMessageId());
                ctx.writeString(buf, tree.getSessionToken());
            }
        },

        TRANSACTION_START {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                long timestamp = ctx.readTimestamp(buf);
                String type = ctx.readString(buf);
                String name = ctx.readString(buf);

                if ("System".equals(type) && name.startsWith("UploadMetric")) {
                    name = "UploadMetric";
                }

                DefaultTransaction t = new DefaultTransaction(type, name);

                t.setTimestamp(timestamp);
                ctx.pushTransaction(t);

                MessageTree tree = ctx.getMessageTree();
                if (tree instanceof DefaultMessageTree) {
                    ((DefaultMessageTree) tree).findOrCreateTransactions().add(t);
                }

                return t;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                ctx.writeId(buf, 't');
                ctx.writeTimestamp(buf, msg.getTimestamp());
                ctx.writeString(buf, msg.getType());
                ctx.writeString(buf, msg.getName());
            }
        },

        TRANSACTION_END {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                String status = ctx.readString(buf);
                String data = ctx.readString(buf);
                long durationInMicros = ctx.readDuration(buf);
                DefaultTransaction t = ctx.popTransaction();

                t.setStatus(status);
                t.addData(data);
                t.setDurationInMicros(durationInMicros);
                return t;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                Transaction t = (Transaction) msg;

                ctx.writeId(buf, 'T');
                ctx.writeString(buf, msg.getStatus());
                ctx.writeString(buf, msg.getData().toString());
                ctx.writeDuration(buf, t.getDurationInMicros());
            }
        },

        EVENT {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                long timestamp = ctx.readTimestamp(buf);
                String type = ctx.readString(buf);
                String name = ctx.readString(buf);
                String status = ctx.readString(buf);
                String data = ctx.readString(buf);
                DefaultEvent e = new DefaultEvent(type, name);

                e.setTimestamp(timestamp);
                e.setStatus(status);
                e.addData(data);

                MessageTree tree = ctx.getMessageTree();
                if (tree instanceof DefaultMessageTree) {
                    ((DefaultMessageTree) tree).findOrCreateEvents().add(e);
                }

                return e;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                ctx.writeId(buf, 'E');
                ctx.writeTimestamp(buf, msg.getTimestamp());
                ctx.writeString(buf, msg.getType());
                ctx.writeString(buf, msg.getName());
                ctx.writeString(buf, msg.getStatus());
                ctx.writeString(buf, msg.getData().toString());
            }
        },

        METRIC {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                long timestamp = ctx.readTimestamp(buf);
                String type = ctx.readString(buf);
                String name = ctx.readString(buf);
                String status = ctx.readString(buf);
                String data = ctx.readString(buf);
                DefaultMetric m = new DefaultMetric(type, name);

                m.setTimestamp(timestamp);
                m.setStatus(status);
                m.addData(data);

                MessageTree tree = ctx.getMessageTree();
                if (tree instanceof DefaultMessageTree) {
                    ((DefaultMessageTree) tree).findOrCreateMetrics().add(m);
                }

                return m;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                ctx.writeId(buf, 'M');
                ctx.writeTimestamp(buf, msg.getTimestamp());
                ctx.writeString(buf, msg.getType());
                ctx.writeString(buf, msg.getName());
                ctx.writeString(buf, msg.getStatus());
                ctx.writeString(buf, msg.getData().toString());
            }
        },

        HEARTBEAT {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                long timestamp = ctx.readTimestamp(buf);
                String type = ctx.readString(buf);
                String name = ctx.readString(buf);
                String status = ctx.readString(buf);
                String data = ctx.readString(buf);
                DefaultHeartbeat h = new DefaultHeartbeat(type, name);

                h.setTimestamp(timestamp);
                h.setStatus(status);
                h.addData(data);

                MessageTree tree = ctx.getMessageTree();
                if (tree instanceof DefaultMessageTree) {
                    ((DefaultMessageTree) tree).addHeartbeat(h);
                }

                return h;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                ctx.writeId(buf, 'H');
                ctx.writeTimestamp(buf, msg.getTimestamp());
                ctx.writeString(buf, msg.getType());
                ctx.writeString(buf, msg.getName());
                ctx.writeString(buf, msg.getStatus());
                ctx.writeString(buf, msg.getData().toString());
            }
        },

        TRACE {
            @Override
            protected Message decode(Context ctx, ByteBuf buf) {
                long timestamp = ctx.readTimestamp(buf);
                String type = ctx.readString(buf);
                String name = ctx.readString(buf);
                String status = ctx.readString(buf);
                String data = ctx.readString(buf);
                DefaultTrace t = new DefaultTrace(type, name);

                t.setTimestamp(timestamp);
                t.setStatus(status);
                t.addData(data);
                return t;
            }

            @Override
            protected void encode(Context ctx, ByteBuf buf, Message msg) {
                ctx.writeId(buf, 'L');
                ctx.writeTimestamp(buf, msg.getTimestamp());
                ctx.writeString(buf, msg.getType());
                ctx.writeString(buf, msg.getName());
                ctx.writeString(buf, msg.getStatus());
                ctx.writeString(buf, msg.getData().toString());
            }
        };

        protected abstract Message decode(Context ctx, ByteBuf buf);

        protected abstract void encode(Context ctx, ByteBuf buf, Message msg);
    }

    private static class Context {
        private static Charset UTF8 = Charset.forName("UTF-8");
        ;

        private MessageTree m_tree;

        private Stack<DefaultTransaction> m_parents = new Stack<DefaultTransaction>();

        private byte[] m_data = new byte[256];

        public Context(MessageTree tree) {
            m_tree = tree;
        }

        public void addChild(Message msg) {
            if (!m_parents.isEmpty()) {
                m_parents.peek().addChild(msg);
            } else {
                m_tree.setMessage(msg);
            }
        }

        public MessageTree getMessageTree() {
            return m_tree;
        }

        public String getVersion(ByteBuf buf) {
            byte[] data = new byte[3];

            buf.readBytes(data);
            return new String(data);
        }

        public DefaultTransaction popTransaction() {
            return m_parents.pop();
        }

        public void pushTransaction(DefaultTransaction t) {
            if (!m_parents.isEmpty()) {
                m_parents.peek().addChild(t);
            }

            m_parents.push(t);
        }

        public long readDuration(ByteBuf buf) {
            return readVarint(buf, 64);
        }

        public char readId(ByteBuf buf) {
            return (char) buf.readByte();
        }

        public String readString(ByteBuf buf) {
            int len = (int) readVarint(buf, 32);

            if (len == 0) {
                return "";
            } else if (len > m_data.length) {
                m_data = new byte[len];
            }

            buf.readBytes(m_data, 0, len);
            return new String(m_data, 0, len);
        }

        public long readTimestamp(ByteBuf buf) {
            return readVarint(buf, 64);
        }

        protected long readVarint(ByteBuf buf, int length) {
            int shift = 0;
            long result = 0;

            while (shift < length) {
                final byte b = buf.readByte();
                result |= (long) (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return result;
                }
                shift += 7;
            }

            throw new RuntimeException("Malformed variable int " + length + "!");
        }

        public void writeDuration(ByteBuf buf, long duration) {
            writeVarint(buf, duration);
        }

        public void writeId(ByteBuf buf, char id) {
            buf.writeByte(id);
        }

        public void writeString(ByteBuf buf, String str) {
            if (str == null || str.length() == 0) {
                writeVarint(buf, 0);
            } else {
                byte[] data = str.getBytes(UTF8);

                writeVarint(buf, data.length);
                buf.writeBytes(data);
            }
        }

        public void writeTimestamp(ByteBuf buf, long timestamp) {
            writeVarint(buf, timestamp); // TODO use relative value of root message timestamp
        }

        private void writeVarint(ByteBuf buf, long value) {
            while (true) {
                if ((value & ~0x7FL) == 0) {
                    buf.writeByte((byte) value);
                    return;
                } else {
                    buf.writeByte(((byte) value & 0x7F) | 0x80);
                    value >>>= 7;
                }
            }
        }

        public void writeVersion(ByteBuf buf, String version) {
            buf.writeBytes(version.getBytes());
        }
    }

}
