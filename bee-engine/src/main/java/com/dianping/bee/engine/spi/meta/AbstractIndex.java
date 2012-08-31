package com.dianping.bee.engine.spi.meta;

public abstract class AbstractIndex implements Index {

	@Override
	public IndexMeta getMeta() {
		return null;
	}

	@Override
	public void setValue(int index, Object value) {

	}

	@Override
	public void addValue(int index, Object value, PredicateType type) {

	}
}
