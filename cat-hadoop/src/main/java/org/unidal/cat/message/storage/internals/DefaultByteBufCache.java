package org.unidal.cat.message.storage.internals;

import io.netty.util.ReferenceCountUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ByteBufCache.class)
public class DefaultByteBufCache implements ByteBufCache, Initializable, LogEnabled {

	private BlockingQueue<ByteBuffer> m_bufs = new ArrayBlockingQueue<ByteBuffer>(8000);

	private Logger m_logger;

	private AtomicInteger m_count = new AtomicInteger();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public ByteBuffer get() {
		ByteBuffer buf = m_bufs.poll();

		if (buf == null) {
			buf = ByteBuffer.allocate(32 * 1024);
		}

		return buf;
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public void put(ByteBuffer buf) {
		byte[] array = buf.array();

		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}

		buf.clear();

		boolean result = m_bufs.offer(buf);

		if (!result) {
			try {
				ReferenceCountUtil.release(buf);
			} catch (Exception e) {
				Cat.logError(e);
			}

			if (m_count.incrementAndGet() % 100 == 0) {
				m_logger.info("error when put back buf");
			}
		}
	}

}
