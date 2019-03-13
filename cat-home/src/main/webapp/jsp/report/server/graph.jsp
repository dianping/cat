<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model" scope="request"/>
<a:serverBody>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>

		<ul class="breadcrumb">
			<table>
			<tr><th>
					<span>开始</span>
					<input type="text" id="startTime" style="width:150px;"/>
				</th>
				<th>
					<span>&nbsp;结束</span>
					<input type="text" id="endTime" style="width:150px;"/>
				</th>
				<th>&nbsp;视角
					<div id="view_button_group" class="btn-group btn-corner">
						<button type="button" id="view_endPoint" value="endPoint" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshViewButton('endPoint')">EndPoint</button>
						<button type="button" id="view_measurement" value="measurement" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshViewButton('measurement')">Measure</button>
						<button type="button" id="view_" value="" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshViewButton('')">组合</button>
					</div>
				</th>
				<th>&nbsp;采样
					<div id="type_button_group" class="btn-group btn-corner">
						<button type="button" id="type_sum" value="sum" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshTypeButton('sum')">求和</button>
						<button type="button" id="type_mean" value="mean" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshTypeButton('mean')">求平均</button>
						<button type="button" id="type_max" value="max" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshTypeButton('max')">最大值</button>
						<button type="button" id="type_min" value="min" class="btn btn-white btn-sm btn-primary" choosed="false" onclick="freshTypeButton('min')">最小值</button>
					</div>
				</th>
				</tr>
			</table>
		</ul><!-- /.breadcrumb -->
	</div>
	
	<div class="page-content">
	<div class="page-content-area">
	<div class="row">
	<div class="col-xs-12">
	<div class="tabbable">
	<br>
	<div class="col-xs-12">
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.id}" class="metricGraph" style="width:450px;height:350px;"></div>
   			</div>
		</c:forEach>
	</div>
	</div></div></div></div></div>
	
	<script type="text/javascript">
	
	function freshTypeButton(type, reload){
		$('#type_button_group').children("button").attr("choosed","false");
		$('#type_button_group').children("button").removeClass("btn-success").addClass("btn-white");
		$('#type_'+type).attr("choosed","true");
		$('#type_'+type).removeClass("btn-white");
		$('#type_'+type).addClass("btn-success");
		
		if(typeof(reload) == "undefined" || reload == false){
			query();
		}
	}
	
	function freshViewButton(view, reload){
		$('#view_button_group').children("button").attr("choosed","false");
		$('#view_button_group').children("button").removeClass("btn-primary").addClass("btn-white");
		$('#view_'+view).attr("choosed","true");
		$('#view_'+view).removeClass("btn-white");
		$('#view_'+view).addClass("btn-primary");
		
		if(typeof(reload) == "undefined" || reload == false){
			query();
		}
	}
	
	function query() {
		var start = $("#startTime").val();
		var end = $("#endTime").val();
		var type = $('#type_button_group').find("[choosed='true']").attr("value");
		var view = $('#view_button_group').find("[choosed='true']").attr("value");
		window.location.href = "?op=graph&graphId=${payload.graphId}&view="+view+"&startDate=" + start + "&endDate=" + end +
				"&type=" + type; 
	}
	
	
	$(document).ready(
		function() {
			$('#startTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$('#endTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			
			$('#startTime').val("${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(payload.historyEndDate,'yyyy-MM-dd HH:mm')}");
			
			$('#serverChart').addClass('active open');
			$('#serverGraph').addClass('active');
			
			var type = "${payload.type}";
			freshTypeButton(type, true);
			
			var view = "${payload.view}";
			freshViewButton(view, true);

			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
				var data = ${item.jsonString};
				graphMetricChart(document.getElementById('${item.id}'), data);
			</c:forEach>
		});	
	</script>
	
</a:serverBody>