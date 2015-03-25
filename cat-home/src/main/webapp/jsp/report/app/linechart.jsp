<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
 	<script type="text/javascript">
		var commandInfo = ${model.command2CodesJson};
		function check() {
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				$('#history').slideDown();
				$("#domains2").val($("#domains").val());
				$("#domains2").change();
				$("#command2").val($("#command").val());
				command2Change();
				$("#code2").val($("#code").val());
				$("#network2").val($("#network").val());
				$("#version2").val($("#version").val());
				$("#connectionType2").val($("#connectionType").val());
				$("#platform2").val($("#platform").val());
				$("#city2").val($("#city").val());
				$("#operator2").val($("#operator").val());
				$("#time2").val($("#time").val());
			} else {
				$('#history').slideUp();
			}
		}
 		var command1Change = function command1Change() {
			var key = $("#command").val();
			var value = commandInfo[key];
			var code = document.getElementById("code");
			$("#code").empty();
			
			var opt = $('<option />');
			opt.html("All");
			opt.val("");
			opt.appendTo(code);
			
			for ( var prop in value) {
				var opt = $('<option />');
				opt.html(value[prop].name);
				opt.val(value[prop].id);
				opt.appendTo(code);
			}
		}
		var command2Change = function command2Change() {
			var key = $("#command2").val();
			var value = commandInfo[key];
			var code = document.getElementById("code2");
			$("#code2").empty();
			var opt = $('<option />');
			opt.html("All");
			opt.val("");
			opt.appendTo(code);
			
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

			return myDate.getFullYear() + "-" + month + "-" + day;
		}
		
		function queryGroupBy(sort){
			var str = document.URL;
			var result = str.split("&groupByField=");
			var field = result[1].split("&")[0];
			query(field,undefined,undefined,undefined,undefined,undefined,undefined,sort);
		}

		function query(field,networkCode,appVersionCode,channelCode,platformCode,cityCode,operatorCode,sort) {
			var time = $("#time").val();
			var command = $("#command").val();
			var code = $("#code").val();
			var network = "";
			var version = "";
			var connectionType = "";
			var platform = "";
			var city = "";
			var operator = "";
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
			var split = ";";
			var query1 = time + split + command + split + code + split
					+ network + split + version + split + connectionType
					+ split + platform + split + city + split + operator + split + split;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value) {
				var time2 = $("#time2").val();
				var command2 = $("#command2").val();
				var code2 = $("#code2").val();
				var network2 = $("#network2").val();
				var version2 = $("#version2").val();
				var connectionType2 = $("#connectionType2").val();
				var platform2 = $("#platform2").val();
				var city2 = $("#city2").val();
				var operator2 = $("#operator2").val();
				query2 = time2 + split + command2 + split + code2 + split
						+ network2 + split + version2 + split + connectionType2
						+ split + platform2 + split + city2 + split
						+ operator2 + split + split;
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
			var domains = $('#domains').val();
			var commandId = $('#command').val();
			var domains2 = $('#domains2').val();
			var commandId2 = $('#command2').val();
			var href = "?query1=" + query1 + "&query2=" + query2 + "&type="
					+ type + "&groupByField=" + field + "&sort=" + sort+"&domains="+domains
					+"&commandId="+commandId+"&domains2="+domains2+"&commandId2="+commandId2
					+"&showActivity=${payload.showActivity}";
			window.location.href = href;
		}
		
		var domain2CommandsJson = ${model.domain2CommandsJson};

		function changeDomain(domainId, commandId, domainInitVal, commandInitVal){
			if(domainInitVal == ""){
				domainInitVal = $("#"+domainId).val()
			}
			var commandSelect = $("#"+commandId);
			var commands = domain2CommandsJson[domainInitVal];
			
			$("#"+domainId).val(domainInitVal);
			commandSelect.empty();
			for(var cou in commands){
				var command = commands[cou];
				if(command['title'] != undefined && command['title'].length > 0){
					commandSelect.append($("<option value='"+command['id']+"'>"+command['title']+"</option>"));
				}else{
					commandSelect.append($("<option value='"+command['id']+"'>"+command['name']+"</option>"));
				}
			}
			if(commandInitVal != ''){
				commandSelect.val(commandInitVal);
			}
		}
		
		function changeCommandByDomain(){
			if($(this).attr("id")=="domains"){
				var domain = $("#domains").val();
				var commandSelect = $("#command");
			}else{
				var domain = $("#domains2").val();
				var commandSelect = $("#command2");
			}
			var commands = domain2CommandsJson[domain];
			commandSelect.empty();
			
			for(var cou in commands){
				var command = commands[cou];
				if(command['title'] != undefined && command['title'].length > 0){
					commandSelect.append($("<option value='"+command['id']+"'>"+command['title']+"</option>"));
				}else{
					commandSelect.append($("<option value='"+command['id']+"'>"+command['name']+"</option>"));
				}
			}
		}
		
		function initDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal){
			var domainsSelect = $("#"+domainSelectId);
			for(var domain in domain2CommandsJson){
				domainsSelect.append($("<option value='"+domain+"'>"+domain+"</option>"))
			}
			changeDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal);
			domainsSelect.on('change', changeCommandByDomain);
			domainsSelect.change();
		}

		$(document).ready(
				function() {
					initDomain('domains', 'command', '${payload.domains}', '${payload.commandId}');
					initDomain('domains2', 'command2', '${payload.domains2}', '${payload.commandId2}');
					command1Change();
					command2Change();
					
					if(${payload.showActivity}){
						$('#activity_trend').addClass('active');
					} else {
						$('#trend').addClass('active');
					}
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

					command1.on('change', command1Change);
					command2.on('change', command2Change);
					if(typeof(words[1]) != 'undefined' && words[1].length > 0){
						$("#command").val(words[1]);
					}else{
						if('${payload.showActivity}' == 'true') {
							$("#command").val('${model.defaultActivity}');
						}else{
							$("#command").val('${model.defaultCommand}');
						}
					}
					
					if (typeof(words[0]) != 'undefined' && words[0].length == 0) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0]);
					}

					command1Change();
					$("#code").val(words[2]);
					$("#network").val(words[3]);
					$("#version").val(words[4]);
					$("#connectionType").val(words[5]);
					$("#platform").val(words[6]);
					$("#city").val(words[7]);
					$("#operator").val(words[8]);
					
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

						if(typeof(words[1]) != 'undefined' && words[0].length > 0 ){
							$("#command2").val(words[1]);
						}
						command2Change();
						$("#code2").val(words[2]);
						$("#network2").val(words[3]);
						$("#version2").val(words[4]);
						$("#connectionType2").val(words[5]);
						$("#platform2").val(words[6]);
						$("#city2").val(words[7]);
						$("#operator2").val(words[8]);
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

					var data = ${model.lineChart.jsonString};
					graphMetricChartForDay(document
							.getElementById('${model.lineChart.id}'), data, datePair);
				});
	</script>
	
		<%@include file="linechartDetail.jsp"%>
</a:body>