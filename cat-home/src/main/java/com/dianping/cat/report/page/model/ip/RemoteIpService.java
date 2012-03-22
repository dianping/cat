package com.dianping.cat.report.page.model.ip;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.helper.Files;
import com.site.helper.Joiners;
import com.site.helper.Joiners.IBuilder;
import com.site.lookup.annotation.Inject;

public class RemoteIpService implements ModelService<IpReport> {
	@Inject
	private String m_host;

	@Inject
	private int m_port = 2281; // default admin port

	@Inject
	private String m_serviceUri = "/cat/r/model";

	URL buildUrl(ModelRequest request) throws MalformedURLException {
		String pairs = Joiners.by('&').prefixDelimiter()
		      .join(request.getProperties().entrySet(), new IBuilder<Map.Entry<String, String>>() {
			      @Override
			      public String asString(Entry<String, String> e) {
				      return e.getKey() + "=" + e.getValue();
			      }
		      });
		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", m_host, m_port, m_serviceUri, "ip",
		      request.getDomain(), request.getPeriod(), pairs);

		return new URL(url);
	}

	@Override
	public ModelResponse<IpReport> invoke(ModelRequest request) {
		ModelResponse<IpReport> response = new ModelResponse<IpReport>();

		try {
			URL url = buildUrl(request);
			String xml = Files.forIO().readFrom(url.openStream(), "utf-8");

			if (xml != null && xml.trim().length() > 0) {
				IpReport report = new DefaultXmlParser().parse(xml);

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

		return period.isCurrent() || period.isLast();
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
   public String getName() {
	   // TODO Auto-generated method stub
	   return null;
   }
}
