<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix='fmt' uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.browser.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.browser.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.browser.Model" scope="request" />

<a:web_body>
<table>
		<tr>
			<th align=left>
					<div class="input-group" style="float:left;">
						<span class="input-group-addon">开始</span>
					<input type="text" id="time" style="width:110px;"/>
					</div>
					<div class="input-group" style="float:left;width:60px">
	              		<span class="input-group-addon">结束</span>
        	      	<input type="text" id="time2" style="width:60px;"/></div>
					<div class="input-group" style="float:left;">
					<span class="input-group-addon">页面</span>
						<span class="input-icon" style="width:250px;">
							<input type="text" placeholder=""  class="search-input search-input form-control ui-autocomplete-input" id="page" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</div> 
				<div class="input-group" style="float:left;">
					<span class="input-group-addon">阶段</span>
					 <select id="step" style="width: 240px;"></select> 
					 <span class="input-group-addon">网络类型</span>
					 <select id="network" style="width: 80px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.networks}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
				 </div>
			</th>
		</tr>
		<tr>
			<th align=left>
			 <div class="input-group" style="float:left;">
			 <span class="input-group-addon">平台</span><select id="platform" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.platforms}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
			 <span class="input-group-addon">地区</span><select id="city" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.cities}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
			  <span class="input-group-addon">运营商</span><select id="operator" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.operators}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> <span class="input-group-addon">来源</span><select id="source" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.sources}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>
			 <input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" /> </div>
			</th>
		</tr>
	</table>
<table width="100%">
	<tr><td colspan='3'><div id="cityChart" style="height: 400px"></div></td></tr>
	<tr>
		<td><div id="operatorChart" style="width:40%; height: 400px"></div></td>
		<td><div id="sourceChart" style="width:40%; height: 400px"></div></td>
	</tr>
	<tr>
		<td><div id="platformChart" style="height: 400px"></div></td>
		<td><div id="networkChart" style="height: 400px"></div></td>
	</tr>
</table>
<c:forEach var="entry" items="${model.webSpeedDisplayInfo.webSpeedDetails}" >
<table class="table table-striped table-condensed table-bordered table-hover"> 
	<tr>
		<th class="text-success" width="40%">${entry.key}</th>
		<th class="text-success" width="30%">访问量</th>
		<th class="text-success" width="30%">平均响应时间</th>
	</tr>
	<c:forEach var="item" items="${entry.value}">
		<tr><td>${item.itemName}</td><td>${item.accessNumberSum}</td><td>${item.responseTimeAvg}</td></tr>
	</c:forEach>
