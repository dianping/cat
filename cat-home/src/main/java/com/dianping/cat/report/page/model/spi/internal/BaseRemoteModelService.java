package com.dianping.cat.report.page.model.spi.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;

import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public abstract class BaseRemoteModelService<T> extends ModelServiceWithCalSupport implements ModelService<T> {
	@Inject
	private String m_host;

	private String m_name;

	@Inject
	private int m_port = 2281; // default admin port

	@Inject
	private String m_serviceUri = "/cat/r/model";

	public BaseRemoteModelService(String name) {
		m_name = name;
	}
	
	protected abstract T buildModel(String xml) throws SAXException, IOException;

	public URL buildUrl(ModelRequest request) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(64);

		for (Entry<String, String> e : request.getProperties().entrySet()) {
			if (e.getValue() != null) {
				sb.append('&');
				sb.append(e.getKey()).append('=').append(e.getValue());
			}
		}
		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", m_host, m_port, m_serviceUri, m_name,
		      request.getDomain(), request.getPeriod(), sb.toString());

		return new URL(url);
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());

		try {
			URL url = buildUrl(request);

			t.addData(url.toString());

			String xml = Files.forIO().readFrom(url.openStream(), "utf-8");
			int len = xml == null ? 0 : xml.length();

			t.addData("length", len);

			if (len > 0) {
				T report = buildModel(xml);

				response.setModel(report);
				t.setStatus(Message.SUCCESS);
			} else {
				t.setStatus("NoReport");
			}

		} catch (Exception e) {
			logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return !period.isHistorical();
	}

	public void setHost(String host) {
		m_host = host;
	}

	public void setPort(int port) {
		m_port = port;
	}

	public void setServiceUri(String serviceUri) {
		m_serviceUri = serviceUri;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name);
		sb.append(']');

		return sb.toString();
	}
}
