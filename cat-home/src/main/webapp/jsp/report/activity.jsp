<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.activity.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.activity.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.activity.Model" scope="request"/>

<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
		<div id="queryBar">
			 <div style="float:left;">
		&nbsp;开始
		<input type="text" id="startTime" style="width:150px;"/>
		结束
		<input class="btn btn-primary  btn-sm"  style="margin-bottom:4px;" value="查询" onclick="queryNew()" type="submit"></div>
		</div>
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
			});
			function queryNew(){
				var startTime=$("#startTime").val();
				var endTime=$("#endTime").val();
				window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime;
			}
		</script>
</a:body>
