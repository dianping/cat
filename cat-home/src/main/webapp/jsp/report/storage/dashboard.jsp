<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
  
	<c:forEach var="entry" items="${model.alertInfos}">
		<table  class="smallTable" style="float:left" border=1>
		<tr><th class="text-danger center">${entry.key}</th></tr>
		<c:forEach var="storage" items="${entry.value.storages}">
		<tr><td>
			<c:set var="storageInfo" value="${storage.value}" />
			<c:set var="times" value="${fn:split(entry.key,':')}" />
			<c:set var="hour" value="${times[0]}" />
			<c:set var="minute" value="${times[1]}" />
			<c:if test="${storageInfo != null && storageInfo.level > 0 }">
				<div class="hide" id="dialog-message-${storageInfo.id}-${hour}-${minute}">
			      	<table class="table table-striped table-condensed table-hover table-bordered">
						<thead><tr>
							<th width="10%" class="center">机器</th>
							<th width="10%" class="center">方法</th>
							<th width="10%" class="center">指标</th>
							<th width="70%" class="center">内容</th>
						</tr></thead>
						<c:forEach var="machine_entry" items="${storageInfo.machines}">
							<tr>
							<td rowspan="${machine_entry.value.count}" class="center" style="vertical-align:middle">
								<c:if test="${machine_entry.value.level == 1}">
									<span class="text-warning">${machine_entry.key}</span>
								</c:if>
								<c:if test="${machine_entry.value.level == 2}">
									<span class="text-danger"><strong>${machine_entry.key}</strong></span>
								</c:if>
							</td>
							<c:forEach var="operation_entry" items="${machine_entry.value.operations}" varStatus="index1">
								<c:if test="${index1.index != 0}"><tr></c:if>
								
								<td rowspan="${operation_entry.value.count}" class="center" style="vertical-align:middle">
								<c:if test="${operation_entry.value.level == 1}">
									<span class="text-warning">${operation_entry.key}</span>
								</c:if>
								<c:if test="${operation_entry.value.level == 2}">
									<span class="text-danger"><strong>${operation_entry.key}</strong></span>
								</c:if>
								</td>
								<c:forEach var="target_entry" items="${operation_entry.value.targets}" varStatus="index2">
									<c:if test="${index2.index != 0}"><tr></c:if>
									<td rowspan="${target_entry.value.count}" class="center" style="vertical-align:middle">
									<c:if test="${target_entry.value.level == 1}">
									<span class="text-warning">${target_entry.key}</span>
									</c:if>
									<c:if test="${target_entry.value.level == 2}">
										<span class="text-danger"><strong>${target_entry.key}</strong></span>
									</c:if>
									<c:forEach var="detail" items="${target_entry.value.details}" varStatus="index3">
										<c:if test="${index3.index != 0}"><tr></c:if>
											<td>
											<c:if test="${detail.level == 1}">
											<span class="text-warning">${detail.content}</span>
											</c:if>
											<c:if test="${detail.level == 2}">
												<span class="text-danger"><strong>${detail.content}</span></strong>
											</c:if>
											</td>
										<c:if test="${index3.index != 0}"></tr></c:if>
									</c:forEach>
									<c:if test="${index2.index != 0}"></tr></c:if>
								</c:forEach>
								<c:if test="${index1.index != 0}"></tr></c:if>
							</c:forEach>
							</tr>
						</c:forEach>
			 		</table>
				</div>	
			</c:if>
		<c:choose>
			<c:when test="${storageInfo != null && storageInfo.level == 1}">
				<button class="btn btn-app btn-sm radius-4 btn-warning alert-modal" data-id="${storageInfo.id}" data-hour="${hour}" data-minute="${minute}" style="height: 40px; min-width: 130px; width: auto">${w:shorten(storageInfo.id, 20)}<span class="label label-inverse arrowed-in">${storageInfo.count}</span></button>
			</c:when>
			<c:when test="${storageInfo != null && storageInfo.level == 2}">
				<button class="btn btn-app btn-sm radius-4 btn-danger alert-modal" data-id="${storageInfo.id}"  data-hour="${hour}" data-minute="${minute}" style="height: 40px;  min-width: 130px; width: auto">${w:shorten(storageInfo.id, 20)}<span class="label label-inverse arrowed-in">${storageInfo.count }</span></button>
			</c:when>
		</c:choose>
		</td></tr>
		</c:forEach>
		</table>
	</c:forEach>
</jsp:body>
</a:report>

<script type="text/javascript">
	$(document).ready(function() {
		$( ".alert-modal" ).on('click', function(e) {
			var targetId = $(this).data("id");
			var hour = $(this).data("hour");
			var minute = $(this).data("minute");
			if(hour < 10){
				hour = "0" + hour;
			}
			if(minute < 10){
				minute = "0" + minute;
			}
			e.preventDefault();
	
			var dialog = $("#dialog-message-"+targetId+"-"+hour+"-"+minute).removeClass('hide').dialog({
				width:'auto',
				modal: true,
				title: "<div class='widget-header widget-header-small'><h4><i class='ace-icon fa fa-check'></i><strong>数据库：[&nbsp;<a href='/cat/r/storage?op=database&domain="+targetId+"&ip=All'>"+targetId+"</a>&nbsp;]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;时间：<span  class='text-danger'>"+hour+" : "+ minute +"</span></strong></h4></div>",
				title_html: true,
				buttons: [ 
					{
						text: "Close",
						"class" : "btn btn-primary btn-xs",
						click: function() {
							$( this ).dialog( "close" ); 
						} 
					}
				]
			});
		});

		
		$('#minute'+${model.minute}).addClass('disabled');
		$('.position').hide();
		$('.switch').hide();
		$('#Dashboard_report').addClass('active open');
		$('#dashbord_database').addClass('active');
	});
</script>