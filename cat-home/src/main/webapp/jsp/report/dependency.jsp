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

<a:report title="Dependency Report" navUrlPrefix="domain=${model.domain}">
	
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
<jsp:body>
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>

<div class="report">
	<div class="pagination text-center">
		<ul>
			<c:forEach var="item" items="${model.minutes}">
				<li id="minute${item}"><a href="?domain=${model.domain}&date=${model.date}&minute=${item}">${item}</a></li>
			</c:forEach>
		</ul>
	</div>
		
	<table class="contents table table-striped table-bordered table-condensed">
		<thead><tr>
			<th>Name</th>
			<th>Total Count</th>
			<th>Failure Count</th>
			<th>Failure%</th>
			<th>Avg</th>
		</tr></thead><tbody>
		<c:forEach var="item" items="${model.segment.indexs}" varStatus="status">
			 <c:set var="itemKey" value="${item.key}"/>
			 <c:set var="itemValue" value="${item.value}"/>
			<tr>
				<td>${itemValue.name}</td>
				<td>${w:format(itemValue.totalCount,'0.#')}</td>
				<td>${w:format(itemValue.errorCount,'0.#')}</td>
				<td>${w:format(itemValue.errorCount/itemValue.totalCount,'0.0')}</td>
				<td>${w:format(itemValue.avg,'0.0')}</td>
			</tr>		
		</c:forEach></tbody>
	</table>
	
	</br>
	<table class="contents table table-striped table-bordered table-condensed">
		<thead><tr>
			<th>Type</th>
			<th>Target</th>
			<th>Total Count</th>
			<th>Failure Count</th>
			<th>Failure%</th>
			<th>Avg</th>
		</tr></thead><tbody>
		<c:forEach var="item" items="${model.segment.dependencies}" varStatus="status">
			 <c:set var="itemKey" value="${item.key}"/>
			 <c:set var="itemValue" value="${item.value}"/>
			<tr>
				<td>${itemValue.type}</td>
				<td>${itemValue.target}</td>
				<td>${w:format(itemValue.totalCount,'0.#')}</td>
				<td>${w:format(itemValue.errorCount,'0.#')}</td>
				<td>${w:format(itemValue.errorCount/itemValue.totalCount,'0.0')}</td>
				<td>${w:format(itemValue.avg,'0.0')}</td>
			</tr>		
		</c:forEach></tbody>
	</table>
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
			"oLanguage": {
	            "sProcessing": "正在加载中......",
	            "sLengthMenu": "每页显示 _MENU_ 条记录",
	            "sZeroRecords": "对不起，查询不到相关数据！",
	            "sEmptyTable": "表中无数据存在！",
	            "sInfo": "",
	            "sInfoFiltered": "数据表中共为 _MAX_ 条记录",
	            "sSearch": "搜索",
	            "oPaginate": {
	                "sFirst": "首页",
	                "sPrevious": "上一页",
	                "sNext": "下一页",
	                "sLast": "末页"
	            }
	        }
		});
		$('.switch').css('display','none');
		
	});
</script>