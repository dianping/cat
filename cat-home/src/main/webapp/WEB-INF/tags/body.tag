<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="navBar"
	class="com.dianping.cat.report.view.NavigationBar" scope="page" />
<res:bean id="res" />
<html>
<head>
<title>CAT - ${model.page.description}</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<res:cssSlot id="head-css" />
<res:jsSlot id="head-js" />
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
<res:useJs value="${res.js.local['highcharts.js']}" target="head-js" />
<res:useCss value='${res.css.local.body_css}' target="head-css" />
<res:useCss value='${res.css.local.tiny_css}' media="screen and (max-width: 1050px)"  target="head-css" />
<res:useCss value='${res.css.local.large_css}' media="screen and (min-width: 1050px)"  target="head-css" />
</head>
<body>
	<div class="navbar navbar-inverse">
      <div class="navbar-inner">
        <div class="container-fluid">
       	  <a class="brand" href="/cat/r/home?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">CAT</a>
          <div class="nav-collapse collapse">
          	<div class="nav  pull-right">
          		<c:forEach var="page" items="${navBar.systemPages}">
					<c:if test="${page.standalone}">
						<li ${model.page.name == page.name ? 'class="active"' : ''}><a
							href="${model.webapp}/${page.moduleName}/${page.path}">${page.title}</a></li>
					</c:if>
					<c:if
						test="${not page.standalone and model.page.name == page.name}">
						<li class="active"><a href="#">${page.title}</a></li>
					</c:if>
				</c:forEach>
					<li id="loginInfo" ></li>
          	</div>
            <ul class="nav">
            	<c:forEach var="page" items="${navBar.visiblePages}">
					<c:if test="${page.standalone}">
						<li ${model.page.name == page.name ? 'class="active"' : ''}><a
							href="${model.webapp}/${page.moduleName}/${page.path}?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">${page.title}</a></li>
					</c:if>
					<c:if
						test="${not page.standalone and model.page.name == page.name}">
						<li class="active"><a href="#">${page.title}</a></li>
					</c:if>
				</c:forEach>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
	<div id="loginModal" class="modal hide fade" tabindex="-1" role="dialog"
		aria-labelledby="myModalLabel" aria-hidden="true" style="width:380px">
		<form class="form-horizontal" name="login" method="post" action="/cat/s/login">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">×</button>
				<h4 id="myModalLabel" class="text-success text-center">用户登录</h4>
			</div>
			<div class="control-group">
				<label class="control-label text-success" for="account">用户名</label>
				<div class="controls">
					<input type="text" name="account" id="account" style="height:auto" class="input-xlarge"
						placeholder="工号或邮箱（例如:2000或yong.you）" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label text-success" for="password">密码</label>
				<div class="controls">
					<input type="password" name="password" id="password" style="height:auto" class="input-xlarge"
						placeholder="sys后台密码" />
				</div>
			</div>
			<div class="modal-footer">
				<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
				<input type="submit" class="btn btn-primary" name="login" value="登录" />
			</div>
		</form>
	</div>
	<script>
		function getcookie(objname) {
			var arrstr = document.cookie.split("; ");
			for ( var i = 0; i < arrstr.length; i++) {
				var temp = arrstr[i].split("=");
				if (temp[0] == objname) {
					return temp[1];
				}
			}
			return "";
		}
		function showDomain() {
			var b = $('#switch').html();
			if (b == '切换') {
				$('.domainNavbar').slideDown();
				$('#switch').html("收起");
			} else {
				$('.domainNavbar').slideUp();
				$('#switch').html("切换");
			}
		}
		function showFrequent(){
			var b = $('#frequent').html();
			if (b == '常用') {
				$('.frequentNavbar').slideDown();
				$('#frequent').html("收起");
			} else {
				$('.frequentNavbar').slideUp();
				$('#frequent').html("常用");
			}
		}
		$(document).ready(function() {
			var ct = getcookie("ct");
			if (ct != "") {
				var length = ct.length;
				var realName = ct.split("|");
				var temp = realName[0];
				
				if(temp.charAt(0)=='"'){
					temp =temp.substring(1,temp.length);
				}
				var name = decodeURI(temp);
				var loginInfo=document.getElementById('loginInfo');
				loginInfo.innerHTML ='<a href="/cat/s/login?op=logout">'+name +'&nbsp;登出</a>';
			}else{
				var loginInfo=document.getElementById('loginInfo');
				loginInfo.innerHTML ='<a href="#loginModal" data-toggle="modal">登陆</a>';
			}
		});
	</script>
	<jsp:doBody />
	<table class="footer" style="margin-top:5px;">
		<tr><td>©2003-2014 dianping.com, All Rights Reserved.</td></tr>
	</table>
	<res:jsSlot id="bottom-js" />
</body>
</html>
