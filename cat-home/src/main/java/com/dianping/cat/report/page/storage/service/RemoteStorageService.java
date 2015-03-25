package com.dianping.cat.report.page.storage.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultSaxParser;
import com.dianping.cat.report.service.BaseRemoteModelService;

public class RemoteStorageService extends BaseRemoteModelService<StorageReport> {
	public RemoteStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	protected StorageReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
