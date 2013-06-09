<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.top.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.top.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.top.Model" scope="request"/>

<style>
.tab-content	table {
  max-width: 100%;
  background-color: transparent;
  border-collapse: collapse;
  border-spacing: 0; 
}
</style>
<a:body>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>

<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.topReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.topReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
		<td class="nav" >
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	
	<div class='text-center' style="margin:3px;">
		<a class='btn btn-small btn-primary' href="?refresh=true&frequency=10">10秒定时刷新</a>
		<a class='btn btn-small btn-primary' href="?refresh=true&frequency=20">20秒定时刷新</a>
		<a class='btn btn-small btn-primary' href="?refresh=true&frequency=30">30秒定时刷新</a>
	</div>
    <%@ include file="topMetric.jsp"%>
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>
<script type="text/javascript">
	$(document).ready(function() {
		$('i[tips]').popover();
		$('#topMetric .nav-tabs a').mouseenter(function (e) {
		  e.preventDefault();
		  $(this).tab('show');
		});	
		
		var refresh = ${payload.refresh};
		var frequency = ${payload.frequency};
		if(refresh){
			setInterval(function(){
				location.reload();				
			},frequency*1000);
		}
	});
</script>
