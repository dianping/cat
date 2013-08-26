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
			<td class="title">&nbsp;&nbsp;From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="switch"><a href="?domain=${model.domain}&op=utilization">Switch To Hourly Mode</a>
			</td>
			<td class="nav">
					&nbsp;&nbsp;<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=-1">${model.currentNav.last}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=1">${model.currentNav.next}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&reportType=${model.reportType}&nav=next">now</a> ]&nbsp;&nbsp;
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
			<table class="table table-striped table-bordered table-condensed">
				<tr>
					<th class="left">id</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&reportType=${payload.reportType}">Machine Number</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=urlCount&reportType=${payload.reportType}">URL Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=urlResponse&reportType=${payload.reportType}">URL Response Time</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=sqlCount&reportType=${payload.reportType}">SQL Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=pigeonCallCount&reportType=${payload.reportType}">Pigeon Call Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=swallowCallCount&reportType=${payload.reportType}">Swallow Call Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=memcacheCount&reportType=${payload.reportType}">Memcache Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=historyUtilization&sort=score&reportType=${payload.reportType}">Score</th>
				</tr>
			
				<c:forEach var="item" items="${model.utilizationList}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${item.id}</td>
						<td style="text-align:right">${item.machineNumber}</td>
						<td style="text-align:right">${w:format(item.urlCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.urlResponseTime,'0.0')}</td>
						<td style="text-align:right">${w:format(item.sqlCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.pigeonCallCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.swallowCallCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.memcacheCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.score,'#,###,###,###,##0')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</div>
<div class="report">
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>
