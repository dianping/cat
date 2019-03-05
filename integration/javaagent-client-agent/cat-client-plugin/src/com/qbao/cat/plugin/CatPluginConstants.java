/**
 * 
 */
package com.qbao.cat.plugin;


/**
 * @author andersen
 *
 */
public interface CatPluginConstants {
	/**
	 * 调用者地址参数名
	 */
	public static final String D_CLIENT_ADDR ="X-CAT-CLIENT-ADDR";
	/**
	 * 调用者domain参数名
	 */
	public static final String D_CLIENT_DOMAIN ="X-CAT-CLIENT-DOMAIN";
	/**
	 * 服务者地址参数名
	 */
	public static final String D_CALL_SERVER_ADDR ="X-CAT-SERVER-ADDR";
	/**
	 * 服务者domain参数名
	 */
	public static final String D_CALL_SERVER_DOMAIN ="X-CAT-SERVER-DOMAIN";
	
	/**
	 *  远程服务者Domain
	 */
	public static final String E_CALLEE_APP="Callee.app";
	/**
	 * 发起调用者Domain
	 */
	public static final String E_CALL_APP="Call.app";
	/**
	 * 远程服务者主机Addr
	 */
	public static final String E_CALLEE_ADDR="Callee.Addr";
	
	/**
	 * 远程调用标记参数名
	 */
	public static final String D_CALL_TRACE_MODE = "X-CAT-TRACE-MODE";
	
	/**
	 * dubbo服务端transaction-type
	 */
	public static final String TYPE_DUBBO_SERVER = "Call.Dubbo.Server";
	
	/**
	 * dubbo客户端transaction-type
	 */
	public static final String TYPE_DUBBO_CLIENT = "Call.Dubbo.Client";
	
	/**
	 * http服务端transaction-type
	 */
	public static final String TYPE_HTTP_SERVER = "Call.Http.Server";
	
	/**
	 * htt客户端transaction-type
	 */
	public static final String TYPE_HTTP_CLIENT = "Call.Http.Client";
	
	/**
	 * http服务端返回码
	 */
	public static final String TYPE_URL_SERVER_RESOPONSE_CODE = "URL.Server.Response.Code"; 
	
	/**
	 * http客户端代理信息，取自x-forwarded-for
	 */
	public static final String TYPE_URL_SERVER = "URL.Server";
	
	/**
	 * http客户端refer信息
	 */
	public static final String TYPE_URL_SERVER_REFERER = "URL.Server.Referer";
	
	/**
	 * http客户端user-agent信息
	 */
	public static final String TYPE_URL_SERVER_AGENT = "URL.Server.Agent";
	
	/**
	 * http客户端请求方式
	 */
	public static final String TYPE_URL_METHOD = "URL.Method";

	/**
	 * 数据库连接串
	 */
	public static final String TYPE_SQL_DATABASE = "SQL.Database";

	/**
	 * 调用方地址
	 */
	public static final String E_CLIENT_ADDR = "Client.addr";
}
