/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.message;

/**
 * <p>
 * <code>Metric</code> is used to log business data point happens at a specific time. Such as an exception thrown, a review added by
 * user, a new user registered, an user logged into the system etc.
 * </p>
 * <p>
 * <p>
 * However, if it could be failure, or last for a long time, such as a remote API call, database call or search engine call etc. It
 * should be logged as a <code>Transaction</code>
 * </p>
 * <p>
 * <p>
 * All CAT message will be constructed as a message tree and send to back-end for further analysis, and for monitoring. Only
 * <code>Transaction</code> can be a tree node, all other message will be the tree leaf. The transaction without other messages
 * nested is an atomic transaction.
 * </p>
 *
 * @author Frankie Wu
 */
public interface Metric {
	/**
	 * Deliver the metric with <code>quantity</code>.
	 * 
	 * @param quantity
	 *           quantity no less than zero
	 */
	public void count(int quantity);

	public void duration(int count, long durationInMillis);

	public String getName();

	public long getTimestamp();

	/**
	 * Deliver the metric with aggregated <code>total</code. and <code>sum</code>.
	 * <p>
	 * 
	 * @param count
	 *           aggregated value of multiple quantities
	 * @param total
	 *           accumulated value of multiple value
	 */
	public void sum(int count, double total);

	/**
	 * Add new tag with given <code>name</code> and <code/>value</code>.
	 * <p>
	 * 
	 * Notes: add tag with same name multiple times, last tag wins.
	 * 
	 * @param name
	 *           tag name
	 * @param value
	 *           tag value
	 */
	public Metric tag(String name, String value);
}
