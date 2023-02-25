<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.logview.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.logview.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.logview.Model" scope="request"/>

<a:application>

<res:useCss value="${res.css.local.logview_css}" target="head-css"/>
<res:useJs value="${res.js.local.logview_js}" target="head-js"/>

<c:choose>
	<c:when test="${empty model.table}">
		<div class="error">抱歉，消息可能是丢失或者已存档。</div>
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="${payload.waterfall=='true'}">
				<div>&nbsp;&nbsp;<a href="?domain=${model.domain}&waterfall=false">列表</a>&nbsp;&nbsp;&nbsp;&nbsp;时序图</div>
			</c:when>
			<c:otherwise>
				<div>&nbsp;&nbsp;列表&nbsp;&nbsp;&nbsp;&nbsp;<a href="?domain=${model.domain}&waterfall=true">时序图</a></div>
			</c:otherwise>
		</c:choose>
		${model.table}
	</c:otherwise>
</c:choose>

<br>

</a:application>