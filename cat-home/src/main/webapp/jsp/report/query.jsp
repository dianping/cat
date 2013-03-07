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
<res:useCss value="${res.css.local.transaction_css}" target="head-css"/>
<script>
$(function() {
   $( "#start" ).datepicker();
   $( "#end" ).datepicker();
});
 
function query(){
  var queryDomain=$("#domain").val();
  var queryType=$("#reportType").val();
  var reportLevel=$("#reportLevel").val();
  var type=$("#type").val();
  var name=$("#name").val();
  var start=$("#start").val();
  var end=$("#end").val();
  
  window.location.href="?queryDomain="+queryDomain+"&queryType="+queryType+"&reportLevel="+reportLevel+"&type="+type+"&name="+name+'&start='+start+"&end="+end;
}
$(document).ready(function() {
	$('#contents').dataTable({
		"sPaginationType": "full_numbers",
		'iDisplayLength': 100,
		"oLanguage": {
            "sProcessing": "正在加载中......",
            "sLengthMenu": "每页显示 _MENU_ 条记录",
            "sZeroRecords": "对不起，查询不到相关数据！",
            "sEmptyTable": "表中无数据存在！",
            "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
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
});
</script>  
<a:body>
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>

<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;Query Transaction\Event\Problem Data By Day</td>
	</table>
	<table>
		<tr>
			<th>
				Report: 
				<select id="reportType">
						<option value="transaction">transaction</option>
						<option value="event">event</option>
						<option value="problem">problem</option>
				</select> 
				ReportType: 
				<select id="reportLevel">
						<option value="day">day</option>
						<option value="hour">hour</option>
				</select> 
				Domain:<input type="text" size="30" id="domain"></inpupt>
				Type:<input type="text" size="30" id="type"></inpupt>
				Name:<input type="text" size="30" id="name"></inpupt>
				StartTime:<input type="text" size="20" id="start"></inpupt>
				EndTime:<input type="text" size="20" id="end"></inpupt>
				<input type="submit" onclick="query()"></input>
			</th>
		</tr>
	</table>
	<table class="project" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<td>Date</td>
					<td>Type</td>
					<td>TotalCount</td>
					<td>FailureCount</td>
					<td>Failure%</td>
					<td>Min</td>
					<td>Max</td>
					<td>Avg</td>
					<td>Line95</td>
				</tr></thead><tbody>
				<c:forEach var="e" items="${model.transactionItems}"
					varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td>${w:format(e.date,'yyyy-MM-dd HH:mm:ss')}</td>
					<td>${e.type}</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					</tr></c:forEach></tbody></table>
					
	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>