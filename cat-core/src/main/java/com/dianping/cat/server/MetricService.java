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

    boolean insert(List<MetricEntity> entities);

    Map<Long, Double> query(QueryParameter parameter);

    List<ServerGroupByEntity> queryByFields(QueryParameter parameter);

    List<String> queryEndPoints(String category);

    List<String> queryEndPoints(String category, String tag, List<String> keywords);

    List<String> queryEndPointsByTag(String category, List<String> tags);

    List<String> queryMeasurements(String category);

    List<String> queryMeasurements(String category, List<String> endPoints);

    List<String> queryMeasurements(String category, String measurement, List<String> endPoints);

    List<String> queryTagValues(String category, String measurement, String tag);

}
