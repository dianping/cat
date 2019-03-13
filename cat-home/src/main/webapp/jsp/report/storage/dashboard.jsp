<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>  
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.storage.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.storage.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.storage.Model" scope="request" />

<a:hourly_report title="Storage Report"
	navUrlPrefix="op=${payload.action.name}&domain=${model.domain}&type=${payload.type}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>
	
	<div class="report">
		<div class="text-center"><%@ include file="timeNavTab.jsp"%> </div>
  	</div>
  	
  	<c:set var="linkMap" value="${model.links}" />
  	<c:if test="${payload.type eq 'SQL'}"><c:set var="name" value="数据库" /></c:if>
  	<c:if test="${payload.type eq 'Cache'}"><c:set var="name" value="缓存" /></c:if>
  	<c:if test="${payload.type eq 'RPC'}"><c:set var="name" value="服务" /></c:if>
  
  <span>
	<c:forEach var="entry" items="${model.alertInfos}">
		<table  class="smallTable" style="float:left" border="1">
		<tr><th class="text-danger center" colspan="2">${entry.key}</th></tr>
		<c:if test="${empty entry.value.storages}">
			<tr><td>
				<button class="btn btn-app btn-sm radius-4 btn-success alert-modal" style="height: 40px; min-width: 130px; width: auto">${name}访问正常</button>
			</td></tr>
		</c:if>
		<c:forEach var="storage" items="${entry.value.storages}">
			<tr><td>
			<c:set var="storageInfo" value="${storage.value}" />
			<c:set var="times" value="${fn:split(entry.key,':')}" />
			<c:set var="hour" value="${times[0]}" />
			<c:set var="minute" value="${times[1]}" />
			<c:if test="${storageInfo != null && storageInfo.level > 0 }">
				<div class="hide dalog-message" id="dialog-message-${storageInfo.id}-${hour}-${minute}" onmouseleave="mouseLeave('dialog-message-${storageInfo.id}-${hour}-${minute}')">
					<%-- <c:if test="${payload.type eq 'SQL'}">
					</c:if>
					<c:if test="${payload.type eq 'Cache'}">缓存集群</c:if> --%>
			      	<table class="table table-striped table-condensed table-hover table-bordered">
			      	<thead><tr><td colspan="4" class="center"><h5><strong>${name}：[&nbsp;<a href='/cat/r/storage?domain=${model.domain}&id=${storageInfo.id}&ip=All&date=${model.date}&type=${payload.type}' target='_blank'>${storageInfo.id}</a>&nbsp;]&nbsp;&nbsp;&nbsp;&nbsp;时间：<span  class='text-danger'>${hour}&nbsp;:&nbsp;${minute}</span></strong></h5></td></tr></thead>
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
									<span class="text-warning"><a href='/cat/r/storage?domain=${model.domain}&id=${storageInfo.id}&ip=${machine_entry.key}&date=${model.date}&type=${payload.type}' target='_blank'>${machine_entry.key}</a></span>
								</c:if>
								<c:if test="${machine_entry.value.level == 2}">
									<span class="text-danger"><strong><a href='/cat/r/storage?domain=${model.domain}&id=${storageInfo.id}&ip=${machine_entry.key}&date=${model.date}&type=${payload.type}' target='_blank'>${machine_entry.key}</a></strong></span>
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
				<button class="btn btn-app btn-sm radius-4 btn-warning alert-modal" data-id="${storageInfo.id}" data-hour="${hour}" data-minute="${minute}" style="height: 40px; min-width: 130px; width: auto">${w:shorten(storageInfo.id, 15)}<span class="label label-inverse arrowed-in">${storageInfo.count}</span></button>
			</c:when>
			<c:when test="${storageInfo != null && storageInfo.level == 2}">
				<button class="btn btn-app btn-sm radius-4 btn-danger alert-modal" data-id="${storageInfo.id}"  data-hour="${hour}" data-minute="${minute}" style="height: 40px;  min-width: 130px; width: auto">${w:shorten(storageInfo.id, 15)}<span class="label label-inverse arrowed-in">${storageInfo.count }</span></button>
			</c:when>
		</c:choose>
		</td>
		<td>
		<c:forEach var="link" items="${linkMap[entry.key][storageInfo.id]}">
			<a href="${link}" target="_blank"><i class="ace-icon fa fa-bolt bigger-200"></i></a>
		</c:forEach>
		</td>
		</tr>
		</c:forEach>
		</table>
	</c:forEach>
	</span>
	<br/>
	<br/>
	<br/>
	<br/>
	<table class="table table-hover table-striped table-condensed table-bordered center"  style="width:100%">
		<c:if test="${w:size(model.alterations) > 0}">
			<tr class="text-success">
				<th class="center">时间</th>
				<th class="center">${name}</th>
				<th class="center">主机名</th>
				<th class="center">IP</th>
				<th class="center">标题</th>
				<th class="left">内容</th>
				<th class="center">状态</th>
			</tr>
			<c:forEach var="alt" items="${model.alterations}">
				<tr>
					<td><fmt:formatDate value="${alt.date}" pattern="HH:mm:ss"/></td>
					<td>${alt.domain}</td>
					<td>${alt.hostname}</td>
					<td>${alt.ip}</td>
					<td>${alt.title}</td>
					<td class="left">${alt.content}</td>
					<td>
						<c:if test="${alt.status == 0}" >
							<button class="btn btn-xs btn-success">
								<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
							</button>
						</c:if>
						<c:if test="${alt.status != 0}" >
							<button class="btn btn-xs btn-danger">
								<i class="ace-icon glyphicon glyphicon-remove bigger-120 btn-danger"></i>
							</button>
						</c:if>
					</td>
				</tr>
			</c:forEach>
		</c:if>
	</table>
</jsp:body>
</a:hourly_report>

<script type="text/javascript">
	function mouseLeave(id) {
		$("#"+id).dialog("close");
	}

	$(document).ready(function() {
		$("#warp_search_group").hide();
		$( ".alert-modal" ).on('click', function(e) {
			var targetId = $(this).data("id");
			var hour = $(this).data("hour");
			var minute = $(this).data("minute");
			e.preventDefault();
			console.log($("#dialog-message-"+targetId+"-"+hour+"-"+minute));
			var dialog = $("#dialog-message-"+targetId+"-"+hour+"-"+minute).removeClass('hide').dialog({
				width:'auto',
				modal: true,
				title_html: true,
			});
		});
		
		$('#minute'+${model.minute}).addClass('disabled');
		$('.position').hide();
		$('.switch').hide();
		$('#Dashboard_report').addClass('active open');
		<c:if test="${payload.type eq 'SQL'}">
			$('#dashbord_database').addClass('active');
		</c:if>
		<c:if test="${payload.type eq 'Cache'}">
			$('#dashbord_cache').addClass('active');
		</c:if>
		<c:if test="${payload.type eq 'RPC'}">
			$('#dashbord_rpc').addClass('active');
		</c:if>
		
	});
</script>