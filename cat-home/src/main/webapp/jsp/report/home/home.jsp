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
   			 	<li class="text-right active"><a href="#tab0" data-toggle="tab"><strong>点评内部</strong></a></li>
   			 	<li class="text-right"><a href="#tab1" data-toggle="tab"><strong>版本说明</strong></a></li>
   			 	<li class="text-right"><a href="#tab2" data-toggle="tab"><strong>集成文档</strong></a></li>
   			 	<li class="text-right"><a href="#tab8" data-toggle="tab"><strong>业务监控</strong></a></li>
   			 	<li class="text-right"><a href="#tab3" data-toggle="tab"><strong>开发者文档</strong></a></li>
   			 	<li class="text-right"><a href="#tab4" data-toggle="tab"><strong>设计文档</strong></a></li>
   			 	<li class="text-right"><a href="#tab5" data-toggle="tab"><strong>用户文档</strong></a></li>
   			 	<li class="text-right"><a href="#tab6" data-toggle="tab"><strong>常见问题</strong></a></li>
   			 	<li class="text-right"><a href="#tab7" data-toggle="tab"><strong>插件扩展</strong></a></li>
  			</ul>
  			<div class="tab-content">
	    		<div class="tab-pane active" id="tab0"><%@ include file="dianping.jsp"%></div>
	    		<div class="tab-pane" id="tab1"><%@ include file="releasenotes.jsp"%></div>
	    		<div class="tab-pane" id="tab2"><%@ include file="integratingDocument.jsp"%></div>
	    		<div class="tab-pane" id="tab8"><%@ include file="integratingBusiness.jsp"%></div>
	    		<div class="tab-pane" id="tab3"><%@ include file="developDocument.jsp"%></div>
	    		<div class="tab-pane" id="tab4"><%@ include file="develop.jsp"%></div>
	    		<div class="tab-pane" id="tab5"><%@ include file="userDocument.jsp"%></div>
	    		<div class="tab-pane" id="tab6"><%@ include file="problem.jsp"%></div>
	    		<div class="tab-pane" id="tab7"><%@ include file="plugin.jsp"%></div>
			</div>
	</div>
</div>
<br>
<br>
<br>
<a href="?op=checkpoint&domain=${model.domain}&date=${model.date}" style="color:#FFF">Do checkpoint here</a>
</a:body>