<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>
<a:mobile>
			<h3 class="text-center text-success">编辑APP监控规则</h3>
			<form name="appRuleUpdate" id="form" method="post">
			<table>
			<tr>
			<c:set var="name" value="${model.ruleInfo['rule']['id']}" />
			
			<th>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">告警名</span>
	              <input type="text" id="name" value="${name}" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:350px">
					<span class="input-group-addon">命令字</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code" style="width:120px">
					<option value='-1'>All</option>
					</select>
	            </div>
			 	<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network">
						<option value='-1'>All</option>
						<option value='*'>*</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            </th>
				</tr>
			<tr>
				<th align=left>
				<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">版本</span>
					<select id="version" style="width: 100px;">
						<option value='-1'>All</option>
						<option value='*'>*</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">连接类型</span>
					<select id="connectionType" style="width: 100px;">
						<option value='-1'>All</option>
						<option value='*'>*</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">平台</span>
					<select id="platform" style="width: 100px;">
						<option value='-1'>All</option>
						<option value='*'>*</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city" style="width: 100px;">
						<option value='-1'>All</option>
						<option value='*'>*</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator" style="width: 100px;">
						<option value='-1'>All</option>
						<option value='*'>*</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
				告警指标 <select id="metric" style="width: 100px;">
						<option value='request'>请求数</option>
						<option value='success'>成功率</option>
						<option value='delay'>响应时间</option>
				</select></th></tr>
				<tr><th>${model.content}</th></tr>
					<tr>
						<td style='text-align:center' colspan='2'><input class="btn btn-primary btn-mini" id="ruleSubmitButton" type="text" name="submit" value="提交"></button></td>
					</tr>
				</table>
			</form>
</a:mobile>

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

	var commandChange = function commandChange(commandDom, codeDom) {
			var command = $("#"+commandDom).val().split('|')[0];
			var cmd = ${model.command2IdJson}[command];
			
			if(typeof(cmd)!="undefined"){
			var commandId = cmd.id;
			var value = queryCodeByCommand(commandId);
			
			$("#"+codeDom).empty();
			
			var opt = $('<option />');
			opt.html("All");
			opt.val("-1");
			opt.appendTo($("#"+codeDom));
			
			for ( var prop in value) {
				var opt = $('<option />');
				opt.html(value[prop]);
				opt.val(prop);
				opt.appendTo($("#"+codeDom));
			}
		}
	}

function update() {
    var configStr = generateConfigsJsonString();
    var name = $("#name").val();
	var command = $("#command").val().split('|')[0];
	var commandId = ${model.command2IdJson}[command].id;
    var code = $("#code").val();
    var network = $("#network").val();
    var version = $("#version").val();
    var connectionType = $("#connectionType").val();
    var platform = $("#platform").val();
    var city = $("#city").val();
    var operator = $("#operator").val();
    var metric = $("#metric").val();
    var split = ";";
    var id = commandId + split + code + split + network + split + version + split + connectionType + split + platform + split + city + split + operator + split + metric + split + name;
    window.location.href = "?op=appRuleSubmit&configs=" + encodeURIComponent(configStr) + "&ruleId=" + encodeURIComponent(id) + "&id=" + encodeURIComponent(${model.ruleInfo.jsonString}['entity']['id']);
}

	$(document).ready(function() {
		var commandSelector = $('#command');
		commandSelector.on('change', commandChange("command","code"));
		var attributes = ${model.ruleInfo.jsonString}['rule']['dynamicAttributes'];
		if(typeof attributes != "undefined"){
			var metric = attributes['metric'];
			var command = attributes['command'];
			var commandName = attributes['commandName'];
			var code = attributes['code'];
			var network = attributes['网络类型'];
			var version = attributes['版本'];
			var connectionType = attributes['连接类型'];
			var platform = attributes['平台'];
			var city = attributes['城市'];
			var operator = attributes['运营商'];
			$("#command").val(commandName);
			$("#code").val(code);
			$("#network").val(network);
			$("#version").val(version);
			$("#connectionType").val(connectionType);
			$("#platform").val(platform);
			$("#city").val(city);
			$("#operator").val(operator);
			$("#metric").val(metric);
		}
		commandChange("command","code");
		$("#code").val(code);
		$('#userMonitor_config').addClass('active open');
		$('#appRule').addClass('active');
		
		var data = [];
		<c:forEach var="command" items="${model.commands}">
					var item = {};
					item['label'] = '${command.value.name}|${command.value.title}';
					if('${command.value.domain}'.length >0 ){
						item['category'] ='${command.value.domain}';
					}else{
						item['category'] ='未知项目';
					}
					
					data.push(item);
		</c:forEach>
				
		$( "#command" ).catcomplete({
			delay: 0,
			source: data
		});
		$('#command').blur(function(){
			commandChange("command","code");
		});
		
		$('#wrap_search').submit(
							function(){
								commandChange("command","code");
								return false;
							}		
						);
		
		initRuleConfigs(["DescVal","DescPer","AscVal","AscPer"]);
		$(document).delegate("#ruleSubmitButton","click",function(){
			update();
		})
	});
</script>