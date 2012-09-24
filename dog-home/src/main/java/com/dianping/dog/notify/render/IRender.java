package com.dianping.dog.notify.render;

import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;

public interface IRender extends LogEnabled{

	boolean init();

	String fetchAll(String template, Map<String, Object> params);

}
