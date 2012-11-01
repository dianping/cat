package com.dianping.cat.consumer.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dainping.cat.consumer.dal.report.Hostinfo;
import com.dainping.cat.consumer.dal.report.HostinfoDao;
import com.dainping.cat.consumer.dal.report.Project;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ProjectEntity;
import com.dainping.cat.consumer.dal.report.Task;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.common.model.entity.CommonReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.BucketManager;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class CommonAnalyzer extends AbstractMessageAnalyzer<CommonReport> implements LogEnabled {

	private Map<String, CommonReport> m_reports = new HashMap<String, CommonReport>();

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private ProjectDao m_projectDao;

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReport(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	@Override
	public CommonReport getReport(String domain) {
		throw new RuntimeException("Can't invoke get report in common anayler!");
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		CommonReport report = m_reports.get(domain);

		if (report == null) {
			report = new CommonReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		String ip = tree.getIpAddress();
		report.getIps().add(ip);
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	private void storeReport(boolean atEnd) {
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			for (CommonReport report : m_reports.values()) {
				String domain = report.getDomain();
				Set<String> ips = report.getIps();

				for (String ip : ips) {
					try {
						Hostinfo info = m_hostInfoDao.createLocal();

						info.setDomain(domain);
						info.setIp(ip);
						m_hostInfoDao.insert(info);
					} catch (DalException e) {
						Cat.logError(e);
						t.setStatus(e);
					}
				}
				insetDomainInfo(domain);
			}
			if (atEnd) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				// Create task for health report
				for (String domain : m_reports.keySet()) {
					try {
						Task task = m_taskDao.createLocal();

						task.setCreationDate(new Date());
						task.setProducer(ip);
						task.setReportDomain(domain);
						task.setReportName("health");
						task.setReportPeriod(period);
						task.setStatus(1); // status todo
						m_taskDao.insert(task);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					}
				}
			}
		} catch (Exception e) {
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			t.complete();
		}
	}

	private void insetDomainInfo(String domain) {
		try {
			m_projectDao.findByDomain(domain, ProjectEntity.READSET_FULL);
		} catch (DalNotFoundException e) {
			Project project = m_projectDao.createLocal();

			project.setDomain(domain);
			project.setProjectLine("Default");

			String temp = domain.toLowerCase();

			setProjectInfo(project, temp);

			try {
				m_projectDao.insert(project);
			} catch (Exception ex) {
				Cat.logError(ex);
			}

		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void setProjectInfo(Project project, String temp) {
		
		String account = " magicMirror AccountMQ      AccountServer      AccountWeb   MessageAdminWeb      MessageMQ      MessageServer      MessageWeb DecoratorServer FeedMQ      FeedServer  SmsServer      SocialFeedMQ      SocialFeedServer      SocialRelationshipServer  ";
		String guanggao = "AdwordsWeb PromoWeb OpenWeb KeywordService KeywordWeb";
		String group = "AuditbackService    BaseDataServer  AuditbackWeb      AutoauditMQ  GroupService      GroupWeb  ReviewServer BadgeMQ      BaseDataServer  UserBaseService      UserService      UserWeb";
		String shop = "DPIndexWeb KeywordService  ShopAccountAdminWeb      PiccenterDisplay      PiccenterService      PiccenterUpload      PiccenterWeb  ShopAccountAdminWeb      ShopAccountServer      ShopAccountWeb      ShopBusinessWeb      ShopSearchWeb      ShopServer      ShopWeb      ShoppicService      ShoppicWeb";

		String tuangouWeb = "BCTuangouWeb  TuanGouApi     TuanGouApi       TuanGouRemote            TuanGouWeb tuangou-paygate";
		String tuangouMobile = "TuanGouApiMobile TuanGouWap";
		String zhifu = "   PayChannel      PayEngine   tuangou-paygate   PayOrder Unipay ";

		String mobile = "      MConfigAPI      MLocationService      MYellowPageAPI      MapService MobileApi";
		String mobileBooking = "MBookingCallcenter      MBookingService      MBookingWeb";
		String mobileMembercard = "MobileMembercardBcWeb      MobileMembercardMainApiWeb      MobileMembercardMainServer";
		String search = "DPSearch";
		String middleware = " Swallow Cat";

		if (account.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.WWW);
			project.setProjectLine(Department.ACCOUT);
		} else if (middleware.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.ARCH);
			project.setProjectLine(Department.MIDDLDWARE);
		} else if (guanggao.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.WWW);
			project.setProjectLine(Department.PROMO);
		} else if (group.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.WWW);
			project.setProjectLine(Department.GROUP);
		} else if (shop.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.WWW);
			project.setProjectLine(Department.SHOP);
		} else if (tuangouWeb.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.TUANGOU);
			project.setProjectLine(Department.TUANGOUTWEB);
		} else if (tuangouMobile.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.TUANGOU);
			project.setProjectLine(Department.TUANGOUMOBILE);
		} else if (zhifu.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.TUANGOU);
			project.setProjectLine(Department.TUANGOUZHIFU);
		} else if (mobile.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.MOBILE);
			project.setProjectLine(Department.MOBILEWEB);
		} else if (mobileBooking.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.MOBILE);
			project.setProjectLine(Department.MOBILEBOOKING);
		} else if (mobileMembercard.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.MOBILE);
			project.setProjectLine(Department.MOBILEMEM);
		} else if (search.toLowerCase().indexOf(temp) > -1) {
			project.setDepartment(Department.SEARCH);
			project.setProjectLine(Department.SEARCH);
		} else {
			project.setDepartment("Default");
			project.setProjectLine("Default");

		}
	}

	public class Department {

		public static final String MOBILE = "手机";

		public static final String SEARCH = "搜索";

		public static final String ARCH = "架构";

		public static final String WWW = "主站";

		public static final String TUANGOU = "团购";

		public static final String ACCOUT = "账户、消息";

		public static final String GROUP = "社区、审核";

		public static final String SHOP = "商户、图片";

		public static final String PROMO = "广告、优惠";

		public static final String TUANGOUTWEB = "团购主页";

		public static final String TUANGOUMOBILE = "团购手机";

		public static final String TUANGOUZHIFU = "支付中心";

		public static final String MIDDLDWARE = "基础组建";

		public static final String MOBILEWEB = "手机后台";

		public static final String MOBILEBOOKING = "预约预定";

		public static final String MOBILEMEM = "会员卡";
	}
}
