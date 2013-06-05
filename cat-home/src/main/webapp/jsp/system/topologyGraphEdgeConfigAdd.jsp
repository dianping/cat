<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form name="topologyGraphEdgeConfigAddSumbit" id="form" method="post"
	action="${model.pageUri}?op=topologyGraphEdgeConfigAddSumbit">
	<h4 class="text-center text-error" id="state">&nbsp;</h4>
	<h4 class="text-center text-error">修改依赖关系配置信息</h4>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td width="40%" style="text-align: right" class="text-success">类型（支持PigeonCall和Database）</td>
			<td><select id="type" name="edgeConfig.type">
				<c:choose>
					<c:when test="${edgeConfig.type eq 'PigeonCall' || payload.type eq 'PigeonCall'}">
						<option value="PigeonCall" selected>PigeonCall</option>
					</c:when>
					<c:otherwise>
						<option value="PigeonCall">PigeonCall</option>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${edgeConfig.type eq 'Database' || payload.type eq 'Database'}">
						<option value="Database" selected>Database</option>
					</c:when>
					<c:otherwise>
						<option value="Database">Database</option>
					</c:otherwise>
				</c:choose>
			</select>
		</tr>
		<tr>
			<td style="text-align: right" class="text-success">调用项目名称</td>
			<td>
				<c:if test="${empty model.edgeConfig.from}">
					<input id="from" name="edgeConfig.from" value="${payload.from}" required/>
				</c:if>
				<c:if test="${not empty model.edgeConfig.from}">
					<input id="from" name="edgeConfig.from" value="${model.edgeConfig.from}" required />
				</c:if>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">被调用项目名称(Service或DB）</td>
			<td>
				<c:if test="${empty model.edgeConfig.to}">
					<input id="to" name="edgeConfig.to" value="${payload.to}" required/>
				</c:if>
				<c:if test="${not empty model.edgeConfig.to}">
					<input id="to" name="edgeConfig.to" value="${model.edgeConfig.to}" required />
				</c:if>
			</td>
		</tr>
		<tr>
			<td style="text-align: right" class="text-success">一分钟异常数warning阈值</td>
			<td><input id="warningThreshold" name="edgeConfig.warningThreshold"
				value="${model.edgeConfig.warningThreshold}" required /></td>
		</tr>
		<tr>
			<td style="text-align: right" class="text-success">一分钟内调用异常数error阈值</td>
			<td><input id="errorThreshold" name="edgeConfig.errorThreshold"
				value="${model.edgeConfig.errorThreshold}" required /></td>
		</tr>
		<tr>
			<td style="text-align: right" class="text-success">调用响应时间warning阈值</td>
			<td><input id="warningResponseTime" name="edgeConfig.warningResponseTime"
				value="${model.edgeConfig.warningResponseTime}" required /></td>
		</tr>
		<tr>
			<td style="text-align: right" class="text-success">调用响应时间error阈值</td>
			<td><input id="errorResponseTime" name="edgeConfig.errorResponseTime"
				value="${model.edgeConfig.errorResponseTime}" required /></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input class='btn btn-primary' id="addOrUpdateEdgeSubmit" type="submit"
				name="submit" value="submit"/></td>
		</tr>
	</table>
</form>