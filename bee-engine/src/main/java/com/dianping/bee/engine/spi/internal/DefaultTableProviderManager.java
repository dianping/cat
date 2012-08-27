package com.dianping.bee.engine.spi.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.session.SessionManager;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultTableProviderManager extends ContainerHolder implements TableProviderManager, Initializable {
	@Inject
	private SessionManager m_sessionManager;

	private Map<String, Map<String, TableProvider>> m_map = new HashMap<String, Map<String, TableProvider>>();

	@Override
	public TableProvider getTableProvider(String table) {
		String database = m_sessionManager.getSession().getDatabase();
		Map<String, TableProvider> map = m_map.get(database);

		if (map != null) {
			return map.get(table.toUpperCase());
		} else {
			return null;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		List<DatabaseProvider> providers = lookupList(DatabaseProvider.class);

		for (DatabaseProvider provider : providers) {
			Map<String, TableProvider> map = new HashMap<String, TableProvider>();

			for (TableProvider table : provider.getTables()) {
				map.put(table.getName().toUpperCase(), table);
			}

			m_map.put(provider.getName(), map);
		}
	}
}
