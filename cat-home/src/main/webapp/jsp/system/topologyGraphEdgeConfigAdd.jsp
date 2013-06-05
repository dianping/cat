<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request" />

<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	
<a:body>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#topylogyEdgeConfigList').addClass('active');
			var action = '${payload.action}';
			if (action == 'TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT') {
				var state = '${model.opState}';
				if (state == 'Success') {
					$('#state').html('操作成功');
				} else {
					$('#state').html('操作失败');
				}
				setInterval(function() {
					$('#state').html('&nbsp;');
				}, 3000);
			}
			var type = '${model.edgeConfig.type}';
			if (type == '') {
				type = '${payload.type}';
			}
			$('#edgeConfigType').val(type);
		});
	</script>
	<div class="row-fluid">
		<div class="span2">
			<%@include file="./configTree.jsp"%>
		</div>
		<div class="span10">
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<form name="topologyGraphEdgeConfigAddSumbit" id="form" method="post"
				action="${model.pageUri}?op=topologyGraphEdgeConfigAddSumbit">
				<h4 class="text-center text-error">修改依赖关系配置信息</h4>
				<table class="table table-striped table-bordered table-condensed">
					<tr>
						<td width="40%" style="text-align: right" class="text-success">依赖类型（支持PigeonCall和Database）</td>
						<td><select id="edgeConfigType" name="edgeConfig.type">
								<option value="PigeonCall">PigeonCall</option>
								<option value="Database">Database</option>
						</select>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">调用项目名称</td>
						<td>
							<c:if test="${empty model.edgeConfig.from}">
								<input name="edgeConfig.from" value="${payload.from}" required/>
							</c:if>
							<c:if test="${not empty model.edgeConfig.from}">
								<input name="edgeConfig.from" value="${model.edgeConfig.from}" required />
							</c:if>
						</td>
					</tr>
					<tr>
						<td style="text-align:right" class="text-success">被调用项目名称（Service或者数据库）</td>
						<td>
							<c:if test="${empty model.edgeConfig.to}">
								<input name="edgeConfig.to" value="${payload.to}" required/>
							</c:if>
							<c:if test="${not empty model.edgeConfig.to}">
								<input name="edgeConfig.to" value="${model.edgeConfig.to}" required />
							</c:if>
						</td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">一分钟异常数warning阈值</td>
						<td><input name="edgeConfig.warningThreshold"
							value="${model.edgeConfig.warningThreshold}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">一分钟内调用异常数error阈值</td>
						<td><input name="edgeConfig.errorThreshold"
							value="${model.edgeConfig.errorThreshold}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">调用响应时间warning阈值</td>
						<td><input name="edgeConfig.warningResponseTime"
							value="${model.edgeConfig.warningResponseTime}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">调用响应时间error阈值</td>
						<td><input name="edgeConfig.errorResponseTime"
							value="${model.edgeConfig.errorResponseTime}" required /></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><input class='btn btn-primary' type="submit"
							name="submit" value="submit" /></td>
					</tr>
				</table>
			</form>
		</div>
	</div>
</a:body>