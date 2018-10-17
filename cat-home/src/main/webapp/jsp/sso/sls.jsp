<%@page import="com.onelogin.saml2.Auth"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%> 
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	 <meta charset="utf-8">
	 <meta http-equiv="X-UA-Compatible" content="IE=edge">
     <meta name="viewport" content="width=device-width, initial-scale=1">
	 <title>A Java SAML Toolkit by OneLogin demo</title>
	 <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">

     <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
     <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
     <!--[if lt IE 9]>
       <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
       <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
     <![endif]-->
</head>
<body>
	<div class="container">
    	<h1>A Java SAML Toolkit by OneLogin demo</h1>
    	<b>Logout</b>   	
	<%
		Auth auth = new Auth(request, response);
		auth.processSLO();
		
		List<String> errors = auth.getErrors();
		
		if (errors.isEmpty()) {
			out.println("<p>Sucessfully logged out</p>");
			out.println("<a href=\"dologin.jsp\" class=\"btn btn-primary\">Login</a>");
		} else {
			out.println("<p>");
			for(String error : errors) {
				out.println(" " + error + ".");
			}
			out.println("</p>");
		}
	%>
	</div>
</body>
</html>
