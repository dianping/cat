package com.dianping.cat.report.page.jsError;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
   private ReportPage m_page;

   @FieldMeta("op")
   private Action m_action;
   
   @FieldMeta("timestamp")
   private long m_timestamp;
   
   @FieldMeta("error")
   private String m_error;
   
   @FieldMeta("file")
   private String m_file;

   @FieldMeta("url")
   private String m_url;
   
   @FieldMeta("host")
   private String m_host;
   
   @FieldMeta("line")
   private String m_line;
   
   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public ReportPage getPage() {
      return m_page;
   }

   @Override
   public void setPage(String page) {
      m_page = ReportPage.getByName(page, ReportPage.JSERROR);
   }
   
   public long getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	public String getError() {
		return m_error;
	}

	public void setError(String error) {
		m_error = error;
	}

	public String getFile() {
		return m_file;
	}

	public void setFile(String file) {
		m_file = file;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	public String getHost() {
		return m_host;
	}

	public void setHost(String host) {
		m_host = host;
	}

	public String getLine() {
		return m_line;
	}

	public void setLine(String line) {
		m_line = line;
	}

	@Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
	
	
}
