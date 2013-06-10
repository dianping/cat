<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>


<form name="topologyGraphNodeConfigAddSumbit" id="form" method="post" action="${model.pageUri}?op=topologyGraphNodeConfigAddSumbit">
	<h4 class="text-center text-error" id="state">&nbsp;</h4>
	<h4 class="text-center text-error">修改拓扑节点配置信息</h4>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td width="40%"  style="text-align:right" class="text-success">节点规则类型</td>
			<td><input id="type" name="type" value="${payload.type}" readonly/></td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">项目名称</td>
			<td>
				<c:if test="${not empty payload.domain}">
					<input id="id" name="domainConfig.id" value="${payload.domain}" readonly required/>
				</c:if>
				<c:if test="${empty payload.domain}">
					<select style="width:200px;" name="domainConfig.id" id="id">
	                     <c:forEach var="item" items="${model.projects}">
	                           <option value="${item.domain}">${item.domain}</option>
	                     </c:forEach>
                 	 </select>
				</c:if>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">一分钟异常数warning阈值</td>
			<td><input id="warningThreshold" name="domainConfig.warningThreshold" value="${model.domainConfig.warningThreshold}" required/></td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">一分钟异常数error阈值</td>
			<td><input id="errorThreshold" name="domainConfig.errorThreshold" value="${model.domainConfig.errorThreshold}" required/></td>
		</tr>
		<c:if test="${payload.type ne 'Exception' }">
		<tr>
			<td style="text-align:right" class="text-success">响应时间warning阈值</td>
			<td><input id="warningResponseTime"  name="domainConfig.warningResponseTime" value="${model.domainConfig.warningResponseTime}" required/></td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">响应时间error阈值</td>
			<td><input id="errorResponseTime"  name="domainConfig.errorResponseTime" value="${model.domainConfig.errorResponseTime}" required/></td>
		</tr>
		</c:if>
		<tr>
			<td>&nbsp;</td>
			<td><input class='btn btn-primary' id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>