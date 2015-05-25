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
			<h3 class="text-center text-success">编辑WEB监控规则</h3>
			<form name="webRuleUpdate" id="form" method="post">
				<table style='width:100%' class='table table-striped table-condensed table-bordered table-hover'>
				<tr>
				<th align=left>
				<c:set var="strs" value="${fn:split(payload.ruleId, ':')}" />
				<c:set var="name" value="${strs[2]}" />
				告警名<input id="name" value="${name}"/> URL 
					<select style="width: 600px;" name="url" id="url">
						<c:forEach var="item" items="${model.patternItems}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}|${item.value.pattern}</option>
						</c:forEach>
					</select>
				返回码 	<select id="code" style="width: 120px;">
						<option value="-1">ALL</option>
							<c:forEach var="item" items="${model.webCodes}" varStatus="status">
								<option value='${item.value.id}'>${item.value.name}</option>
							</c:forEach>
						</select>
				</th></tr>
				<tr><th>地区 
					<select style="width: 100px;" name="city" id="city">
						<option value="-1">ALL</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
				 运营商 <select style="width: 120px;" name="operator" id="operator">
						<option value="-1">ALL</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select>告警指标 <select id="metric" style="width: 100px;">
						<option value='request'>请求数</option>
						<option value='success'>成功率</option>
						<option value='delay'>响应时间</option>
				</select>
				</th></tr>
				<tr><th align=left>${model.content}</th></tr>
					<tr>
						<td style='text-align:center' colspan='2'><input class="btn btn-primary btn-mini" id="ruleSubmitButton" type="text" name="submit" value="提交"></button></td>
					</tr>
				</table>
			</form>
</a:config>

<script type="text/javascript">

function update() {
    var configStr = generateConfigsJsonString();
    var name = $("#name").val();
    var command = $("#url").val();
    var code = $("#code").val();
    var city = $("#city").val();
    var operator = $("#operator").val();
    var metric = $("#metric").val();
    var split = ";";
    var id = command + split + code + split + city + split + operator + ":" + metric + ":" + name;
    window.location.href = "?op=webRuleSubmit&configs=" + configStr + "&ruleId=" + id;
}

	$(document).ready(function() {
		var ruleId = "${payload.ruleId}";
		if(ruleId.length > 0){
			document.getElementById("name").disabled = true;
			document.getElementById("url").disabled = true;
			document.getElementById("code").disabled = true;
			document.getElementById("city").disabled = true;
			document.getElementById("operator").disabled = true;
			document.getElementById("metric").disabled = true;
		}
		var words = ruleId.split(":")[0].split(";");
		if(typeof words != "undefined" && words.length == 4){
			var metric = ruleId.split(":")[1];
			var command = words[0];
			var code = words[1];
			var city = words[2];
			var operator = words[3];
			$("#url").val(command);
			$("#code").val(code);
			$("#city").val(city);
			$("#operator").val(operator);
			$("#metric").val(metric);
		}
		$('#userMonitor_config').addClass('active open');
		$('#webRule').addClass('active');
		initRuleConfigs(["DescVal","DescPer","AscVal","AscPer"]);
		$(document).delegate("#ruleSubmitButton","click",function(){
			update();
		})
	});
</script>