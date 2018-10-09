<%@page import="com.onelogin.saml2.Auth"%>
<%@ page import="com.onelogin.saml2.settings.Saml2Settings" %>
<%@ page import="java.util.List" %>
<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
</head>
<body>
	<%
		Auth auth = new Auth(request, response);
		if (request.getParameter("attrs") == null) {
			auth.login();
		} else {
			String x = request.getPathInfo();
//			auth.login("/java-saml-tookit-jspsample/attrs.jsp");
			auth.login("/attrs.jsp");
		}
	%>
</body>
</html>
