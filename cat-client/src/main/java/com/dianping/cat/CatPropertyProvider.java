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
package com.dianping.cat;

import java.util.ServiceLoader;

/**
 * 应用属性配置SPI
 *  此为配置的接口和扩展点，如具体应用要扩展，请实现此接口并在应用如下文件中指定实现类
 *  META-INF\services\com.dianping.cat.CatPropertyProvider
 * @author qxo
 *
 */
public interface CatPropertyProvider {
	
	public static final CatPropertyProvider INST = ServiceLoader.load(CatPropertyProvider.class).iterator().next();

	public String getProperty(String name, String defaultValue);
}
