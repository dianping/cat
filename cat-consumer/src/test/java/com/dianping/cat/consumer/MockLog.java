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
package com.dianping.cat.consumer;

import org.codehaus.plexus.logging.Logger;

public class MockLog implements Logger {

	@Override
	public void debug(String message) {

	}

	@Override
	public void debug(String message, Throwable throwable) {

	}

	@Override
	public boolean isDebugEnabled() {

		return false;
	}

	@Override
	public void info(String message) {

	}

	@Override
	public void info(String message, Throwable throwable) {

	}

	@Override
	public boolean isInfoEnabled() {

		return false;
	}

	@Override
	public void warn(String message) {

	}

	@Override
	public void warn(String message, Throwable throwable) {

	}

	@Override
	public boolean isWarnEnabled() {

		return false;
	}

	@Override
	public void error(String message) {

	}

	@Override
	public void error(String message, Throwable throwable) {

	}

	@Override
	public boolean isErrorEnabled() {

		return false;
	}

	@Override
	public void fatalError(String message) {

	}

	@Override
	public void fatalError(String message, Throwable throwable) {

	}

	@Override
	public boolean isFatalErrorEnabled() {

		return false;
	}

	@Override
	public Logger getChildLogger(String name) {

		return null;
	}

	@Override
	public int getThreshold() {

		return 0;
	}

	@Override
	public void setThreshold(int threshold) {

	}

	@Override
	public String getName() {

		return null;
	}

}