<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.bug.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.bug.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.bug.Model" scope="request"/>

<a:body>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		$('#utilization').addClass('active');
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.utilizationReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.utilizationReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="switch"><a href="?op=historyUtilization">Switch To History Mode</a>
			</td>
			<td class="nav" >
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&op=utilization">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?op=utilization">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
</div>

<%@ include file="detail.jsp"%>

<div class="report">
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>

</a:body>
<script type="text/javascript">
	$(document).ready(function() {
		var tab = '${payload.tab}';
		if(tab=='tab2'){
			$('#tab2Href').trigger('click');
		}else{
			$('#tab1Href').trigger('click');
		}
	});
</script>
