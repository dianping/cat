/**
 * 
 */
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
package com.qbao.cat.plugin.db.nosql;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.mongodb.client.MongoCollection;
import com.qbao.cat.plugin.DefaultPluginTemplate;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class NewMongoPluginTemplate extends DefaultPluginTemplate {
	
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.PluginTemplate#scope()
	 */
	@Override
	@Pointcut
	public void scope() {
	}

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.DefaultPluginTemplate#beginLog(org.aspectj.lang.ProceedingJoinPoint)
	 */
	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		Transaction transaction = null;		
		transaction = newTransaction("MongoDB", String.valueOf(pjp.getSignature().toShortString()));
		MongoCollection collector = (MongoCollection) pjp.getTarget();
		Cat.logEvent("DB.Collection", collector.getNamespace().getFullName());
		Cat.logEvent("Method", pjp.getSignature().toString());
		return transaction;
	}

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.DefaultPluginTemplate#endLog(com.dianping.cat.message.Transaction, java.lang.Object, java.lang.Object[])
	 */
	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
		// TODO Auto-generated method stub

	}

}