</table>
</c:forEach>	
	<script>
	function query() {
		var time = $("#time").val();
		var times = time.split(" ");
		var period = times[0];
		var start = converTimeFormat(times[1]);
		var end = converTimeFormat($("#time2").val());

		var page = $("#page").val();
		var step = $("#step").val();
		var network = $("#network").val();
		var platform = $("#platform").val();
		var city = $("#city").val();
		var operator = $("#operator").val();
		var source = $("#source").val();
		var split = ";";
		var query1 = period + split + page + split + step + split + network
				 + split + platform + split + city + split
				+ operator + split + source + split +  start + split + end ;
	
		window.location.href = "?op=speedGraph&query1=" + query1;
	}
	
	function getDate() {
		var myDate = new Date();
		var myMonth = new Number(myDate.getMonth());
		var month = myMonth + 1;
		var day = myDate.getDate();

		if (month < 10) {
			month = '0' + month;
		}
		if (day < 10) {
			day = '0' + day;
		}

		return myDate.getFullYear() + "-" + month + "-" + day + " 00:00";
	}
	function getTime(){
		var myDate = new Date();
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
	
	$(document).ready(
		function() {
			$('#Browser').addClass('active open');
			$('#web_speedGraph').addClass('active');
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
			//custom autocomplete (category selection)
			$.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						that._renderItemData( ul, item );
					});
					ul.bind("click",function(){
						onPageChange();
					});
				}
			});
				
		 	var data = [];
			<c:forEach var="speed" items="${model.speeds}">
				var item = {};
				item['label'] = '${speed.value.id}|${speed.key}';
				data.push(item);
			</c:forEach>
				
			$("#page").catcomplete({
				delay: 0,
				source: data
			}); 
			
			var query1 = '${payload.query1}';
			var words = query1.split(";");

			if (typeof (words[0]) != "undefined" && words[0].length == 0) {
				$("#time").val(getDate());
			} else {
				$("#time").val(words[0] + " " + words[8]);
			}
			
			if(words[9] == null || words.length == 1){
				$("#time2").val(getTime());
			}else{
				$("#time2").val(words[9]);
			}
			
			if(typeof words[1] != "undefined"  && words[1].length > 0) {
				$("#page").val(words[1]);
			}
			onPageChange();
			if(typeof words[2] != "undefined"  && words[2].length > 0) {
				$("#step").val(words[2]);
			}
			$("#network").val(words[3]);
			$("#platform").val(words[4]);
			$("#city").val(words[5]);
			$("#operator").val(words[6]);
			$("#source").val(words[7]);
			
			graphColumnChart('#cityChart', '${model.webSpeedDisplayInfo.cityChart.title}', '',
					${model.webSpeedDisplayInfo.cityChart.xAxisJson}, '${model.webSpeedDisplayInfo.cityChart.yAxis}',
					${model.webSpeedDisplayInfo.cityChart.valuesJson}, '${model.webSpeedDisplayInfo.cityChart.serieName}');
			
			graphColumnChart('#operatorChart', '${model.webSpeedDisplayInfo.operatorChart.title}', '',
					${model.webSpeedDisplayInfo.operatorChart.xAxisJson}, '${model.webSpeedDisplayInfo.operatorChart.yAxis}',
					${model.webSpeedDisplayInfo.operatorChart.valuesJson}, '${model.webSpeedDisplayInfo.operatorChart.serieName}');

			graphColumnChart('#sourceChart', '${model.webSpeedDisplayInfo.sourceChart.title}', '',
					${model.webSpeedDisplayInfo.sourceChart.xAxisJson}, '${model.webSpeedDisplayInfo.sourceChart.yAxis}',
					${model.webSpeedDisplayInfo.sourceChart.valuesJson}, '${model.webSpeedDisplayInfo.sourceChart.serieName}');

			graphColumnChart('#platformChart', '${model.webSpeedDisplayInfo.platformChart.title}', '',
					${model.webSpeedDisplayInfo.platformChart.xAxisJson}, '${model.webSpeedDisplayInfo.platformChart.yAxis}',
					${model.webSpeedDisplayInfo.platformChart.valuesJson}, '${model.webSpeedDisplayInfo.platformChart.serieName}');

			graphColumnChart('#networkChart', '${model.webSpeedDisplayInfo.networkChart.title}', '',
					${model.webSpeedDisplayInfo.networkChart.xAxisJson}, '${model.webSpeedDisplayInfo.networkChart.yAxis}',
					${model.webSpeedDisplayInfo.networkChart.valuesJson}, '${model.webSpeedDisplayInfo.networkChart.serieName}');

	});
	
	var page2Steps = ${model.page2StepsJson};

	function onPageChange() {
	    var	page = $("#page").val();
		var stepSelect = $("#step");
		var steps = page2Steps[page.split("|")[1]]['steps'];
		stepSelect.empty();
		
		for(var s in steps){
			var step = steps[s];
			if(step['title'] != undefined && step['title'].length > 0){
				stepSelect.append($("<option value='"+step['id']+"'>"+step['title']+"</option>"));
			}else{
				stepSelect.append($("<option value='"+step['id']+"'>"+step['step']+"</option>"));
			}
		} 
	}
	</script>
</a:web_body>