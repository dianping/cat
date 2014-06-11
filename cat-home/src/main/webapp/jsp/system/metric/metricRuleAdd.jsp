<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<form name="metricRuleAddSubmit" id="form" method="post" action="${model.pageUri}?op=metricRuleAddSubmit">
	<span class="text-center text-error" id="state">&nbsp;</span>
	<p class="text-center text-error"><strong>修改业务监控规则</strong></p>
	<input name="productLineName" value="${payload.productLineName}" type="hidden"/>
	<table class="table table-striped table-bordered table-condensed table-hover">
		<tr>
			<td><textarea name="content" style="width:auto" rows="20" cols="100">${model.metricItemConfigRule}</textarea></td>
		</tr>
		<tr>
			<td style="text-align:center">
				<input class='btn btn-primary' type="submit" name="submit" value="提交" />
				<input class='btn btn-danger' type="button" data-dismiss="modal" aria-hidden="true" value="取消" />
			</td>
		</tr>
	</table>
</form>