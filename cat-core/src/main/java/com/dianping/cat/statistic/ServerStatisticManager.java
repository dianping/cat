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
package com.dianping.cat.statistic;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.statistic.ServerStatistic.Statistic;

@Named
public class ServerStatisticManager {

	public ServerStatistic m_serverState = new ServerStatistic();

	private volatile Statistic m_currentStatistic = null;

	private volatile long m_currentMinute = -1;

	public void addBlockLoss(long total) {
		getCurrentStatistic().addBlockLoss(total);
	}

	public void addBlockTime(long total) {
		getCurrentStatistic().addBlockTime(total);
	}

	public void addBlockTotal(long total) {
		getCurrentStatistic().addBlockTotal(total);
	}

	public void addMessageDump(long total) {
		getCurrentStatistic().addMessageDump(total);
	}

	public void addMessageDumpLoss(long total) {
		getCurrentStatistic().addMessageDumpLoss(total);
	}

	public void addMessageSize(String domain, int size) {
		getCurrentStatistic().addMessageSize(domain, size);
	}

	public void addMessageTotal(long total) {
		getCurrentStatistic().addMessageTotal(total);
	}

	public void addMessageTotal(String domain, long total) {
		getCurrentStatistic().addMessageTotal(domain, total);
	}

	public void addMessageTotalLoss(long total) {
		getCurrentStatistic().addMessageTotalLoss(total);
	}

	public void addMessageTotalLoss(String domain, long total) {
		getCurrentStatistic().addMessageTotalLoss(domain, total);
	}

	public void addNetworkTimeError(long total) {
		getCurrentStatistic().addNetworkTimeError(total);
	}

	public void addPigeonTimeError(long total) {
		getCurrentStatistic().addPigeonTimeError(total);
	}

	public void addProcessDelay(double delay) {
		getCurrentStatistic().addProcessDelay(delay);
	}

	public Statistic findOrCreateState(long time) {
		return m_serverState.findOrCreate(time);
	}

	private Statistic getCurrentStatistic() {
		long time = System.currentTimeMillis();

		time = time - time % (60 * 1000);

		if (time != m_currentMinute) {
			synchronized (this) {
				if (time != m_currentMinute) {
					m_currentStatistic = m_serverState.findOrCreate(time);
					m_currentMinute = time;
				}
			}
		}
		return m_currentStatistic;
	}

	public void removeState(long time) {
		m_serverState.remove(time);
	}
}
