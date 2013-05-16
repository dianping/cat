<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />


<a:body>

	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
	
	<script type="text/javascript">
		$(document).ready(function() {
			var id = '${payload.action.name}'+'${model.alarmTemplate.name}';
			$('#'+id).addClass("active");
		});
	</script>
	
	<div class="container-fluid">
      	<div class="row-fluid">
        <div class="span2">
		<%@include file="./alarm.jsp"%>
		</div>
		<div class="span10">
			</br>
			</br>
			<table class="alarm table table-striped table-bordered  table-condensed">
				<tr><td><a class="btn btn-primary btn-small" href="?op=alarmTemplateUpdate&alarmTemplateId=${model.alarmTemplate.id}" target="_blank">修改</a></td></tr>
				<tr><td>模板名称</td><td>${model.alarmTemplate.name}</td></tr>
				<tr><td>模板内容</td>
					<td>
						<textarea style="height:500px;width:500px" id="content" name="content">${model.alarmTemplate.content}</textarea>
					</td>
					</tr>
			</table>
		</div></div>
	</div>
</a:body>