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
<res:useCss value='${res.css.local.body_css}' target="head-css" />
</head>
<body>
	<div class="navbar navbar-inverse">
      <div class="navbar-inner">
        <div class="container-fluid">
          <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="brand" href="#">CAT</a>
          <div class="nav-collapse collapse">
            <p id="loginInfo" class="navbar-text pull-right">
            </p>
            <ul class="nav">
            	<c:forEach var="page" items="${navBar.visiblePages}">
					<c:if test="${page.standalone}">
						<li ${model.page.name == page.name ? 'class="active"' : ''}><a
							href="${model.webapp}/${page.moduleName}/${page.path}?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">${page.title}</a></li>
					</c:if>
					<c:if
						test="${not page.standalone and model.page.name == page.name}">
						<li class="active">${page.title}</li>
					</c:if>
				</c:forEach>
				<li>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</li>
				<c:forEach var="page" items="${navBar.systemPages}">
					<c:if test="${page.standalone}">
						<li ${model.page.name == page.name ? 'class="active"' : ''}><a
							href="${model.webapp}/${page.moduleName}/${page.path}">${page.title}</a></li>
					</c:if>
					<c:if
						test="${not page.standalone and model.page.name == page.name}">
						<li class="active">${page.title}</li>
					</c:if>
				</c:forEach>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
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
			loginInfo.innerHTML =name +"&nbsp;&nbsp;"+ '<a class="btn btn-small" href="/cat/s/login?op=logout">Logout</a>';
		}else{
			var loginInfo=document.getElementById('loginInfo');
			loginInfo.innerHTML ='<a  class="btn btn-small" href="/cat/s/login"> Login</a>';
		}
	</script>
	<jsp:doBody />
	<table class="footer">
		<tr><td>[ @dianping CAT ] 分机【1810】</td></tr>
	</table>
	<res:jsSlot id="bottom-js" />
</body>
</html>
