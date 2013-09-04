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
			$('#service').addClass('active');
		});
	</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title text-success"><span class="text-success"><span class="text-error">【报表时间】</span>&nbsp;&nbsp;From ${w:format(model.serviceReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.serviceReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			</td>
			<td class="nav" >
				<a href="?op=historyService&domain=${model.domain}" class="switch"><span class="text-error">【切到历史模式】</span></a>
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="?date=${model.date}&step=${nav.hours}&${navUrlPrefix}&op=service">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="?${navUrlPrefix}&op=service">now</a> ]&nbsp;
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
			</br>
			<table class="table table-striped table-bordered table-condensed">
				<tr>
					<th class="left">Server(Domain)</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=total">Total</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=failure">Failure</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=failurePercent">Failure%</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=service&sort=avg">Avg(ms)</th>
				</tr>
			
				<c:forEach var="item" items="${model.serviceList}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${item.id}</td>
						<td style="text-align:right">${w:format(item.totalCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.failureCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.failurePercent,'0.00%')}</td>
						<td style="text-align:right">${w:format(item.avg,'0.00')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</div>
</a:body>
