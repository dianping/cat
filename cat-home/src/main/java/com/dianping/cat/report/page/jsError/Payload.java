package com.dianping.cat.report.page.jsError;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;

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

   @FieldMeta("line")
   private String m_line;

   @FieldMeta("data")
   private String m_data;
   
   @Override
   public Action getAction() {
      return m_action;
   }

   public String getData() {
		return m_data;
	}

   public String getError() {
		return m_error;
	}

   public String getFile() {
		return m_file;
	}
   
   public String getLine() {
		return m_line;
	}

	@Override
   public ReportPage getPage() {
      return m_page;
   }

	public long getTimestamp() {
		return m_timestamp;
	}

	public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

	public void setData(String data) {
		m_data = data;
	}

	public void setError(String error) {
		m_error = error;
	}

	public void setFile(String file) {
		m_file = file;
	}

	public void setLine(String line) {
		m_line = line;
	}

	@Override
   public void setPage(String page) {
      m_page = ReportPage.getByName(page, ReportPage.JSERROR);
   }

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	@Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
	
}
