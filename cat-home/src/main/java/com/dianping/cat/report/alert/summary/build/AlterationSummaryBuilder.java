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
package com.dianping.cat.report.alert.summary.build;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;

@Named(type = SummaryBuilder.class, value = AlterationSummaryBuilder.ID)
public class AlterationSummaryBuilder extends SummaryBuilder {

	public static final String ID = "AlterationSummaryContentGenerator";

	@Inject
	private AlterationDao m_alterationDao;

	@Override
	public Map<Object, Object> generateModel(String domain, Date date) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();

		try {
			List<Alteration> alterations = m_alterationDao
									.findByDomainAndTime(getStartDate(date), date, domain,	AlterationEntity.READSET_FULL);

			dataMap.put("count", alterations.size());
			dataMap.put("items", alterations);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return dataMap;
	}

	@Override
	public String getID() {
		return ID;
	}

	private Date getStartDate(Date date) {
		return new Date(date.getTime() - AlertSummaryExecutor.ALTERATION_DURATION);
	}

	@Override
	protected String getTemplateAddress() {
		return "alterationInfo.ftl";
	}

}
