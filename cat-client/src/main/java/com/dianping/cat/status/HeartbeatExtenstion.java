package com.dianping.cat.status;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class HeartbeatExtenstion implements StatusExtension, Initializable {

	@Override
	public String getId() {
		return "MyTestId";
	}

	@Override
	public String getDescription() {
		return "MyDescription";
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> maps = new HashMap<String, String>();

		maps.put("key1", String.valueOf(1));
		maps.put("key2", String.valueOf(2));
		maps.put("key3", String.valueOf(3));

		return maps;
	}

	@Override
	public void initialize() throws InitializationException {
		StatusExtensionRegister.getInstance().register(this);
	}

}
