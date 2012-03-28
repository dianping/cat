package com.dianping.cat.job.configuration;

import java.io.File;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.job.configuration.model.entity.Config;
import com.dianping.cat.job.configuration.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class HdfsConfig implements Initializable {

	private String m_serverUrl;
	private String m_dumpUrl;

	public String getServerUrl() {
		return m_serverUrl;
	}

	public String getDumpUrl() {
		return m_dumpUrl;
	}

	@Override
	public void initialize() throws InitializationException {
		String path = "/data/appdatas/cat/hdfs.xml";
		try {
			String xml = Files.forIO().readFrom(new File(path), "utf-8");
			Config config = new DefaultXmlParser().parse(xml);
			m_serverUrl = config.getHdfses().get("data").getPath();
			m_dumpUrl = config.getHdfses().get("dump").getPath();
		} catch (Exception e) {
			throw new InitializationException("Init hdfs file config error", e);
		}
	}
}
