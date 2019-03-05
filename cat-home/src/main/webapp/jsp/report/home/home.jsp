<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.home.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.home.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.home.Model" scope="request"/>
<a:home>
	<div class="tab-content">
			<c:choose>
				<c:when test="${payload.docName == 'dianping'}">
	   		<%@ include file="index.jsp"%>
	   	</c:when>
	   	<c:when test="${payload.docName == 'release'}">
	   		<%@ include file="releasenotes.jsp"%>
	   	</c:when>
	   	<%--<c:when test="${payload.docName == 'deploy'}">--%>
	   		<%--<%@ include file="deploy.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'integration'}">--%>
	   		<%--<%@ include file="integrating.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'user'}">--%>
	   		<%--<%@ include file="application.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'mobileMonitor'}">--%>
	   		<%--<%@ include file="mobile.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'serverMonitor'}">--%>
	   		<%--<%@ include file="server/server.jsp"%>--%>
	   	<%--</c:when>--%>
	  	<%--<c:when test="${payload.docName == 'alert'}">--%>
	   		<%--<%@ include file="alert.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'interface'}">--%>
	   		<%--<%@ include file="interface.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'develop'}">--%>
	   		<%--<%@ include file="developDocument.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'design'}">--%>
	   		<%--<%@ include file="develop.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<%--<c:when test="${payload.docName == 'problem'}">--%>
	   		<%--<%@ include file="problem.jsp"%>--%>
	   	<%--</c:when>--%>
	   	<c:when test="${payload.docName == 'plugin'}">
	   		<%@ include file="plugin.jsp"%>
	   	</c:when>
	   	<c:when test="${payload.docName == 'browserMonitor'}">
	   		<%@ include file="browser.jsp"%>
	   	</c:when>
	   	<c:otherwise>
	   		<%@ include file="index.jsp"%>
	   	</c:otherwise>
	 		</c:choose>
	</div>
<br>
<br>
<a href="?op=checkpoint&domain=${model.domain}&date=${model.date}" style="color:#FFF">Do checkpoint here</a>
<script>
	var liElement = $('#${payload.docName}Button');
	if(liElement.size() == 0){
		liElement = $('#indexButton');
	}
	liElement.addClass('active');
</script>
</a:home>