<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />
<a:mobile>
	<script src="${model.webapp}/js/echarts-all.js"></script>
	<script src="${model.webapp}/js/baseGraph.js"></script>

	<table align="center">
		<tr>
			<th>
				<div class="input-group" style="float: left; height: 34px">
					<span class="input-group-addon">开始</span> <input type="text"
						id="time" style="width: 140px; height: 34px" />
				</div>
				<div class="input-group" style="float: left; width: 60px; height: 34px">
					<span class="input-group-addon">结束</span> <input type="text"
						id="time2" style="width: 60px; height: 34px" />
				</div>
				<div class="input-group" style="float: left; height: 34px">
					<span class="input-group-addon">命令字</span>
					<form id="wrap_search" style="margin-bottom: 0px;">
						<span class="input-icon" style="width: 350px; height: 34px"> <input
							type="text" placeholder=""
							class="search-input search-input form-control ui-autocomplete-input"
							id="command" autocomplete="on" data="" /> <i
							class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
				</div> <input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" />
			</th>
		</tr>
	</table>
	<div style="float: left; margin-left: 20px; margin-top: 10px">
		<ul class="nav nav-tabs padding-12 tab-color-blue background-blue"
			style="height: 45px;">
			<li class="active"><a href="#city_delay" data-toggle="tab"><strong>响应时间</strong></a></li>
			<li><a href="#city_success" data-toggle="tab"><strong>成功率(%)</strong></a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="city_delay">
				<div id="delay" style="height: 360px; width: 600px;"></div>
			</div>
			<div class="tab-pane" id="city_success">
				<div id="success" style="height: 360px; width: 600px;"></div>
			</div>
		</div>
	</div>

	<div
		style="height: 405px; width: 430px; margin-left: 20px; margin-top: 10px; float: left">
		<ul class="nav nav-tabs padding-12 tab-color-blue background-blue"
			style="height: 85px;">
			<h4 align="center">运营商性能</h4>
			<li class="active"><a href="#operator_delay" data-toggle="tab"><strong>响应时间</strong></a></li>
			<li><a href="#operator_success" data-toggle="tab"><strong>成功率(%)</strong></a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="operator_delay">
				<div id="operator" style="height: 320px; width: 400px;"></div>
			</div>
			<div class="tab-pane" id="operator_success">
				<div id="operator-success" style="height: 320px; width: 400px;"></div>
			</div>
		</div>
	</div>
	<div
		style="height: 450px; width: 630px; float: left; margin-left: 20px; margin-top: 40px; ">
		<ul class="nav nav-tabs padding-12 tab-color-blue background-blue"
			style="height: 85px;">
		<h4 align="center">访问趋势</h4>
			<li class="active"><a href="#trend_delay" data-toggle="tab"><strong>响应时间</strong></a></li>
			<li><a href="#trend_success" data-toggle="tab"><strong>成功率(%)</strong></a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="trend_delay">
				<div id="lineChart" style="height: 340px;"></div>
			</div>
			<div class="tab-pane" id="trend_success">
				<div id="successLineChart" style="height: 340px;"></div>
			</div>
		</div>
	</div>
	
	<div
		style="height: 450px; width: 420px; margin-left: 30px; float: left; margin-top: 40px">
		<ul class="nav nav-tabs padding-12 tab-color-blue background-blue"
			style="height: 85px;">
			<h4 align="center">平台性能</h4>
			<li class="active"><a href="#platform_delay" data-toggle="tab"><strong>响应时间</strong></a></li>
			<li><a href="#platform_success" data-toggle="tab"><strong>成功率(%)</strong></a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="platform_delay">
				<div id="platform" style="height: 340px; width: 380px;"></div>
			</div>
			<div class="tab-pane" id="platform_success">
				<div id="platform-success" style="height: 340px; width: 380px;"></div>
			</div>
		</div>
	</div>
	<div
		style="width: 1050px; margin-left: 30px; float: left; margin-top: 40px">
		<ul class="nav nav-tabs padding-12 tab-color-blue background-blue"
			style="height: 85px;">
			<h4 align="center">版本性能</h4>
			<li class="active"><a href="#version_delay" data-toggle="tab"><strong>响应时间</strong></a></li>
			<li><a href="#version_success" data-toggle="tab"><strong>成功率(%)</strong></a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="version_delay">
				<div id="version" style="width: 1000px;"></div>
			</div>
			<div class="tab-pane" id="version_success">
				<div id="version-success" style="width: 1000px;"></div>
			</div>
		</div>
	</div>
</a:mobile>
<script type="text/javascript">
function query() {
	var time = $("#time").val();
	var times = time.split(" ");
	var period = times[0];
	var start = converTimeFormat(times[1]);
	var end = converTimeFormat($("#time2").val());
	var command = $("#command").val().split('|')[0];
	var commandId = ${model.command2IdJson}[command].id;
	var split = ";";
	var query1 = period + split + commandId + split + split + split  + split + split  + split  + split  + split + start + split + end;
	
	var field = $("#piechartSelect").val();
	var href = "?op=dashboard&query1=" + query1 + "&commandId="+$("#command").val() ;
		window.location.href = href;
}

