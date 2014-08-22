<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.home.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.home.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.home.Model" scope="request"/>
<style>
 .detailContent{
 	margin-left:20px;
 }
</style>
<a:body>

<div class="row-fluid">
	<div class="span12">
		 <div class="tabbable tabs-left " id="content"> <!-- Only required for left/right tabs -->
  			<ul class="nav nav-tabs well" style="margin-top:30px;">
   			 	<li class="text-right" id="dianpingButton"><a href="?op=view&docName=dianping"><strong>点评内部</strong></a></li>
   			 	<li class="text-right" id="releaseButton"><a href="?op=view&docName=release"><strong>版本说明</strong></a></li>
   			 	<li class="text-right" id="integrationButton"><a href="?op=view&docName=integration"><strong>集成文档</strong></a></li>
   			 	<li class="text-right" id="alertButton"><a href="?op=view&docName=alert"><strong>告警文档</strong></a></li>
   			 	<li class="text-right" id="businessMonitorButton"><a href="?op=view&docName=businessMonitor"><strong>业务监控</strong></a></li>
   			 	<li class="text-right" id="alterationButton"><a href="?op=view&docName=alteration"><strong>变更监控</strong></a></li>
   			 	<li class="text-right" id="networkButton"><a href="?op=view&docName=network"><strong>网络监控</strong></a></li>
   			 	<li class="text-right" id="userMonitorButton"><a href="?op=view&docName=userMonitor"><strong>用户端监控</strong></a></li>
   			 	<li class="text-right" id="developButton"><a href="?op=view&docName=develop"><strong>开发者文档</strong></a></li>
   			 	<li class="text-right" id="designButton"><a href="?op=view&docName=design"><strong>设计文档</strong></a></li>
   			 	<li class="text-right" id="userButton"><a href="?op=view&docName=user"><strong>用户文档</strong></a></li>
   			 	<li class="text-right" id="problemButton"><a href="?op=view&docName=problem"><strong>常见问题</strong></a></li>
   			 	<li class="text-right" id="pluginButton"><a href="?op=view&docName=plugin"><strong>插件扩展</strong></a></li>
  			</ul>
  			<div class="tab-content">
  				<br/>
  				<c:choose>
	  				<c:when test="${payload.docName == 'dianping'}">
			    		<%@ include file="dianping.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'release'}">
			    		<%@ include file="releasenotes.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'integration'}">
			    		<%@ include file="integratingDocument.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'alert'}">
			    		<%@ include file="alert.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'businessMonitor'}">
			    		<%@ include file="integratingBusiness.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'alteration'}">
			    		<%@ include file="alterationDocument.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'network'}">
			    		<%@ include file="networkDocument.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'userMonitor'}">
			    		<%@ include file="userMonitor.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'develop'}">
			    		<%@ include file="developDocument.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'design'}">
			    		<%@ include file="develop.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'user'}">
			    		<%@ include file="userDocument.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'problem'}">
			    		<%@ include file="problem.jsp"%>
			    	</c:when>
			    	<c:when test="${payload.docName == 'plugin'}">
			    		<%@ include file="plugin.jsp"%>
			    	</c:when>
			    	<c:otherwise>
			    		<%@ include file="dianping.jsp"%>
			    	</c:otherwise>
	    		</c:choose>
			</div>
	</div>
</div>
<br>
<br>
<br>
<a href="?op=checkpoint&domain=${model.domain}&date=${model.date}" style="color:#FFF">Do checkpoint here</a>
<script>
	var liElement = $('#${payload.docName}Button');
	if(liElement.size() == 0){
		liElement = $('#dianpingButton');
	}
	liElement.addClass('active');
</script>
</a:body>