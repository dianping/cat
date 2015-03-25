package com.dianping.cat.report.page.storage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type = StorageConstants.SQL_TYPE;

	@FieldMeta("operations")
	private String m_operations;

	@FieldMeta("project")
	private String m_project;

	@FieldMeta("sort")
	private String m_sort = "domain";

	@FieldMeta("minute")
	private String m_minute;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("count")
	private int m_minuteCounts = StorageConstants.DEFAULT_MINUTE_COUNT;

	@FieldMeta("tops")
	private int m_topCounts = StorageConstants.DEFAULT_TOP_COUNT;

	@FieldMeta("id")
	private String m_id = Constants.CAT;

	public Payload() {
		super(ReportPage.STORAGE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public long getCurrentDate() {
		long timestamp = getCurrentTimeMillis();

		return timestamp - timestamp % TimeHelper.ONE_HOUR;
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis() - TimeHelper.ONE_MINUTE * 1;
	}

	public int getFrequency() {
		return m_frequency;
	}

	public String getId() {
		return m_id;
	}

	public String getMinute() {
		return m_minute;
	}

	public int getMinuteCounts() {
		return m_minuteCounts;
	}

	public String getOperations() {
		return m_operations;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getProject() {
		return m_project;
	}

	public String getSort() {
		return m_sort;
	}

	public int getTopCounts() {
		return m_topCounts;
	}

	public String getType() {
		return m_type;
	}

	public boolean isFullScreen() {
		return m_fullScreen;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_STORAGE);
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setMinute(String minute) {
		m_minute = minute;
	}

	public void setMinuteCounts(int minuteCounts) {
		m_minuteCounts = minuteCounts;
	}

	public void setOperations(String operations) {
		m_operations = operations;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STORAGE);
	}

	public void setProject(String project) {
		m_project = project;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	public void setSort(String sort) {
		m_sort = sort;
	}

	public void setTopCounts(int topCounts) {
		m_topCounts = topCounts;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_STORAGE;
		}
	}
}
