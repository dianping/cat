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
package com.qbao.cat.plugin.common;

import java.util.StringTokenizer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.DefaultPluginTemplate;

@Aspect
public abstract class CommonPluginTemplate extends DefaultPluginTemplate {

	@Override
	@Pointcut
	public void scope() {}

	
	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}


	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		StringBuilder type = new StringBuilder();
		String packageStr = pjp.getSignature().getDeclaringType().getPackage().getName();
		StringTokenizer st = new StringTokenizer(packageStr, ".");
		for(int i=0;i<2;i++){
			type.append(st.nextToken());
			type.append(".");
		}
		type.append("Method");
		Transaction transaction = Cat.newTransaction(type.toString(),pjp.getSignature().toString());
		return transaction;
	}
	
	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {}

}
