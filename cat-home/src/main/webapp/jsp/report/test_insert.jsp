<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.test.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.test.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.test.Model" scope="request"/>
insert data : ${model.name}