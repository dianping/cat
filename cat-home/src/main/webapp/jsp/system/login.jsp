<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.login.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.login.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.login.Model" scope="request"/>

<form name="login" method="post" action="${model.pageUri}">
	<input type="hidden" name="rtnUrl" value="${payload.rtnUrl}" />
	<table border="0">
		<tr>
			<td>Account:</td>
			<td><input type="text" name="account" value="${payload.account}" /></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input type="password" name="password" /></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><input type="submit" name="login" value="Login" /></td>
		</tr>
	</table>
</form>