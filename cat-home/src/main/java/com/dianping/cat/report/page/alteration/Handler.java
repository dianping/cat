package com.dianping.cat.report.page.alteration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AlterationDao m_alterationDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alteration")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "alteration")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		String type = payload.getType();
		String domain = payload.getDomain();
		String hostname = payload.getHostname();

		switch (action) {
		case INSERT:
			String title = payload.getTitle();
			String ip = payload.getIp();
			String user = payload.getUser();
			String content = payload.getContent();
			String url = payload.getUrl();
			Date date = payload.getAlterationDate();

			Alteration alt = new Alteration();
			alt.setType(type);
			alt.setDomain(domain);
			alt.setTitle(title);
			alt.setIp(ip);
			alt.setUser(user);
			alt.setContent(content);
			alt.setUrl(url);
			alt.setHostname(hostname);
			alt.setDate(date);
			
			if(!isArguComplete(alt)){
				setInsertResult(model, 2);
				break;
			}else{
				try {
					m_alterationDao.insert(alt);
					setInsertResult(model, 0);
				} catch (Exception e) {
					Cat.logError(e);
					setInsertResult(model, 1);
				}
				break;
			}
			
		case VIEW:
			long granularity = payload.getGranularity();
			List<Alteration> alts;
			Date startTime = payload.getStartTime();
			Date endTime = payload.getEndTime();
			List<AltBarrel> barrels = new ArrayList<AltBarrel>();
			
			try {
				alts = m_alterationDao
				      .findByDtdh(startTime, endTime, type, domain, hostname, AlterationEntity.READSET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				break;
			}
			
			long endMill = endTime.getTime();
			Map<Long, List<Alteration>> alterations = new TreeMap<Long, List<Alteration>>();
			

			for (Alteration alt_genBarrel : alts) {
				long barTime = alt_genBarrel.getDate().getTime();
				long key = getBarrelKey(barTime, endMill, granularity);
				List<Alteration> tmpAlts_genBarrel = alterations.get(key);
				
				if(tmpAlts_genBarrel==null){
					alterations.put(key, new ArrayList<Alteration>());
					tmpAlts_genBarrel = alterations.get(key);	
					barrels.add(new AltBarrel(sdf.format(new Date(key)), sdf.format(new Date(key + granularity)),
							tmpAlts_genBarrel));
				}
				
				tmpAlts_genBarrel.add(alt_genBarrel);				
			}						
			
			model.setBarrels(barrels);
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALTERATION);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	public boolean isArguComplete(Alteration alt){
		if(alt.getType()==null||"".equals(alt.getType()))
			return false;
		if(alt.getTitle()==null||"".equals(alt.getTitle()))
			return false;
		if(alt.getDomain()==null||"".equals(alt.getDomain()))
			return false;
		if(alt.getHostname()==null||"".equals(alt.getHostname()))
			return false;
		if(alt.getDate()==null||"".equals(alt.getDate()))
			return false;
		if(alt.getUser()==null||"".equals(alt.getUser()))
			return false;
		if(alt.getContent()==null||"".equals(alt.getContent()))
			return false;
			
		return true;
	}
	
	private long getBarrelKey(long barTime, long endMill, long granularity) {
		long key;
		if(endMill == barTime){
			key = barTime - granularity;
		}else if((endMill-barTime)/granularity == 0){
			key = barTime;
		}
		else{
			key = endMill - ((endMill-barTime)/granularity+1)*granularity;
		}
		return key;
   }
	
	//status code: 0-success 1-fail 2-fail(lack args)
	public void setInsertResult(Model model, int status){
		if(status==0){
			model.setInsertResult("{\"status\":200}");
		}else if(status==1){
			model.setInsertResult("{\"status\":500}");
		}else if(status==2){
			model.setInsertResult("{\"status\":200, \"errorMessage\":\"lack args\"}");
		}
		
	}

	public class AltBarrel {
		private List<Alteration> alterations;

		private String startTime;

		private String endTime;

		public AltBarrel(String startTime, String endTime, List<Alteration> tmpAlts) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.alterations = tmpAlts;
		}

		public List<Alteration> getMiniAlterations() {
			int length = alterations.size();

			if (length > 10) {
				return alterations.subList(0, 10);
			}
			return alterations;
		}
		
		public List<Alteration> getAlterations() {
			// TODO
			int length = alterations.size();

			if (length > 10) {
				return alterations.subList(0, 10);
			}
			return alterations;
		}

		public void setAlterations(List<Alteration> alterations) {
			this.alterations = alterations;
		}

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

	}
}
