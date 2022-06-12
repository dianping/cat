package com.dianping.cat.message.tree;

import io.netty.buffer.ByteBuf;

public interface MessageEncoder {
   public void encode(MessageTree tree, ByteBuf buf);
}
