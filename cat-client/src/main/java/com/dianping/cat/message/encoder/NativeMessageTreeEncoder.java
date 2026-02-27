package com.dianping.cat.message.encoder;

import java.nio.charset.Charset;
import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;

import io.netty.buffer.ByteBuf;

public class NativeMessageTreeEncoder implements MessageTreeEncoder {
	public static final String ID = "NT1"; // native message tree version 1

	@Override
	public void encode(MessageTree tree, ByteBuf buf) {
		Context ctx = new Context(tree);

		Encoder.HEADER.encode(ctx, buf, null);

		Message root = tree.getMessage();

		if (root != null) {
			encodeMessage(ctx, buf, root);
		}
	}

	private void encodeMessage(Context ctx, ByteBuf buf, Message msg) {
		if (msg instanceof Transaction) {
			Transaction transaction = (Transaction) msg;
			List<Message> children = transaction.getChildren();

			Encoder.TRANSACTION_START.encode(ctx, buf, msg);

			for (Message child : children) {
				if (child != null) {
					encodeMessage(ctx, buf, child);
				}
			}

			Encoder.TRANSACTION_END.encode(ctx, buf, msg);
		} else if (msg instanceof Event) {
			Encoder.EVENT.encode(ctx, buf, msg);
		} else if (msg instanceof Heartbeat) {
			Encoder.HEARTBEAT.encode(ctx, buf, msg);
		} else if (msg instanceof Trace) {
			Encoder.TRACE.encode(ctx, buf, msg);
		} else {
			throw new RuntimeException(String.format("Unsupported message(%s).", msg));
		}
	}

	private static class Context {
		private static Charset UTF8 = Charset.forName("UTF-8");;

		private MessageTree m_tree;

		public Context(MessageTree tree) {
			m_tree = tree;
		}

		public MessageTree getMessageTree() {
			return m_tree;
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
			writeVarint(buf, timestamp);
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

	private static enum Encoder {
		HEADER {
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
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				ctx.writeId(buf, 't');
				ctx.writeTimestamp(buf, msg.getTimestamp());
				ctx.writeString(buf, msg.getType());
				ctx.writeString(buf, msg.getName());
			}
		},

		TRANSACTION_END {
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
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				ctx.writeId(buf, 'E');
				ctx.writeTimestamp(buf, msg.getTimestamp());
				ctx.writeString(buf, msg.getType());
				ctx.writeString(buf, msg.getName());
				ctx.writeString(buf, msg.getStatus());
				ctx.writeString(buf, msg.getData().toString());
			}
		},

		HEARTBEAT {
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
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				ctx.writeId(buf, 'L');
				ctx.writeTimestamp(buf, msg.getTimestamp());
				ctx.writeString(buf, msg.getType());
				ctx.writeString(buf, msg.getName());
				ctx.writeString(buf, msg.getStatus());
				ctx.writeString(buf, msg.getData().toString());
			}
		};

		protected abstract void encode(Context ctx, ByteBuf buf, Message msg);
	}
}
