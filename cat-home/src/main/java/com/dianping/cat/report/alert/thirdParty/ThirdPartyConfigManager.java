package com.dianping.cat.report.alert.thirdParty;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Par;
import com.dianping.cat.home.alert.thirdparty.entity.Socket;
import com.dianping.cat.home.alert.thirdparty.entity.ThirdPartyConfig;
import com.dianping.cat.home.alert.thirdparty.transform.DefaultSaxParser;

public class ThirdPartyConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private ThirdPartyConfig m_thirdPartyConfig;

	private static final String CONFIG_NAME = "thirdPartyConfig";

	public ThirdPartyConfig getConfig() {
		return m_thirdPartyConfig;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_thirdPartyConfig = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_thirdPartyConfig = DefaultSaxParser.parse(content);
				m_configId = config.getId();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_thirdPartyConfig == null) {
			m_thirdPartyConfig = new ThirdPartyConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_thirdPartyConfig = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public boolean insert(Http http) {
		Http hp = null;
		String url = http.getUrl();

		for (Http h : m_thirdPartyConfig.getHttps()) {
			if (h.getUrl().equals(url)) {
				h.setType(http.getType());
				h.setDomain(http.getDomain());
				h.getPars().clear();

				for (Par par : http.getPars()) {
					h.addPar(par);
				}
				hp = h;
			}
		}
		if (hp == null) {
			m_thirdPartyConfig.addHttp(http);
		}
		return storeConfig();
	}

	public boolean insert(Socket socket) {
		Socket sk = null;
		String url = socket.getIp();
		int port = socket.getPort();

		for (Socket s : m_thirdPartyConfig.getSockets()) {
			if (s.getIp().equals(url) && s.getPort() == port) {
				sk = s;
				s.setDomain(socket.getDomain());
			}
		}
		if (sk == null) {
			m_thirdPartyConfig.addSocket(socket);
		}
		return storeConfig();
	}

	public boolean remove(String id, String type) {
		if ("http".equals(type)) {
			Http hp = null;

			for (Http h : m_thirdPartyConfig.getHttps()) {
				if (h.getUrl().equals(id)) {
					hp = h;
				}
			}
			if (hp != null) {
				m_thirdPartyConfig.getHttps().remove(hp);
			}
		} else if ("socket".equals(type)) {
			Socket sk = null;
			String[] info = id.split("-");
			String ip = info[0];
			int port = Integer.valueOf(info[1]);

			for (Socket s : m_thirdPartyConfig.getSockets()) {
				if (s.getIp().equals(ip) && s.getPort() == port) {
					sk = s;
				}
			}
			if (sk != null) {
				m_thirdPartyConfig.getSockets().remove(sk);
			}
		}
		return storeConfig();
	}

	public List<Socket> querSockets() {
		return m_thirdPartyConfig.getSockets();
	}

	public List<Http> queryHttps() {
		return m_thirdPartyConfig.getHttps();
	}

	public Http queryHttp(String url) {
		List<Http> https = m_thirdPartyConfig.getHttps();

		for (Http http : https) {
			if (http.getUrl().equals(url)) {
				return http;
			}
		}
		return null;
	}

	public Socket querySocket(String id) {
		String[] infos = id.split("-");
		String ip = infos[0];
		int port = Integer.parseInt(infos[1]);
		List<Socket> sockets = m_thirdPartyConfig.getSockets();

		for (Socket socket : sockets) {
			if (socket.getIp().equals(ip) && socket.getPort() == port) {
				return socket;
			}
		}
		return null;
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_thirdPartyConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}
