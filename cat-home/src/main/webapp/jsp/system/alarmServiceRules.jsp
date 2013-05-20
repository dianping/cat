<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context"	scope="request" />
<jsp:useBean id="payload"type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model"scope="request" />

<a:body>

	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		init();
		var id = '${payload.action.name}';
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
			<table class="alarm table table-striped table-bordered  table-condensed" id="contents" width="100%">
				<thead>
				<tr class="odd">
					<th><span class="text-success">项目名</span></th>
					<th><span class="text-success">操作</span>&nbsp;&nbsp; <a class="btn btn-primary btn-small" href="?op=alarmRuleAdd&type=service">新增</a></th>
				</tr></thead><tbody>
				<c:forEach var="item" items="${model.userSubStates}"
					varStatus="status">
					<tr>
						<td>${item.alarmRule.domain}</td>
						<td><c:choose>
								<c:when test="${item.subscriberState == 0}">
									<a class="btn btn-primary btn-small" href="?op=serviceAlarmRuleSub&alarmRuleId=${item.alarmRule.id}&subState=0" onclick="return sub(this)">订阅</a>
								</c:when>
								<c:otherwise>
									<a  class="btn btn-danger btn-small" href="?op=serviceAlarmRuleSub&alarmRuleId=${item.alarmRule.id}&subState=1" onclick="return sub(this)">取消</a>
								</c:otherwise>
							</c:choose> 
							<a  class="btn btn-primary btn-small" href="?op=alarmRuleUpdate&alarmRuleId=${item.alarmRule.id}&type=service">编辑</a> 
							<a  class="btn btn-danger btn-small" href="?op=serviceAlarmRuleDelete&alarmRuleId=${item.alarmRule.id}">删除</a> 
						</td>
					</tr>
				</c:forEach></tbody>
			</table>
		</div></div>
	</div>
</a:body>