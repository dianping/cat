package com.dianping.cat.report.page.model.logview;

import java.io.File;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.helper.Files;
import com.site.lookup.annotation.Inject;

public class LocalLogViewService implements ModelService<String> {
	@Inject
	private MessagePathBuilder m_builder;

	@Override
	public boolean isEligable(ModelRequest request) {
		return !request.getPeriod().isHistorical();
	}

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		File baseDir = m_builder.getLogViewBaseDir();
		String path = request.getProperty("path");
		File file = new File(baseDir, path);
		ModelResponse<String> response = new ModelResponse<String>();

		if (file.exists()) {
			try {
				String html = Files.forIO().readFrom(file, "utf-8");

				response.setModel(html);
			} catch (Exception e) {
				response.setException(e);
			}
		}

		return response;
	}
}
