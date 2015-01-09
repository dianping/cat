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

	/**
	 * Pigeon Transation Type
	 */
	public static final String TYPE_CALL = "Call";

	public static final String TYPE_RESULT = "Result";

	public static final String TYPE_TimeOut = "PigeonTimeOut";

	public static final String TYPE_SERVICE = "Service";

	public static final String TYPE_REMOTE_CALL = "RemoteCall";

	public static final String TYPE_REQUEST = "Request";

	public static final String TYPE_RESPONSE = "Response";

	/**
	 * Pigeon Event Type, it is used to record the param
	 */

	public static final String TYPE_PIGEON_REQUEST = "PigeonRequest";

	public static final String TYPE_PIGEON_RESPONSE = "PigeonResponse";

	/**
	 * Pigeon Event name
	 */
	public static final String NAME_REQUEST = "PigeonRequest";

	public static final String NAME_RESPONSE = "PigeonResponse";

	public static final String NAME_TIME_OUT = "ClientTimeOut";

	/**
	 * Pigeon Context Info
	 */
    public static final String ROOT_MESSAGE_ID = "RootMessageId";
    public static final String CURRENT_MESSAGE_ID = "CurrentMessageId";
    public static final String SERVER_MESSAGE_ID = "ServerMessageId";
    public static final String APP = "app";

	public static final String TYPE_SQL = "SQL";

    public static final String TYPE_SQL_METHOD = "SQL.Method";

    public static final String TYPE_SQL_DATABASE = "SQL.Database";

	public static final String TYPE_SQL_PARAM = "SQL.PARAM";

	public static final String TYPE_URL = "URL";
    public static final String NAME_URL_CLIENT = "URL.Client";
    public static final String NAME_URL_METHOD = "URL.Method";

	public static final String TYPE_URL_FORWARD = "URL.Forward";
    public static final String NAME_URL_FORWARD_METHOD = "URL.Forward.Method";
    public static final String TYPE_ESB_CALL = "ESBCall";
    public static final String TYPE_ESB_CALL_SERVER = "ESBCall.server";
    public static final String TYPE_ESB_CALL_APP = "ESBCall.app";
    public static final String TYPE_ESB_SERVICE = "ESBService";
    public static final String TYPE_ESB_SERVICE_CLIENT = "ESBService.client";
    public static final String TYPE_ESB_SERVICE_APP = "ESBService.app";

    public static final String TYPE_SOA_CALL = "SOACall";
    public static final String TYPE_SOA_CALL_SERVER = "SOACall.server";
    public static final String TYPE_SOA_CALL_APP = "SOACall.app";

    public static final String TYPE_SOA_SERVICE = "SOAService";
    public static final String TYPE_SOA_SERVICE_CLIENT = "SOAService.client";
    public static final String TYPE_SOA_SERVICE_APP = "SOAService.app";

	public static final String TYPE_ACTION = "Action";

	public static final String TYPE_METRIC = "MetricType";

	public static final String TYPE_TRACE = "TraceMode";

	public static final int ERROR_COUNT = 100;

	public static final int SUCCESS_COUNT = 1000;

}