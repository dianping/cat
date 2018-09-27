<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.browser.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.browser.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.browser.Model" scope="request" />

<a:web_body>
	<script type="text/javascript">
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
			var command = $("#command").val().split('|')[0];
			var commandId = ${model.pattern2Items}[command].id;
			var code = $("#codeStatus").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var network = $("#network").val();
			var split = ";";
			var query1 = period + split + commandId + split + code + split
					+ city + split + operator + split + start + split + end + split + network;
			
			var field = $("#piechartSelect").val();
			var href = "?op=piechart&query1=" + query1 + "&groupByField=" + field+"&api1="+$("#command").val();
 			window.location.href = href;
 		}
		
		function refreshDisabled(){
			document.getElementById("code").disabled = false;
			document.getElementById("city").disabled = false;
			document.getElementById("operator").disabled = false;
			document.getElementById("network").disabled = false;
			document.getElementById($("#piechartSelect").val()).disabled = true;
		}

		$(document).ready(
				function() {
					$('#web_piechart').addClass('active');
					$('#Browser').addClass('active open');
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
					var command1 = $('#command');
					var words = query1.split(";");

					$("#piechartSelect").on('change', refreshDisabled);
					
					if (words[0] == null || words.length == 1) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0] + " " + words[5]);
					}
					
					if(words[6] == null || words.length == 1){
						$("#time2").val(getTime());
					}else{
						$("#time2").val(words[6]);
					}
					
					if(typeof(words[1]) != 'undefined' && words[1].length > 0){
						$("#command").val('${payload.api1}');
					}else{
						$("#command").val('${model.defaultApi}');
					}
					$("#codeStatus").val(words[2]);
					$("#city").val(words[3]);
					$("#operator").val(words[4]);
					$("#network").val(words[7]);
					$("#piechartSelect").val('${payload.groupByField.name}');
					refreshDisabled();
					
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
					<c:forEach var="command" items="${model.pattermItems}">
						var item = {};
						
						item['label'] = '${command.value.name}|${command.value.pattern}';
						if('${command.value.domain}'.length >0 ){
							item['category'] ='${command.value.group}';
						}else{
							item['category'] ='未知项目';
						}
						
						data.push(item);
					</c:forEach>
							
					$( "#command" ).catcomplete({
						delay: 0,
						source: data
					});
					$('#wrap_search').submit(
							function(){
								return false;
							}		
						);
					graphPieChartWithName(document.getElementById('piechart'), ${model.ajaxDataDisplayInfo.pieChart.jsonString},  '${model.ajaxDataDisplayInfo.pieChart.title}');
					graphColumnChart('#barchart', '${model.ajaxDataDisplayInfo.barChart.title}', '',
							${model.ajaxDataDisplayInfo.barChart.xAxisJson}, '${model.ajaxDataDisplayInfo.barChart.yAxis}',
							${model.ajaxDataDisplayInfo.barChart.valuesJson}, '${model.ajaxDataDisplayInfo.barChart.serieName}');
				});
	</script>
	
		<%@include file="ajaxPieChartDetail.jsp"%>
</a:web_body>

<style type="text/css">
	.row-fluid .span2{
		width:10%;
	}
	.row-fluid .span10{
		width:87%;
	}
</style>