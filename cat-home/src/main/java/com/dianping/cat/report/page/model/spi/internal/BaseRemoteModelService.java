package com.dianping.cat.report.page.model.spi.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.SAXException;

import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.helper.Files;
import com.site.helper.Joiners;
import com.site.helper.Joiners.IBuilder;
import com.site.lookup.annotation.Inject;

public abstract class BaseRemoteModelService<T> implements ModelService<T> {
	@Inject
	private String m_host;

	@Inject
	private int m_port = 2281; // default admin port

	@Inject
	private String m_serviceUri = "/cat/r/model";

	private String m_name;

	public BaseRemoteModelService(String name) {
		m_name = name;
	}

	protected URL buildUrl(ModelRequest request) throws MalformedURLException {
		String pairs = Joiners.by('&').prefixDelimiter()
		      .join(request.getProperties().entrySet(), new IBuilder<Map.Entry<String, String>>() {
			      @Override
			      public String asString(Entry<String, String> e) {
				      return e.getKey() + "=" + e.getValue();
			      }
		      });
		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", m_host, m_port, m_serviceUri, m_name,
		      request.getDomain(), request.getPeriod(), pairs);

		return new URL(url);
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();

		try {
			URL url = buildUrl(request);
			String xml = Files.forIO().readFrom(url.openStream(), "utf-8");

			if (xml != null && xml.trim().length() > 0) {
				T report = parse(xml);

				response.setModel(report);
			}
		} catch (Exception e) {
			response.setException(e);
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return !period.isHistorical();
	}

	protected abstract T parse(String xml) throws SAXException, IOException;

	public void setHost(String host) {
		m_host = host;
	}

	public void setPort(int port) {
		m_port = port;
	}

	public void setServiceUri(String serviceUri) {
		m_serviceUri = serviceUri;
	}
}
