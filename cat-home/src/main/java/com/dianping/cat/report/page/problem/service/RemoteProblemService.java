package com.dianping.cat.report.page.problem.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteProblemService extends BaseRemoteModelService<ProblemReport> {
	public RemoteProblemService() {
		super(ProblemAnalyzer.ID);
	}

	@Override
	protected ProblemReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
