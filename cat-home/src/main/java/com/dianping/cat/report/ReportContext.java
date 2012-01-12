package com.dianping.cat.report;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ebay.webres.resource.runtime.ResourceConfigurator;
import com.ebay.webres.resource.runtime.ResourceInitializer;
import com.ebay.webres.resource.runtime.ResourceRuntime;
import com.ebay.webres.resource.runtime.ResourceRuntimeContext;
import com.ebay.webres.resource.spi.IResourceRegistry;
import com.ebay.webres.tag.resource.ResourceTagConfigurator;
import com.ebay.webres.taglib.basic.ResourceTagLibConfigurator;
import com.site.web.mvc.Action;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.Page;

public class ReportContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {

   @SuppressWarnings("deprecation")
   @Override
   public void initialize(HttpServletRequest request, HttpServletResponse response) {
      super.initialize(request, response);

      String contextPath = request.getContextPath();

      if (!ResourceRuntime.INSTANCE.hasConfig(contextPath)) {
         File warRoot = new File(request.getRealPath("/"));

         ResourceRuntime.INSTANCE.removeConfig(contextPath);
         ResourceInitializer.initialize(contextPath, warRoot);

         IResourceRegistry registry = ResourceRuntime.INSTANCE.getConfig(contextPath).getRegistry();

         new ResourceConfigurator().configure(registry);
         new ResourceTagConfigurator().configure(registry);
         new ResourceTagLibConfigurator().configure(registry);
      }

      ResourceRuntimeContext.setup(contextPath);
   }

}
