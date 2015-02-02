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
	<script type="text/javascript">
		$(document).ready(function() {
			$('#application_config').addClass('active open');
			$('#topologyGraphEdgeConfigList').addClass('active');
			var type = '${payload.type}';
			if(type==''){
				type = 'PigeonCall';
			}
			$('#tab-'+type).addClass('active');
			$('#tabContent-'+type).addClass('active');
			
			var action = '${payload.action.name}';
			if(action=='topologyGraphEdgeConfigDelete'||action=='topologyGraphEdgeConfigAddSumbit'){
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
			<c:if test="${w:size(model.edgeConfigs) ==0 }">
				<div class="row-fluid">
				<div class="span10"><h5 class="text-center text-danger">拓扑图依赖关系配置信息 </h5></div>
				<div class="span2 text-center"><a class="btn btn-primary btn-sm  update" href="?op=topologyGraphEdgeConfigAdd">新增</a></div>
			</div>
			</c:if>
			<div class="tabbable tabs-left" id="content" > <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs">
			  	<c:forEach var="item" items="${model.edgeConfigs}" varStatus="status">
				    <c:set var="key" value="${item.key}"/>
				    <li id="tab-${item.key}" class="text-right"><a href="#tabContent-${item.key}" data-toggle="tab"> ${item.key}</a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content" >
			  	<c:forEach var="item" items="${model.edgeConfigs}" varStatus="status">
				     <c:set var="key" value="${item.key}"/>
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent-${item.key}">
					    <h4 class="text-center text-danger">拓扑图依赖关系配置信息:${item.key}</h4>
				     	<table class="table table-striped table-condensed  table-bordered table-hover" >
				     		<thead><tr>
				     			<th>类型</th>
				     			<th>调用者</th>
				     			<th>被调用者</th>
				     			<th>最少调用次数</th>
				     			<th>异常Warning阀值</th>
				     			<th>异常Error阀值</th><th>响应时间Warning阀值</th>
				     			<th>响应时间Error阀值</th>
				     			<th width="8%">操作 <a href="?op=topologyGraphEdgeConfigAdd&type=${item.key}" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				     		</tr></thead>
				     		<tr class="text-danger">
				     			<td>默认值</td>
				     			<th>ALL</th>
				     			<th>ALL</th>
				     			<td style="text-align:right">${value.nodeConfig.defaultMinCountThreshold}</td>
				     			<td style="text-align:right">${value.nodeConfig.defaultWarningThreshold}</td>
				     			<td style="text-align:right">${value.nodeConfig.defaultErrorThreshold}</td>
					     		<td style="text-align:right">${value.nodeConfig.defaultWarningResponseTime}</td>
					     		<td style="text-align:right">${value.nodeConfig.defaultErrorResponseTime}</td>
						     	<td></td>
					     	</tr>
					     	<c:forEach var="temp" items="${value.edgeConfigs}">
					     		<tr>
					     		<td>${temp.type}</td>
				     			<td>${temp.from}</td>
				     			<td>${temp.to}</td>
				     			<td style="text-align:right">${temp.minCountThreshold}</td>
				     			<td style="text-align:right">${temp.warningThreshold}</td>
				     			<td style="text-align:right">${temp.errorThreshold}</td>
					     		<td style="text-align:right">${temp.warningResponseTime}</td>
					     		<td style="text-align:right">${temp.errorResponseTime}</td>
						     		<td><a href="?op=topologyGraphEdgeConfigAdd&type=${temp.type}&from=${temp.from}&to=${temp.to}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=topologyGraphEdgeConfigDelete&type=${temp.type}&from=${temp.from}&to=${temp.to}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					     		</tr>
					     	</c:forEach>
				     	</table>
				     </div>
				</c:forEach>
			  </div>
		   </div>
</a:config>
