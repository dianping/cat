<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:application>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		$('#Offline_report').addClass('active open');
		$('#jar_report').addClass('active');
		init();
	});
</script>
<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>
		<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(model.jarReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.jarReport.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
		<div class="nav-search nav" id="nav-search">
			<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&op=jar">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?op=jar">now</a> ]&nbsp;
		</div>
	</div>
	<div style="display:inline-flex;padding-top:3px;">
		<table id="contents" class="table table-striped table-condensed table-hover">
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
	</table></div>
</a:application>
