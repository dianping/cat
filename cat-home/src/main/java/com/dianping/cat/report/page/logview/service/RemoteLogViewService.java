package com.dianping.cat.report.page.logview.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.xml.sax.SAXException;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class RemoteLogViewService extends BaseRemoteModelService<String> {
	
	private ServerConfigManager m_manager;
	
	public RemoteLogViewService() {
		super("logview");
	}

	@Override
	protected String buildModel(String content) throws SAXException, IOException {
		return content;
	}

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		ModelResponse<String> response = new ModelResponse<String>();
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());

		try {
			URL url = buildUrl(request);

			t.addData(url.toString());

			InputStream in = Urls.forIO().connectTimeout(1000).readTimeout(5000).openStream(url.toExternalForm());
			GZIPInputStream gzip = new GZIPInputStream(in);
			String xml = Files.forIO().readFrom(gzip, "utf-8");

			int len = xml == null ? 0 : xml.length();

			t.addData("length", len);

			if (len > 0) {
				String report = buildModel(xml);

				response.setModel(report);
				t.addData("hit", "true");
			}
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(Message.SUCCESS);
		} finally {
			t.complete();
		}
		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		if (m_manager.isHdfsOn()) {
			ModelPeriod period = request.getPeriod();

			if (period.isHistorical()) {
				long time = Long.parseLong(request.getProperty("timestamp"));
				long current = System.currentTimeMillis();
				long currentHour = current - current % TimeHelper.ONE_HOUR;

				if (time == currentHour - 2 * TimeHelper.ONE_HOUR) {
					return true;
				}
			} else {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	public void setManager(ServerConfigManager manager) {
   	m_manager = manager;
   }
	
}
