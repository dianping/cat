<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

<a:mobile>
	<script type="text/javascript">
		var commandsMap = ${model.commandsJson};
		var commandInfo = ${model.command2CodesJson};
		var globalInfo = ${model.globalCodesJson};
		
		var queryCodeByCommand = function queryCode(commandId){
			var value = commandInfo[commandId];
			var command = commandsMap[commandId];
			var globalValue = globalInfo[command.namespace];
			
			if(typeof globalValue == "undefined") {
				globalValue = globalInfo['点评主APP'];
			}
			
			var globalcodes = globalValue.codes;
			var result = {};
			
			for(var tmp in globalcodes){
				result[globalcodes[tmp].id] =globalcodes[tmp].name;
			}
			
			for (var prop in value) {
				result[value[prop].id] =value[prop].value;
			}
			
			return result;
		}
		
		var command1Change = function command1Change() {
			var command = $("#command").val().split('|')[0];
			var commandId = ${model.command2IdJson}[command].id;
			var value = queryCodeByCommand(commandId);
			var code = document.getElementById("code");
			$(code).empty();
			
			var opt = $('<option />');
			opt.html("All");
			opt.val("");
			opt.appendTo(code);
			
			for ( var prop in value) {
				var opt = $('<option />');

				opt.html(value[prop]);
				opt.val(prop);
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
		
		function query() {
			queryWithSort("request");
		}
		
		function queryWithSort(sort) {
			var time = $("#time").val();
			var times = time.split(" ");
			var period = times[0];
			var command = $("#command").val().split('|')[0];
			var commandId = ${model.command2IdJson}[command].id;
			var code = $("#code").val();
			var network = $("#network").val();
			var version = $("#app-version").val();
			var connectionType = $("#connect-type").val();
			var platform = $("#platform").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var source = $("#source").val();
			var group = $("#group").val();
			var split = ";";
			var query1 = period + split + commandId + split + code + split
					+ network + split + version + split + connectionType
					+ split + platform + split + city + split + operator + split + source + split + times[1] + split + $("#time2").val();
			
			var field = $("#piechartSelect").val();
			var href = "?op=piechart&query1=" + query1 + "&groupByField=" + field+"&commandId="+$("#command").val() + "&sort=" + sort+"&appId="+$("#appId").val();
 			window.location.href = href;
 		}
		
		function refreshDisabled(){
			document.getElementById("code").disabled = false;
			document.getElementById("network").disabled = false;
			document.getElementById("app-version").disabled = false;
			document.getElementById("connect-type").disabled = false;
			document.getElementById("platform").disabled = false;
			document.getElementById("city").disabled = false;
			document.getElementById("operator").disabled = false;
			document.getElementById("source").disabled = false;
			document.getElementById($("#piechartSelect").val()).disabled = true;
		}
		
		$(document).delegate('#appId', 'change', function(e){

			var appId = $("#appId").val();
			
			$.ajax({
				async: false,
				type: "get",
				dataType: "json",
				url: "/cat/r/app?op=appCommands&appId="+appId,
				success : function(response, textStatus) {
					var data = [];
					var commands = response;
					$("#command").val("");
					
					for ( var prop in commands) {
						var command = commands[prop];
						var item = {};
						item['label'] = command['name'] + "|" + command['title'];
						if(command['domain'].length >0 ){
							item['category'] = command['domain'];
						}else{
							item['category'] = '未知项目';
						}
						var commandStr = $("#command").val();
						
						if(commandStr == "" && item['label'].indexOf("all|all") == -1){
							$("#command").val(item['label']);
							commandChange("command","code");
						}
						
						data.push(item);
					}
					$( "#command" ).catcomplete({
						delay: 0,
						source: data
					});
				}
			});
		});
		

		$(document).ready(
				function() {
					$('#accessPiechart').addClass('active');
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

					command1.on('change', command1Change);
					$("#piechartSelect").on('change', refreshDisabled);
					
					if (words[0] == null || words.length == 1) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0] + " " + words[10]);
					}
					
					if(words[10] == null || words.length == 1){
						$("#time2").val(getTime());
					}else{
						$("#time2").val(words[11]);
					}
					
					if(typeof(words[1]) != 'undefined' && words[1].length > 0){
						$("#command").val('${payload.commandId}');
					}else{
						$("#command").val('${model.defaultCommand}');
					}
					command1Change();

					$("#appId").val("${payload.appId}");
					$("#code").val(words[2]);
					$("#network").val(words[3]);
					$("#app-version").val(words[4]);
					$("#connect-type").val(words[5]);
					$("#platform").val(words[6]);
					$("#city").val(words[7]);
					$("#operator").val(words[8]);
					$("#source").val(words[9]);
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
					 var app = ${model.sourceJson}[${payload.appId}].value;
					 
					<c:forEach var="command" items="${model.commands}">
							var item = {};
							item['label'] = '${command.value.name}|${command.value.title}';
							
							if('${command.value.namespace}' == app){
								if('${command.value.domain}'.length >0 ){
									item['category'] ='${command.value.domain}';
								}else{
									item['category'] ='未知项目';
								}
								
								data.push(item);
							}
					</c:forEach>
							
					$( "#command" ).catcomplete({
						delay: 0,
						source: data
					});
					$('#command').blur(function(){
						command1Change();
					})
					$('#wrap_search').submit(
							function(){
								command1Change();
								return false;
							}		
						);
					
					graphPieChartWithName(document.getElementById('piechart'), ${model.commandDisplayInfo.pieChart.jsonString},  '${model.commandDisplayInfo.pieChart.title}');
					graphColumnChart('#barchart', '${model.commandDisplayInfo.barChart.title}', '',
							${model.commandDisplayInfo.barChart.xAxisJson}, '${model.commandDisplayInfo.barChart.yAxis}',
							${model.commandDisplayInfo.barChart.valuesJson}, '${model.commandDisplayInfo.barChart.serieName}');

				});
	</script>
	
		<%@include file="piechartDetail.jsp"%>
</a:mobile>

<style type="text/css">
	.row-fluid .span2{
		width:10%;
	}
	.row-fluid .span10{
		width:87%;
	}
</style>