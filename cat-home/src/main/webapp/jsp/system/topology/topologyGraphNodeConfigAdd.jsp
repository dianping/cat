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
<form name="topologyGraphNodeConfigAddSumbit" id="form" method="post" action="${model.pageUri}?op=topologyGraphNodeConfigAddSumbit">
	<h4 class="text-center text-danger" id="state">&nbsp;</h4>
	<h4 class="text-center text-danger">修改拓扑节点配置信息</h4>
	<table class="table table-striped table-condensed  ">
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
			<td style="text-align:right" class="text-success">最少访问次数</td>
			<td><input id="warningThreshold" name="domainConfig.minCountThreshold" value="${model.domainConfig.minCountThreshold}" required/></td>
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
		<c:if test="${payload.type eq 'Exception' }">
			<tr style="display:none">
				<td style="text-align:right" class="text-success">响应时间warning阈值</td>
				<td><input id="warningResponseTime"  name="domainConfig.warningResponseTime" value="100" required/></td>
			</tr>
			<tr style="display:none">
				<td style="text-align:right" class="text-success">响应时间error阈值</td>
				<td><input id="errorResponseTime"  name="domainConfig.errorResponseTime" value="100" required/></td>
			</tr>
		</c:if>
		<tr>
			<td>&nbsp;</td>
			<td><input class='btn btn-primary btn-sm' id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>
</a:config>