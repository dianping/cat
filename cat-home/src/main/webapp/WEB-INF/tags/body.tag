<%@ tag trimDirectiveWhitespaces="true" %>
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
<res:useCss value='${res.css.local.body_css}' target="head-css" />
</head>
<body>
	<table id="login" width="100%">
		<tr>
			<td width="90%"></td>
			<td id="loginInfo" style="text-align:right"></td>
			<td width="2%">&nbsp;</td>
		</tr>
	</table>
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
			loginInfo.innerHTML =name +"&nbsp;&nbsp;"+ '<a href="/cat/s/login?op=logout">Logout</a>';
		}else{
			var loginInfo=document.getElementById('loginInfo');
			loginInfo.innerHTML ='<a href="/cat/s/login"> Login</a>';
		}
	</script>
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td width="80%"><ul class="tabs">
					<c:forEach var="page" items="${navBar.visiblePages}">
						<c:if test="${page.standalone}">
							<li ${model.page.name == page.name ? 'class="selected"' : ''}><a
								href="${model.webapp}/${page.moduleName}/${page.path}?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">${page.title}</a></li>
						</c:if>
						<c:if
							test="${not page.standalone and model.page.name == page.name}">
							<li class="selected">${page.title}</li>
						</c:if>
					</c:forEach>
				</ul></td>
			<td width="20%"><ul class="tabs">
					<c:forEach var="page" items="${navBar.systemPages}">
						<c:if test="${page.standalone}">
							<li ${model.page.name == page.name ? 'class="selected"' : ''}><a
								href="${model.webapp}/${page.moduleName}/${page.path}">${page.title}</a></li>
						</c:if>
						<c:if
							test="${not page.standalone and model.page.name == page.name}">
							<li class="selected">${page.title}</li>
						</c:if>
					</c:forEach>
				</ul></td>
		</tr>
	</table>
	<jsp:doBody />

	<res:jsSlot id="bottom-js" />
</body>
</html>
