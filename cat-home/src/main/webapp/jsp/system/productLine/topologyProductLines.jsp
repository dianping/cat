<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
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

			
			$('#topologyProductLines').addClass('active');
			$('#content .nav-tabs a').mouseenter(function (e) {
				  e.preventDefault();
				  $(this).tab('show');
			});
			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
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
	<div class="row-fluid">
        <div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<h5 id="state" class="text-center text-error">&nbsp;</h5>
			<div class="tabbable"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs">
			  	<c:forEach var="item" items="${model.typeToProductLines}" varStatus="status">
			  		<c:set var="type" value="${item.key}"/>
				    <li id="tab-${type}" class="text-right"><a href="#tabContent-${type}" data-toggle="tab"> <h4 class="text-error">${type}</h4></a></li>
				</c:forEach>
			  </ul>
				<div class="tab-content">
			  	<c:forEach var="listItem" items="${model.typeToProductLines}" varStatus="status">
				<c:set var="type" value="${listItem.key}"/>
				<div class="tab-pane" id="tabContent-${type}">
				<table class="table table-striped table-bordered">
					<tr class="text-success">
						<th width="10%">产品线</th>
						<th width="10%">标题</th>
						<th width="5%">顺序</th>
						<th width="5%">业务监控</th>
						<th width="5%">端到端监控</th>
						<th width="5%">应用监控</th>
						<th width="5%">网络监控</th>
						<th width="5%">系统监控</th>
						<th width="40%">项目列表</th>
						<th width="10%">操作 <a href="?op=topologyProductLineAdd&type=${type}" class='update btn btn-primary btn-small'>新增</a></th>
					</tr>
					<c:forEach var="item" items="${listItem.value}" varStatus="status">
						<tr><td>${item.id}</td><td>${item.title}</td>
						<td>${item.order}</td>
						<td><c:if test="${item.metricDashboard}"><span class="text-error"><strong>是</strong></span></c:if>
							<c:if test="${!item.metricDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.userMonitorDashboard}"><span class="text-error"><strong>是</strong></span></c:if>
							<c:if test="${!item.userMonitorDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.applicationDashboard}"><span class="text-error"><strong>是</strong></span></c:if>
							<c:if test="${!item.applicationDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.networkDashboard}"><span class="text-error"><strong>是</strong></span></c:if>
							<c:if test="${!item.networkDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td><c:if test="${item.systemMonitorDashboard}"><span class="text-error"><strong>是</strong></span></c:if>
							<c:if test="${!item.systemMonitorDashboard}"><span><strong>否</strong></span></c:if>  </td>
						<td>
							<c:forEach var="domain" items="${item.domains}"> 
								${domain.key},
							</c:forEach>
						</td>
						<td><a href="?op=topologyProductLineAdd&productLineName=${item.id}&type=${type}" class='update btn btn-primary btn-small'>修改</a>
						<a href="?op=topologyProductLineDelete&productLineName=${item.id}&type=${type}" class='delete btn-danger btn btn-primary btn-small'>删除</a></td>
					</tr>
				</c:forEach>
			</table></div></c:forEach></div></div>
		</div>
	</div>
	<c:if test="${model.opState == 'Success' && model.duplicateDomains != null}">
		<script>
			$(document).ready(function(){
				alert("无法添加项目：${model.duplicateDomains} 原因：以上项目属于其它产品线");
			})
		</script>
	</c:if>
</a:body>