<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.nettopo.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.nettopo.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.nettopo.Model" scope="request"/>


<a:body>
<res:useJs value="${res.js.local['netgraph.js']}" target="head-js"/>

<style type="text/css">
    .topology {
    	width: 600px;
    	text-align: center;
    	margin: 20px 10px;
    	height: 700px;
    	float: left;
    }
    p{
    	font-size: 18px;
    	font-weight: bold;
    }
    #content {
    	width: 1240px;
    }
</style>

<div id="content">

<c:forEach var="topo" items="${model.netData}" varStatus="idx">
	<div class="topology">
		<p>${topo.key}</p>
		<div id="topo-${idx.index}">
		</div>
	</div>
</c:forEach>

</div>

<script type="text/javascript">

<c:forEach var="topo" items="${model.netData}" varStatus="idx">
	$_netgraph.build("topo-${idx.index}",${topo.value});
</c:forEach>

</script>

</a:body>