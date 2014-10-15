<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<script type="text/javascript">
		var commandInfo = ${model.command};
		var command1Change = function command1Change() {
			var key = $("#command").val();
			var value = commandInfo[key];
			var code = document.getElementById("code");
			
			for ( var prop in value) {
				var opt = $('<option />');

				opt.html(value[prop].name);
				opt.val(value[prop].id);
				opt.appendTo(code);
			}
		}

		function getDate() {
			var myDate = new Date();
			var myMonth = new Number(myDate.getMonth());
			var month = myMonth + 1;
			var day = myDate.getDate();
			
			if(month<10){
				month = '0' + month;
			}
			if(day<10){
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

		function query() {
			var time = $("#time").val();
			var times = time.split(" ");
			var period = times[0];
			var start = converTimeFormat(times[1]);
			var end = converTimeFormat($("#time2").val());
			var command = $("#command").val();
			var code = $("#code").val();
			var network = $("#network").val();
			var version = $("#app-version").val();
			var connectionType = $("#connnect-type").val();
			var platform = $("#platform").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var split = ";";
			var query1 = period + split + command + split + code + split
					+ network + split + version + split + connectionType
					+ split + platform + split + city + split + operator + split + start + split + end;
			
			var field = $("#piechartSelect").val();
			var href = "?op=piechart&query1=" + query1 + "&groupByField=" + field;
 			window.location.href = href;
 		}
		
		function refreshDisabled(){
			document.getElementById("code").disabled = false;
			document.getElementById("network").disabled = false;
			document.getElementById("app-version").disabled = false;
			document.getElementById("connnect-type").disabled = false;
			document.getElementById("platform").disabled = false;
			document.getElementById("city").disabled = false;
			document.getElementById("operator").disabled = false;
			document.getElementById($("#piechartSelect").val()).disabled = true;
		}

		$(document).ready(
				function() {
					$('#datetimepicker1').datetimepicker();
					$('#datetimepicker2').datetimepicker({
						pickDate: false
						});

					var query1 = '${payload.query1}';
					var command1 = $('#command');
					var words = query1.split(";");

					command1.on('change', command1Change);
					$("#command").val(words[1]);
					
					$("#piechartSelect").on('change', refreshDisabled);
					
					if (words[0] == null || words.length == 1) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0] + " " + words[9]);
					}
					
					if(words[10] == null || words.length == 1){
						$("#time2").val(getTime());
					}else{
						$("#time2").val(words[10]);
					}

					command1Change();
					$("#code").val(words[2]);
					$("#network").val(words[3]);
					$("#app-version").val(words[4]);
					$("#connnect-type").val(words[5]);
					$("#platform").val(words[6]);
					$("#city").val(words[7]);
					$("#operator").val(words[8]);
					$("#piechartSelect").val('${payload.groupByField.name}');
					refreshDisabled();
					
					graphPieChart(document.getElementById('piechart'), ${model.pieChart.jsonString});
				});
	</script>
	
	<div class="row-fluid">
	<div class="span2">
        <div class="well sidebar-nav">
          <ul class="nav nav-list">
			<li class='nav-header'><a href="?op=view&showActivity=false"><strong>API访问趋势</strong></a></li>
			<li class='nav-header'><a href="?op=view&showActivity=true"><strong>运营活动趋势</strong></a></li>
          	<li class='nav-header active'><a href="?op=piechart"><strong>访问量分布</strong></a></li>
          </ul>
        </div>
	</div>
	<div class="span10">
		<%@include file="appPiechartDetail.jsp"%>
	</div>
		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>

<style type="text/css">
	.row-fluid .span2{
		width:10%;
	}
	.row-fluid .span10{
		width:87%;
	}
</style>