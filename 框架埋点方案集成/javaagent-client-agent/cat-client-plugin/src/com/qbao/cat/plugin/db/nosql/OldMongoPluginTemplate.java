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
import com.mongodb.DBCollection;
import com.qbao.cat.plugin.DefaultPluginTemplate;

/**
 * 根据 <a href="https://stackoverflow.com/questions/29364787/mongocollection-versus-dbcollection-java">
 * https://stackoverflow.com/questions/29364787/mongocollection-versus-dbcollection-java</a>介绍；
 * 3.0以后的客户端版本一般用MongoCollection, 而不再用DbCollection
 * @author andersen
 *
 */
@Aspect
public abstract class OldMongoPluginTemplate extends DefaultPluginTemplate {
	@Pointcut
	public void scope() {
	}

	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}

	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		Transaction transaction = null;
		transaction = newTransaction("MongoDB", String.valueOf(pjp.getSignature().toShortString()));
		DBCollection collector = (DBCollection) pjp.getTarget();
		Cat.logEvent("Host", collector.getDB().getMongo().getServerAddressList().toString());
		Cat.logEvent("Connection", collector.toString());
		Cat.logEvent("DB", collector.getDB().getName());
		Cat.logEvent("Method", pjp.getSignature().toString());
		return transaction;
	}

	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
	}
}