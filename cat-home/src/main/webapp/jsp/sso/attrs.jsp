<%@page import="com.onelogin.saml2.Auth"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.lang3.StringUtils" %>
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
	<%
		Boolean found = false;
		@SuppressWarnings("unchecked")
		Enumeration<String> elems = (Enumeration<String>) session.getAttributeNames();	
	
		while (elems.hasMoreElements() && !found) {
			String value = (String) elems.nextElement();
			if (value.equals("attributes") || value.equals("nameId")) {
				found = true;
			}
		}
	
		if (found) {
			String nameId = (String) session.getAttribute("nameId");
			@SuppressWarnings("unchecked")
			Map<String, List<String>> attributes = (Map<String, List<String>>) session.getAttribute("attributes");

			if (!nameId.isEmpty()) {
				out.println("<div><b> NameId:</b> " + nameId + "</div>");
			}
			
			if (attributes.isEmpty()) {
			%>
				<div class="alert alert-danger" role="alert">You don't have any attributes</div>
			<%							
			}
			else {
    		%>
    			<div><b>Attributes:</b></div>
        		<table class="table table-striped">
      				<thead>
      					<tr>
        					<th>Name</th>
        					<th>Values</th>
        				</tr>
      				</thead>
      				<tbody>
    		<%				
				Collection<String> keys = attributes.keySet();
				for(String name :keys){
					out.println("<tr><td>" + name + "</td><td>");
					List<String> values = attributes.get(name);
					for(String value :values) {
						out.println("<li>" + value + "</li>");
					}
					
					out.println("</td></tr>");
				}
			%>
					</tbody>
				</table>
			<%
			}

			out.println("<a href=\"dologout.jsp\" class=\"btn btn-primary\">Logout</a>");
		} else {
			out.println("<div class=\"alert alert-danger\" role=\"alert\">Not authenticated</div>");
			out.println("<a href=\"dologin.jsp\" class=\"btn btn-primary\">Login</a>");
		}
	%>
	</div>
</body>
</html>

