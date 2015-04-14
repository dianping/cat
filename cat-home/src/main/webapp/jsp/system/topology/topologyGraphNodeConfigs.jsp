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
			$('#application_config').addClass('active open');
			$('#topologyGraphNodeConfigList').addClass('active');
			
 			var type = '${payload.type}';
			if(type==''){
				type = 'URL';
			}
			
			$('#tab-'+type).addClass('active');
			$('#tabContent-'+type).addClass('active');
			
			var action = '${payload.action.name}';
			if(action=='topologyGraphNodeConfigDelete'||action=='topologyGraphNodeConfigAddSumbit'){
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
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs">
			  	<c:forEach var="item" items="${model.config.nodeConfigs}" varStatus="status">
				    <c:set var="key" value="${item.key}"/>
				    <li id="tab-${key}" class="text-right"><a href="#tabContent-${key}" data-toggle="tab">${key}</a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="item" items="${model.config.nodeConfigs}" varStatus="status">
				     <c:set var="key" value="${item.key}"/>
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent-${key}">
					    <h4 class="text-center text-danger">拓扑图节点配置信息：${item.key}</h4>
				     	<table class="table table-striped table-condensed  table-bordered table-hover">
				     		<thead><tr>
				     			<th>项目</th>
				     			<th>最小的个数</th>
				     			<th>异常Warning阀值</th>
				     			<th>异常Error阀值</th><th>响应时间Warning阀值</th>
				     			<th>响应时间Error阀值</th>
				     			<th width="8%">操作 <a href="?op=topologyGraphNodeConfigAdd&type=${item.key}" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				     		</tr></thead>
				     		<tr class="text-danger"><td><h5>默认值</h5></td>
				     			<td><h5 class="text-right">${value.defaultMinCountThreshold}</h5></td>
				     			<td><h5 class="text-right">${value.defaultWarningThreshold}</h5></td>
					     		<td><h5 class="text-right">${value.defaultErrorThreshold}</h5></td>
						     	<c:if test="${item.key eq 'Exception'}">
						     		<td><h5 class="text-right">——</h5></td>
						     		<td><h5 class="text-right">——</h5></td>
						     	</c:if>
						     	<c:if test="${item.key ne 'Exception'}">
						     		<td><h5 class="text-right">${value.defaultWarningResponseTime}</h5></td>
						     		<td><h5 class="text-right">${value.defaultErrorResponseTime}</h5></td>
						     	</c:if>
						     	<td style="text-align:center;vertical-align:middle;"><a href="?op=topologyGraphNodeConfigAdd&type=${item.key}&domain=ALL" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a></td>
					     		</tr>
					     	<c:forEach var="domainConfig" items="${value.domainConfigs}">
					     		<c:set var="temp" value="${domainConfig.value}"/>
				     			<tr><td>${temp.id}</td>
				     			<td style="text-align:right">${temp.minCountThreshold}</td>
				     			<td style="text-align:right">${temp.warningThreshold}</td>
				     			<td style="text-align:right">${temp.errorThreshold}</td>
				     			<c:if test="${item.key eq 'Exception'}">
				     				<td style="text-align:right">——</td>
					     			<td style="text-align:right">——</td>
				     			</c:if>
				     			<c:if test="${item.key ne 'Exception'}">
				     				<td style="text-align:right">${temp.warningResponseTime}</td>
					     			<td style="text-align:right">${temp.errorResponseTime}</td>
				     			</c:if>
						     	<td><a href="?op=topologyGraphNodeConfigAdd&type=${item.key}&domain=${temp.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=topologyGraphNodeConfigDelete&type=${item.key}&domain=${temp.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					     		</tr>
					     	</c:forEach>
				     	</table>
				     </div>
				</c:forEach>
			  </div>
		   </div>
</a:config>
