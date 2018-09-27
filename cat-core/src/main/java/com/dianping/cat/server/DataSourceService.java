package com.dianping.cat.server;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

public interface DataSourceService<T> extends Initializable {

	public T getConnection(String category);

}
