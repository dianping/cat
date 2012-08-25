package com.dianping.bee.engine.spi.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.site.lookup.annotation.Inject;

public class DefaultTableProviderManager implements TableProviderManager, Initializable {
	@Inject
	private DatabaseProvider m_databaseProvider;

	private Map<String, TableProvider> m_tables = new HashMap<String, TableProvider>();

	@Override
	public String getDatabaseName() {
		return m_databaseProvider.getName();
	}

	@Override
	public TableProvider getTableProvider(String table) {
		return m_tables.get(table.toUpperCase());
	}

	@Override
	public void initialize() throws InitializationException {
		TableProvider[] tables = m_databaseProvider.getTables();

		for (TableProvider table : tables) {
			m_tables.put(table.getName().toUpperCase(), table);
		}
	}
}
