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
			$('#topylogyEdgeConfigList').addClass('active');
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
			var type = '${payload.type}';
			if(type==''){
				type = 'PigeonCall';
			}
			$('#tab-'+type).addClass('active');
			$('#tabContent-'+type).addClass('active');
			
			$(document).delegate('.update', 'click', function(e){
				var anchor = this,
					el = $(anchor);
				
				if(e.ctrlKey || e.metaKey){
					return true;
				}else{
					e.preventDefault();
				}
				//var cell = document.getElementById('');
				$.ajax({
					type: "get",
					url: anchor.href,
					success : function(response, textStatus) {
						$('#myModal').html(response);
						$('#myModal').modal();
					}
				});
			});
			
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
	<div class="row-fluid">
        <div class="span2">
			<%@include file="./configTree.jsp"%>
		</div>
		<div class="span10">
			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<h5 id="state" class="text-center text-error">&nbsp;</h5>
			<c:if test="${w:size(model.edgeConfigs) ==0 }">
				<div class="row-fluid">
				<div class="span10"><h5 class="text-center text-error">拓扑图依赖关系配置信息 </h5></div>
				<div class="span2 text-center"><a class="btn btn-primary btn-small  update" href="?op=topologyGraphEdgeConfigAdd">新增</a></div>
			</div>
			</c:if>
			<div class="tabbable tabs-left" id="topMetric"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs">
			  	<c:forEach var="item" items="${model.edgeConfigs}" varStatus="status">
				    <c:set var="key" value="${item.key}"/>
				    <li id="tab-${item.key}" class="text-right"><a href="#tabContent-${item.key}" data-toggle="tab"> <h5 class="text-error">${item.key}</h5></a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="item" items="${model.edgeConfigs}" varStatus="status">
				     <c:set var="key" value="${item.key}"/>
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent-${item.key}">
					    <h4 class="text-center text-error">拓扑图依赖关系配置信息:${item.key}</h4>
				     	<table class="table table-striped table-bordered table-condensed">
				     		<tr class="text-success">
				     			<th><h5 class='text-center'>类型</h5></th>
				     			<th><h5 class='text-center'>调用者</h5></th>
				     			<th><h5 class='text-center'>被调用者</h5></th>
				     			<th><h5 class='text-center'>异常Warning阀值</h5></th>
				     			<th><h5 class='text-center'>异常Error阀值</h5></th><th><h5 class='text-center'>响应时间Warning阀值</h5></th>
				     			<th><h5 class='text-center'>响应时间Error阀值</h5></th>
				     			<th><h5 class='text-center'>操作&nbsp;&nbsp;<a class="btn btn-primary btn-small update" href="?op=topologyGraphEdgeConfigAdd&type=${item.key}">新增</a></h5></th>
				     		</tr>
				     		<tr class="text-error">
				     			<td><h5>默认值</h5></td>
				     			<th><h5>ALL</h5></th>
				     			<th><h5>ALL</h5></th>
				     			<td style="text-align:right"><h5>${value.nodeConfig.defaultWarningThreshold}</h5></td>
				     			<td style="text-align:right"><h5>${value.nodeConfig.defaultErrorThreshold}</h5></td>
					     		<td style="text-align:right"><h5>${value.nodeConfig.defaultWarningResponseTime}</h5></td>
					     		<td style="text-align:right"><h5>${value.nodeConfig.defaultErrorResponseTime}</h5></td>
						     	<td></td>
					     		</tr>
					     	<c:forEach var="temp" items="${value.edgeConfigs}">
					     		<tr>
					     		<td>${temp.type}</td>
				     			<td>${temp.from}</td>
				     			<td>${temp.to}</td>
				     			<td style="text-align:right">${temp.warningThreshold}</td>
				     			<td style="text-align:right">${temp.errorThreshold}</td>
					     		<td style="text-align:right">${temp.warningResponseTime}</td>
					     		<td style="text-align:right">${temp.errorResponseTime}</td>
						     	<td style="text-align:center"><a href="?op=topologyGraphEdgeConfigAdd&type=${temp.type}&from=${temp.from}&to=${temp.to}" class="btn update btn-primary btn-small">修改</a>
						     		<a href="?op=topologyGraphEdgeConfigDelete&type=${temp.type}&from=${temp.from}&to=${temp.to}" class="btn btn-primary btn-small btn-danger delete">删除</a></td>
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