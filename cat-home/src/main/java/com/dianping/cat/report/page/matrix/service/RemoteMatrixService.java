package com.dianping.cat.report.page.matrix.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteMatrixService extends BaseRemoteModelService<MatrixReport> {
	public RemoteMatrixService() {
		super(MatrixAnalyzer.ID);
	}

	@Override
	protected MatrixReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
