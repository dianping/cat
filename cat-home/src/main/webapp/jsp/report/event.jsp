<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.event.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.event.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.event.Model" scope="request" />
<c:set var="report" value="${model.report}"/>

<a:report title="Event Report${empty payload.type ? '' : ' :: '}<a href='?domain=${model.domain}&date=${model.date}&type=${payload.type}'>${payload.type}</a>" navUrlPrefix="domain=${model.domain}${empty payload.type ? '' : '&type='}${payload.type}" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>

<res:useCss value="${res.css.local.event_css}" target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.event_js}" target="head-js"/>

<br>
<table class="event">
	<c:choose>
		<c:when test="${empty payload.type}">
			<tr>
			<th><a href="?domain=${model.domain}&date=${model.date}&sort=type"> Type</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&sort=total">Total Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&sort=failure">Failure Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th><th>Min/Max/Avg/Std(ms)</th></tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}" varStatus="status">
				<c:set var="e" value="${item.detail}"/>
				<c:set var="lastIndex" value="${status.index}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td><a href="?domain=${report.domain}&date=${model.date}&type=${item.type}">${item.type}</a></td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
		<tr>
			<th><a href="?op=graphs&domain=${report.domain}&date=${model.date}&type=${payload.type}" class="graph_link" data-status="-1">[:: show ::]</a>
			<a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=type"> Name</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=total">Total Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=failure">Failure Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&type=${payload.type}&sort=failurePercent">Failure%</a></th>
			<th>Total Count</th><th>Failure Count</th><th>Failure%</th>
			<th>Sample Link</th><th>Min/Max/Avg/Std(ms)</th></tr>
			
			<tr class="graphs"><td colspan="6"><div id="-1" style="display:none"></div></td></tr>
			<c:forEach var="item" items="${model.displayNameReport.results}" varStatus="status">
				<c:set var="e" value="${item.detail}"/>
				<c:set var="lastIndex" value="${status.index}"/>
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td><a href="?op=graphs&domain=${report.domain}&date=${model.date}&type=${payload.type}&name=${e.id}" class="graph_link" data-status="${status.index}">[:: show ::]</a> ${e.id}</td>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent,'0.00')}</td>
					<td><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}">Log View</a></td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="${status.index}" style="display:none"></div></td></tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>
<font color="white">${lastIndex+1}</font>

</jsp:body>

</a:report>

<res:useJs value="${res.js.local.event_js}" target="bottom-js" />