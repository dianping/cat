package com.dianping.cat.configuration;

import com.dianping.cat.configuration.model.IEntity;

public interface ConfigureSource<T extends IEntity<T>> {
	/**
	 * Get the whole or part of configure, which will be merged to the configure model.
	 * 
	 * @return whole or part of configure
	 * @throws Exception
	 *            if any exception happens
	 */
	public T getConfig() throws Exception;

	/**
	 * Order of configure source. lower value has higher priority.
	 * 
	 * @return order
	 */
	public int getOrder();
}
