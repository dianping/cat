package com.dianping.cat.report.page.model.problem;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class RemoteProblemService extends BaseRemoteModelService<ProblemReport> {
	public RemoteProblemService() {
		super("problem");
	}

	@Override
	protected ProblemReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
