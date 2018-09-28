package com.dianping.cat.message.io;

import com.dianping.cat.Cat;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class BufReleaseHelper {

    public static void release(ByteBuf buf) {
        try {
            if (buf != null) {
                ReferenceCountUtil.release(buf);
            }
        } catch (Exception e) {
            Cat.logError(e);
        }
    }

}