package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import com.dianping.cat.Cat;

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