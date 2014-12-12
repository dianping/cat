<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			var type = '${payload.type}';
			if(type ==''){
				type = '业务监控';
			}
			$('#tab-'+type).addClass('active');
			$('#tabContent-'+type).addClass('active');

			$('#projects_config').addClass('active open');
			$('#topologyProductLines').addClass('active');
			$('#content .nav-tabs a').mouseenter(function (e) {
				  e.preventDefault();
				  $(this).tab('show');
			});

			var action = '${payload.action.name}';
			if(action=='topologyProductLineDelete'||action=='topologyProductLineAddSubmit'){
				var state = '${model.opState}';
				if(state=='Success'){
					$('#state').html('操作成功');
				}else{
					$('#state').html('操作失败');
				}
				setInterval(function(){
					$('#state').html('&nbsp;');
				},3000);
			}
		});
	</script>
			<!-- Modal -->
			<div class="tabbable"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;">
			  	<c:forEach var="item" items="${model.typeToProductLines}" varStatus="status">
			  		<c:set var="type" value="${item.key}"/>
				    <li id="tab-${type}" class="text-right"><a href="#tabContent-${type}" data-toggle="tab"><strong>${type}</strong></a></li>
				</c:forEach>
			  </ul>
				<div class="tab-content">
			  	<c:forEach var="listItem" items="${model.typeToProductLines}" varStatus="status">
				<c:set var="type" value="${listItem.key}"/>
				<div class="tab-pane" id="tabContent-${type}">
				<table class="table table-striped table-condensed table-hover table-bordered">
					<thead><tr>
						<th width="15%">产品线</th>
						<th width="20%">标题</th>
						<th width="5%">顺序</th>
						<th width="7%">业务监控</th>
						<th width="7%">端到端监控</th>
						<th width="7%">应用监控</th>
						<th width="7%">网络监控</th>
						<th width="7%">系统监控</th>
						<th width="7%">数据库监控</th>
						<!-- <th width="40%">项目列表</th> -->
						<th width="8%">操作 <a href="?op=topologyProductLineAdd&type=${type}" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
					</tr></thead>
					<c:forEach var="item" items="${listItem.value}" varStatus="status">
						<tr><td>${item.id}</td><td>${item.title}</td>
						<td>${item.order}</td>
						<td><c:if test="${item.metricDashboard}"><span class="text-danger"><strong>是</strong></span></c:if>
							<c:if test="${!item.metricDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.userMonitorDashboard}"><span class="text-danger"><strong>是</strong></span></c:if>
							<c:if test="${!item.userMonitorDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.applicationDashboard}"><span class="text-danger"><strong>是</strong></span></c:if>
							<c:if test="${!item.applicationDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.networkDashboard}"><span class="text-danger"><strong>是</strong></span></c:if>
							<c:if test="${!item.networkDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.systemMonitorDashboard}"><span class="text-danger"><strong>是</strong></span></c:if>
							<c:if test="${!item.systemMonitorDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.databaseMonitorDashboard}"><span class="text-danger"><strong>是</strong></span></c:if>
							<c:if test="${!item.databaseMonitorDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<%-- <td>
							<c:forEach var="domain" items="${item.domains}"> 
								${domain.key},
							</c:forEach>
						</td> --%>
						<td><a href="?op=topologyProductLineAdd&productLineName=${item.id}&type=${type}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=topologyProductLineDelete&productLineName=${item.id}&type=${type}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach>
			</table></div></c:forEach></div></div>
	<c:if test="${model.opState == 'Success' && model.duplicateDomains != null}">
		<script>
			$(document).ready(function(){
				alert("无法添加项目：${model.duplicateDomains} 原因：以上项目属于其它产品线");
			})
		</script>
	</c:if>
</a:config>>
