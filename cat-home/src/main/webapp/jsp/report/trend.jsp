<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.trend.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.trend.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.trend.Model" scope="request"/>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<res:bean id="res"/>

<html>
<head>
    <style type="text/css">
      body {
        margin: 0px;
        padding: 0px;
      }
      #testGraph {
        width : 600px;
        height: 384px;
        margin: 8px auto;
      }
    </style>
  </head>
	<body>
		<div id ='testGraph'></div>
	</body>
<script>
	var data=${model.graph};
</script>
    <res:useJs value="${res.js.local.flotr2_js}" target="bottom-js" />
	<res:useJs value="${res.js.local.trend_js}" target="bottom-js" />
</html>