<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.trend.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.trend.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.trend.Model"
	scope="request" />

<res:bean id="res" />
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.trend_css}' target="head-css" />
<res:useCss value='${res.css.local.jqueryUI_css}' target="head-css" />
<res:useCss value='${res.css.local.calendar_css}' target="head-css" />
<res:useJs value="${res.js.local.jqueryMin_js}" target="head-js" />
<res:useJs value="${res.js.local.jqueryUIMin_js}" target="head-js" />
<res:useJs value="${res.js.local.flotr2_js}" target="head-js" />
<res:useJs value="${res.js.local.datepicker_js}" target="head-js" />

<a:body>
<div class="report">
<table class="header">
	<tr>
		<td class="title">Trend Grapy</td>
		<td class="timestamp">Generated: ${model.creatTime}</td>
	</tr>
</table>

<table class="navbar">
	<tr>
		<td class="domain">
		<div class="domain"><c:forEach var="domain"
			items="${model.domains}">
			<c:choose>
				<c:when test="${model.domain eq domain}">
					<a href="?domain=${domain}" class="current">[&nbsp;${domain}&nbsp;]</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${domain}">[&nbsp;${domain}&nbsp;]</a>
				</c:otherwise>
			</c:choose>
		</c:forEach></div>
		</td>
	</tr>
</table>
<table>
	<tr style="text-align: left">
		<th>Graph Types: <c:forEach var="graphItem" items="${model.graphTypes}" varStatus="status">[&nbsp;
			<c:choose>
				<c:when test="${model.graphType eq graphItem}">
					<a href="?domain=${model.domain}&graphType=${graphItem}" class="current">${graphItem}</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&graphType=${graphItem}" >${graphItem}</a>
				</c:otherwise>
			</c:choose>
			&nbsp;]
		</c:forEach></th>
	</tr>
</table>

<table>
	<tr>
		<td>Report Type</td>
		<td>
		<input type="text" id="hiddenDateType" style="display: none" value="${model.dateType}">
		<select size="1" id="id_dateType">
			<option value="day">日报表</option>
			<option value="week">周报表</option>
			<option value="month">月报表</option>
		</select></td>
		<td>Choose Date<input type="text" id="datepicker" value="${model.queryDate}"></input></td>
	</tr>
	<tr>
		<td>Custom</td>
		<td>
		<input type="text" id="hiddenSelfQueryOption" style="display: none" value="${model.selfQueryOption}"></input>
		<select  id="id_selfDefinedType"  onchange="showSelfDefined()">
			<option value="transaction">transaction</option>
			<option value="event">event</option>
			<option value="problem">problem</option>
		</select></td>
		<td>ip:<input type='text' id="ip" value="${model.queryIP}"></input> 
			Type:<input type='text' id="type" value="${model.queryType}"></input> 
			<span id="status">Name:</span><input type='text' id="nameOrStatus" value="${model.queryName}" ></input>
			<input type="button" value="search" onclick="searchReport('${model.domain}','${model.graphType}')" /></td>
	</tr>
</table>

<div>the graph here!!</div>

<table class="footer">
	<tr>
		<td>[ end ]</td>
	</tr>
</table>
</div>

<res:useJs value="${res.js.local.trend_js}" target="bottom-js" />
</a:body>
