package com.dianping.cat.broker.view;

import org.unidal.web.mvc.Page;

import com.dianping.cat.broker.api.ApiPage;

public class NavigationBar {
	public Page[] getVisiblePages() {
      return new Page[] {
   
      ApiPage.SINGLE,
      
      ApiPage.BATCH

		};
   }
}
