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

<table>
<tr>
		<th>
			Domain
			<select id="domain">
			<option value="MobileApi">MobileApi</option>
			<option value="TuangouApi">TuangouApi</option>
			<option value="Cat">Cat</option>
		</select>
		</th>
		<th>
		Report Type:
		<select id="reportType">
			<option value="transaction">Transaction</option>
			<option value="event">Event</option>
			<option value="problem">Problem</option>
		</select>
		</th>
		<th>
		Date Type:
		<select size="1" id="id_dateType">
			<option value="day">日报表</option>
			<option value="week">周报表</option>
			<option value="month">月报表</option>
		</select>
		StartTime<input type="text" id="startDate" size="10"
				onchange="onStartDateChange()" value="">
		EndTime  <input type="text" id="endDate" size="10" value="">
				</th>
				<th>
		<input value="Go" onclick="showSummarizedReport()" type="submit">
	</th>
</tr>
</table>
</br>

<res:useJs value="${res.js.local.historyReport_js}" target="bottom-js" />
</jsp:body>

</a:simpleReport>