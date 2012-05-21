<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.heatmap.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.heatmap.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.heatmap.Model" scope="request" />

<script type="text/javascript">
var urlPrefix = "/cat/r/heatmap?domain=${model.domain}&date=${model.date}";
</script>

<script type="text/javascript" src="http://ditu.google.cn/maps/api/js?sensor=false&language=zh"></script>
<script type="text/javascript" src="/cat/js/heatmap/heatcanvas.js"></script>
<script type="text/javascript" src="/cat/js/heatmap/heatcanvas-googlemaps.js"></script>
<script type="text/javascript" src="/cat/js/heatmap/heatcanvas-worker.js"></script>
<script type="text/javascript" src="/cat/js/heatmap/heatSlider.js"></script>
<script type="text/javascript" src="/cat/js/heatmap/jquery-1.7.2.min.js"></script>
<script src="/cat/js/heatmap/gmap3.min.js"></script>
<script src="/cat/js/heatmap/jquery-autocomplete.min.js"></script>
<script src="/cat/js/heatmap.js"></script>

<link rel="stylesheet" href="/cat/css/jquery-autocomplete.css" />
<link rel="stylesheet" href="/cat/css/heatmap.css" />

<c:choose>
<c:when test="${model.display eq 'full'}">
    	<div class="wrapper">
			<div class="searchbox">&nbsp;&nbsp;地址: <input type="text" id="address" size="60"
			autocomplete="off">	&nbsp;&nbsp;热门城市：<span class="hotcities" id="hotcities"><a
			href="#" data-latlng="31.230393,121.473704">上海</a><a href="#"
			data-latlng="39.904214,116.40741300000002">北京</a><a href="#"
			data-latlng="30.274089,120.155069">杭州</a><a href="#"
			data-latlng="30.593087,114.30535699999996">武汉</a><a href="#"
			data-latlng="32.060255,118.796877">南京</a></span>
		</div>
			<img id="wait_logo" src="/cat/images/wait_logo.gif" width="100"
			height="100"></img>
	    	<div id="map"></div> 
	    </div>
	    <res:bean id="res"/>
	    <res:useJs value="${res.js.local.heatmapbutton_js}" target="bottom-js" />
</c:when>
<c:otherwise>

<a:report title="HeatMap Report" navUrlPrefix="domain=${model.domain}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<%-- 		
		<res:useJs value="${res.js.local.heatmap['heatcanvas.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap['heatcanvas-googlemaps.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap['heatcanvas-worker.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap['heatSlider.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap['jquery-1.7.2.min.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap['gmap3.min.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap['jquery-autocomplete.min.js']}" target="bottom-js" />
		<res:useJs value="${res.js.local.heatmap_js}" target="head-js" />
 --%>		
    	<div class="wrapper">
			<div class="searchbox">&nbsp;&nbsp;地址: <input type="text" id="address" size="60"
			autocomplete="off">	&nbsp;&nbsp;热门城市：<span class="hotcities" id="hotcities"><a
			href="#" data-latlng="31.230393,121.473704">上海</a><a href="#"
			data-latlng="39.904214,116.40741300000002">北京</a><a href="#"
			data-latlng="30.274089,120.155069">杭州</a><a href="#"
			data-latlng="30.593087,114.30535699999996">武汉</a><a href="#"
			data-latlng="32.060255,118.796877">南京</a></span>
		</div>
			<img id="wait_logo" src="/cat/images/wait_logo.gif" width="100"
			height="100"></img>
	    	<div id="map"></div> 
	    </div>   	
<br>

<res:useJs value="${res.js.local.heatmapbutton_js}" target="bottom-js" />
</jsp:body>
</a:report>
</c:otherwise>
</c:choose>