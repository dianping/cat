<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix='fmt' uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.crash.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.crash.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.crash.Model" scope="request" />

<a:mobile>
	<script type="text/javascript">
		function check() {
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				$('#history').slideDown();
				$("#startTime2").val($("#startTime").val());
				$("#endTime2").val($("#endTime").val());
				$("#appName2").val($("#appName").val());
				$("#appVersion2").val($("#appVersion").val());
				$("#platVersion2").val($("#platVersion").val());
				$("#modules2").val($("#modules").val());
				$("#device2").val($("#device").val());
				$("#platform2").val($("#platform").val());
			} else {
				$('#history').slideUp();
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
		
		function query() {
			var times = $("#startTime").val().split(" ");
			var period = times[0];
			var startTime = times[1];
			var endTime = $("#endTime").val();
			var appName = $("#appName").val();
			var appVersion = $("#appVersion").val();
			var platVersion = $("#platVersion").val();
			var module = $("#modules").val();
			var platform = $("#platform").val();
			var split = ";";
			var query1 = period + split + startTime + split + endTime + split + appName + split + appVersion
					+ split + platVersion + split + module + split  + platform;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				var times2 = $("#startTime2").val().split(" ");
				var period2 = times2[0];
				var startTime2 = times2[1];
				var endTime2 = $("#endTime2").val();
				var appName2 = $("#appName2").val();
				var appVersion2 = $("#appVersion2").val();
				var platVersion2 = $("#platVersion2").val();
				var module2 = $("#modules2").val();
				var platform2 = $("#platform2").val();
				query2 = period2 + split + startTime2 + split + endTime2 + split + appName2 + split + appVersion2
				+ split + platVersion2 + split + module2 + split + platform2;
			}

			window.location.href = "?op=appCrashTrend&query1=" + query1 + "&query2=" + query2;
		}

		$(document).ready(
			function() {
				$('#App_report').addClass("active open");
				$('#appCrashTrend').addClass('active');
				$('#startTime').datetimepicker({
					format:'Y-m-d H:i',
					step:30,
					maxDate:0
				});
				$('#endTime').datetimepicker({
					datepicker:false,
					format:'H:i',
					step:30,
					maxDate:0
				});
				$('#startTime2').datetimepicker({
					format:'Y-m-d H:i',
					step:30,
					maxDate:0
				});
				$('#endTime2').datetimepicker({
					datepicker:false,
					format:'H:i',
					step:30,
					maxDate:0
				});

				var query1 = '${payload.query1}';
				var query2 = '${payload.query2}';
				var words = query1.split(";");

				if ((typeof (words[0]) != "undefined" && words[0].length == 0) 
						|| typeof (words[0]) == "undefined") {
					$("#startTime").val(getDate());
				} else {
					$("#startTime").val(words[0] + " " + words[1]);
				}
				if ((typeof (words[2]) != "undefined" && words[2].length == 0) 
						|| typeof (words[2]) == "undefined" ) {
					$("#endTime").val(getTime());
				} else {
					$("#endTime").val(words[2]);
				}
			
				if (typeof(words[3]) == 'undefined' || words[3] == ''){
					$("#appName").val("1");
				} else {
					$("#appName").val(words[3]);
				}
				$("#appVersion").val(words[4]);
				$("#platVersion").val(words[5]);
				$("#modules").val(words[6]);
				
				if (typeof(words[7]) == 'undefined' || words[7] == ''){
					$("#platform").val("1");
				} else {
					$("#platform").val(words[7]);
				}

				var datePair = {};
				datePair["当前值"]=$("#startTime").val().split(" ")[0];

				if (query2 != null && query2 != '') {
					$('#history').slideDown();
					document.getElementById("checkbox").checked = true;
					var words = query2.split(";");

					if (typeof (words[0]) != "undefined"
						&& words[0].length == 0) {
						$("#startTime2").val(getDate());
					} else {
						$("#startTime2").val(words[0] + " " + words[1]);
					}
					if (typeof (words[2]) != "undefined"
					&& words[2].length == 0) {
						$("#endTime2").val(getTime());
					} else {
						$("#endTime2").val(words[2]);
					}
					datePair["对比值"]=$("#startTime2").val().split(" ")[0];
					
					$("#appName2").val(words[3]);
					$("#appVersion2").val(words[4]);
					$("#platVersion2").val(words[5]);
					$("#modules2").val(words[6]);
	
					if (typeof(words[7]) == 'undefined' || words[7] == ''){
						$("#platform2").val("1");
					} else {
						$("#platform2").val(words[7]);
					}				
				} else {
					$("#startTime2").val(getTime());
				}

				var data = ${model.crashLogDisplayInfo.lineChart.jsonString};
				graphMetricChartForDay(document.getElementById('${model.crashLogDisplayInfo.lineChart.id}'), data, datePair);
			});
	</script>

	<%@include file="appCrashTrendDetail.jsp"%>
</a:mobile>

<style type="text/css">
.row-fluid .span2 {
	width: 10%;
}

.row-fluid .span10 {
	width: 87%;
}
</style>