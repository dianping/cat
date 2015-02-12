<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.activity.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.activity.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.activity.Model" scope="request"/>

<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
		<div id="queryBar">
			 <div style="float:left;">
		&nbsp;开始
		<input type="text" id="startTime" style="width:150px;"/>
		结束
		<input type="text" id="endTime" style="width:150px;"/>
		<input class="btn btn-primary  btn-sm"  style="margin-bottom:4px;" value="查询" onclick="queryNew()" type="submit"></div>
		</div>
			<c:forEach var="item" items="${model.charts}" varStatus="status">
				<div class="col-xs-12">
					<h5 class="text-center text-error"> ${item.key}</h5>
					<c:forEach var="chart" items="${item.value}" varStatus="status">
		   				<div style="float:left;">
		   						<div id="${chart.id}" class="metricGraph" style="width:40%;height:350px;"></div>
		   				</div>
		   			</c:forEach>
				</div>
			</c:forEach>
		<script type="text/javascript">
			$(document).ready(function(){
				$('#System_report').addClass("open active");
				$('#system_activity').addClass("active");
				
				$('#startTime').datetimepicker({
					format:'Y-m-d H:i',
					step:60,
					maxDate:0
				});
				$('#endTime').datetimepicker({
					format:'Y-m-d H:i',
					step:60,
					maxDate:0
				});
				$('#startTime').val("${w:format(model.start,'yyyy-MM-dd HH:mm')}");
				$('#endTime').val("${w:format(model.end,'yyyy-MM-dd HH:mm')}");
				
				<c:forEach var="item" items="${model.charts}" varStatus="status">
						<c:forEach var="chart" items="${item.value}" varStatus="status">
		   						var data = ${chart.jsonString};
		   						graphLineChart(document.getElementById('${chart.id}'), data);			
			   			</c:forEach>
				</c:forEach>
				
			});
			function queryNew(){
				var startTime=$("#startTime").val();
				var endTime=$("#endTime").val();
				window.location.href="?op=view&startTime="+startTime+"&endTime="+endTime;
			}
		</script>
</a:body>
