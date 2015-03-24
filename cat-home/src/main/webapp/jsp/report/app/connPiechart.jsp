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
		var command1Change = function command1Change() {
			var key = $("#command").val();
			var value = commandInfo[key];
			var code = document.getElementById("code");
			$(code).empty();
			
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
			var group = $("#group").val();
			var split = ";";
			var query1 = period + split + command + split + code + split
					+ network + split + version + split + connectionType
					+ split + platform + split + city + split + operator + split + start + split + end;
			
			var field = $("#piechartSelect").val();
			var href = "?op=connPiechart&query1=" + query1 + "&groupByField=" + field + "&domains="+group;
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
			if(typeof commandInitVal != 'undefined' && commandInitVal.length > 0){
				commandSelect.val(commandInitVal);
			}
		}
		
		function changeCommandByDomain(){
			var domain = $("#group").val();
			var commandSelect = $("#command");
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
			command1Change();
			domainsSelect.on('change', changeCommandByDomain);
		}

		$(document).ready(
				function() {
					$('#connPiechart').addClass('active');
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
					initDomain('group', 'command', '${payload.domains}', words[1]);
					
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
					
					if(words[1] != undefined || words.length > 1){
						$("#command").val(words[1]);
					}else{
						$("#command").val('${model.defaultCommand}');
					}

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
	
		<%@include file="piechartDetail.jsp"%>
</a:body>

<style type="text/css">
	.row-fluid .span2{
		width:10%;
	}
	.row-fluid .span10{
		width:87%;
	}
</style>