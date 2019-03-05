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
package com.dianping.cat.server;

import java.util.List;
import java.util.Map;

public interface MetricService {

	public boolean insert(List<MetricEntity> entities);

	public Map<Long, Double> query(QueryParameter parameter);

	public List<ServerGroupByEntity> queryByFields(QueryParameter parameter);

	public List<String> queryEndPoints(String category);

	public List<String> queryEndPoints(String category, String tag, List<String> keywords);

	public List<String> queryEndPointsByTag(String category, List<String> tags);

	public List<String> queryMeasurements(String category);

	public List<String> queryMeasurements(String category, List<String> endPoints);

	public List<String> queryMeasurements(String category, String measurement, List<String> endPoints);

	public List<String> queryTagValues(String category, String measurement, String tag);

}
