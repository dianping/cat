<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />


<a:body>

	<res:useJs value="${res.js.local['dtree.js']}" target="head-js"/>
	<res:useCss value='${res.css.local.dtree_css}' target="head-css" />
	<res:useCss value='${res.css.local.alarm_css}' target="head-css" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js"/>
	
	<div class="body-content">
		<%@include file="./alarm.jsp" %>	
	    <script type="text/javascript">
			d.openAll();
			d.s(${model.templateIndex});
		</script>
		<div class="content-right">
			</br>
			</br>
			<table>
				<tr><td><a href="?op=alarmTemplateUpdate&alarmTemplateId=${model.alarmTemplate.id}" target="_blank">修改</a></td></tr>
				<tr><td>模板名称</td><td>${model.alarmTemplate.name}</td></tr>
				<tr><td>模板内容</td>
					<td>
						<textarea style="height:500px;width:500px" id="content" name="content">${model.alarmTemplate.content}</textarea>
					</td>
					</tr>
			</table>
		</div>
	</div>
</a:body>