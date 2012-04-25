<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.sql.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.sql.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.sql.Model"	scope="request" />

<a:report
	title="SQL Report Created By Hadoop Job , One Hour Delay"
	navUrlPrefix="domain=${model.domain}"
	timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	
<res:useCss value='${res.css.local.sql_css}' target="head-css"/>
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.transaction_js}" target="head-js"/>

<br>
<table class="sql-table">
	<tr>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=name">SQL</a></th>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=total">Total</a></th>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=failure">Failure</a></th>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=failurePercent">Failure%</a></th>
		<th>Min(ms)</th>
		<th>Max(ms)</th>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=avg">Avg(ms)</a></th>
		<th>Std(ms)</th>
		<th>95% Line</th>
		<th>DB Time</th>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=longsql">LongSQL</a></th>
		<th><a href="?domain=${model.domain}&date=${model.date}&sort=longsqlPercent">Long%</a></th>
		<th>Sample</th>
	</tr>
	<c:forEach var="reportRecord" items="${model.report.reportRecords}" varStatus="status">
		<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
			<td><a href="?op=graphs&id=${reportRecord.record.id}" onclick="return showGraphs(this,${status.index},'${reportRecord.record.id}');">[:: show ::]</a> 
				${reportRecord.record.name}</td>
			<td>${w:format(reportRecord.record.totalCount,'0.0')}</td>
			<td>${w:format(reportRecord.record.failureCount,'0.0')}</td>
			<td>${w:format(reportRecord.failurePercent,'0.00%')}</td>
			<td>${w:format(reportRecord.record.minValue,'0.0')}</td>
			<td>${w:format(reportRecord.record.maxValue,'0.0')}</td>
			<td>${w:format(reportRecord.avg,'0.0')}<td>
			<td>${w:format(reportRecord.std,'0.0')}</td>
			<td>${w:format(reportRecord.record.avg2Value,'0.0')}</td>
			<td>${w:format(reportRecord.record.sumValue,'0.0')}</td>
			<td>${w:format(reportRecord.record.longSqls,'0.0')}</td>
			<td>${w:format(reportRecord.longPercent,'0.00%')}</td>
			<td><a href='cat/r/m/${reportRecord.record.sampleLink}/logview.html'>Link</a></td>
		</tr>
		<tr class="graphs"><td colspan="10" align="center"><div id="${status.index}" style="display:none"></div></td></tr>
	</c:forEach> 
</table>
<br>
</jsp:body>
</a:report>

<res:useJs value="${res.js.local.sql_js}" target="bottom-js" />
