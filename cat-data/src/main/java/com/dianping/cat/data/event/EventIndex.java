package com.dianping.cat.data.event;

import com.dianping.bee.engine.spi.AbstractIndexMeta;
import com.dianping.bee.engine.spi.IndexMeta;

public class EventIndex extends AbstractIndexMeta<EventColumn> implements IndexMeta {
	public static final EventIndex IDX_STARTTIME_DOMAIN = new EventIndex(EventColumn.StartTime, false,
	      EventColumn.Domain, true);

	private EventIndex(Object... args) {
		super(args);
	}

	public static EventIndex[] values() {
		return new EventIndex[] { IDX_STARTTIME_DOMAIN };
	}
}