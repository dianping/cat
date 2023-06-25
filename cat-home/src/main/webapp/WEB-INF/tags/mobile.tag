<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#nav_mobile").addClass("disabled");
		});
	</script>
	<div class="main-container" id="main-container">
			<script type="text/javascript">
				try{ace.settings.check('main-container' , 'fixed')}catch(e){}
			</script>
			<!-- #section:basics/sidebar -->
			<div id="sidebar" class="sidebar   responsive">
				<script type="text/javascript">
					try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
				</script>
				<ul class="nav nav-list" style="top: 0px;">
					<li id="Dashboard" class="hsub"><a href="/cat/r/app?op=dashboard">
						<i class="menu-icon fa fa-tachometer"></i>
						<span class="menu-text">监控大盘</span>
					</a>
					<b class="arrow"></b>
					</li>
					<li id="App_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-phone"></i> <span class="menu-text">Mobile</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="trend"><a href="/cat/r/app?op=view&showActivity=false&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>Api访问趋势</a>
								<b class="arrow"></b></li>
							<li id="accessPiechart"><a href="/cat/r/app?op=piechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>Api访问分布</a>
								<b class="arrow"></b></li>
							<li id="apiDaily" style="display:none"><a href="/cat/r/app?op=commandDaily">
								<i class="menu-icon fa fa-caret-right"></i>Api访问日报表</a>
								<b class="arrow"></b></li>
							<li id="speed"><a href="/cat/r/app?op=speed&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>App页面测速</a>
								<b class="arrow"></b></li>
							<li id="speedGraph"><a href="/cat/r/app?op=speedGraph">
								<i class="menu-icon fa fa-caret-right"></i>App测速分布</a>
								<b class="arrow"></b></li>
							<li id="appCrashLog"><a href="/cat/r/crash?op=appCrashLog">
								<i class="menu-icon fa fa-caret-right"></i>AppCrash日志</a>
								<b class="arrow"></b></li>
							<li id="appCrashTrend"><a href="/cat/r/crash?op=appCrashTrend">
								<i class="menu-icon fa fa-caret-right"></i>AppCrash趋势</a>
								<b class="arrow"></b></li>
							 <li id="crashStatistics"><a href="/cat/r/crash?op=crashStatistics">
								<i class="menu-icon fa fa-caret-right"></i>Crash日报表</a>
								<b class="arrow"></b></li>
							<li id="statistics"><a href="/cat/r/appstats?domain=${model.domain}&type=all">
								<i class="menu-icon fa fa-caret-right"></i>APP每日报表</a>
								<b class="arrow"></b></li>
							 <%--<li id="traceLog"><a href="http://tracer.cat.dp/" target="_blank">
								<i class="menu-icon fa fa-caret-right"></i>移动日志查询</a>
								<b class="arrow"></b></li>--%>
							<li id="appLog"><a href="/cat/r/applog?op=appLog">
								<i class="menu-icon fa fa-caret-right"></i>代码级日志</a>
								<b class="arrow"></b></li>
							<li id="connTrend"><a href="/cat/r/app?op=connLinechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>长连访问趋势</a>
								<b class="arrow"></b></li>
							<li id="connPiechart"><a href="/cat/r/app?op=connPiechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>长连访问分布</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
				</ul>

				<!-- #section:basics/sidebar.layout.minimize -->
				<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
					<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
				</div>

				<!-- /section:basics/sidebar.layout.minimize -->
				<script type="text/javascript">
					try{ace.settings.check('sidebar' , 'collapsed')}catch(e){}
				</script>
			</div>
			<!-- /section:basics/sidebar -->
			<div class="main-content">
 				<div id="dialog-message" class="hide">
					<p>
						你确定要删除吗？(不可恢复)
					</p>
				</div>
 				<div style="padding-top:2px;padding-right:8px;">
 				<jsp:doBody/>
 				</div>
			</div>
		</div></a:base>
		<script  type="text/javascript">
	$(document).ready(function() {
		$("#tab_realtime").click(function(){
			window.location.href = "/cat/r/t?";
		});
		$("#tab_offtime").click(function(){
			window.location.href = "/cat/r/statistics?op=service";
		});
		$("#tab_document").click(function(){
			window.location.href = "/cat/r/home?";
		});
		$("#tab_config").click(function(){
			window.location.href = "/cat/s/config?op=projects";
		});});
		$("#tab_config").addClass("disabled");
</script>
