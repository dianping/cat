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
package com.dianping.cat.message;

public interface MessageTree extends Cloneable {
	public String getDomain();

	public String getHostName();

	public String getIpAddress();

	public Message getMessage();

	public String getMessageId();

	public String getParentMessageId();

	public String getRootMessageId();

	public String getSessionToken();

	public String getThreadGroupName();

	public String getThreadId();

	public String getThreadName();

	public void setDomain(String domain);

	public void setHostName(String hostName);

	public void setIpAddress(String ipAddress);

	public void setMessage(Message message);

	public void setMessageId(String messageId);

	public void setParentMessageId(String parentMessageId);

	public void setRootMessageId(String rootMessageId);

	public void setSessionToken(String session);

	public void setThreadGroupName(String name);

	public void setThreadId(String threadId);

	public void setThreadName(String id);

}
