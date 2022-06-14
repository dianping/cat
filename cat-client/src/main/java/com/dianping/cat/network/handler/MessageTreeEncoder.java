package com.dianping.cat.network.handler;

import com.dianping.cat.message.tree.MessageEncoder;
import com.dianping.cat.message.tree.MessageTree;
import com.dianping.cat.message.tree.NativeMessageEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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
@Sharable
public class MessageTreeEncoder extends MessageToByteEncoder<MessageTree> {
	private MessageEncoder m_encoder = new NativeMessageEncoder();

	@Override
	protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, MessageTree tree, boolean preferDirect)
	      throws Exception {
		return ctx.channel().alloc().heapBuffer(2 * 1024);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MessageTree tree, ByteBuf buf) throws Exception {
		int writerIndex = buf.writerIndex();

		buf.writeInt(0); // length placeholder
		m_encoder.encode(tree, buf);

		int size = buf.readableBytes();

		buf.setInt(writerIndex, size - 4); // actual length
	}
}
