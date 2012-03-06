<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.problem.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.problem.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.problem.Model" scope="request"/>
<c:set var="report" value="${model.report}"/>

<res:useCss value="${res.css.local.problem_css}" target="head-css"/>

<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<res:useJs value="${res.js.local.problem_js}" target="head-js"/>

<a:report title="Problem Report" timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>
Hello, Problem
</jsp:body>

</a:report>