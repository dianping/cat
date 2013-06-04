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
			var action = '${payload.action}';
			if(action=='TOPOLOGY_GRAPH_CONFIG_NODE_ADD_OR_UPDATE_SUBMIT'){
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
			<form name="topologyGraphNodeConfigAddSumbit" id="form" method="post" action="${model.pageUri}?op=topologyGraphNodeConfigAddSumbit">
				<table class="table table-striped table-bordered table-condensed">
					<tr>
						<td>规则类型</td>
						<td><input type="name" name="type" value="${payload.type}" readonly/></td>
						<td></td>
					</tr>
					<tr>
						<td>项目名称</td>
						<td>
							<c:if test="${not empty payload.domain}">
								<input type="name" name="domainConfig.id" value="${payload.domain}" required/>
							</c:if>
							<c:if test="${empty payload.domain}">
								<input type="name" name="domainConfig.id" value="${model.domainConfig.id}" required/>
							</c:if>
						</td>
						<td>ALL表示所有项目的默认值</td>
					</tr>
					<tr>
						<td>异常数warning阈值</td>
						<td><input type="name" name="domainConfig.warningThreshold" value="${model.domainConfig.warningThreshold}" required/></td>
						<td style='color:red'>异常warning阈值（1一分钟内异常数目）</td>
					</tr>
					<tr>
						<td>异常数error阈值</td>
						<td><input type="name" name="domainConfig.errorThreshold" value="${model.domainConfig.errorThreshold}" required/></td>
						<td style='color:red'>异常error阈值（1一分钟内异常数目）</td>
					</tr>
					<tr>
						<td>响应时间warning阈值</td>
						<td><input type="name" name="domainConfig.warningResponseTime" value="${model.domainConfig.warningResponseTime}" required/></td>
						<td style='color:red'>响应时间warning阈值</td>
					</tr>
					<tr>
						<td>响应时间error阈值</td>
						<td><input type="name" name="domainConfig.errorResponseTime" value="${model.domainConfig.errorResponseTime}" required/></td>
						<td style='color:red'>响应时间error阈值</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><input class='btn btn-primary' type="submit" name="submit" value="submit" /></td>
					</tr>
				</table>
			</form>
		</div>
	</div>
</a:body>