package com.dianping.cat.report.page.storage;

import java.util.Arrays;
import java.util.List;

public class StorageConstants {

	public static final String SQL_TYPE = "SQL";

	public static final String CACHE_TYPE = "Cache";

	public static final String COUNT = "count";

	public static final String ERROR = "error";

	public static final String ERROR_PERCENT = "errorPercent";

	public static final String AVG = "avg";

	public static final String LONG = "long";

	public static final List<String> TITLES = Arrays.asList(COUNT, AVG, ERROR, LONG);

	public static final int DEFAULT_MINUTE_COUNT = 8;

	public static final int DEFAULT_TOP_COUNT = 10;

	public static final List<String> CACHE_METHODS = Arrays.asList("add", "get", "mGet", "remove");

	public static final List<String> SQL_METHODS = Arrays.asList("select", "delete", "insert", "update");

	public static final String FIELD_SEPARATOR = ";";

	public static final String IP_FORMAT = "${ip}";

	public static final String ID_FORMAT = "${id}";

}
