package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class DailyTaskProducer implements Runnable, Initializable {

	private static final int TYPE_DAILY = 1;

	private static final int NUM_OF_THREADS = 2;

	private static final int PERIOD = 24 * 60 * 60 * 1000;
	
	private static final Set<String> dailyReportNameSet = new HashSet<String>() ;
	
	@Inject
	private TaskDao taskDao;

	@Inject
	private ReportDao reportDao;

	@Inject
	private DailyreportDao dailyReportDao;

	private Logger logger;

	private ScheduledExecutorService service = Executors.newScheduledThreadPool(NUM_OF_THREADS);

	static{
		dailyReportNameSet.add("event");
		dailyReportNameSet.add("transaction");
		dailyReportNameSet.add("problem");
	}

	@Override
	public void run() {
		Date now = new Date();
		Date today = TaskHelper.todayZero(now);
		Date tomorrow = TaskHelper.tomorrowZero(now);
		DailyTask dailyTask = new DailyTask(today, tomorrow);
		// schedule a task:next day's 00:04
		Date startDateOfNextTask = TaskHelper.startDateOfNextTask(now, 1);
		long delay = startDateOfNextTask.getTime() - now.getTime();
		service.scheduleAtFixedRate(dailyTask, delay, PERIOD, TimeUnit.MILLISECONDS);
	}


	private class DailyTask implements Runnable {

		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		private Date start;

		private Date end;

		public DailyTask(Date start, Date end) {
			super();
			this.start = start;
			this.end = end;
		}

		public void run() {
			
			Set<String> domainSet = new HashSet<String>();
			Set<String> nameSet = new HashSet<String>();
			
			getDomainAndNameSet(domainSet,nameSet,start,end);
			nameSet.retainAll(dailyReportNameSet);
			
			for (String domain : domainSet) { // iterate domains
				for (String name : nameSet) {
					Task task = taskDao.createLocal();
					task.setCreationDate(new Date());
					task.setProducer(ip);
					task.setReportDomain(domain);
					task.setReportName(name);
					task.setReportPeriod(start);
					task.setStatus(1); // status todo
					task.setTaskType(TYPE_DAILY);
					try {
						taskDao.insert(task);
					} catch (DalException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void getDomainAndNameSet(Set<String> domainSet,Set<String> nameSet,Date start,Date end){
		List<Report> domainNames = new ArrayList<Report>();
		try {
			domainNames = reportDao.findAllByDomainNameDuration(start, end, null, null,
			      ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			logger.error("domainNames", e);
		}

		if (domainNames == null || domainNames.size() == 0) {
			return; // no hourly report
		}
		
		for (Report domainName : domainNames) {
			domainSet.add(domainName.getDomain());
			// ignore heartbeat and ip daily report merge
			if (!"heartbeat".equals(domainName.getName()) && !"ip".equals(domainName.getName())) {
				nameSet.add(domainName.getName());
			}
		}
	}
	

	@Override
	public void initialize() throws InitializationException {
		Date now=new Date();
		Date todayZero=TaskHelper.todayZero(now);
		Date yesterday=TaskHelper.yesterdayZero(now);
		if (!isYesterdayTaskGenerated(now,todayZero, yesterday)) {
			DailyTask dailyTask = new DailyTask(yesterday, todayZero);
			long startOfTask  = TaskHelper.startDateOfNextTask(now, 0).getTime();
			long delay=startOfTask-now.getTime();
			service.schedule(dailyTask, delay, TimeUnit.MILLISECONDS);
		}
	}

	private boolean isYesterdayTaskGenerated(Date now,Date todayZero,Date yesterdayZero) {
		Date startDayOfTodayTask = TaskHelper.startDateOfNextTask(now, 0);
		long nowLong = now.getTime();
		long startOfTask = startDayOfTodayTask.getTime();

		if (nowLong <=startOfTask) {
			return false;
		}
		
		if(nowLong>startOfTask){
			List<Dailyreport> allReports=new ArrayList<Dailyreport>();
			try {
				allReports=dailyReportDao.findAllByPeriod(yesterdayZero, todayZero, DailyreportEntity.READSET_COUNT);
         } catch (DalException e) {
         	logger.error("dailyDomainNames", e);
         }
			
			Set<String> domainSet = new HashSet<String>();
			Set<String> nameSet = new HashSet<String>();
			
			getDomainAndNameSet(domainSet,nameSet,yesterdayZero,todayZero);
			nameSet.retainAll(dailyReportNameSet);
			
			int total=allReports.get(0).getCount();
			int domanSize=domainSet.size();
			int nameSize=nameSet.size();
			
			if(total!=domanSize*nameSize){
				return false;
			}
		}
		return true;
	}

}
