package org.unidal.cat.message.storage.internals;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.message.storage.MessageFinder;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.internal.MessageId;

@Named(type = MessageFinderManager.class)
public class DefaultMessageFinderManager implements MessageFinderManager {
	
	private Map<Integer, List<MessageFinder>> m_map = new HashMap<Integer, List<MessageFinder>>();

	@Override
	public synchronized void close(int hour) {
		m_map.remove(hour);
	}

	@Override
	public ByteBuf find(MessageId id) {
		int hour = id.getHour();
		List<MessageFinder> finders = m_map.get(hour);

		if (finders != null) {
			for (MessageFinder finder : finders) {
				ByteBuf buf = finder.find(id);

				if (buf != null) {
					return buf;
				}
			}
		}

		return null;
	}

	@Override
	public void register(int hour, MessageFinder finder) {
		List<MessageFinder> finders = m_map.get(hour);

		if (finders == null) {
			synchronized (m_map) {
				finders = m_map.get(hour);

				if (finders == null) {
					finders = new ArrayList<MessageFinder>();

					m_map.put(hour, finders);
				}
			}
		}

		finders.add(finder);
	}

}
