<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
		</br>
			<form name="urlPatternUpdate" id="form" method="post" action="${model.pageUri}?op=urlPatternUpdateSubmit">
				<table style='width:80%' class='table table-striped table-bordered'>
					<tr>
						<th>唯一ID</th>
						<td><input type="text" class="input-xlarge"  name="patternItem.name" required value="${model.patternItem.name}"/></td>
						<td><span class="text-error">不能有特殊字符，仅限于英文字母和-</span></td>
					</tr>
					<tr>
						<th>所属组</th>
						<td><input type="text" class="input-xlarge"  name="patternItem.group" required value="${model.patternItem.group}"/></td>
						<td><span class="text-error">暂时不起作用，仅仅用作url的分组，用于展示目的</span></td>
					</tr>
					<tr>
						<th>所属组</th>
						<td><input type="text" class="input-xlarge"  name="patternItem.pattern" required value="${model.patternItem.pattern}"/></td>
						<td><span class="text-error">支付完全匹配方式，和部分匹配，比如 http://www.dianping.com/{City}/food，{City}可以匹配任何字符串</span></td>
					</tr>
					<tr>
						<td style='text-align:center' colspan='3'><input class='btn btn-primary' type="submit" name="submit" value="submit" /></td>
					</tr>
				</table>
			</form> </div></div></div>
</a:body>
<script type="text/javascript">
	$(document).ready(function() {
		$('#urlPatternList').addClass('active');
	});
</script>