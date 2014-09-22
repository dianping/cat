<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request" />

<form>
	<input id="ruleId" type="hidden" value="${model.id}" />
	${model.content}
</form>