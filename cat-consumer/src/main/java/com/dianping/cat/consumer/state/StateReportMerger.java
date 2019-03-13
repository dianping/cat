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
package com.dianping.cat.consumer.state;

import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultMerger;

public class StateReportMerger extends DefaultMerger {

	public StateReportMerger(StateReport stateReport) {
		super(stateReport);
	}

	@Override
	protected void mergeDetail(Detail old, Detail detail) {
		old.setSize(detail.getSize() + old.getSize());
		old.setTotal(detail.getTotal() + old.getTotal());
		old.setTotalLoss(detail.getTotalLoss() + old.getTotalLoss());
	}

	@Override
	protected void mergeMachine(Machine old, Machine machine) {
		double oldCount = 0;
		double newCount = 0;
		if (old.getAvgTps() > 0) {
			oldCount = old.getTotal() / old.getAvgTps();
		}
		if (machine.getAvgTps() > 0) {
			newCount = machine.getTotal() / machine.getAvgTps();
		}
		double totalCount = oldCount + newCount;
		if (totalCount > 0) {
			old.setAvgTps((old.getTotal() + machine.getTotal()) / totalCount);
		}

		old.setTotal(old.getTotal() + machine.getTotal());
		old.setTotalLoss(old.getTotalLoss() + machine.getTotalLoss());
		old.setSize(old.getSize() + machine.getSize());

		old.setDump(old.getDump() + machine.getDump());
		old.setDumpLoss(old.getDumpLoss() + machine.getDumpLoss());
		old.setDelaySum(old.getDelaySum() + machine.getDelaySum());
		old.setDelayCount(old.getDelayCount() + machine.getDelayCount());

		old.setBlockTotal(old.getBlockTotal() + machine.getBlockTotal());
		old.setBlockLoss(old.getBlockLoss() + machine.getBlockLoss());
		old.setBlockTime(old.getBlockTime() + machine.getBlockTime());
		old.setPigeonTimeError(old.getPigeonTimeError() + machine.getPigeonTimeError());
		old.setNetworkTimeError(old.getNetworkTimeError() + machine.getNetworkTimeError());

		if (machine.getMaxTps() > old.getMaxTps()) {
			old.setMaxTps(machine.getMaxTps());
		}

		long count = old.getDelayCount();
		double sum = old.getDelaySum();
		if (count > 0) {
			old.setDelayAvg(sum / count);
		}
	}

	@Override
	protected void mergeMessage(Message old, Message message) {
		old.setTime(message.getTime());
		old.setId(message.getId());
		old.setTotal(old.getTotal() + message.getTotal());
		old.setTotalLoss(old.getTotalLoss() + message.getTotalLoss());
		old.setSize(old.getSize() + message.getSize());
		old.setDumpLoss(old.getDumpLoss() + message.getDumpLoss());
		old.setDump(old.getDump() + message.getDump());
		old.setDelayCount(old.getDelayCount() + message.getDelayCount());
		old.setDelaySum(old.getDelaySum() + message.getDelaySum());
		old.setBlockTotal(old.getBlockTotal() + message.getBlockTotal());
		old.setBlockLoss(old.getBlockLoss() + message.getBlockLoss());
		old.setBlockTime(old.getBlockTime() + message.getBlockTime());
		old.setPigeonTimeError(old.getPigeonTimeError() + message.getPigeonTimeError());
		old.setNetworkTimeError(old.getNetworkTimeError() + message.getNetworkTimeError());
	}

	@Override
	protected void mergeProcessDomain(ProcessDomain old, ProcessDomain processDomain) {
		old.getIps().addAll(processDomain.getIps());
		old.setSize(old.getSize() + processDomain.getSize());
		old.setTotal(old.getTotal() + processDomain.getTotal());
		old.setTotalLoss(old.getTotalLoss() + processDomain.getTotalLoss());
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		super.visitStateReport(stateReport);

		StateReport report = getStateReport();
		report.setDomain(stateReport.getDomain());
		report.setStartTime(stateReport.getStartTime());
		report.setEndTime(stateReport.getEndTime());
	}

}
