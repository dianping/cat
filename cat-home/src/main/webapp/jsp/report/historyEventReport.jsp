<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.historyReport.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.historyReport.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.historyReport.Model" scope="request" />

<a:simpleReport title="History Report">
	<jsp:body>
	<res:useCss value='${res.css.local.jqueryUI_css}' target="head-css" />
	<res:useCss value='${res.css.local.calendar_css}' target="head-css" />
	<res:useJs value="${res.js.local.jqueryMin_js}" target="head-js" />
	<res:useJs value="${res.js.local.jqueryUIMin_js}" target="head-js" />
	<res:useJs value="${res.js.local.datepicker_js}" target="head-js" />

<%@ include file="historyReport.jsp"%>
</br>
<table class="machines">
	<tr style="text-align: left">
		<th>Machines:  <c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp; 
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=event&domain=${model.domain}&ip=${ip}&startDate=${payload.startDate}&endDate=${payload.endDate}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=event&domain=${model.domain}&ip=${ip}&startDate=${payload.startDate}&endDate=${payload.endDate}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class="event">
	<c:choose>
		<c:when test="${empty payload.type}">
			<tr>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&sort=type"> Type</a></th>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&sort=total">Total Count</a></th>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&sort=failure">Failure Count</a></th>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th>
					</tr>
			<c:forEach var="item" items="${model.eventTypes.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align: left"><a
								href="?op=event&domain=${report.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&type=${item.type}">${item.type}</a></td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
		<tr>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&type=${payload.type}&sort=type"> Name</a></th>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&type=${payload.type}&sort=total">Total Count</a></th>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&type=${payload.type}&sort=failure">Failure Count</a></th>
			<th><a	href="?op=event&domain=${model.domain}&startDate=${payload.startDate}&endDate=${payload.endDate}&ip=${model.ipAddress}&type=${payload.type}&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th>
					</tr>
			<c:forEach var="item" items="${model.eventNames.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td style="text-align: left">${e.id}</td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a	href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
				</tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>

<res:useJs value="${res.js.local.historyReport_js}" target="bottom-js" />
</jsp:body>

</a:simpleReport>