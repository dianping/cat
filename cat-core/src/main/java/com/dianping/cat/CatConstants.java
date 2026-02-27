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

public class CatConstants {
	/**
		* Cat Json length
		*/
	public static final int MAX_LENGTH = 1000;

	public static final int MAX_ITEM_LENGTH = 50;

	/**
		* Cat instrument attribute names
		*/
	public static final String CAT_STATE = "cat-state";

	public static final String CAT_PAGE_URI = "cat-page-uri";

	public static final String CAT_PAGE_TYPE = "cat-page-type";

	/**
		* Pigeon Transation Type
		*/
	public static final String TYPE_CALL = "Call";

	public static final String TYPE_RESULT = "Result";

	public static final String TYPE_TimeOut = "PigeonTimeOut";

	public static final String TYPE_SERVICE = "Service";

	public static final String TYPE_REMOTE_CALL = "RemoteCall";

	public static final String TYPE_REQUEST = "Request";

	public static final String TYPE_RESPONSE = "Respone";

	/**
		* Pigeon Event Type, it is used to record the param
		*/

	public static final String TYPE_PIGEON_REQUEST = "PigeonRequest";

	public static final String TYPE_PIGEON_RESPONSE = "PigeonRespone";

	/**
		* Pigeon Event name
		*/
	public static final String NAME_REQUEST = "PigeonRequest";

	public static final String NAME_RESPONSE = "PigeonRespone";

	public static final String NAME_TIME_OUT = "ClientTimeOut";

	/**
		* Pigeon Context Info
		*/
	public static final String PIGEON_ROOT_MESSAGE_ID = "RootMessageId";

	public static final String PIGEON_CURRENT_MESSAGE_ID = "CurrentMessageId";

	public static final String PIGEON_SERVER_MESSAGE_ID = "ServerMessageId";

	public static final String PIGEON_RESPONSE_MESSAGE_ID = "ResponseMessageId";

	public static final String TYPE_SQL = "SQL";

	public static final String TYPE_SQL_PARAM = "SQL.PARAM";

	public static final String TYPE_URL = "URL";

	public static final String TYPE_URL_FORWARD = "URL.Forward";

	public static final String TYPE_ACTION = "Action";

	public static final String TYPE_METRIC = "MetricType";

	public static final String TYPE_TRACE = "TraceMode";

	public static final int ERROR_COUNT = 100;

	public static final int SUCCESS_COUNT = 1000;

	public static final String CAT_SYSTEM = "System";

	public static final char BATCH_FLAG = '@';

	public static final String SPLIT = ";";

	public static final String OTHERS = "OTHERS";

	public static final String CAT_HOME_DEFAULT_DIR = "/data/appdatas/cat/";

}