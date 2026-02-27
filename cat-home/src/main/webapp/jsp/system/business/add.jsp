<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.business.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.business.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.business.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useCss value="${res.css.local['multiple-select.css']}" target="head-css" />
	<res:useJs value="${res.js.local['jquery.multiple.select.js']}" target="head-js" />
	
		<h4 class="text-success text-center">修改业务监控规则</h4>
		<form name="addSubmit" id="form" method="post" action="">
			<table class="table table-striped table-condensed  ">
				<tr>
					<td width="20%" style="text-align:right"  class="text-success">项目名称</td>
					<td width="20%" >
						<c:if test="${not empty payload.domain}">
							<input name="" value="${payload.domain}" readonly required/>
						</c:if>
					</td>
					<td width="25%" style="text-align:right" class="text-success">BusinessKey</td>
					<td width="35%" >
						<c:if test="${not empty model.businessItemConfig.id}">
							<input name="businessItemConfig.id" id="businessItemConfig_id" value="${model.businessItemConfig.id}" readonly required/>
						</c:if>
						<c:if test="${empty  model.businessItemConfig.id}">
							<input name="businessItemConfig.id" id="businessItemConfig_id" value="${model.businessItemConfig.id}" required/>
						</c:if>
					</td>
				</tr>
				<tr>
					<td  style="text-align:right" class="text-success">显示标题</td>
					<td ><input name="businessItemConfig.title" id="businessItemConfig_title" value="${model.businessItemConfig.title}" required/></td>
					<td  style="text-align:right" class="text-success">显示顺序（数字）</td>
					<td ><input name="businessItemConfig.viewOrder" id="businessItemConfig_viewOrder" value="${model.businessItemConfig.viewOrder}" required/></td>
				</tr>
				<tr>
					<td style="text-align:right" class="text-success">是否告警</td>
					<td >
						<c:choose>
							<c:when test="${model.businessItemConfig.alarm}">
								<input type="radio" name="businessItemConfig_alarm" value="true" checked />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_alarm" value="false" />否
							</c:when>
							<c:otherwise>
						    	<input type="radio" name="businessItemConfig_alarm" value="true" />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_alarm" value="false" checked />否
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align:right" class="text-success" >显示次数曲线</td>
					<td>
						<c:choose>
							<c:when test="${model.businessItemConfig.showCount}">
								<input type="radio" name="businessItemConfig_showCount" value="true" checked />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_showCount" value="false" />否
							</c:when>
							<c:otherwise>
						    	<input type="radio" name="businessItemConfig_showCount" value="true" />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_showCount" value="false" checked />否
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<tr>
					<td style="text-align:right" class="text-success" >显示平均曲线</td>
					<td>
						<c:choose>
							<c:when test="${model.businessItemConfig.showAvg}">
								<input type="radio" name="businessItemConfig_showAvg" value="true" checked />是	&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_showAvg" value="false" />否
							</c:when>
							<c:otherwise>
						    	<input type="radio" name="businessItemConfig_showAvg" value="true" />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_showAvg" value="false" checked />否
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align:right" class="text-success">显示求和曲线</td>
					<td>
						<c:choose>
							<c:when test="${model.businessItemConfig.showSum}">
								<input type="radio" name="businessItemConfig_showSum" value="true" checked />是	&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_showSum" value="false" />否
							</c:when>
							<c:otherwise>
						    	<input type="radio" name="businessItemConfig_showSum" value="true" />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_showSum" value="false" checked />否
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<tr>
				<td style="text-align:right" class="text-success">是否为敏感数据</td>
					<td colspan='3'>
						<c:choose>
							<c:when test="${model.businessItemConfig.privilege}">
								<input type="radio" name="businessItemConfig_privilege" value="true" checked />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_privilege" value="false" />否
							</c:when>
							<c:otherwise>
						    	<input type="radio" name="businessItemConfig_privilege" value="true" />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig_privilege" value="false" checked />否
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<tr>
					<td style="text-align:center" colspan='4'><input class='btn btn-primary btn-xs' id="addOrUpdateNodeSubmit" type="text" name="submit" value="提交" /></td>
				</tr>
			</table>
		</form>

	<script>
		$(document).ready(function(){
			$('#application_config').addClass('active open');
			$('#businessConfig').addClass('active');

			$(document).delegate("#addOrUpdateNodeSubmit","click",function(){
                window.location.href = "${model.pageUri}?op=addSubmit&domain=${payload.domain}"
                + "&businessItemConfig.id=" + encodeURIComponent($("#businessItemConfig_id").val())
                +  "&businessItemConfig.title=" + encodeURIComponent($("#businessItemConfig_title").val())
                +  "&businessItemConfig.viewOrder=" + $("#businessItemConfig_viewOrder").val()
                +  "&businessItemConfig.alarm=" + $('input:radio[name="businessItemConfig_alarm"]:checked').val()
                +  "&businessItemConfig.showCount=" + $('input:radio[name="businessItemConfig_showCount"]:checked').val()
                +  "&businessItemConfig.showAvg=" + $('input:radio[name="businessItemConfig_showAvg"]:checked').val()
                +  "&businessItemConfig.showSum=" + $('input:radio[name="businessItemConfig_showSum"]:checked').val()
                +  "&businessItemConfig.privilege=" + $('input:radio[name="businessItemConfig_privilege"]:checked').val()
                ;
			});
		});
	</script>
</a:config>