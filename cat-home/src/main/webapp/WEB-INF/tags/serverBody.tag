<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try{ace.settings.check('main-container', 'fixed')}catch(e){}
			
			$(document).ready(function() {
				$("#nav_server").addClass("disabled");
			});
		</script>
		<div id="sidebar" class="sidebar responsive">
			<script type="text/javascript">
				try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
			</script>
			<ul class="nav nav-list" style="top: 0px;">
				<li id="serverChart" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-desktop"></i> <span class="menu-text">Servers</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="view"><a href="/cat/r/server?domain=${model.domain}&op=view">
							<i class="menu-icon fa fa-caret-right"></i>指标查看</a>
							<b class="arrow"></b>
						</li>
						<li id="serverAggregate"><a href="/cat/r/server?domain=${model.domain}&op=aggregate">
							<i class="menu-icon fa fa-caret-right"></i>指标聚合</a>
							<b class="arrow"></b>
						</li>
						<li id="serverScreen"><a href="/cat/r/server?domain=${model.domain}&op=screen">
							<i class="menu-icon fa fa-caret-right"></i>系统大盘</a>
							<b class="arrow"></b>
						</li>
						<li id="serverGraph"><a href="/cat/r/server?domain=${model.domain}&graphId=${payload.graphId}&op=graph">
							<i class="menu-icon fa fa-caret-right"></i>系统指标</a>
							<b class="arrow"></b>
						</li>
						<li id="networkTopology"><a href="/cat/r/network?op=dashboard&domain=cat">
							<i class="menu-icon fa fa-caret-right"></i>网络大盘</a>
							<b class="arrow"></b>
						</li>
					</ul>
				</li>
				<li id="serverConfig" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-cogs"></i> <span class="menu-text">Config</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="serverScreens"><a href="/cat/r/server?op=screens&domain=${model.domain}">
							<i class="menu-icon fa fa-caret-right"></i>Screen配置</a>
							<b class="arrow"></b>
						</li>
						<li id="serverMetric"><a href="/cat/r/server?op=serverMetricConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>指标模板配置</a>
								<b class="arrow"></b>
						</li>
						<li id="influx"><a href="/cat/r/server?op=influxConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>influxDB配置</a>
								<b class="arrow"></b>
						</li>
						<li id="netGraphConfigUpdate"><a href="/cat/r/server?op=netGraphConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>网络拓扑配置</a>
								<b class="arrow"></b>
						</li>
						<li id="server_network"><a href="/cat/r/server?op=serverAlarmRule&type=network">
								<i class="menu-icon fa fa-caret-right"></i>网络告警规则</a>
								<b class="arrow"></b>
						</li>
						<li id="server_system"><a href="/cat/r/server?op=serverAlarmRule&type=system">
								<i class="menu-icon fa fa-caret-right"></i>系统告警规则</a>
								<b class="arrow"></b>
						</li>
						<li id="server_database"><a href="/cat/r/server?op=serverAlarmRule&type=database">
								<i class="menu-icon fa fa-caret-right"></i>数据库告警规则</a>
								<b class="arrow"></b>
						</li>
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
</a:base>
