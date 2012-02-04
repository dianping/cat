<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.ebay.com/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.ip.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.ip.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.ip.Model" scope="request" />

<a:report title="Hot IP Report">

	<jsp:attribute name="domain">
    	hello
    </jsp:attribute>
	<jsp:attribute name="time">
		[ <a href="">-1d</a> ] [ <a href="">-2h</a> ] [ <a href="">-1h</a> ] [ <a href="">+1h</a> ] [ <a href="">+2h</a> ] [ <a href="">+1d</a> ]
    </jsp:attribute>

	<jsp:body>
		<pre>
		${model.reportInJson}
		</pre>
	</jsp:body>
	
</a:report>
