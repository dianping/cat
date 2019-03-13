<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request"/>

<a:web_body>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
			<h3 class="text-center text-success">修改Web端URL的规则</h3>
			<form name="urlPatternUpdate" id="form" method="post" action="${model.pageUri}?op=urlPatternUpdateSubmit&key=${model.patternItem.name}&id=${model.patternItem.id}">
				<table style='width:100%' class='table table-striped table-condensed table-bordered table-hover'>
					<tr>
						<th width="10%">唯一ID</th>
						<td width="25%"><input type="text" class="input-xlarge"  name="patternItem.name" required value="${model.patternItem.name}"/></td>
						<td width="65%"><span class="text-danger">不能有特殊字符，仅限于英文字母和-</span></td>
					</tr>
					<tr>
						<th>所属组</th>
						<td><input type="text" class="input-xlarge"  name="patternItem.group" required value="${model.patternItem.group}"/></td>
						<td><span class="text-danger">不起作用，仅仅用作url的分组，用于展示分组</span></td>
					</tr>
					<tr>
						<th>Pattern名</th>
						<td><input type="text" class="input-xlarge"  name="patternItem.pattern" required value="${model.patternItem.pattern}"/></td>
						<td><span class="text-danger">仅支持完全匹配方式，确保和JS埋点一直，比如http://m.api.dianping.com/searchshop.api</span></td>
					</tr>
					<tr>
						<th>项目名</th>
						<td><input type="text" class="input-xlarge"  name="patternItem.domain" required value="${model.patternItem.domain}"/></td>
						<td><span class="text-danger">后续配置在这个规则上的告警，会根据此项目名查找需要发送告警的联系人信息(告警人信息来源CMDB)</span></td>
					</tr>
					<tr>
						<td style='text-align:center' colspan='3'><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form> 
</a:web_body>
<script type="text/javascript">
	$(document).ready(function() {
		$('#Web_config').addClass('active open');
		$('#urlPatterns').addClass('active');
	});
</script>