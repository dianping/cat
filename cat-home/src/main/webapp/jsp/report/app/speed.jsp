<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix='fmt' uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

<a:mobile>
	<script type="text/javascript">
		function check() {
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				$('#history').slideDown();
				$("#page2").val($("#page").val());
				$("#page2").change();
				$("#step2").val($("#step").val());
				$("#network2").val($("#network").val());
				$("#version2").val($("#version").val());
				$("#platform2").val($("#platform").val());
				$("#city2").val($("#city").val());
				$("#operator2").val($("#operator").val());
				$("#time2").val($("#time").val());
			} else {
				$('#history').slideUp();
			}
		}
		var page2Steps = ${model.appSpeedDisplayInfo.page2StepsJson};
		
		function changeStepByPage(){
			var page = "";
			var stepSelect;
			
			if($(this).attr("id")=="page") {
				page = $("#page").val();
				stepSelect = $("#step");
			}else {
				page = $("#page2").val();
				stepSelect = $("#step2");
			}
			var steps = page2Steps[page];
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

			return myDate.getFullYear() + "-" + month + "-" + day;
		}

		function query() {
			var time = $("#time").val();
			var page = $("#page").val();
			var step = $("#step").val();
			var network = $("#network").val();
			var version = $("#version").val();
			var platform = $("#platform").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var split = ";";
			var query1 = time + split + page + split + step + split + network
					+ split + version + split + platform + split + city + split
					+ operator + split + split;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				var time2 = $("#time2").val();
				var page2 = $("#page2").val();
				var step2 = $("#step2").val();
				var network2 = $("#network2").val();
				var version2 = $("#version2").val();
				var platform2 = $("#platform2").val();
				var city2 = $("#city2").val();
				var operator2 = $("#operator2").val();
				query2 = time2 + split + page2 + split + step2 + split
						+ network2 + split + version2 + split + platform2
						+ split + city2 + split + operator2 + split + split;
			}

			window.location.href = "?op=speed&query1=" + query1 + "&query2=" + query2;
		}

		$(document).ready(
			function() {
				$('#speed').addClass('active');
				$('#time').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});
				$('#time2').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});

				var query1 = '${payload.query1}';
				var query2 = '${payload.query2}';
				var words = query1.split(";");

				$("#page").on('change', changeStepByPage);
				$("#page2").on('change', changeStepByPage);

				if (typeof (words[0]) != "undefined"
						&& words[0].length == 0) {
					$("#time").val(getDate());
				} else {
					$("#time").val(words[0]);
				}
				if(typeof words[1] != "undefined"  && words[1].length > 0) {
					$("#page").val(words[1]);
				}
				$("#page").change();
				if(typeof words[2] != "undefined"  && words[2].length > 0) {
					$("#step").val(words[2]);
				}
				$("#network").val(words[3]);
				$("#version").val(words[4]);
				$("#platform").val(words[5]);
				$("#city").val(words[6]);
				$("#operator").val(words[7]);
				
				var datePair = {};
				datePair["当前值"]=$("#time").val();

				if (query2 != null && query2 != '') {
					$('#history').slideDown();
					document.getElementById("checkbox").checked = true;
					var words = query2.split(";");

					if (typeof (words[0]) != "undefined"
						&& words[0].length == 0) {
						$("#time2").val(getDate());
					} else {
						$("#time2").val(words[0]);
					}
					
					datePair["对比值"]=$("#time2").val();
					if(typeof words[1] != "undefined"  && words[1].length > 0) {
						$("#page2").val(words[1]);
					}
					$("#page2").change();
					if(typeof words[2] != "undefined" && words[2].length > 0) {
						$("#step2").val(words[2]);
					}
					$("#network2").val(words[3]);
					$("#version2").val(words[4]);
					$("#platform2").val(words[5]);
					$("#city2").val(words[6]);
					$("#operator2").val(words[7]);
				} else {
					$("#time2").val(getDate());
				}

				var data = ${model.appSpeedDisplayInfo.lineChart.jsonString};
				
				graphMetricChartForDay(document.getElementById('${model.appSpeedDisplayInfo.lineChart.id}'),
						data, datePair); 
			});
	</script>

			<%@include file="speedDetail.jsp"%>
</a:mobile>

<style type="text/css">
.row-fluid .span2 {
	width: 10%;
}

.row-fluid .span10 {
	width: 87%;
}
</style>