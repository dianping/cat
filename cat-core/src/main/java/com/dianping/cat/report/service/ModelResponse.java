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
package com.dianping.cat.report.service;

public class ModelResponse<M> {
	private Exception m_exception;

	private M m_model;

	public Exception getException() {
		return m_exception;
	}

	public void setException(Exception exception) {
		m_exception = exception;
	}

	public M getModel() {
		return m_model;
	}

	public void setModel(M model) {
		m_model = model;
	}

	@Override
	public String toString() {
		return String.format("ModelResponse[model=%s, exception=%s]", m_model, m_exception);
	}

}
