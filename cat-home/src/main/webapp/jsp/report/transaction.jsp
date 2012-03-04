<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<c:set var="report" value="${model.report}"/>

<res:useCss value="${res.css.local.transaction_css}" target="head-css"/>

<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.transaction_js}" target="head-js"/>

<a:report title="Transaction Report${empty payload.type ? '' : ' :: '}<a href='?type=${payload.type}'>${payload.type}</a>" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>
<table class="transaction" border="0">
	<tr><th>${empty payload.type ? "Type" : "Name"}</th><th>Total Count</th><th>Failure Count</th><th>Failure%</th><th>Sample Link</th><th>Min/Max/Avg/Std(ms)</th></tr>
	<c:choose>
		<c:when test="${empty payload.type}">
			<c:forEach var="type" items="${report.types}" varStatus="status">
				<c:set var="e" value="${type.value}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td><a href="?type=${e.id}">${e.id}</a></td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${e.failPercent}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
					<td>${w:format(e.min,'0')}/${w:format(e.max,'0')}/${w:format(e.avg,'0.0')}/${w:format(e.std,'0.0')}</td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<c:forEach var="name" items="${report.types[payload.type].names}" varStatus="status">
				<c:set var="e" value="${name.value}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td><a href="?op=graphs&domain=${report.domain}&type=${payload.type}&name=${e.id}" onclick="return showGraphs(this,${status.index},'${report.domain}','${payload.type}','${e.id}');">[:: show ::]</a> ${e.id}</td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${e.failPercent}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
					<td>${w:format(e.min,'0')}/${w:format(e.max,'0')}/${w:format(e.avg,'0.0')}/${w:format(e.std,'0.0')}</td>
				</tr>
				<tr class="graphs"><td colspan="6"><div id="${status.index}"></div></td></tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>
</jsp:body>

</a:report>

<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />