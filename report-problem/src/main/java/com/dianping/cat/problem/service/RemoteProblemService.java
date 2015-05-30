package com.dianping.cat.problem.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.problem.analyzer.ProblemAnalyzer;
import com.dianping.cat.problem.model.entity.ProblemReport;
import com.dianping.cat.problem.model.transform.DefaultSaxParser;
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
