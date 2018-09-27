package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ClientMessageEncoder extends MessageToByteEncoder<ClientMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ClientMessage msg, ByteBuf out) {
		out.writeInt(ClientMessage.PROTOCOL_ID);
		out.writeInt(msg.getVersion());
		out.writeInt(msg.getData().length);
		out.writeBytes(msg.getData());
	}

}
