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
package com.dianping.cat.report.page.business.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteBusinessService extends BaseRemoteModelService<BusinessReport> {

	public RemoteBusinessService() {
		super(BusinessAnalyzer.ID);
	}

	@Override
	protected BusinessReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}

	@Override
	public boolean isServersFixed() {
		return true;
	}

}
