<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
			<h3 class="text-center text-success">编辑APP监控规则</h3>
			<form name="appRuleUpdate" id="form" method="post">
				<table style='width:100%' class='table table-striped table-condensed '>
			<tr>
			<c:set var="strs" value="${fn:split(payload.ruleId, ':')}" />
			<c:set var="name" value="${strs[2]}" />
				<th align=left>告警名<input id="name" value="${name}"/> 命令字 <select id="command" style="width: 350px;">
						<c:forEach var="item" items="${model.commands}" varStatus="status">
							<c:choose>
								<c:when test="${empty item.title}">
									<option value='${item.id}'>${item.name}</option>
								</c:when>
								<c:otherwise>
									<option value='${item.id}'>${item.title}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
				</select> 返回码 <select id="code" style="width: 120px;">
				</select> 网络类型 <select id="network" style="width: 80px;">
						<option value='-1'>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version" style="width: 100px;">
						<option value='-1'>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 连接类型 <select id="connectionType" style="width: 100px;">
						<option value='-1'>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform" style="width: 100px;">
						<option value='-1'>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 地区 <select id="city" style="width: 100px;">
						<option value='-1'>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator" style="width: 100px;">
						<option value='-1'>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 告警指标 <select id="metric" style="width: 100px;">
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
</a:config>

<script type="text/javascript">
var commandInfo = ${model.commandJson};
var commandChange = function commandChange() {
	var key = $("#command").val();
	var value = commandInfo[key];
	var code = document.getElementById("code");
	code.length = 0;
	var opt = $('<option />');
	opt.html("All");
	opt.val("-1");
	opt.appendTo(code);
	for ( var prop in value) {
		var opt = $('<option />');

		opt.html(value[prop].name);
		opt.val(value[prop].id);
		opt.appendTo(code);
	}
}

function update() {
    var configStr = generateConfigsJsonString();
    var name = $("#name").val();
    var command = $("#command").val();
    var code = $("#code").val();
    var network = $("#network").val();
    var version = $("#version").val();
    var connectionType = $("#connectionType").val();
    var platform = $("#platform").val();
    var city = $("#city").val();
    var operator = $("#operator").val();
    var metric = $("#metric").val();
    var split = ";";
    var id = command + split + code + split + network + split + version + split + connectionType + split + platform + split + city + split + operator + ":" + metric + ":" + name;
    window.location.href = "?op=appRuleSubmit&configs=" + configStr + "&ruleId=" + id;
}

	$(document).ready(function() {
		var commandSelector = $('#command');
		commandSelector.on('change', commandChange);
		var ruleId = "${payload.ruleId}";
		if(ruleId.length > 0){
			document.getElementById("name").disabled = true;
			document.getElementById("command").disabled = true;
			document.getElementById("code").disabled = true;
			document.getElementById("network").disabled = true;
			document.getElementById("version").disabled = true;
			document.getElementById("connectionType").disabled = true;
			document.getElementById("platform").disabled = true;
			document.getElementById("city").disabled = true;
			document.getElementById("operator").disabled = true;
			document.getElementById("metric").disabled = true;
		}
		var words = ruleId.split(":")[0].split(";");
		if(typeof words != "undefined" && words.length == 8){
			var metric = ruleId.split(":")[1];
			var command = words[0];
			var code = words[1];
			var network = words[2];
			var version = words[3];
			var connectionType = words[4];
			var platform = words[5];
			var city = words[6];
			var operator = words[7];
			$("#command").val(command);
			commandChange();
			$("#code").val(code);
			$("#network").val(network);
			$("#version").val(version);
			$("#connectionType").val(connectionType);
			$("#platform").val(platform);
			$("#city").val(city);
			$("#operator").val(operator);
			$("#metric").val(metric);
		}
		commandChange();
		$('#userMonitor_config').addClass('active open');
		$('#appRule').addClass('active');
		initRuleConfigs(["DescVal","DescPer","AscVal","AscPer"]);
		$(document).delegate("#ruleSubmitButton","click",function(){
			update();
		})
	});
</script>