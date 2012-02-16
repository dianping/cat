<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.service.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.service.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.service.Model" scope="request"/>
<domains>${model.domains}</domains>
<ips>${model.ips}</ips>
<data>${model.xmlData}</data>