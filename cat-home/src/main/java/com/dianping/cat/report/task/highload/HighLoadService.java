package com.dianping.cat.report.task.highload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.home.dal.report.Highload;
import com.dianping.cat.home.dal.report.HighloadDao;
import com.dianping.cat.home.dal.report.HighloadEntity;
import com.dianping.cat.report.task.highload.TransactionHighLoadUpdater.HighLoadReport;

public class HighLoadService {

	@Inject
	private HighloadDao m_dao;

	private HighLoadReport convertToReport(Highload highload) throws SAXException, IOException {
		HighLoadReport report = new HighLoadSqlUpdater().new HighLoadReport();
		String nameContext = highload.getTransactionNameContent();
		TransactionName name = DefaultSaxParser.parseEntity(TransactionName.class, nameContext);

		report.setName(name);
		report.setType(highload.getType());
		report.setDate(highload.getDate());
		report.setDomain(highload.getDomain());
		report.setWeight(highload.getWeight());
		return report;
	}

	public Map<String, List<HighLoadReport>> queryHighLoadReports(Date date) throws DalException {
		Map<String, List<HighLoadReport>> reports = new HashMap<String, List<HighLoadReport>>();
		List<Highload> highloads = m_dao.findByDate(date, HighloadEntity.READSET_FULL);

		for (Highload highload : highloads) {
			try {
				HighLoadReport report = convertToReport(highload);
				String type = report.getType();
				List<HighLoadReport> listForType = reports.get(type);

				if (listForType == null) {
					listForType = new ArrayList<HighLoadReport>();
					reports.put(type, listForType);
				}
				listForType.add(report);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return reports;
	}

}
