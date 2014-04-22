<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:body>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		$('#heavy').addClass('active');
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title text-success"><span class="text-success"><span class="text-error">【报表时间】</span>&nbsp;&nbsp;From ${w:format(model.heavyReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.heavyReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="nav" >
				<a href="?op=historyHeavy&domain=${model.domain}" class="switch"><span class="text-error">【切到历史模式】</span></a>
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?op=heavy&date=${model.date}&step=${nav.hours}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?op=heavy">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
</div>
<div class="row-fluid">
    <div class="span2">
		<%@include file="../bugTree.jsp"%>
	</div>
	<div class="span10">
		<div class="report">
			<%@ include file="detail.jsp"%>
		</div>
	</div>
</div>
</a:body>
