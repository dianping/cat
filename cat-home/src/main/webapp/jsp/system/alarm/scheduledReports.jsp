<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context"	scope="request" />
<jsp:useBean id="payload"type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model"scope="request" />

<a:application>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
	
	<script type="text/javascript">
		$(document).ready(function() {
			$("#scheduledReports").addClass("active");
			$(".delete").bind("click", function() {
			 	return confirm("确定要删除此项目吗(不可恢复)？");
			});
		});
	</script>
		<div style="height:24px"></div>
      	<div class="row-fluid">
        <div class="span2">
		<%@include file="./alarm.jsp"%>
		</div>
		<div class="span10">
			<table class="alarm table table-striped table-condensed   " id="contents" width="100%">
				<thead>
				<tr >
					<th><span class="text-success">项目名</span></th>
					<th><span class="text-success">报表内容</span></th>
					<th><span class="text-success">操作</span>&nbsp;&nbsp;</th>
				</tr></thead><tbody>
				<c:forEach var="item" items="${model.userReportSubStates}"
					varStatus="status">
					<tr>
						<td>${item.scheduledReport.domain}</td>
						<td>${item.scheduledReport.names}</td>
						<td><c:choose>
								<c:when test="${item.subscriberState == 0}">
									<a class="btn btn-primary btn-sm" href="?op=scheduledReportSub&scheduledReportId=${item.scheduledReport.id}&subState=0" onclick="return sub(this)">订阅</a>
								</c:when>
								<c:otherwise>
									<a class="btn btn-danger btn-sm" href="?op=scheduledReportSub&scheduledReportId=${item.scheduledReport.id}&subState=1" onclick="return sub(this)">取消</a>
								</c:otherwise>
							</c:choose> 
							<a class="btn btn-primary btn-sm" href="?op=scheduledReportUpdate&scheduledReportId=${item.scheduledReport.id}"  target="_blank">编辑</a> 
							<a class="delete btn btn-danger btn-sm" href="?op=scheduledReportDelete&scheduledReportId=${item.scheduledReport.id}">删除</a> 
						</td>
					</tr>
				</c:forEach></tbody>
			</table>
		</div></div>
</a:application>