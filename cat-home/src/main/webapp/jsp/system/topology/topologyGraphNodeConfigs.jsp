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
			$('#topylogyNodeConfigList').addClass('active');
			$('#content .nav-tabs a').mouseenter(function (e) {
				  e.preventDefault();
				  $(this).tab('show');
			});
			var type = '${payload.type}';
			if(type==''){
				type = 'URL';
			}
			$('#tab-'+type).addClass('active');
			$('#tabContent-'+type).addClass('active');
			
			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
			
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
						nodeValidate();
						$("#id").select2();
					}
				});
			});
			
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
	<div class="row-fluid">
        <div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs">
			  	<c:forEach var="item" items="${model.config.nodeConfigs}" varStatus="status">
				    <c:set var="key" value="${item.key}"/>
				    <li id="tab-${key}" class="text-right"><a href="#tabContent-${key}" data-toggle="tab"> <h5 class="text-error">${key}</h5></a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="item" items="${model.config.nodeConfigs}" varStatus="status">
				     <c:set var="key" value="${item.key}"/>
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent-${key}">
					    <h4 class="text-center text-error">拓扑图节点配置信息：${item.key}</h4>
				     	<table class="table table-striped table-bordered table-condensed">
				     		<tr class="text-success">
				     			<th><h5 class='text-center'>项目</h5></th><th><h5 class='text-center'>异常Warning阀值</h5></th>
				     			<th><h5 class='text-center'>异常Error阀值</h5></th><th><h5 class='text-center'>响应时间Warning阀值</h5></th>
				     			<th><h5 class='text-center'>响应时间Error阀值</h5></th>
				     			<th><h5 class='text-center'>操作&nbsp;&nbsp;<a class="btn update btn-primary btn-small" href="?op=topologyGraphNodeConfigAdd&type=${item.key}">新增</a></h5></th>
				     		</tr>
				     		<tr class="text-error"><td><h5>默认值</h5></td><td><h5 class="text-right">${value.defaultWarningThreshold}</h5></td>
					     		<td><h5 class="text-right">${value.defaultErrorThreshold}</h5></td>
						     	<c:if test="${item.key eq 'Exception'}">
						     		<td><h5 class="text-right">——</h5></td>
						     		<td><h5 class="text-right">——</h5></td>
						     	</c:if>
						     	<c:if test="${item.key ne 'Exception'}">
						     		<td><h5 class="text-right">${value.defaultWarningResponseTime}</h5></td>
						     		<td><h5 class="text-right">${value.defaultErrorResponseTime}</h5></td>
						     	</c:if>
						     	<td style="text-align:center;vertical-align:middle;"><a href="?op=topologyGraphNodeConfigAdd&type=${item.key}&domain=ALL" class="btn update btn-primary btn-small">修改</a></td>
					     		</tr>
					     	<c:forEach var="domainConfig" items="${value.domainConfigs}">
					     		<c:set var="temp" value="${domainConfig.value}"/>
				     			<tr><td>${temp.id}</td>
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
					     		<td style="text-align:center"><a href="?op=topologyGraphNodeConfigAdd&type=${item.key}&domain=${temp.id}" class="btn update btn-primary btn-small">修改</a>
						     	<a href="?op=topologyGraphNodeConfigDelete&type=${item.key}&domain=${temp.id}" class="btn btn-primary btn-small btn-danger delete">删除</a></td>
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