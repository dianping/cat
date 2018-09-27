<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model" scope="request"/>

<a:serverBody>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#serverConfig').addClass('active open');
			$('#server_${payload.type}').addClass('active');
			
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setInterval(function(){
				$('#state').html('&nbsp;');
			},3000);
		});
	</script>
			<table class="table table-striped table-condensed table-bordered  table-hover">
	     		<thead><tr>
	     			<th width="30%">EndPoint</th>
	     			<th width="20%">Measurement</th>
	     			<th width="30%">Tags</th>
	     			<th width="10%">聚合</th>
	     			<th width="8%">操作 <a href="?op=serverAlarmRuleUpdate&type=${payload.type}" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
	     		</tr></thead>
		     	<c:forEach var="item" items="${model.serverAlarmRules}" varStatus="status">
	     			<tr>
	     			<td>${item.endPoint}</td>
	     			<td>${item.measurement}</td>
	     			<td>${item.tags}</td>
	     			<td>${model.metricTitles[item.type]}</td>
			     	<td><a href="?op=serverAlarmRuleUpdate&type=${payload.type}&ruleId=${item.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=serverAlarmRuleDelete&type=${payload.type}&ruleId=${item.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
		     		</tr>
		     	</c:forEach>
	     	</table>
</a:serverBody>