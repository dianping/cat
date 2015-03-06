<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.storage.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.storage.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.storage.Model" scope="request" />

<a:report title="Storage Report"
	navUrlPrefix="op=${payload.action.name}&domain=${model.domain}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>
	
	<div class="report">
		<div class="text-center"><%@ include file="dependencyTimeNavTab.jsp"%> </div>
  	</div>
  	<c:set var="alertInfo" value="${model.alertInfo}" />
  	
	<table class="table table-hover table-striped table-condensed table-bordered"  style="width:100%">
	<c:forEach var="entry" items="${model.departments}" varStatus="index">
	<tr>
		<td width="10%" rowspan="${w:size(entry.value.productlines)}">${entry.key}</td>
			<c:if test="${index.index != 0}"><tr></c:if>
			<c:forEach var="e" items="${entry.value.productlines}">
				<td width="10%">${e.key}</td>
				<td>
				<c:forEach var="storage" items="${e.value.storages}">
					<c:set var="storageInfo" value="${alertInfo.storages[storage]}" />
					<c:if test="${storageInfo != null && storageInfo.level > 0 }">
						<div class="modal fade" id="${storageInfo.id}" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
							  <div class="modal-dialog" style="width:1100px">
							    <div class="modal-content">
							      <div class="modal-body">
							      	<h4 class=" text-center">数据库：<a href="/cat/r/storage?op=database&domain=${storage}&ip=All">${storage}</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;告警时间：第&nbsp;&nbsp;<span  class="text-danger">${model.minute}</span>&nbsp;&nbsp;分</h4>
							      	<table class="table table-striped table-condensed table-hover table-bordered">
										<tr class="text-success">
											<th width="10%">机器</th>
											<th width="20%">方法</th>
											<th width="20%">指标</th>
											<th width="50%">内容</th>
										</tr>
										<c:forEach var="machine_entry" items="${storageInfo.machines}">
											<tr>
											<td rowspan="${machine_entry.value.count}" class="center" style="vertical-align:middle">
											<c:if test="${machine_entry.value.level == 1}">
													<span class="text-warning">
												</c:if>
												<c:if test="${machine_entry.value.level == 2}">
													<span class="text-danger">
												</c:if>
											${machine_entry.key}</span></td>
											<c:forEach var="operation_entry" items="${machine_entry.value.operations}" varStatus="index1">
											<c:if test="${index1.index != 0}"><tr></c:if>
												<td rowspan="${operation_entry.value.count}" class="center" style="vertical-align:middle">
												<c:if test="${operation_entry.value.level == 1}">
													<span class="text-warning">
												</c:if>
												<c:if test="${operation_entry.value.level == 2}">
													<span class="text-danger">
												</c:if>
												${operation_entry.key}</span></td>
												<c:forEach var="target_entry" items="${operation_entry.value.targets}" varStatus="index2">
													<c:if test="${index2.index != 0}"><tr></c:if>
													<td rowspan="${target_entry.value.count}" class="center" style="vertical-align:middle">
													<c:if test="${target_entry.value.level == 1}">
													<span class="text-warning">
													</c:if>
													<c:if test="${target_entry.value.level == 2}">
														<span class="text-danger">
													</c:if>
													${target_entry.key}</span></td>
													<c:forEach var="detail" items="${target_entry.value.details}" varStatus="index3">
														<c:if test="${index3.index != 0}"><tr></c:if>
															<td>
															<c:if test="${detail.level == 1}">
															<span class="text-warning">
															</c:if>
															<c:if test="${detail.level == 2}">
																<span class="text-danger">
															</c:if>
															${detail.content}</span></td>
														<c:if test="${index3.index != 0}"></tr></c:if>
													</c:forEach>
													<c:if test="${index4.index != 0}"></tr></c:if>
												</c:forEach>
												<c:if test="${index3.index != 0}"></tr></c:if>
											</c:forEach>
											</tr>
										</c:forEach>
							 		</table>
							      </div>
							    </div>
							  </div>
							</div>	
					</c:if>
				<c:choose>
					<c:when test="${storageInfo != null && storageInfo.level == 1}">
						<button class="btn btn-app btn-lg radius-4 btn-warning alert-modal" data-id="${storage}" style="height: 50px; width: 100px">${storage}<span class="label label-inverse arrowed-in">${alertInfo.storages[storage].count }</span></button>
					</c:when>
					<c:when test="${storageInfo != null && storageInfo.level == 2}">
						<button class="btn btn-app btn-lg radius-4 btn-danger alert-modal" data-id="${storage}" style="height: 50px; width: 100px">${storage}<span class="label label-inverse arrowed-in">${alertInfo.storages[storage].count }</span></button>
					</c:when>
					<c:otherwise>
						<button class="btn btn-app btn-lg radius-4 btn-success alert-modal" onclick="document.location.href='/cat/r/storage?op=database&domain=${storage}&ip=All';" style="height: 50px; width: 100px">${storage}<span class="label label-inverse arrowed-in">${alertInfo.storages[storage].count }</span></button>
					</c:otherwise>
				</c:choose>
				</c:forEach>
				</td>
			</tr>
			</c:forEach>
	</c:forEach>
	</table>
</jsp:body>
</a:report>

<script type="text/javascript">
	$(document).ready(function() {
		$(".alert-modal").click(function(){
			var targetId = $(this).data("id");
			$("#"+targetId).modal();
		});
		$('#minute'+${model.minute}).addClass('disabled');
		$('.position').hide();
		$('.switch').hide();
		$('#Dashboard_report').addClass('active open');
		$('#dashbord_database').addClass('active');
	});
</script>