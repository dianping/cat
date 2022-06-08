package com.dianping.cat.configuration2;

import com.dianping.cat.configure.client2.entity.ClientConfig;

public interface Refreshable {
	/**
	 * Return a new configure if any change happens.
	 * 
	 * @param config
	 *           existing configure
	 * @return new configure
	 * @throws Exception
	 *            if any exception happens
	 */
	public ClientConfig refresh(ClientConfig config) throws Exception;
}
