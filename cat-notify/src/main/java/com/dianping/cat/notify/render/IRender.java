package com.dianping.cat.notify.render;

import java.util.Map;

public interface IRender {

	boolean init();

	String fetchAll(String template, Map<String, Object> params);

}
