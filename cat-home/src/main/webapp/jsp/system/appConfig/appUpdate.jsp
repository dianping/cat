<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>
名称： <input name="name" value="${model.updateCommand.name}" id="commandName"/><br/>
项目： <input name="domain" value="${model.updateCommand.domain}" id="commandDomain"/><br/>
标题： <input name="title" value="${model.updateCommand.title}" id="commandTitle"/><br/>
<c:if test="${not empty payload.id}">
	<input name="id" value="${payload.id}" id="commandId" style="display:none"/>
</c:if>