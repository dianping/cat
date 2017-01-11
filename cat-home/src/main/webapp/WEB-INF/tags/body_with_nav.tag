<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base_with_nav>
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try{ace.settings.check('main-container' , 'fixed')}catch(e){}
		</script>
		<div id="sidebar" class="sidebar   responsive">
			<script type="text/javascript">
				try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
			</script>
			<div class="sidebar-shortcuts" id="sidebar-shortcuts">
				<div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large">
					<button class="btn btn-success" id="tab_realtime">
						<i class="ace-icon fa fa-signal"></i>&nbsp;&nbsp;实时
					</button>
					<!-- #section:basics/sidebar.layout.shortcuts -->
					<button class="btn btn-warning" id="tab_document">
						<i class="ace-icon fa fa-users"></i>&nbsp;&nbsp;文档
					</button>
				</div>
				<div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini">
					<span class="btn btn-success"></span>
					<span class="btn btn-info"></span>
					<span class="btn btn-warning"></span>
					<span class="btn btn-danger"></span>
				</div>
			</div>
			<ul class="nav nav-list" style="top: 0px;">
				<li id="Dashboard_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-tachometer"></i> <span class="menu-text">Dashboard</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="dashbord_system"><a href="/cat/r/top?op=view&domain=${model.domain}">
							<i class="menu-icon fa fa-caret-right"></i>报错大盘</a>
							<b class="arrow"></b></li>
						<li id="dashbord_metric"><a href="/cat/r/metric?op=dashboard&domain=${model.domain}">
							<i class="menu-icon fa fa-caret-right"></i>业务大盘</a>
							<b class="arrow"></b></li>
						<li id="dashbord_database"><a href="/cat/r/storage?op=dashboard&domain=${model.domain}">
							<i class="menu-icon fa fa-caret-right"></i>数据库大盘</a>
							<b class="arrow"></b></li>
						<li id="dashbord_cache"><a href="/cat/r/storage?op=dashboard&domain=${model.domain}&type=Cache">
							<i class="menu-icon fa fa-caret-right"></i>缓存大盘</a>
							<b class="arrow"></b></li>
					</ul>
				</li>
				<li id="Transaction_report" >
					<a href="/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon glyphicon glyphicon-time"></i>
						<span class="menu-text">Transaction</span>
					</a>
				</li>
				<li id="Event_report" >
					<a href="/cat/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon fa fa-flag"></i>
						<span class="menu-text">Event</span>
					</a>
				</li>					
				<li id="Problem_report" >
					<a href="/cat/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon fa fa-bug"></i>
						<span class="menu-text">Problem</span>
					</a>
				</li>			
				<li id="Heartbeat_report" >
					<a href="/cat/r/h?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon  fa fa-heart"></i>
						<span class="menu-text">Heartbeat</span>
					</a>
				</li>		
				<li id="Cross_report" >
					<a href="/cat/r/cross?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon  glyphicon glyphicon-random"></i>
						<span class="menu-text">Cross</span>
					</a>
				</li>
				<li id="Cache_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-flash"></i> <span class="menu-text">Cache</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="cache_operation"><a href="/cat/r/storage?id=memcached&type=Cache&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">
							<i class="menu-icon fa fa-caret-right"></i>访问趋势</a>
							<b class="arrow"></b></li>
						<li id="cache_info"><a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=view">
							<i class="menu-icon fa fa-caret-right"></i>访问情况</a>
							<b class="arrow"></b></li>
					</ul>
				</li>
				<li id="Database_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-lemon-o"></i> <span class="menu-text">Database</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="database_operation"><a href="/cat/r/storage?id=cat&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">
							<i class="menu-icon fa fa-caret-right"></i>访问趋势</a>
							<b class="arrow"></b></li>
						<li id="database_system"><a href="/cat/r/database?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=view">
							<i class="menu-icon fa fa-caret-right"></i>系统指标</a>
							<b class="arrow"></b></li>
					</ul>
				</li>
				<li id="State_report" >
					<a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon fa fa-bar-chart-o"></i>
						<span class="menu-text">State</span>
					</a>
				</li>
				</ul>
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
		<div class="main-content">
				<div id="dialog-message" class="hide">
				<p>
					你确定要删除吗？(不可恢复)
				</p>
			</div>
				<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<jsp:doBody/>
				</div>
		</div>
	</div>
</a:base_with_nav>
<script  type="text/javascript">
	$(document).ready(function() {
		$("#tab_realtime").click(function(){
			window.location.href = "/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
		});
		$("#tab_offtime").click(function(){
			window.location.href = "/cat/r/statistics?op=service&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
		});
		$("#tab_document").click(function(){
			window.location.href = "/cat/r/home?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
		});
		$("#tab_config").click(function(){
			window.location.href = "/cat/s/config?op=projects";
		});});
		$("#tab_realtime").addClass("disabled");

</script>