$(document).ready(
	function() {
		$('#Dashboard').addClass('active');
		$('#App_report').removeClass('active open');
		$('#time').datetimepicker({
			format:'Y-m-d H:i',
			step:30,
			maxDate:0
		});
		$('#time2').datetimepicker({
			datepicker:false,
			format:'H:i',
			step:30,
			maxDate:0
		});
		var query1 = '${payload.query1}';
		var words = query1.split(";");
		if (words[0] == null || words.length == 1) {
			$("#time").val(getFromTime());
		} else {
			$("#time").val(words[0] + " " + words[9]);
		}
		
		if(words[10] == null || words.length == 1){
			$("#time2").val(getTime(new Date()));
		}else{
			$("#time2").val(words[10]);
		}
		
		if(typeof(words[1]) != 'undefined' && words[1].length > 0){
			$("#command").val('${payload.commandId}');
		}else{
			$("#command").val('all|all');
		}
		 $.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						if ( item.category != currentCategory ) {
							ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
							currentCategory = item.category;
						}
						that._renderItemData( ul, item );
					});
				}
			});

		
		var data = [];
		<c:forEach var="command" items="${model.commands}">
					var item = {};
					item['label'] = '${command.value.name}|${command.value.title}';
					if('${command.value.domain}'.length >0 ){
						item['category'] ='${command.value.domain}';
					}else{
						item['category'] ='未知项目';
					}
					data.push(item);
		</c:forEach>
				
		$( "#command" ).catcomplete({
			delay: 0,
			source: data
		});
		
		graphMapChart('delay', '${model.dashBoardInfo.mapChart.title}', '${model.dashBoardInfo.mapChart.subTitle}', 'delay', ${model.dashBoardInfo.mapChart.min}, ${model.dashBoardInfo.mapChart.max},  ${model.dashBoardInfo.mapChart.data});
		graphSuccessMapChart('success', '${model.dashBoardInfo.successMapChart.title}', '${model.dashBoardInfo.successMapChart.subTitle}', 'successRatio', ${model.dashBoardInfo.successMapChart.min}, ${model.dashBoardInfo.successMapChart.max},  ${model.dashBoardInfo.successMapChart.data});

		graphBarChart('#operator', '${model.dashBoardInfo.operatorChart.title}', '',
				${model.dashBoardInfo.operatorChart.xAxisJson}, '${model.dashBoardInfo.operatorChart.yAxis}',
				${model.dashBoardInfo.operatorChart.valuesJson}, '${model.dashBoardInfo.operatorChart.serieName}');
		graphBarChart('#operator-success', '${model.dashBoardInfo.operatorSuccessChart.title}', '',
				${model.dashBoardInfo.operatorSuccessChart.xAxisJson}, '${model.dashBoardInfo.operatorSuccessChart.yAxis}',
				${model.dashBoardInfo.operatorSuccessChart.valuesJson}, '${model.dashBoardInfo.operatorSuccessChart.serieName}', 90);
		graphColumnChart('#version', '${model.dashBoardInfo.versionChart.title}', '',
				${model.dashBoardInfo.versionChart.xAxisJson}, '${model.dashBoardInfo.versionChart.yAxis}',
				${model.dashBoardInfo.versionChart.valuesJson}, '${model.dashBoardInfo.versionChart.serieName}');
		graphColumnChart('#version-success', '${model.dashBoardInfo.versionSuccessChart.title}', '',
				${model.dashBoardInfo.versionSuccessChart.xAxisJson}, '${model.dashBoardInfo.versionSuccessChart.yAxis}',
				${model.dashBoardInfo.versionSuccessChart.valuesJson}, '${model.dashBoardInfo.versionSuccessChart.serieName}', 90);
		graphColumnChart('#platform', '${model.dashBoardInfo.platformChart.title}', '',
				${model.dashBoardInfo.platformChart.xAxisJson}, '${model.dashBoardInfo.platformChart.yAxis}',
				${model.dashBoardInfo.platformChart.valuesJson}, '${model.dashBoardInfo.platformChart.serieName}');
		graphColumnChart('#platform-success', '${model.dashBoardInfo.platformSuccessChart.title}', '',
				${model.dashBoardInfo.platformSuccessChart.xAxisJson}, '${model.dashBoardInfo.platformSuccessChart.yAxis}',
				${model.dashBoardInfo.platformSuccessChart.valuesJson}, '${model.dashBoardInfo.platformSuccessChart.serieName}', 90);
		
		var data = ${model.dashBoardInfo.lineChart.jsonString};
		graphLineChart(document.getElementById("lineChart"), data);
		
		var successData = ${model.dashBoardInfo.successLineChart.jsonString};
		graphLineChart(document.getElementById("successLineChart"), successData);
	});
	
function getFromTime() {
	var now = new Date();
	var myDate = now.getTime() - 30 * 60 * 1000;
	var from = new Date(myDate);
	
	var day = getDay(from);
	var nowDay = getDay(now);
	
	if(day == nowDay){
		return day + " " + getTime(from) ;
	}else {
		return nowDay + " 00:00";
	}
	
}

function getDay(myDate){
	var myMonth = new Number(myDate.getMonth());
	var month = myMonth + 1;
	var day = myDate.getDate();
	
	if(month<10){
		month = '0' + month;
	}
	if(day<10){
		day = '0' + day;
	}
	
	return myDate.getFullYear() + "-" + month + "-" + day ;
}

function getTime(myDate){
	var myHour = new Number(myDate.getHours());
	var myMinute = new Number(myDate.getMinutes());
	
	if(myHour < 10){
		myHour = '0' + myHour;
	}
	if(myMinute < 10){
		myMinute = '0' + myMinute;
	}
	return myHour + ":" + myMinute;
}

function converTimeFormat(time){
	var times = time.split(":");
	var hour = times[0];
	var minute = times[1];
	
	if(hour.length == 1){
		hour = "0" + hour;
	}
	if(minute.length == 1) {
		minute = "0" + minute;
	}
	return hour + ":" + minute;
}

</script>
