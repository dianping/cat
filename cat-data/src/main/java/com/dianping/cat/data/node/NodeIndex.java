package com.dianping.cat.data.node;

import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.meta.AbstractIndexMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;

public class NodeIndex extends AbstractIndexMeta<NodeColumn> implements IndexMeta {
	public static final NodeIndex IDX_StartTime = new NodeIndex(NodeColumn.StartTime, false);

	private NodeIndex(Object... args) {
		super(args);
	}

	@Override
	public Class<? extends Index> getIndexClass() {
		if (this == IDX_StartTime) {
			return NodeIndexer.class;
		} else {
			throw new UnsupportedOperationException("No index defined for index: " + this);
		}
	}

	public static NodeIndex[] values() {
		return new NodeIndex[] { IDX_StartTime };
	}
}