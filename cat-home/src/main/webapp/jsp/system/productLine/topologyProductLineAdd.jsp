<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<link rel="stylesheet" href="${model.webapp}/assets/css/chosen.css" />
	<script src="${model.webapp}/assets/js/chosen.jquery.min.js"></script>
			<form name="topologyGraphEdgeConfigAddSumbit" id="form" method="get"
				action="${model.pageUri}?op=topologyProductLineAddSubmit">
				<h4 class="text-center text-danger">修改产品线配置信息</h4>
				<input type="hidden" name="op" value="topologyProductLineAddSubmit" />
				<table class="table table-striped table-condensed table-border table-hover ">
					<tr>
						<td style="width:20%;text-align: right" class="text-success">产品线名称（全英文）</td>
						<td><input name="productLine.id"
							value="${model.productLine.id}" required />
							<input name="type" value="${payload.type}" type="hidden"/>	
						</td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">产品线标题（中文）</td>
						<td><input name="productLine.title"
							value="${model.productLine.title}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">产品线顺序（数字）</td>
						<td><input name="productLine.order"
							value="${model.productLine.order}" required /></td>
					</tr>
					<c:if test="${payload.type eq '业务监控' || payload.type eq '应用监控'}">
					<tr>
						<td style="text-align: right" class="text-success">是否显示到监控依赖大盘</td>
						<td><c:choose>
								<c:when test="${model.productLine.applicationDashboard}">
									<input type="radio" name="productLine.applicationDashboard" value="true" checked />是	
									<input type="radio" name="productLine.applicationDashboard" value="false" />否
							</c:when>
								<c:otherwise>
									<input type="radio" name="productLine.applicationDashboard" value="true" />是
									<input type="radio" name="productLine.applicationDashboard" value="false" checked />否
						</c:otherwise>
							</c:choose></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">是否显示到业务监控大盘</td>
						<td><c:choose>
							<c:when test="${model.productLine.metricDashboard}">
								<input type="radio" name="productLine.metricDashboard" value="true" checked />是	
								<input type="radio" name="productLine.metricDashboard" value="false" />否
							</c:when>
							<c:otherwise>
								<input type="radio" name="productLine.metricDashboard" value="true" />是
								<input type="radio" name="productLine.metricDashboard" value="false" checked />否
							</c:otherwise>
						</c:choose></td>
					</tr>
					</c:if>
					<c:if test="${payload.type eq '网络监控'}">
						<input type="hidden" name="productLine.networkMonitorDashboard" value="true" />
					</c:if>
					<c:if test="${payload.type eq '系统监控'}">
						<input type="hidden" name="productLine.systemMonitorDashboard" value="true" />
					</c:if>
					<c:if test="${payload.type eq '外部监控'}">
						<input type="hidden" name="productLine.userkMonitorDashboard" value="true" />
					</c:if>
					<c:if test="${payload.type eq '数据库监控'}">
						<input type="hidden" name="productLine.databaseMonitorDashboard" value="true" />
					</c:if>
					<tr>
						<td style="text-align: right" class="text-success">选择产品线的项目</td>
						<td style="width:50%;">
							<select multiple class="chosen-select" id="domain_select" name="domains" 
								data-placeholder="Choose a State...">
								<c:forEach var="item" items="${model.projects}">
									<c:set var="domains" value="${model.productLine.domains}" />
									<c:choose>
									<c:when test="${not empty domains[item.domain]}">
										<option value="${item.domain}" selected>${item.domain}</option>
									</c:when>
									<c:otherwise>
										<option value="${item.domain}">${item.domain}</option>
									</c:otherwise>
									</c:choose>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td colspan='2' style="text-align:center;"><input class='btn btn-primary' id="addOrUpdateEdgeSubmit"
							type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
</a:config>
<script type="text/javascript">
		$(document).ready(function() {
			$('#projects_config').addClass('active open');
			$('#topologyProductLines').addClass('active');
			$("#domainSelect").select2({
				placeholder : "选择属于这个产品线的项目",
				allowClear : true
			});
			
			$('.chosen-select').chosen({allow_single_deselect:true}); 
			//resize the chosen on window resize
		
			$(window)
			.off('resize.chosen')
			.on('resize.chosen', function() {
				$('.chosen-select').each(function() {
					 var $this = $(this);
					 $this.next().css({'width': $this.parent().width()});
				})
			}).trigger('resize.chosen');
		
			$('#chosen-multiple-style').on('click', function(e){
				var target = $(e.target).find('input[type=radio]');
				var which = parseInt(target.val());
				if(which == 2) $('#form-field-select-4').addClass('tag-input-style');
				 else $('#form-field-select-4').removeClass('tag-input-style');
			});
			
		/* 	var initDomains = [];
			<c:forEach var="domain" items="${model.productLine.domains}">
				initDomains.push("${domain.key}");
				document.getElementById("${domain.key}").checked = true;
			</c:forEach>
			
			$("#domainSelect").val(initDomains).trigger("change"); */
			
		});
	</script>