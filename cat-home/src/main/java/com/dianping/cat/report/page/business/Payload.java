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
package com.dianping.cat.report.page.business;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.site.lookup.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type = Type.Domain.getName();

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("timeRange")
	private int m_timeRange;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.BUSINESS);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.BUSINESS);
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public Date getStartDate() {
		Date start = null;
		try {
			if (m_customStart != null && m_customStart.length() > 0) {
				start = m_format.parse(m_customStart);
				start = buildDate(start);
			} else {
				start = new Date(getEndDate().getTime() - TimeHelper.ONE_HOUR * getTimeRange(4));
			}
			return start;
		} catch (Exception e) {
			return TimeHelper.getCurrentHour(1 - getTimeRange(4));
		}

	}

	public int getTimeRange(int d) {
		if (m_timeRange == 0) {
			return d;
		}
		return m_timeRange;
	}

	private Date buildDate(Date date) {
		long time = date.getTime();
		return new Date(time - time % TimeHelper.ONE_HOUR + m_step * TimeHelper.ONE_HOUR);
	}

	public Date getEndDate() {
		Date end = null;
		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				end = m_format.parse(m_customEnd);
			} else {
				end = TimeHelper.getCurrentHour(1);
			}

			return buildDate(end);
		} catch (Exception e) {
			return TimeHelper.getCurrentHour(1);
		}
	}

	public String getName() {
		if (StringUtils.isEmpty(m_name)) {
			return Constants.CAT;
		} else {
			return m_name;
		}
	}

	public void setName(String name) {
		m_name = name;
	}

	public int getTimeRange() {
		return m_timeRange;
	}

	public void setTimeRange(int timeRange) {
		m_timeRange = timeRange;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}

}
