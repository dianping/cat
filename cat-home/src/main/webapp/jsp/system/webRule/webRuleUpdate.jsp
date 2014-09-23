<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
		</br>
			<form name="appRuleUpdate" id="form" method="post">
				<table style='width:100%' class='table table-striped table-bordered'>
				<tr>
				<th align=left>
				组<select style="width: 100px;" name="group" id="group">
				</select> URL <select style="width: 600px;" name="url" id="url"></select>
				省份 <select style="width: 100px;" name="province" id="province">
				</select> 城市 <select style="width: 100px;" name="city" id="city">
				</select> 运营商 <select style="width: 120px;" name="operator" id="operator">
						<option value="">ALL</option>
						<option value="中国电信">中国电信</option>
						<option value="中国移动">中国移动</option>
						<option value="中国联通">中国联通</option>
						<option value="中国铁通">中国铁通</option>
						<option value="其他">其他</option>
						<option value="国外其他">国外其他</option>
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
			</form> </div></div></div>
</a:body>

<script type="text/javascript">

function update() {
    var configStr = generateConfigsJsonString();
    var url = $("#url").val();
    var city = $("#city").val();
    var operator = $("#operator").val();
    var metric = $("#metric").val();
    var split = ";";
    var id = url + split + city + split + operator + ":" +  metric;
    window.location.href = "?op=webRuleSubmit&key=" + "&{payload.key}" +"&configs=" + configStr + "&ruleId=" + id;
}

	$(document).ready(function() {
		$(document).delegate("#ruleSubmitButton","click",function(){
			update();
		})
		var ruleId = "${payload.ruleId}";
		if(ruleId.length > 0){
			document.getElementById("group").disabled = true;
			document.getElementById("url").disabled = true;
			document.getElementById("province").disabled = true;
			document.getElementById("city").disabled = true;
			document.getElementById("operator").disabled = true;
			document.getElementById("metric").disabled = true;
		}
		var words = ruleId.split(":")[0].split(";");
		var metric = ruleId.split(":")[1];
		var urlStr = words[0];
		var cityStr = words[1];
		var operatorStr = words[2];
		
		$('#metric').val(metric);
		$('#operator').val(operatorStr);
		$('#url').val(urlStr);
		
		var cityData = ${model.cityInfo};
		var select = $('#province');
		
		var urlData = ${model.group2PatternItemJson};
		var group = $('#group');
		
		function groupChange(){
			var key = $("#group").val();
			var value = urlData[key];
			var url = document.getElementById("url");
			url.length=0;
			for (var prop in value) {
			    var opt = $('<option />');
		  		
		  		opt.html(value[prop].pattern);
			  	opt.val(value[prop].name);
		  		opt.appendTo(url);
			}
		}
		group.on('change',groupChange);
		
		function provinceChange(){
			var key = $("#province").val();
			var value = cityData[key];
			
			select = document.getElementById("city");
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
		select.on('change',provinceChange);
		
		for (var prop in cityData) {
		  	if (cityData.hasOwnProperty(prop)) { 
		  		var opt = $('<option />');
		  		
		  		if(prop==''){
			  		opt.html('ALL');
		  		}else{
			  		opt.html(prop);
		  		}
		  		opt.val(prop);
		  		opt.appendTo(select);
		  }
		}
		
		for (var prop in urlData) {
		  	if (urlData.hasOwnProperty(prop)) { 
		  		var opt = $('<option />');
		  		
		  		opt.val(prop);
		  		opt.html(prop);
		  		opt.appendTo(group);
		  }
		}
		
		if(typeof(cityStr) != 'undefined'){
			var array = cityStr.split('-');
			$('#province').val(array[0]);
		}
		provinceChange();

		if(typeof(array) != 'undefined' && array.length==2){
			$("#city").val(cityStr);
		}
		
		for(var key in urlData){
			for (var i=0; i<urlData[key].length; i++ ){
				if(urlData[key][i].name == urlStr){
					$('#group').val(urlData[key][i].group);
					break;
				}
			}
		}
		groupChange();
		$("#url").val(urlStr);
		
		$('#webRule').addClass('active');
	});
</script>