<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />
<a:body>
	<div class="row-fluid">
		<%@include file="menu.jsp"%>
	<div class="span10">
		<%@include file="crashLogDetail.jsp"%>
	</div>
		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>

<script type="text/javascript">
	function query(){
		var plat = $("#platformType").val();
		var appVersion = queryField('${model.fieldsInfo.appVersions}','appVersion');
		var platVersion = queryField('${model.fieldsInfo.platVersions}','platformVersion');
		var module = queryField('${model.fieldsInfo.modules}','module');
		var level = queryField('${model.fieldsInfo.levels}','level');
		var split = ";";
		var query = plat + split + appVersion + split + platVersion + split + module + split + level;
		window.location.href = "?op=${payload.action.name}&query1=" + query + "&step=${payload.step}";
	}
	
	function clickMe(fields, prefix) {
		var fs = [];
		if(fields != "[]") {
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		var num = 0;
		for( var i=0; i<fs.length; i++){
		 	var f = prefix + "_" + fs[i];
			if(document.getElementById(f).checked){
				num ++;
			}else{
				document.getElementById(prefix + "All").checked = false;
				break;
			} 
		}
		if(num > 0 && num == fs.length) {
			document.getElementById(prefix + "All").checked = true;
		}
	}
	
	function clickAll(fields, prefix) {
		var fs = [];
		if(fields != "[]"){
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		for( var i=0; i<fs.length; i++){
		 	var f = prefix + "_" + fs[i];
			document.getElementById(f).checked = document.getElementById(prefix + "All").checked;
		}
	}
	
	function queryField(fields, prefix){
		if(fields != "[]") {
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		var url = '';
		var num = 0;
		if(document.getElementById(prefix + "All").checked == false && fs.length > 0) {
			for( var i=0; i<fs.length; i++){
			 	var f = prefix + "_" + fs[i];
				if(document.getElementById(f).checked){
					url += fs[i] + ":";
				} 
			}
			url = url.substring(0, url.length-1);
		}else{
			url = "";
		}
		return url;
	}
	
	function docReady(field, fields, prefix){
		var urls = [];
		
		if(field == ''){
			document.getElementById(prefix + "All").checked = true;
			clickAll(fields, prefix);
		}else{
			urls = field.split(":");
		}
		for(var i=0; i<urls.length; i++) {
			document.getElementById(prefix + "_" + urls[i]).checked = true;
		}
	}
	
	$("#platformType")
	  .change(function () {
		  window.location.href = "?op=${payload.action.name}&query1=" + this.value + ";;;;&date=${model.date}&reportType=${model.reportType}";
	  })
$(document).ready(
		function() {
			$('#crashLog').addClass('active');
			
			var fields = "${payload.query1}".split(";");
			docReady(fields[4], '${model.fieldsInfo.levels}','level');
			$("#platformType").val(fields[0]);
			docReady(fields[1], '${model.fieldsInfo.appVersions}','appVersion');
			docReady(fields[2], '${model.fieldsInfo.platVersions}','platformVersion');
			docReady(fields[3], '${model.fieldsInfo.modules}','module');
		});
</script>

<style type="text/css">
	.row-fluid .span2 {
		width:10%;
	}
	.row-fluid .span10 {
		width:87%;
	}
	.report .btn-group {
		position: relative;
		display: inline-block;
		font-size: 0;
		white-space: normal;
		vertical-align: middle;
	}
</style>