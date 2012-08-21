package com.dianping.bee.engine.spi.internal;

import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.site.lookup.annotation.Inject;

public class TableHelper {
	@Inject
	private TableProviderManager m_manager;

	public ColumnMeta findColumn(String table, String column) {
		return null;
	}
}
