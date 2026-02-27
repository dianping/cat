package com.dianping.cat.configuration.source;

import java.io.File;
import java.io.FileInputStream;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.model.ClientConfigHelper;
import com.dianping.cat.configuration.model.entity.ClientConfig;

public class ClientXmlSource implements ConfigureSource<ClientConfig> {
	@Override
	public ClientConfig getConfig() throws Exception {
		File clientXmlFile = new File(Cat.getCatHome(), CatClientConstants.CLIENT_XML);

		if (clientXmlFile.exists()) {
			ClientConfig clientConfig = ClientConfigHelper.fromXml(new FileInputStream(clientXmlFile));

			return clientConfig;
		}

		return null;
	}

	@Override
	public int getOrder() {
		return 220;
	}
}
