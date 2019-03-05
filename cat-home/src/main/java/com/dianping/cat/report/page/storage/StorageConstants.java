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
package com.dianping.cat.report.page.storage;

import java.util.Arrays;
import java.util.List;

public class StorageConstants {

	public static final String COUNT = "count";

	public static final String ERROR = "error";

	public static final String ERROR_PERCENT = "errorPercent";

	public static final String AVG = "avg";

	public static final String LONG = "long";

	public static final List<String> TITLES = Arrays.asList(COUNT, AVG, ERROR, LONG);

}
