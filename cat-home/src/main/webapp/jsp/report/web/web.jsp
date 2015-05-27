<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.web.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.web.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.web.Model" scope="request" />
<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
 	<script type="text/javascript">
		function check() {
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				$('#history').slideDown();
				$("#command2").val($("#command").val());
				$("#code2").val($("#code").val());
				$("#city2").val($("#city").val());
				$("#operator2").val($("#operator").val());
				$("#time2").val($("#time").val());
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

			return myDate.getFullYear() + "-" + month + "-" + day;
		}
		
		function query() {
			var time = $("#time").val();
			var command = $("#command").val().split('|')[0];
			var code = $("#code").val();
			var city = "";
			var operator = "";
			if(typeof(cityCode) == "undefined"){
				city = $("#city").val();
			}else{
				city = cityCode;
			}
			if(typeof(operatorCode) == "undefined"){
				operator = $("#operator").val();
			}else{
				operator = operatorCode;
			}
			var split = ";";
			var commandId = ${model.pattern2Items}[command].id;
			var query1 = time + split + commandId + split + code + split
					    + city + split + operator + split + split;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value) {
				var time2 = $("#time2").val();
				var command2 = $("#command2").val().split('|')[0];
				var commandId2 = ${model.pattern2Items}[command2].id;
				var code2 = $("#code2").val();
				var city2 = $("#city2").val();
				var operator2 = $("#operator2").val();
				query2 = time2 + split + commandId2 + split + code2 + split
						+ split + city2 + split + operator2 + split + split;
			}

			var checkboxs = document.getElementsByName("typeCheckbox");
			var type = "";

			for (var i = 0; i < checkboxs.length; i++) {
				if (checkboxs[i].checked) {
					type = checkboxs[i].value;
					break;
				}
			}
			var commandId = $('#command').val();
			var commandId2 = $('#command2').val();
			var href = "?query1=" + query1 + "&query2=" + query2 + "&type="
					+ type +"&api1="+commandId+"&api2="+commandId2;
			window.location.href = href;
		}
		
		
		$(document).ready(
				function() {
					$('#web_trend').addClass('active');
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
					var command1 = $('#command');
					var command2 = $('#command2');
					var words = query1.split(";");

					if(typeof(words[1]) != 'undefined' && words[1].length > 0){
						$("#command").val('${payload.api1}');
					}else{
						$("#command").val('${model.defaultApi}');
					}
					
					if (typeof(words[0]) != 'undefined' && words[0].length == 0) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0]);
					}

					$("#code").val(words[2]);
					$("#city").val(words[3]);
					$("#operator").val(words[4]);
					
					var datePair = {};
					datePair["当前值"]=$("#time").val();

					if (query2 != null && query2 != '') {
						$('#history').slideDown();
						document.getElementById("checkbox").checked = true;
						var words = query2.split(";");

						if (words[0] == null || words[0].length == 0) {
							$("#time2").val(getDate());
						} else {
							$("#time2").val(words[0]);
						}
						
						datePair["对比值"]=$("#time2").val();

						if(typeof(words[1]) != 'undefined' && words[1].length > 0){
							$("#command2").val('${payload.api2}');
						}else{
							$("#command2").val('${model.defaultApi}');
						}
						$("#code2").val(words[2]);
						$("#city2").val(words[3]);
						$("#operator2").val(words[4]);
					} else {
						$("#time2").val(getDate());
					}

					var checkboxs = document.getElementsByName("typeCheckbox");

					for (var i = 0; i < checkboxs.length; i++) {
						if (checkboxs[i].value == "${payload.type}") {
							checkboxs[i].checked = true;
							break;
						}
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
					$( "#command2" ).catcomplete({
						delay: 0,
						source: data
					});
					$('#wrap_search').submit(
							function(){
								return false;
							}		
						);
					$('#wrap_search2').submit(
							function(){
								return false;
							}		
						);
					var data = ${model.lineChart.jsonString};
					graphMetricChartForDay(document
							.getElementById('${model.lineChart.id}'), data, datePair);
				});
	</script>
	
		<%@include file="webDetail.jsp"%>
</a:body>