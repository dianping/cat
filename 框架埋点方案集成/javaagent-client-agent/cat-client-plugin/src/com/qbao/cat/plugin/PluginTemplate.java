/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qbao.cat.plugin;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public interface PluginTemplate {
	
	String POINTCUT_NAME = "scope()";
	
	void doBefore(JoinPoint joinPoint);

	
	void doAfter(JoinPoint joinPoint);

	
	Object doAround(ProceedingJoinPoint pjp) throws Throwable;

	
	void doReturn(JoinPoint joinPoint, Object retVal);

	
	void doThrowing(JoinPoint joinPoint, Throwable ex);
	
	/**
	 * 标识方法，保持空方法体即可，aspectj会在运行时动态生成里面内容
	 */
	void scope();
	
	
}
