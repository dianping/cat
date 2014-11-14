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
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		$('#jar').addClass('active');
		init();
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title text-success"><span class="text-success"><span class="text-error">【报表时间】</span>&nbsp;&nbsp;From ${w:format(model.jarReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.jarReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			</td>
			<td class="nav" >
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&op=jar">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?op=jar">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
</div>
<div class="row-fluid">
      <div class="span2">
		<%@include file="../reportTree.jsp"%>
	</div>
	<div class="span10">
	<br/>
	<table id="contents" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th>domain</th>
			<th>ip</th>
			<c:forEach var="item" items="${model.jars}" varStatus="status">
				<th>${item}</th>
			</c:forEach>
		</tr></thead>
	<tbody>
		<c:forEach var="item" items="${model.jarReport.domains}" varStatus="status">
			<c:forEach var="machine" items="${item.value.machines}">
				<tr>
					<td>${item.key}</td>
					<td>${machine.key}</td>
					<c:forEach var="jar" items="${machine.value.jars}">
						<td>${jar.version}</td>
					</c:forEach>				
				</tr>
			</c:forEach>
		</c:forEach></tbody>
	</table></div></div>
</a:body>
