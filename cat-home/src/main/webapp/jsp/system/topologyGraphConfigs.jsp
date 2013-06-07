<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#topylogyConfigList').addClass('active');
			$('#tab0').addClass('active');
			$('#tabContent0').addClass('active');
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
			var action = '${payload.action}';
			if(action=='TOPOLOGY_GRAPH_CONFIG_NODE_DELETE'){
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
			<%@include file="./configTree.jsp"%>
		</div>
		<div class="span10">
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<div class="tabbable tabs-left" id="topMetric"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs">
			  	<c:forEach var="item" items="${model.config.nodeConfigs}" varStatus="status">
				    <c:set var="key" value="${item.key}"/>
				    <li id="tab${status.index}" class="text-right"><a href="#tabContent${status.index}" data-toggle="tab"> <h4 class="text-error">${key}</h4></a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="item" items="${model.config.nodeConfigs}" varStatus="status">
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent${status.index}">
					     <h2 class="text-center text-error">${item.key}</h2>
				     	<table class="table table-striped table-bordered table-condensed">
				     		<tr class="text-success">
				     			<th><h4 class='text-center'>项目</h4></th><th><h4 class='text-center'>异常Warning阀值</h4></th>
				     			<th><h4 class='text-center'>异常Error阀值</h4></th><th><h4 class='text-center'>响应时间Warning阀值</h4></th>
				     			<th><h4 class='text-center'>响应时间Error阀值</h4></th>
				     			<th><h4 class='text-center'>操作<a class="btn btn-primary btn-small" href="?op=topologyGraphConfigNodeAdd&type=${item.key}">新增</a></h4></th>
				     		</tr>
				     		<tr class="text-error"><td><h5>默认值</h5></td><td><h5 class="text-right">${value.defaultWarningThreshold}</h5></td>
					     		<td><h5 class="text-right">${value.defaultErrorThreshold}</h5></td>
						     	<td><h5 class="text-right">${value.defaultWarningResponseTime}</h5></td>
						     	<td><h5 class="text-right">${value.defaultErrorResponseTime}</h5></td>
						     	<td><a href="?op=topologyGraphConfigNodeAdd&type=${item.key}&domain=ALL" class="btn btn-primary btn-small">修改</a></td>
					     		</tr>
					     	<c:forEach var="domainConfig" items="${value.domains}">
					     		<c:set var="temp" value="${domainConfig.value}"/>
				     			<tr><td>${temp.id}</td>
				     			<td style="text-align:right">${temp.warningThreshold}</td>
				     			<td style="text-align:right">${temp.errorThreshold}</td>
					     		<td style="text-align:right">${temp.warningResponseTime}</td>
					     		<td style="text-align:right">${temp.errorResponseTime}</td>
						     	<td><a href="?op=topologyGraphConfigNodeAdd&type=${item.key}&domain=${temp.id}" class="btn btn-primary btn-small">修改</a>
						     	<a href="?op=topologyGraphConfigNodeDelete&type=${item.key}&domain=${temp.id}" class="btn btn-primary btn-small btn-danger delete">删除</a></td>
					     		</tr>
					     	</c:forEach>
				     	</table>
				     </div>
				</c:forEach>
			  </div>
		   </div>
		</div>
	</div>
</a:body>