<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.web.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.web.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.web.Model" scope="request" />
${model.json}