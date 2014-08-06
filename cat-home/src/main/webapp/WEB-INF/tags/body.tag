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
	<div class="navbar navbar-inverse">
      <div class="navbar-inner">
        <div class="container-fluid">
       	  <a class="brand" style="padding-right:20px" href="/cat/r/home?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">CAT</a>
          <div class="nav-collapse collapse">
          	<div class="nav pull-right">
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
            <ul class="nav">
         	  <li	class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown">Config<b class="caret"></b></a>
          		<ul class="dropdown-menu">
		           <li class='nav-header' style="margin-top:0px;">全局配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=projects">项目信息配置</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=topologyProductLines">项目分组配置</a></li>
		           <li class='nav-header' style="margin-top:0px;">应用监控配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=topologyGraphNodeConfigList">拓扑节点阀值</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=topologyGraphEdgeConfigList">拓扑依赖阀值</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=exception">异常告警配置</a></li>
		           <li class='nav-header' style="margin-top:0px;">外部监控配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=urlPatterns">URL合并规则</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=aggregations">JS合并规则</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=appConfigUpdate">APP监控配置</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=thirdPartyConfigUpdate">第三方监控配置</a></li>
		           <li class='nav-header' style="margin-top:0px;">业务监控配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=metricConfigList">业务监控规则</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=metricGroupConfigUpdate">业务指标分组</a></li>
		           <li class='nav-header' style="margin-top:0px;">网络监控配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=networkRuleConfigUpdate">网络告警配置</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=netGraphConfigUpdate">网络拓扑配置</a></li>
			       <li class='nav-header' style="margin-top:0px;">监控告警配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=systemRuleConfigUpdate">系统告警配置</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=alertDefaultReceivers">默认告警配置</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=alertPolicy">告警类型设置</a></li>
			       <li><a style="padding:1px 30px" href="?op=bugConfigUpdate">异常类型配置</a></li>
			       <li class='nav-header' style="margin-top:0px;">其他监控配置</li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=domainGroupConfigUpdate">机器分组配置</a></li>
			       <li><a style="padding:1px 30px" href="/cat/s/config?op=routerConfigUpdate">客户端路由配置</a></li>
			      </ul>
			     </li>
        	 </ul>
            <ul class="nav">
          		<li	class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown">Other<b class="caret"></b></a>
          			<ul class="dropdown-menu">
					<li class="nav-header">报表</li>
					<li><a style="padding:1px 30px" href="/cat/r/matrix?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">资源消耗统计</a></li>
					<li><a style="padding:1px 30px" href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=view">分BU统计异常</a></li>
				    <li><a style="padding:1px 30px" href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=alert">异常告警排行榜</a></li>
				  	<li><a style="padding:1px 30px" href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization">线上容量规划</a></li>
				  	<li><a style="padding:1px 30px" href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service">服务可用性排行</a></li>
				  	<li><a style="padding:1px 30px" href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy">重量级访问排行</a></li>
				    <li><a style="padding:1px 30px" href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=summary">告警智能分析</a></li>
					
					<li><a style="padding:1px 30px" href="/cat/s/alarm?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">报表订阅中心</a></li>
					<li class="nav-header">监控</li>
					<li><a style="padding:1px 30px" href="/cat/r/cdn?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">CDN监控</a></li>
					<li><a style="padding:1px 30px" href="/cat/r/network?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">网络监控</a></li>
					<li><a style="padding:1px 30px" href="/cat/r/system?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">PAAS系统监控</a></li>
					<li><a style="padding:1px 30px" href="/cat/r/alteration?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">线上变更监控</a></li>
	          		</ul>
          		</li>
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
				<h5 id="myModalLabel" class="text-success text-center">用户登录</h5>
			</div>
			<div class="control-group">
				<label class="control-label text-success" for="account">用户名</label>
				<div class="controls">
					<input type="text" name="account" id="account" style="height:auto" class="input-xlarge"
						placeholder="域账号（例如:yong.you）" />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label text-success" for="password">密码</label>
				<div class="controls">
					<input type="password" name="password" id="password" style="height:auto" class="input-xlarge"
						placeholder="域账号域名（例如:XXX）" />
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
