/**
 * Project: whale-engine
 * 
 * File Created at 2012-8-15
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.engine.spi.impl;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class Department {

	private Integer Id;

	private String name;

	public Integer getId() {
		return Id;
	}

	public String getName() {
		return name;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
