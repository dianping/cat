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

<script type="text/javascript">
	$(document).ready(function() {
		$('#Offline_report').addClass('active open');
		$('#service_report').addClass('active');
	});
</script>
<div class="report">
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>
		<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(model.serviceReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.serviceReport.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
		<div class="nav-search nav" id="nav-search">
			<a href="?op=historyService&domain=${model.domain}" class="switch"><span class="text-danger">【切到历史模式】</span></a>
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="?date=${model.date}&step=${nav.hours}&${navUrlPrefix}&op=service">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="?${navUrlPrefix}&op=service">now</a> ]&nbsp;
		</div>
	</div>
</div>
<div class="row-fluid">
		<div class="report">
			<table class="table table-striped table-condensed   table-hover">
				<tr>
					<th class="left">Server(Domain)</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=total">Total</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=failure">Failure</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyService&sort=failurePercent&reportType=${payload.reportType}">Failure%</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=availability">Availability%</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=avg">Avg(ms)</th>
				</tr>
			
				<c:forEach var="item" items="${model.serviceList}" varStatus="status">
					<tr class="">
						<td>${item.id}</td>
						<td style="text-align:right">${w:format(item.totalCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.failureCount,'#,###,###,###,##0')}</td>
						<c:if test="${item.failurePercent > 0.0001}">
							<td style="text-align:right;color:red">${w:format(item.failurePercent,'0.00000%')}</td>
							<td style="text-align:right;color:red">${w:format(1-item.failurePercent,'0.00000%')}</td>
						</c:if>
						<c:if test="${item.failurePercent <= 0.0001}">
							<td style="text-align:right">${w:format(item.failurePercent,'0.00000%')}</td>
							<td style="text-align:right">${w:format(1-item.failurePercent,'0.00000%')}</td>
						</c:if>
						<td style="text-align:right">${w:format(item.avg,'0.00')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
</div>
</a:application>
