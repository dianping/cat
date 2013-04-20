<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model"
	scope="request" />

<a:body>
	<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />


	<div class="row-fluid">
		<div class="span12 column">
			<div class="page-header">
				<h1>
					Report <small>#1001</small>
				</h1>
			</div>
			<ul class="nav nav-tabs">
				<li class="active">
					<a href="#">
						<img style="vertical-align: text-bottom;" height="15" width="15" src="${res.img.local['star_black_small.png']}">
						Summary
					</a>
				</li>
				<li>
					<a href="#detail">
						<img style="vertical-align: text-bottom;" height="15" width="15" src="${res.img.local['details_black_small.png']}">
						Detail Report
					</a>
				</li>
				<li>
					<a href="#ve">
						<img style="vertical-align: text-bottom;" height="15" width="15" src="${res.img.local['settings_black_small.png']}">
						View/ Edit ABTest Details
					</a>
				</li>
			</ul>
		</div>
	</div>
</a:body>