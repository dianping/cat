package com.dianping.cat.report.page.ip;

import java.util.List;

import com.dianping.cat.report.page.ip.Model.DisplayModel;

public class MobileModel {
	private List<DisplayModel> m_displayModels;

	public MobileModel(){
		
	}
	
	public List<DisplayModel> getDisplayModels() {
   	return m_displayModels;
   }

	public MobileModel setDisplayModels(List<DisplayModel> displayModels) {
   	m_displayModels = displayModels;
   	return this;
   }
}
