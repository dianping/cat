<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.web.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.web.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.web.Model" scope="request" />

<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<script type="text/javascript">
		var urlData = ${model.items};
		var cityData = ${model.cityInfo};
		
		function check() {
			var value = document.getElementById("checkbox").checked;
	
			if (value == true) {
				$('#history').slideDown();
				$('#startTime2').val($('#startTime').val());
				$('#endTime2').val($('#endTime').val());
				$("#group2").val($("#group").val());
				$("#group2").change();
				$("#url2").val($("#url").val());
				$("#province2").val($("#province").val());
				$('#province2').change();
				$("#city2").val($("#city").val());
				$("#channel2").val($("#channel").val());
				$("#type2").val($("#type").val());
			} else {
				$('#history').slideUp();
			}
		}
		function query(){
			var group = $("#group").val();
			var url = $("#url").val();
			var city = $("#city").val();
			var type = $("#type").val();
			var channel = $("#channel").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			
			var value = document.getElementById("checkbox").checked;
			if (value) {
				var start2 = $("#startTime2").val();
				var end2 = $("#endTime2").val();
				var group2 = $("#group2").val();
				var url2 = $("#url2").val();
				var city2 = $("#city2").val();
				var channel2 = $("#channel2").val();
				var split = ";";
				start += split + start2;
				end += split + end2;
				url += split + url2;
				group += split + group2;
				city += split + city2;
				channel += split + channel2;
			} 
			window.location.href="?url="+url +"&group="+group+ "&city="+city+"&type="+type+"&channel="+channel+"&startDate="+start+"&endDate="+end;

		}
		
		function groupChange(){
			var key;
			var url;
			if($(this).attr("id")=="group") {
				key = $('#group').val();
				url = document.getElementById('url');
			} else {
				key = $("#group2").val();
				url = document.getElementById("url2");
			}
			url.length=0;
			var value = urlData[key];
			for (var prop in value) {
			    var opt = $('<option />');
		  		
		  		opt.html(value[prop].pattern);
			  	opt.val(value[prop].name);
		  		opt.appendTo(url);
			}
		}
		
		function typeChange(){
			var type = $('#type').val();
			if(type != "info"){
				$('#history').slideUp();
				document.getElementById("checkbox").checked = false;
				document.getElementById("checkbox").style.visibility = 'hidden';
				document.getElementById("checkboxLabel").style.visibility = 'hidden';
			}else{
				document.getElementById("checkbox").style.visibility = 'visible';
				document.getElementById("checkboxLabel").style.visibility = 'visible';
			}
		}
		
		function provinceChange(){
			var select;
			var key;
			if($(this).attr("id")=="province") {
				key = $("#province").val();
				select = document.getElementById("city");
			} else {
				key = $("#province2").val();
				select = document.getElementById("city2");
			}
			var value = cityData[key];
			select.length=0;
			for (var prop in value) {
			    var opt = $('<option />');
		  		var city = value[prop].city;
		  		
		  		if(city==''){
			  		opt.html('ALL');
		  		}else{
			  		opt.html(city);
		  		}
		  		
		  		var province = value[prop].province;
			  	if(province ==''){
			  		opt.val('');
			  	}else{
				  	opt.val(province+'-' + city);
			  	}
		  		opt.appendTo(select);
			}
		}
		
		function init(province, group) {
			for (var prop in cityData) {
			  	if (cityData.hasOwnProperty(prop)) { 
			  		var opt = $('<option />');
			  		
			  		if(prop==''){
				  		opt.html('ALL');
			  		}else{
				  		opt.html(prop);
			  		}
			  		opt.val(prop);
			  		opt.appendTo($('#'+province));
			  }
			}
			
			for (var prop in urlData) {
			  	if (urlData.hasOwnProperty(prop)) { 
			  		var opt = $('<option />');
			  		
			  		opt.val(prop);
			  		opt.html(prop);
			  		opt.appendTo($('#'+group));
			  }
			}
		}
		
		$(document).ready(function() {
			$('#web_trend').addClass('active');
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
				step:60,
				maxDate:0
			});
			$('#endTime2').datetimepicker({
				datepicker:false,
				format:'H:i',
				step:60,
				maxDate:0
			});

			$('#startTime').val("${w:format(model.start,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(model.end,'HH:mm')}");
			
			$('#group').on('change',groupChange);
			$('#group2').on('change',groupChange);
			$('#province').on('change',provinceChange);
			$('#province2').on('change',provinceChange);

			init('province','group');
			init('province2','group2');
			
			$('#type').on('change', typeChange);
			
			var type = '${payload.type}';
			$('#type').val(type);
			$('#type').change();
			
			var channels = '${payload.channel}'.split(';');
			var channel = channels[0];
			var channel2 = channels[1];
			var urls = '${payload.url}'.split(';');
			var url = urls[0];
			var url2 = urls[1];
			var cities = '${payload.city}'.split(';');
			var city = cities[0];
			var city2 = cities[1];
			var groups = '${payload.group}'.split(';');
			var group = groups[0];
			var group2 = groups[1];
			
			$('#group').val(group);
			$('#group').change();
			$("#url").val(url);
			var array = city.split('-');

			$('#province').val(array[0]);
			$('#province').change();
			
			if(array.length==2){
				$("#city").val(city);
			}
			$('#channel').val(channel);
			
			var datePair = {};
			datePair["当前值"]="${w:format(model.start,'yyyy-MM-dd')}";
			
			if(typeof url2 != 'undefined' && typeof group2 != 'undefined' && typeof city2 != 'undefined' 
					&& typeof channel2 != 'undefined') {
				$('#history').slideDown();
				document.getElementById("checkbox").checked = true;
				
				$('#group2').val(group2);
				$('#group2').change();
				$("#url2").val(url2);
				var array2 = city2.split('-');

				$('#province2').val(array2[0]);
				$('#province2').change();
				
				if(array2.length==2){
					$("#city2").val(city2);
				}
				$('#channel2').val(channel2);
				
				if('${model.compareStart}'.length > 0 && '${model.compareEnd}'.length > 0) {
					$('#startTime2').val("${w:format(model.compareStart,'yyyy-MM-dd HH:mm')}");
					$('#endTime2').val("${w:format(model.compareEnd,'HH:mm')}");
				}
				datePair["对比值"]="${w:format(model.compareStart,'yyyy-MM-dd')}";
			}
			
			<c:choose>
			<c:when test="${payload.type eq 'info'}">
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					var data = ${item.jsonString};
					graphMetricChartForDay(document
							.getElementById('${item.id}'), data, datePair);
				</c:forEach>
				<c:forEach var="item" items="${model.pieCharts}" varStatus="status">
					var data = ${item.jsonString};
					graphPieChart(document.getElementById('${item.title}'),data);
				</c:forEach>
			</c:when>
			<c:otherwise>
				<c:forEach var="item" items="${model.lineChart.subTitles}" varStatus="status">
					datePair['${item}'] = datePair["当前值"];
				</c:forEach>
				graphMetricChartForDay(document.getElementById('lineChart'), ${model.lineChart.jsonString},datePair);
				graphPieChart(document.getElementById('pieChart'), ${model.pieChart.jsonString});
			</c:otherwise>
			</c:choose>
		});
		</script>
<%@include file="webDetail.jsp"%>
</a:body>