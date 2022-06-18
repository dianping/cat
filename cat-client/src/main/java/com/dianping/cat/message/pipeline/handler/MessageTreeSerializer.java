package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;
import com.dianping.cat.message.tree.MessageEncoder;
import com.dianping.cat.message.tree.MessageTree;
import com.dianping.cat.message.tree.NativeMessageEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * Message Tree bytes are:
 * 
 * <pre>
 * | content length | content bytes |
 *                                            
 * |     4 bytes    |    {length}   |
 * </pre>
 * 
 * @author qmwu2000
 */
// Component
public class MessageTreeSerializer extends MessageHandlerAdaptor {
	public static String ID = "serializer";

	private MessageEncoder m_encoder = new NativeMessageEncoder();

	@Override
	public int getOrder() {
		return 300;
	}

	@Override
	public void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer();
		int writerIndex = buf.writerIndex();

		buf.writeInt(0); // length placeholder
		m_encoder.encode(tree, buf);

		int size = buf.readableBytes();

		buf.setInt(writerIndex, size - 4); // actual length

		ctx.fireMessage(buf); // deliver the ByteBuf to the next handler
	}
}
