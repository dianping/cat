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
				<table style='width:60%' class='table table-striped table-bordered'>
					<tr>
						<td>唯一ID</td>
						<td><input type="text" class="input-xlarge"  name="patternItem.name" required value="${model.patternItem.name}"/></td>
					</tr>
					<tr>
						<td>所属组</td>
						<td><input type="text" class="input-xlarge"  name="patternItem.group" required value="${model.patternItem.group}"/></td>
					</tr>
					<tr>
						<td>pattern</td>
						<td><input type="text" class="input-xlarge"  name="patternItem.pattern" required value="${model.patternItem.pattern}"/></td>
					</tr>
					<tr>
						<td style='text-align:center' colspan='2'><input class='btn btn-primary' type="submit" name="submit" value="submit" /></td>
					</tr>
				</table>
			</form> </div></div></div>
</a:body>
<script type="text/javascript">
	$(document).ready(function() {
		$('#urlPatternList').addClass('active');
	});
</script>