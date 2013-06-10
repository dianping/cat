<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:body>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />

	<div class="row-fluid">
		<div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<form name="topologyGraphEdgeConfigAddSumbit" id="form" method="post"
				action="${model.pageUri}?op=topologyProductLineAddSubmit">
				<h4 class="text-center text-error" id="state">&nbsp;</h4>
				<h4 class="text-center text-error">修改产品线配置信息</h4>
				<table class="table table-striped table-bordered table-condensed">
					<tr>
						<td style="text-align: right" class="text-success">产品线名称</td>
						<td><input name="productLine.id"
							value="${model.productLine.id}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">产品线标题</td>
						<td><input name="productLine.title"
							value="${model.productLine.title}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">产品线顺序</td>
						<td><input name="productLine.order"
							value="${model.productLine.order}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">是否显示到监控大盘</td>
						<td><c:choose>
								<c:when test="${model.productLine.important}">
									<input type="radio" name="productLine.important" value="true" checked />是	
									<input type="radio" name="productLine.important" value="false" />否
							</c:when>
								<c:otherwise>
									<input type="radio" name="productLine.important" value="true" />是
									<input type="radio" name="productLine.important" value="false" checked />否
						</c:otherwise>
							</c:choose></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">选择产品线的项目</td>
						<td><select style="width: 500px;" name="domains" multiple=""
							id="domainSelect">
								<c:forEach var="item" items="${model.projects}">
									<option value="${item.domain}">${item.domain}</option>
								</c:forEach>
						</select></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><input class='btn btn-primary' id="addOrUpdateEdgeSubmit"
							type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
		</div>
	</div>
</a:body>
<script type="text/javascript">
		$(document).ready(function() {
			$('#topologyProductLines').addClass('active');
			
			$("#domainSelect").select2({
				placeholder : "选择属于这个产品线的项目",
				allowClear : true
			});
			
			var initDomains = [];
			<c:forEach var="domain" items="${model.productLine.domains}">
				initDomains.push("${domain.key}");
			</c:forEach>
			$("#domainSelect").val(initDomains).trigger("change");
		});
	</script>