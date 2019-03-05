/**
 * 
 */
package com.qbao.cat.plugin.remote;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.qbao.cat.plugin.CatPluginConstants;
import com.qbao.cat.plugin.DefaultPluginTemplate;
import com.qbao.cat.plugin.constants.MyCatConstants;

/**
 * @author andersen
 *
 */
public abstract class ServerPluginTemplate<T> extends DefaultPluginTemplate{

	public abstract boolean isEnableTrace(T request);

	protected void logClientTrace(String messsageId, String parentId, String rootId) {
		RemoteContext context = new RemoteContext();
		context.addProperty(Cat.Context.CHILD, messsageId);
		context.addProperty(Cat.Context.PARENT, parentId);
		context.addProperty(Cat.Context.ROOT, rootId);
		Cat.logRemoteCallServer(context);
	}

	protected void logClientInfo(String clientAddr, String clientDomain) {
		Cat.logEvent(MyCatConstants.E_CLIENT_ADDR,clientAddr);
		Cat.logEvent(MyCatConstants.E_CALL_APP,clientDomain);
	}

	public static class RemoteContext implements Cat.Context {

		private Map<String, String> traceData = new HashMap<String, String>();

		@Override
		public void addProperty(String key, String value) {
			traceData.put(key, value);
		}

		@Override
		public String getProperty(String key) {
			return traceData.get(key);
		}

		public Map<String, String> getAllData() {
			return traceData;
		}

	}

}
