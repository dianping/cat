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
			<form name="topologyGraphEdgeConfigAddSumbit" id="form" method="post" action="${model.pageUri}">
				<h4 class="text-center text-danger">修改产品线配置信息</h4>
				<input type="hidden" name="op" value="topologyProductLineAddSubmit" />
				<table class="table table-striped table-condensed table-border table-hover ">
					<tr>
						<td style="width:20%;text-align: right" class="text-success">产品线名称（全英文）</td>
						<td><input type="text" name="productLine.id" value="${model.productLine.id}" required />
							<input name="type" value="${payload.type}" type="hidden"/>	
						</td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">产品线标题（中文）</td>
						<td><input type="text" name="productLine.title" value="${model.productLine.title}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">产品线顺序（数字）</td>
						<td><input type="text" name="productLine.order" value="${model.productLine.order}" required /></td>
					</tr>
					<tr>
						<td style="text-align: right" class="text-success">选择产品线的项目</td>
						<td style="width:50%;">
							<select multiple class="chosen-select tag-input-style" id="domain_select" name="domains" 
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
						<td colspan='2' style="text-align:center;">
							<input class='btn btn-primary btn-sm' id="addOrUpdateEdgeSubmit" type="submit" name="submit" value="提交" />
						</td>
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
		});
</script>
<style>
.chosen-container-multi .chosen-choices li.search-choice .search-choice-close {
background:inherit;
}
</style>