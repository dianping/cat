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

import java.util.List;

/**
	* <p>
	* <code>Transaction</code> is any interesting unit of work that takes time to complete and may fail.
	* </p>
	* <p>
	* <p>
	* Basically, all data access across the boundary needs to be logged as a <code>Transaction</code> since it may fail and
	* time consuming. For example, URL request, disk IO, JDBC query, search query, HTTP request, 3rd party API call etc.
	* </p>
	* <p>
	* <p>
	* Sometime if A needs call B which is owned by another team, although A and B are deployed together without any
	* physical boundary. To make the ownership clear, there could be some <code>Transaction</code> logged when A calls B.
	* </p>
	* <p>
	* <p>
	* Most of <code>Transaction</code> should be logged in the infrastructure level or framework level, which is
	* transparent to the application.
	* </p>
	* <p>
	* <p>
	* All CAT message will be constructed as a message tree and send to back-end for further analysis, and for monitoring.
	* Only <code>Transaction</code> can be a tree node, all other message will be the tree leaf.　The transaction without
	* other messages nested is an atomic transaction.
	* </p>
	*
	* @author Frankie Wu
	*/
public interface Transaction extends Message {
	/**
		* Add one nested child message to current transaction.
		*
		* @param message to be added
		*/
	public Transaction addChild(Message message);

	/**
		* Get all children message within current transaction.
		* <p>
		* <p>
		* Typically, a <code>Transaction</code> can nest other <code>Transaction</code>s, <code>Event</code>s and
		* <code>Heartbeat</code> s, while an <code>Event</code> or <code>Heartbeat</code> can't nest other messages.
		* </p>
		*
		* @return all children messages, empty if there is no nested children.
		*/
	public List<Message> getChildren();

	/**
		* How long the transaction took from construction to complete. Time unit is microsecond.
		*
		* @return duration time in microsecond
		*/
	public long getDurationInMicros();

	/**
		* How long the transaction took from construction to complete. Time unit is millisecond.
		*
		* @return duration time in millisecond
		*/
	public long getDurationInMillis();

	/**
		* set duration in millisecond.
		*
		* @return duration time in millisecond
		*/
	public void setDurationInMillis(long durationInMills);

	/**
		* Has children or not. An atomic transaction does not have any children message.
		*
		* @return true if child exists, else false.
		*/
	public boolean hasChildren();

	/**
		* Check if the transaction is stand-alone or belongs to another one.
		*
		* @return true if it's an root transaction.
		*/
	public boolean isStandalone();
}
