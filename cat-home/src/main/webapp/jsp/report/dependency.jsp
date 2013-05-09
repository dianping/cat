<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
<script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>

<a:report title="Dependency Report"
	navUrlPrefix="domain=${model.domain}">
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useCss value='${res.css.local.report_css}' target="head-css" />
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}"
			target="head-js" />
<div class="report">
	<c:forEach var="item" items="${model.minutes}" varStatus="status">
		<c:if test="${status.index % 20 ==0}">
			<div class="pagination text-center">
			<ul>
		</c:if>
		<li id="minute${item}"><a
					href="?domain=${model.domain}&date=${model.date}&minute=${item}">
					<c:if test="${item < 10}">0${item}</c:if>
					<c:if test="${item >= 10}">${item}</c:if>
				</a></li>
		<c:if test="${status.index % 20 ==19 || status.last}">
			</ul>
			</div>
		</c:if>
	</c:forEach>
			
	<div class="row-fluid">
  <div class="span7">
	<table	class="contents table table-striped table-bordered table-condensed">
		<thead>	<tr>
			<th>Name</th>
			<th>Total Count</th>
			<th>Failure Count</th>
			<th>Failure%</th>
			<th>Avg(ms)</th>
		</tr></thead><tbody>
		<c:forEach var="item" items="${model.segment.indexs}"
								varStatus="status">
			 <c:set var="itemKey" value="${item.key}" />
			 <c:set var="itemValue" value="${item.value}" />
			<tr>
				<td>${itemValue.name}</td>
				<td>${w:format(itemValue.totalCount,'#,###,###,###,##0')}</td>
				<td>${w:format(itemValue.errorCount,'#,###,###,###,##0')}</td>
				<td>${w:format(itemValue.errorCount/itemValue.totalCount,'0.0')}</td>
				<td>${w:format(itemValue.avg,'0.0')}</td>
			</tr>		
		</c:forEach></tbody>
	</table>
	
	</br>
	<table	class="contents table table-striped table-bordered table-condensed">
		<thead>	<tr>
			<th>Type</th>
			<th>Target</th>
			<th>Total Count</th>
			<th>Failure Count</th>
			<th>Failure%</th>
			<th>Avg(ms)</th>
		</tr></thead><tbody>
		<c:forEach var="item" items="${model.segment.dependencies}"
								varStatus="status">
			 <c:set var="itemKey" value="${item.key}" />
			 <c:set var="itemValue" value="${item.value}" />
			<tr>
				<td>${itemValue.type}</td>
				<td>${itemValue.target}</td>
				<td>${w:format(itemValue.totalCount,'#,###,###,###,##0')}</td>
				<td>${w:format(itemValue.errorCount,'#,###,###,###,##0')}</td>
				<td>${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
				<td>${w:format(itemValue.avg,'0.0')}</td>
			</tr>		
		</c:forEach></tbody>
	</table>
	</div>
  <div class="span5">
  	<table	class="contents table table-striped table-bordered table-condensed">
  		<tr><td>CAT告警：Connection Error</td></tr>
	</table>
  </div>
</div>
</div>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
		$('.switch').css('display','none');
		
		$('.contents').dataTable({
			"sPaginationType": "full_numbers",
			'iDisplayLength': 50,
			"bPaginate": false,
			"bFilter": false,
		});
		$('.switch').css('display','none');
	});
</script>
<style>
	.pagination{
		margin:4px 0;
	}
</style>