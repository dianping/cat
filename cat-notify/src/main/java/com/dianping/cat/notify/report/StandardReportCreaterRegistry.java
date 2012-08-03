package com.dianping.cat.notify.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.dianping.cat.notify.config.ConfigContext;
import com.dianping.cat.notify.model.entity.DefaultReports;
import com.dianping.cat.notify.model.entity.Report;
import com.dianping.cat.notify.model.entity.ScheduleReports;
import com.dianping.cat.notify.model.entity.TemplateReport;
import com.dianping.cat.notify.model.transform.DefaultDomParser;
import com.dianping.cat.notify.server.ContainerHolder;
import com.site.helper.Files;

public class StandardReportCreaterRegistry implements ReportCreaterRegistry {

	private final static Logger logger = LoggerFactory.getLogger(StandardReportCreaterRegistry.class);

	private Map<String, List<ReportCreater>> m_reportCreaters;// domain=>ReportCreater列表
	
	private DefaultReports m_defaultReportConfig;
	
	private Set<String> m_templateExcludeDomain = new HashSet<String>();;//不能使用默认ReportCreater的域名集合
	
	private ContainerHolder m_holder;

	@Override
	public boolean initReportCreaters(ConfigContext configContext,
			ContainerHolder holder) {
		try {
			m_reportCreaters = new HashMap<String, List<ReportCreater>>();
			
			String configPath = configContext.getProperty("schedulejob.config.path");
			DefaultDomParser parser = new DefaultDomParser();
			String source = Files.forIO().readFrom(new FileInputStream(configPath), "utf-8");
			ScheduleReports scheduleReports = parser.parse(source);
			ClassLoader loader = getClass().getClassLoader();
			
			m_defaultReportConfig = scheduleReports.getDefaultReports();
			m_holder = holder;
			
			initTemplateExcludeDomain();
			
			registerCustomerReportCreaters(holder, scheduleReports, loader);	
			
			return true;
		} catch (IOException e) {
			logger.error("fail to read the config file.", e);
		} catch (SAXException e) {
			logger.error("fail to parse the config file.", e);
		} catch (ClassNotFoundException e) {
			logger.error("fail to load the report create class.", e);
		} catch (IllegalAccessException e) {
			logger.error(
					"have not privalleage to load the report create class.", e);
		} catch (InstantiationException e) {
			logger.error("fail to  instantiation the report create class.", e);
		}
		return false;
	}
	
	private void initTemplateExcludeDomain(){
		if (m_defaultReportConfig == null) {
			return;
		}
		if (m_defaultReportConfig.getExclude() != null && m_defaultReportConfig.getExclude().trim().length() != 0) {
			String[] forbidDomains = m_defaultReportConfig.getExclude().split(",");
			for (String forbidDomain : forbidDomains) {
				m_templateExcludeDomain.add(forbidDomain);
			}
		}
	}

	private void registerCustomerReportCreaters(ContainerHolder holder,
			ScheduleReports schedule_Reports, ClassLoader loader)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		//init the customer report 
		List<Report> reports = schedule_Reports.getReports();
		for (Report report : reports) {
			String className = report.getCreateClass();
			Class<?> createClass = loader.loadClass(className);
			ReportCreater reportCreater = (ReportCreater) createClass.newInstance();
			ReportConfig reportConfig = new ReportConfig(report);
			if (!reportCreater.init(reportConfig, holder)) {
				logger.error("fail to init the report create:",report.toString());
				continue;
			}
			List<ReportCreater> reportList = m_reportCreaters.get(report.getDomain());
			if (reportList == null) {
				reportList = new ArrayList<ReportCreater>();
				m_reportCreaters.put(report.getDomain(), reportList);
			}
			reportList.add(reportCreater);
		}
	}

	@Override
	public List<ReportCreater> getReportCreaters(String domain) {
	    List<ReportCreater>  reportCreaterList = m_reportCreaters.get(domain);
	    if(reportCreaterList != null){
	    	return reportCreaterList;
	    }
	    if(m_templateExcludeDomain.contains(domain)){
	    	return null;
	    }
	    reportCreaterList = registerDefaultReportCreaters(domain);
		return reportCreaterList;
	}
	
	private List<ReportCreater> registerDefaultReportCreaters(String domain) {
		// init the template report
		try {
			List<TemplateReport> templateReportList = m_defaultReportConfig.getTemplateReports();
			if (templateReportList == null || templateReportList.size() == 0) {
				return null;
			}
			ClassLoader loader = getClass().getClassLoader();
			List<ReportCreater> reportList = new ArrayList<ReportCreater>();
			for (TemplateReport tReport : templateReportList) {
				String className = tReport.getCreateClass();
				Class<?> createClass = loader.loadClass(className);
				ReportCreater reportCreater = (ReportCreater) createClass
						.newInstance();
				ReportConfig reportConfig = new ReportConfig(tReport);
				if (!reportCreater.init(reportConfig, m_holder)) {
					logger.error("fail to init the report create:",
							tReport.toString());
					continue;
				}
				reportList.add(reportCreater);
			}
			m_reportCreaters.put(domain, reportList);
			return reportList;
		} catch (ClassNotFoundException e) {
			logger.error("fail to load the report create class.", e);
		} catch (IllegalAccessException e) {
			logger.error("have not privalleage to load the report create class.", e);
		} catch (InstantiationException e) {
			logger.error("fail to  instantiation the report create class.", e);
		}
		return null;
	}


}
