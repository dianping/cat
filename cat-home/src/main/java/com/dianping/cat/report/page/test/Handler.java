package com.dianping.cat.report.page.test;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Family;
import com.dianping.cat.home.dal.report.FamilyDao;
import com.dianping.cat.home.dal.report.FamilyEntity;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.util.StringUtils;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private FamilyDao familyDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "test")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "test")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case INSERT:
			if (!checkArgs(payload)) {
				setInsertResult(model, 2);
			} else {
				Family family = buildFamily(payload);
				try {
					int insertResult = familyDao.insert(family);
					if (insertResult <= 0) {
						setInsertResult(model, 1);
					} else {
						setInsertResult(model, 0);
					}
				} catch (DalException e) {
					Cat.logError(e);
					setInsertResult(model, 1);
				}
			}
			break;
		case QUERYALL:
			List<Family> families = null;
			try {
	         families = familyDao.findAll(FamilyEntity.READSET_FULL);
         } catch (DalException e) {
         	Cat.logError(e);
         }

         model.setFamilies(families);
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.TEST);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private Family buildFamily(Payload payload) {
		Family family = new Family();
		family.setAge(payload.getAge());
		family.setName(payload.getName());
		
		return family;
	}

	/**
	 * status code:
	 * 0: success
	 * 1: fail
	 * 2: fail, due to lack args
	 * @param model
	 * @param status
	 */
	private void setInsertResult(Model model, int status) {
		switch (status) {
		case 0:
			model.setInsertResult("{\"status\":200}");
			break;
		case 1:
			model.setInsertResult("{\"status\":500}");
			break;
		case 2:
			model.setInsertResult("{\"status\":500,\"errorInfo\":\"lack args\"}");
			break;
		default:
			model.setInsertResult("{\"status\":500}");
			break;
		}
	}

	private boolean checkArgs(Payload payload) {
		if (StringUtils.isEmpty(payload.getName())) {
			return false;
		} else {
			return true;
		}
	}
}
