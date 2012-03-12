<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.sql.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.sql.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.sql.Model"	scope="request" />

<a:report
	title="SQL Report"
	navUrlPrefix="domain=${model.domain}"
	timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	
<res:useCss value="${res.css.local.transaction_css}" target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.transaction_js}" target="head-js"/>

<br>
<table class="sql">
	<tr>
		<th>SQL</th>
		<th>Total</th>
		<th>Failure</th>
		<th>Failure%</th>
		<th>LongSQL</th>
		<th>Long%</th>
		<th>Min/Max/Avg/Std(ms)</th>
		<th>95% Avg</th>
		<th>Sample Link</th>
	</tr>
	<c:forEach var="reportRecord" items="${model.report.reportRecords}" varStatus="status">
		<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
			<td>${reportRecord.record.name}</td>
			<td>${reportRecord.record.totalcount}</td>
			<td>${reportRecord.record.failures}</td>
			<td>${reportRecord.failurePercent}</td>
			<td>${reportRecord.record.longsqls}</td>
			<td>${reportRecord.longPercent}</td>
			<td>${reportRecord.record.minvalue}/${reportRecord.record.maxvalue}/${reportRecord.avg}/${reportRecord.std}</td>
			<td>${reportRecord.avg}</td>
			<td><a href='cat/r/m/${reportRecord.record.samplelink}/logview.html'>Link</a></td>
		</tr>
	</c:forEach> 
</table>
<br>
</jsp:body>
</a:report>
