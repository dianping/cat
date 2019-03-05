<%@page import="java.util.*,com.onelogin.saml2.Auth,com.onelogin.saml2.settings.Saml2Settings" language="java" contentType="application/xhtml+xml"%><%
Auth auth = new Auth();
Saml2Settings settings = auth.getSettings();
settings.setSPValidationOnly(true);
String metadata = settings.getSPMetadata();
List<String> errors = Saml2Settings.validateMetadata(metadata);
if (errors.isEmpty()) {
	System.out.println(metadata);
} else {
	response.setContentType("text/html; charset=UTF-8");

	for (String error : errors) {
	    System.out.println("<p>"+error+"</p>");
	}
}%>