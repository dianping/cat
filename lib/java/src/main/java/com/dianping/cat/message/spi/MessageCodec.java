package com.dianping.cat.message.spi;

import io.netty.buffer.ByteBuf;

public interface MessageCodec {

    MessageTree decode(ByteBuf buf);

    ByteBuf encode(MessageTree tree);

    void reset();
}
