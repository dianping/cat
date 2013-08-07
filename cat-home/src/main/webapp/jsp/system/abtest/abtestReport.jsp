<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />

<style>
<!--
#content{
    width:1200px;
    margin:0 auto;
}
-->
</style>

<a:body>
	<div id="content" lass="row-fluid">
		<div class="span12 column">
			<h3>Report <small>${model.abtest.name} #${model.abtest.id}</small></h3>
			<ul class="nav nav-tabs">
				<li class="active">
					<a href="?op=report&id=${payload.id }">
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
					<a href="?op=detail&id=${payload.id}">
						<img style="vertical-align: text-bottom;" height="15" width="15" src="${res.img.local['settings_black_small.png']}">
						View/ Edit ABTest Details
					</a>
				</li>
			</ul>
		</div>
	</div>
   
    <div style="width:600px;margin: 0 auto;">
        <h3 style="color:#1D8BE0;"> Report function is under construction...</h3>
       <img style="margin: 0 auto;" src="${res.img.local['wangzhanjianshezhong.jpg']}">
    </div>
   
</a:body>