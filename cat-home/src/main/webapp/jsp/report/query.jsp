<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.query.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.query.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.query.Model" scope="request"/>
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
<script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>
<a:body>
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['query.js']}" target="head-js"/>
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
<script>
$(document).ready(function() {
	init();
	
	var report = '${payload.queryType}';
	var reportLevel = '${payload.reportLevel}';
	
	$('#reportType').val(report);
	$('#reportLevel').val(reportLevel);
});
</script> 
<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;Query Data</td>
	</table>
	</br>
	<table align="center" width="60%" rules="all" border=1>
		<thead>
			<tr>
				<th colspan="3">
				查询一段时间内的综合数据
			</th>
			</tr>
		</thead>
		<tbody>
		<tr>
			<td>
				<span class="required">&nbsp;*</span><span class="lable">查询报表</span>
			</td>
			<td><select id="reportType" style="width:150px">
						<option value="transaction">transaction</option>
						<option value="event">event</option>
						<option value="problem">problem</option>
				</select></td>
			<td>报表支持Transaction\Event\Problem报表</td>
		</tr>
		<tr>
			<td><span class="required">&nbsp;*</span><span class="lable">报表类型</span>
			<td><select id="reportLevel" style="width:150px">
						<option value="day">day</option>
						<option value="hour">hour</option>
				</select></td>
			<td>报表类型支持小时、天</td>
		</tr>
		<tr>
			<td><span class="required">&nbsp;*</span><span class="lable">项目名称</span>
			<td><input type="text" size="30" id="domain" value="${payload.queryDomain}"></inpupt></td>
			<td class="required">大小写敏感</td>
		</tr>
		<tr>
			<td><span class="required">&nbsp;*</span><span class="lable">Type</span>
			<td><input type="text" size="30" id="type" value="${payload.type}"></inpupt></td>
			<td>查询的第一级</td>
		</tr>
		<tr>
			<td><span class="required">&nbsp;&nbsp;</span><span class="lable">Name</span>
			<td><input type="text" size="30" id="name" value="${payload.name}"></inpupt></td>
			<td>查询的第二级（有此项即查询第二级的数据）</td>
		</tr>
		<tr>
			<td><span class="required">&nbsp;*</span><span class="lable">开始时间</span>
			<td><input type="text" size="30" id="start" value="${payload.startStr}"></inpupt></td>
			<td>查询开始时间</td>
		</tr>
		<tr>
			<td><span class="required">&nbsp;*</span><span class="lable">结束时间</span>
			<td><input type="text" size="30" id="end" value="${payload.endStr}"></inpupt></td>
			<td>查询结束时间<span class="required">（时间间隔不允许超过一个月）</span></td>
		</tr>
		<tr>
			<th colspan="3">
				<input type="submit" onclick="query()" style="width:100px;">
				</th>
		</tr>
		</tbody>
	</table>
	</br></br>
	
	<c:if test="${payload.queryType eq 'transaction' }">
		<table class="project" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<td>Date</td>
					<td>Type</td>
					<c:if test="${not empty payload.name}">
						<td>Name</td>
					</c:if>
					<td>TotalCount</td>
					<td>FailureCount</td>
					<td>Failure%</td>
					<td>Min(ms)</td>
					<td>Max(ms)</td>
					<td>Avg(ms)</td>
					<td>Line95(ms)</td>
				</tr></thead><tbody>
				<c:forEach var="e" items="${model.transactionItems}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td>${w:format(e.date,'yyyy-MM-dd HH:mm:ss')}</td>
					<td>${e.type}</td>
					<c:if test="${not empty payload.name}">
						<td>${e.name}</td>
					</c:if>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					</tr></c:forEach></tbody></table>
	</c:if>
	
	<c:if test="${payload.queryType eq 'event' }">
		<table class="project" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<td>Date</td>
					<td>Type</td>
					<c:if test="${not empty payload.name}">
						<td>Name</td>
					</c:if>
					<td>TotalCount</td>
					<td>FailureCount</td>
					<td>Failure%</td>
				</tr></thead><tbody>
				<c:forEach var="e" items="${model.eventItems}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td>${w:format(e.date,'yyyy-MM-dd HH:mm:ss')}</td>
					<td>${e.type}</td>
					<c:if test="${not empty payload.name}">
						<td>${e.name}</td>
					</c:if>
					<td>${e.totalCount}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					</tr></c:forEach></tbody></table>
	</c:if>
	
	<c:if test="${payload.queryType eq 'problem' }">
		<table class="project" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<td>Date</td>
					<td>Type</td>
					<c:if test="${not empty payload.name}">
						<td>Name</td>
					</c:if>
					<td>FailureCount</td>
				</tr></thead><tbody>
				<c:forEach var="e" items="${model.problemItems}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td>${w:format(e.date,'yyyy-MM-dd HH:mm:ss')}</td>
					<td>${e.type}</td>
					<c:if test="${not empty payload.name}">
						<td>${e.name}</td>
					</c:if>
					<td>${e.totalCount}</td>
					</tr></c:forEach></tbody></table>
	</c:if>
	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>