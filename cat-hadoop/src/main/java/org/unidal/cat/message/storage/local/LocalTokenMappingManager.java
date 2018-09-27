package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

@Named(type = TokenMappingManager.class, value = "local")
public class LocalTokenMappingManager extends ContainerHolder implements TokenMappingManager {
	private Map<Pair<Integer, String>, TokenMapping> m_cache = new HashMap<Pair<Integer, String>, TokenMapping>();

	@Override
	public void close(int hour) {
		Set<Pair<Integer, String>> removes = new HashSet<Pair<Integer, String>>();

		for (Entry<Pair<Integer, String>, TokenMapping> entry : m_cache.entrySet()) {
			Pair<Integer, String> entryKey = entry.getKey();
			Integer key = entryKey.getKey();

			if (key <= hour) {
				removes.add(entryKey);
			}
		}

		for (Pair<Integer, String> pair : removes) {
			TokenMapping mapping = null;

			synchronized (this) {
				mapping = m_cache.remove(pair);
			}

			if (mapping != null) {
				mapping.close();
			}
			super.release(mapping);
		}
	}

	@Override
	public TokenMapping getTokenMapping(int hour, String ip) throws IOException {
		Pair<Integer, String> pair = new Pair<Integer, String>(hour, ip);
		TokenMapping mapping = m_cache.get(pair);

		if (mapping == null) {
			synchronized (this) {
				mapping = m_cache.get(pair);

				if (mapping == null) {
					mapping = lookup(TokenMapping.class, "local");
					mapping.open(hour, ip);
					m_cache.put(pair, mapping);
				}
			}
		}

		return mapping;
	}

}
