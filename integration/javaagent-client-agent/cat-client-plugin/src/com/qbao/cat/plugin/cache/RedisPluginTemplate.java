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
package com.qbao.cat.plugin.cache;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.DefaultPluginTemplate;

import redis.clients.jedis.BinaryClient;

@Aspect
public abstract class RedisPluginTemplate extends DefaultPluginTemplate {

	@Override
	@Pointcut
	public void scope() {

	}

	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}


	@Override
	public Transaction beginLog(ProceedingJoinPoint pjp) {
		Transaction transaction = null;
		BinaryClient jedis = (BinaryClient)pjp.getTarget();
		if (jedis != null){
			transaction = Cat.newTransaction("Cache.Redis_" + jedis.getHost(), pjp.getSignature().toString());
		}
		return transaction;
	}

	@Override
	public void endLog(Transaction transaction, Object retVal, Object... params) {

	}

}
