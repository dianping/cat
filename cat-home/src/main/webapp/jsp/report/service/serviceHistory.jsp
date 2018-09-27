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
		<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</span>
		<div class="nav-search nav" id="nav-search">
			<a class="switch" href="?domain=${model.domain}&op=service"><span class="text-danger">【切到小时模式】</span></a>
					<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq payload.reportType}">
								&nbsp;&nbsp;[ <a href="?op=historyService&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=historyService&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;&nbsp;[ <a href="?op=historyService&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&step=-1">${model.currentNav.last}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyService&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&step=1">${model.currentNav.next}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyService&domain=${model.domain}&ip=${model.ipAddress}&reportType=${payload.reportType}&nav=next">now</a> ]&nbsp;&nbsp;
		</div>
	</div>
</div>
<div class="row-fluid">
		<div class="report">
			<table class="table table-striped table-condensed   table-hover">
				<tr>
					<th class="left">Server(Domain)</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyService&sort=total&reportType=${payload.reportType}">Total</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyService&sort=failure&reportType=${payload.reportType}">Failure</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyService&sort=failurePercent&reportType=${payload.reportType}">Failure%</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyService&sort=availability&reportType=${payload.reportType}">Availability%</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyService&sort=avg&reportType=${payload.reportType}">Avg(ms)</th>
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
