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
				result[value[prop].id] =value[prop].name;
			}
			
			return result;
		}
		function check() {
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				$('#history').slideDown();
				console.log($("#app").val());
				$("#appId2").val($("#appId").val());
				$("#command2").val($("#command").val());
				$("#network2").val($("#network").val());
				$("#version2").val($("#version").val());
				$("#connectionType2").val($("#connectionType").val());
				$("#platform2").val($("#platform").val());
				$("#city2").val($("#city").val());
				$("#operator2").val($("#operator").val());
				$("#source2").val($("#source").val());
				$("#time2").val($("#time").val());
				$("#endTime2").val($("#endTime").val());
				commandChange("command2","code2");
				$("#code2").val($("#code").val());
				
				 var data2 = [];
				 var app2 = ${model.sourceJson}[$("#appId").val()].value;
				
				<c:forEach var="command" items="${model.commands}">
					var item = {};
					item['label'] = '${command.value.name}|${command.value.title}';
					
					if('${command.value.namespace}' == app2){
						if('${command.value.domain}'.length >0 ){
							item['category'] ='${command.value.domain}';
						}else{
							item['category'] ='未知项目';
						}
						
						data2.push(item);
					}
				</c:forEach>
				$( "#command2" ).catcomplete({
					delay: 0,
					source: data2
				});
			} else {
				$('#history').slideUp();
			}
		}
 		var commandChange = function commandChange(commandDom, codeDom) {
 			var command = $("#"+commandDom).val().split('|')[0];
 			var cmd = ${model.command2IdJson}[command];
 			
 			if(typeof(cmd)!="undefined"){
				var commandId = cmd.id;
				var value = queryCodeByCommand(commandId);
				
				$("#"+codeDom).empty();
				
				var opt = $('<option />');
				opt.html("All");
				opt.val("");
				opt.appendTo($("#"+codeDom));
				
				for ( var prop in value) {
					var opt = $('<option />');
					opt.html(value[prop]);
					opt.val(prop);
					opt.appendTo($("#"+codeDom));
				}
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
		
		function queryGroupBy(sort){
			var str = document.URL;
			var result = str.split("&groupByField=");
			var field = result[1].split("&")[0];
			query(field,undefined,undefined,undefined,undefined,undefined,undefined,undefined,sort);
		}

		function query(field,networkCode,appVersionCode,channelCode,platformCode,cityCode,operatorCode,sourceCode,sort) {
			var times = $("#time").val().split(" ");
			var period = times[0];
			var start = times[1];
			var end = $("#endTime").val();
			var command = $("#command").val().split('|')[0];
			var code = $("#code").val();
			var network = "";
			var version = "";
			var connectionType = "";
			var platform = "";
			var city = "";
			var operator = "";
			var source = "";
			if(typeof(networkCode) == "undefined"){
				network = $("#network").val();
			}else{
				network = networkCode;
			}
			if(typeof(appVersionCode) == "undefined"){
				version = $("#version").val();
			}else{
				version = appVersionCode;
			}
			if(typeof(channelCode) == "undefined"){
				connectionType = $("#connectionType").val();
			}else{
				connectionType = channelCode;
			}
			if(typeof(platformCode) == "undefined"){
				platform = $("#platform").val();
			}else{
				platform = platformCode;
			}
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
			if(typeof(sourceCode) == "undefined"){
				source = $("#source").val();
			}else{
				source = sourceCode;
			}
			var split = ";";
			var commandId = ${model.command2IdJson}[command].id;
			var query1 = period + split + commandId + split + code + split
					+ network + split + version + split + connectionType
					+ split + platform + split + city + split + operator + split + source + split + start + split + end;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value) {
				var times2 = $("#time2").val().split(" ");
				var period2 = times2[0];
				var start2 = times2[1];
				var end2 = $("#endTime2").val();
				var command2 = $("#command2").val().split('|')[0];
				var commandId2 = ${model.command2IdJson}[command2].id;
				var code2 = $("#code2").val();
				var network2 = $("#network2").val();
				var version2 = $("#version2").val();
				var connectionType2 = $("#connectionType2").val();
				var platform2 = $("#platform2").val();
				var city2 = $("#city2").val();
				var operator2 = $("#operator2").val();
				var source2 = $("#source2").val();
				query2 = period2 + split + commandId2 + split + code2 + split
						+ network2 + split + version2 + split + connectionType2
						+ split + platform2 + split + city2 + split
						+ operator2 + split + source2 + split + start2 + split + end2;
			}

			var checkboxs = document.getElementsByName("typeCheckbox");
			var type = "";

			for (var i = 0; i < checkboxs.length; i++) {
				if (checkboxs[i].checked) {
					type = checkboxs[i].value;
					break;
				}
			}
			
			if(typeof(field) == "undefined"){
				field = "";
			}
			if(typeof(sort) == "undefined"){
				sort = "";
			}
			var commandId = $('#command').val();
			var commandId2 = $('#command2').val();
			var href = "?query1=" + query1 + "&query2=" + query2 + "&type="
					+ type + "&groupByField=" + field + "&sort=" + sort
					+"&commandId="+commandId+"&commandId2="+commandId2+"&appId="+$("#appId").val()+"&appId2="+$("#appId2").val();
			window.location.href = href;
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
		
		$(document).delegate('#appId2', 'change', function(e){
			var appId = $("#appId2").val();
			
			$.ajax({
				async: false,
				type: "get",
				dataType: "json",
				url: "/cat/r/app?op=appCommands&appId="+appId,
				success : function(response, textStatus) {
					var data = [];
					var commands = response;
					$("#command2").val("");
					
					for ( var prop in commands) {
						var command = commands[prop];
						var item = {};
						item['label'] = '${command.value.name}|${command.value.title}';
						item['label'] = command['name'] + "|" + command['title'];
						if(command['domain'].length >0 ){
							item['category'] = command['domain'];
						}else{
							item['category'] = '未知项目';
						}
						
						var commandStr = $("#command2").val();
						
						if(commandStr == "" && item['label'].indexOf("all") == -1){
							$("#command2").val(item['label']);
							commandChange("command2","code2");
						}
						
						data.push(item);
					}
					$( "#command2" ).catcomplete({
						delay: 0,
						source: data
					});
				}
			});
		});

		$(document).ready(
				function() {
					$('#trend').addClass('active');
					$('#time').datetimepicker({
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
					$('#time2').datetimepicker({
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
					var command1 = $('#command');
					var command2 = $('#command2');
					var words = query1.split(";");
					
					command1.on('change', commandChange("command","code"));
					
					if(typeof(words[1]) != 'undefined' && words[1].length > 0){
						$("#command").val('${payload.commandId}');
					}else{
						$("#command").val('${model.defaultCommand}');
					}
					commandChange("command","code");
					
					if (typeof(words[0]) != 'undefined' && words[0].length == 0) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0] + " " + words[10]);
					}
					
					if(words[10] == null || words.length == 1){
						$("#endTime").val(getTime());
					}else{
						$("#endTime").val(words[11]);
					}
					
					$("#code").val(words[2]);
					$("#network").val(words[3]);
					$("#version").val(words[4]);
					$("#connectionType").val(words[5]);
					$("#platform").val(words[6]);
					$("#city").val(words[7]);
					$("#operator").val(words[8]);
					$("#source").val(words[9]);
					$("#appId").val("${payload.appId}");
					$("#appId2").val("${payload.appId2}");
					
					var datePair = {};
					datePair["当前值"]=$("#time").val().split(" ")[0];

					if (query2 != null && query2 != '') {
						$('#history').slideDown();
						document.getElementById("checkbox").checked = true;
						var words = query2.split(";");

						if (words[0] == null || words[0].length == 0) {
							$("#time2").val(getDate());
						} else {
							$("#time2").val(words[0] + " " + words[10]);
						}
						
						if(words[10] == null || words.length == 1){
							$("#endTime2").val(getTime());
						}else{
							$("#endTime2").val(words[11]);
						}
						
						datePair["对比值"]=$("#time2").val().split(" ")[0];
						
						command2.on('change', commandChange("command2","code2"));

						if(typeof(words[1]) != 'undefined' && words[1].length > 0){
							$("#command2").val('${payload.commandId2}');
						}else{
							$("#command2").val('${model.defaultCommand}');
						}
						commandChange("command2","code2");
						
						$("#code2").val(words[2]);
						$("#network2").val(words[3]);
						$("#version2").val(words[4]);
						$("#connectionType2").val(words[5]);
						$("#platform2").val(words[6]);
						$("#city2").val(words[7]);
						$("#operator2").val(words[8]);
						$("#source2").val(words[9]);
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
				 var data1 = [];
				 var data2 = [];
				 var app1 = ${model.sourceJson}[${payload.appId}].value;
				 var app2 = ${model.sourceJson}[${payload.appId2}].value;
				 
					<c:forEach var="command" items="${model.commands}">
							var item = {};
							item['label'] = '${command.value.name}|${command.value.title}';
							
							if('${command.value.namespace}' == app1){
								if('${command.value.domain}'.length >0 ){
									item['category'] ='${command.value.domain}';
								}else{
									item['category'] ='未知项目';
								}
								
								data1.push(item);
							}
					</c:forEach>
					
					<c:forEach var="command" items="${model.commands}">
						var item = {};
						item['label'] = '${command.value.name}|${command.value.title}';
						
						if('${command.value.namespace}' == app2){
							if('${command.value.domain}'.length >0 ){
								item['category'] ='${command.value.domain}';
							}else{
								item['category'] ='未知项目';
							}
							
							data2.push(item);
						}
					</c:forEach>
							
					$( "#command" ).catcomplete({
						delay: 0,
						source: data1
					});
					$('#command').blur(function(){
						commandChange("command","code");
					})
					$('#command2').blur(function(){
						commandChange("command2","code2");
					})
					$( "#command2" ).catcomplete({
						delay: 0,
						source: data2
					});
					$('#wrap_search').submit(
							function(){
								commandChange("command","code");
								return false;
							}		
						);
					$('#wrap_search2').submit(
							function(){
								commandChange("command2","code2");
								return false;
							}		
						);
					var data = ${model.lineChart.jsonString};
					graphMetricChartForDay(document
							.getElementById('${model.lineChart.id}'), data, datePair);
				});
	</script>
	
		<%@include file="linechartDetail.jsp"%>
</a:mobile>