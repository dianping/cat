<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.login.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.login.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.login.Model" scope="request"/>

<res:bean id="res" />
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
<res:useCss value='${res.css.local.body_css}' target="head-css" />
<res:useCss value='${res.css.local.tiny_css}' media="screen and (max-width: 1050px)"  target="head-css" />
<res:useCss value='${res.css.local.large_css}' media="screen and (min-width: 1050px)"  target="head-css" />

<style>
.form-horizontal .control-label{
	width:50px;
}
.form-horizontal .controls{
	margin-left:70px;
}
</style>
		<form class="form-horizontal" name="login" id="form" method="post" action="/cat/s/login">
			<h4 id="myModalLabel" class="text-success">用户登录</h4>
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
			<div class="control-group">
			    <div class="controls">
			      <input type="submit" class="btn btn-primary" name="login" value="登录" />
			    </div>
			  </div>
		</form>
	</div>
