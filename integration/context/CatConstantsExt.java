package com.dianping.cat.context.context;

import com.dianping.cat.CatConstants;

/**
 * 1、继承、扩展CatConstants常量类，添加一些常用的Type
 * 2、添加header常量，用于http协议传输rootId、parentId、childId三个context属性
 * @author soar
 * @date 2019-01-10
 */
public class CatConstantsExt extends CatConstants {

    /**
     * Type 常量
     */
    public static final String Type_URL_METHOD = "URL.method";
    public static final String Type_URL_CLIENT = "URL.client";
    public static final String Type_URL_FORWORD = "URL.forword";

    public static final String Type_Service = "Service";
    public static final String Type_Service_METHOD= "Service.method";
    public static final String Type_Service_CLIENT = "Service.client";

    public static final String Type_SQL = "SQL";
    public static final String Type_SQL_METHOD= "SQL.method";
    public static final String Type_SQL_CLIENT = "SQL.client";

    public static final String Type_Cache = "Cache";
    public static final String Type_Cache_METHOD= "Cache.method";
    public static final String Type_Cache_CLIENT = "Cache.client";

    public static final String Type_Call = "Call";
    public static final String Type_Call_METHOD= "Call.method";
    public static final String Type_Call_CLIENT = "Call.client";

    /**
     * http header 常量
     */
    public static final String CAT_HTTP_HEADER_ROOT_MESSAGE_ID = "X-CAT-ROOT-MESSAGE-ID";
    public static final String CAT_HTTP_HEADER_PARENT_MESSAGE_ID = "X-CAT-ROOT-PARENT-ID";
    public static final String CAT_HTTP_HEADER_CHILD_MESSAGE_ID = "X-CAT-ROOT-CHILD-ID";
}
