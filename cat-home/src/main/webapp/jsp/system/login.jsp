<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.login.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.login.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.login.Model" scope="request"/>
<form name="login" id="form" method="post" action="${model.pageUri}">
	<input type="hidden" name="rtnUrl" value="${payload.rtnUrl}" />
	<table border="0">
		<tr>
			<td>Account:</td>
			<td><input type="text" name="account" value="${payload.account}" /></td>
			<td style="color:red;">工号（例如:2000）、公司邮箱前缀（例如: yong.you）</td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input type="password" name="password" /></td>
			<td style="color:red;">sys后台密码</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td colspan="2"><input type="submit" name="login" value="Login" /></td>
		</tr>
	</table>
</form>
