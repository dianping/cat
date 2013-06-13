package com.dianping.cat.report.page.model.logview;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteLogViewService extends BaseRemoteModelService<String> {
	public RemoteLogViewService() {
		super("logview");
	}

	@Override
	protected String buildModel(String content) throws SAXException, IOException {
		return content;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		if (period.isHistorical()) {
			long time = Long.parseLong(request.getProperty("timestamp"));
			long current = System.currentTimeMillis();
			long currentHour = current - current % TimeUtil.ONE_HOUR;

			if (time == currentHour - 2 * TimeUtil.ONE_HOUR) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

}
