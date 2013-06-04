<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request" />

<a:body>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#topylogyConfigList').addClass('active');
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
				<table class="table table-striped table-bordered table-condensed">
					<tr>
						<td style="text-align: right">依赖类型</td>
						<td><select id="edgeConfigType" name="edgeConfig.type">
								<option value="PigeonCall">PigeonCall</option>
								<option value="Database">Database</option>
						</select>
						<td style='color: red'>项目依赖类型，目前支持PigeonCall和Database</td>
					</tr>
					<tr>
						<td style="text-align: right">调用项目名称</td>
						<td><input name="edgeConfig.from"
							value="${model.edgeConfig.from}" required /></td>
						<td style='color: red'>项目调用者比如XXXWeb</td>
					</tr>
					<tr>
						<td style="text-align: right">被调用项目名称</td>
						<td><input name="edgeConfig.to"
							value="${model.edgeConfig.to}" required /></td>
						<td style='color: red'>项目调用者比如XXX-Service,或者XXX-DB</td>
					</tr>
					<tr>
						<td style="text-align: right">异常数warning阈值</td>
						<td><input name="edgeConfig.warningThreshold"
							value="${model.edgeConfig.warningThreshold}" required /></td>
						<td style='color: red'>异常warning阈值（1一分钟内异常数目）</td>
					</tr>
					<tr>
						<td style="text-align: right">异常数error阈值</td>
						<td><input name="edgeConfig.errorThreshold"
							value="${model.edgeConfig.errorThreshold}" required /></td>
						<td style='color: red'>异常error阈值（1一分钟内异常数目）</td>
					</tr>
					<tr>
						<td style="text-align: right">响应时间warning阈值</td>
						<td><input name="edgeConfig.warningResponseTime"
							value="${model.edgeConfig.warningResponseTime}" required /></td>
						<td style='color: red'>响应时间warning阈值</td>
					</tr>
					<tr>
						<td style="text-align: right">响应时间error阈值</td>
						<td><input name="edgeConfig.errorResponseTime"
							value="${model.edgeConfig.errorResponseTime}" required /></td>
						<td style='color: red'>响应时间error阈值</td>
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