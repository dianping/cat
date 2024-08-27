<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#nav_config").addClass("disabled");
		});
	</script>
	<div class="main-container" style="overflow: hidden;width: 100%;height: 100%;" id="main-container">
			<script type="text/javascript">
				try{ace.settings.check('main-container' , 'fixed')}catch(e){}
			</script>
			<!-- #section:basics/sidebar -->
			<div id="sidebar" class="sidebar   responsive">
				<script type="text/javascript">
					try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
				</script>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="domain-config" class="hsub">
					<a href="/cat/s/config?op=projects" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-home"></i> <span class="menu-text">项目配置</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="projects"><a href="/cat/s/config?op=projects">
								<i class="menu-icon fa fa-caret-right"></i>项目基本信息</a>
								<b class="arrow"></b></li>
							<li id="domainGroupConfigUpdate"><a href="/cat/s/config?op=domainGroupConfigs">
								<i class="menu-icon fa fa-caret-right"></i>机器分组配置</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="system-config" class="hsub">
						<a href="/cat/s/config?op=networkRuleConfigList" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-cog"></i> <span class="menu-text">系统配置</span>
							<b class="arrow fa fa-angle-down"></b>
						</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="serverConfigUpdate"><a href="/cat/s/config?op=serverConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>服务端配置</a>
								<b class="arrow"></b></li>
							<li id="serverFilterUpdate"><a href="/cat/s/config?op=serverFilterConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>服务端过滤</a>
								<b class="arrow"></b></li>
							<li id="routerConfigUpdate"><a href="/cat/s/config?op=routerConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>客户端路由</a>
								<b class="arrow"></b></li>
							<li id="alertSenderConfig"><a href="/cat/s/config?op=alertSenderConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>告警渠道</a>
								<b class="arrow"></b></li>
							<li id="alertPolicy"><a href="/cat/s/config?op=alertPolicy">
								<i class="menu-icon fa fa-caret-right"></i>告警策略</a>
								<b class="arrow"></b></li>
							<li id="alertDefaultReceivers"><a href="/cat/s/config?op=alertDefaultReceivers">
								<i class="menu-icon fa fa-caret-right"></i>告警对象</a>
								<b class="arrow"></b></li>
							<li id="sampleConfigUpdate"><a href="/cat/s/config?op=sampleConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>消息采样</a>
								<b class="arrow"></b></li>
							<li id="reportReloadConfigUpdate"><a href="/cat/s/config?op=reportReloadConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>报表重载</a>
								<b class="arrow"></b></li>
							<li id="resourceUpdate"><a href="/cat/s/permission?op=resource">
								<i class="menu-icon fa fa-caret-right"></i>资源管理</a>
								<b class="arrow"></b></li>
							<li id="userUpdate"><a href="/cat/s/permission?op=user">
								<i class="menu-icon fa fa-caret-right"></i>用户管理</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="server-config" class="hsub">
					<a href="/cat/s/config?op=metricConfigList" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-cloud" style="top:3px"></i> <span class="menu-text">Server</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="transactionRule"><a href="/cat/s/config?op=transactionRule">
								<i class="menu-icon fa fa-caret-right"></i>Transaction告警</a>
								<b class="arrow"></b></li>
							<li id="eventRule"><a href="/cat/s/config?op=eventRule">
								<i class="menu-icon fa fa-caret-right"></i>Event告警</a>
								<b class="arrow"></b></li>
							<li id="exception"><a href="/cat/s/config?op=exception">
								<i class="menu-icon fa fa-caret-right"></i>Exception告警</a>
								<b class="arrow"></b></li>
							<li id="heartbeatRuleConfigList"><a href="/cat/s/config?op=heartbeatRuleConfigList">
								<i class="menu-icon fa fa-caret-right"></i>HeartBeat告警</a>
								<b class="arrow"></b></li>
							<%--<li id="thirdPartyConfigUpdate"><a href="/cat/s/config?op=thirdPartyRuleConfigs">
								<i class="menu-icon fa fa-caret-right"></i>Ping告警配置</a>
								<b class="arrow"></b></li>--%>
							<li id="storageDatabaseRule"><a href="/cat/s/config?op=storageRule&type=SQL">
								<i class="menu-icon fa fa-caret-right"></i>数据库访问告警</a>
								<b class="arrow"></b></li>
							<li id="storageCacheRule"><a href="/cat/s/config?op=storageRule&type=Cache">
								<i class="menu-icon fa fa-caret-right"></i>缓存访问告警</a>
								<b class="arrow"></b></li>
							<li id="storageRPCRule"><a href="/cat/s/config?op=storageRule&type=RPC">
								<i class="menu-icon fa fa-caret-right"></i>服务访问告警</a>
								<b class="arrow"></b></li>
							<li id="topoGraphFormatUpdate"><a href="/cat/s/config?op=topoGraphFormatUpdate">
								<i class="menu-icon fa fa-caret-right"></i>应用大盘配置</a>
								<b class="arrow"></b></li>
							<li id="topologyGraphNodeConfigList"><a href="/cat/s/config?op=topologyGraphNodeConfigList">
								<i class="menu-icon fa fa-caret-right"></i>应用大盘阈值</a>
								<b class="arrow"></b></li>
							<li id="topologyGraphEdgeConfigList"><a href="/cat/s/config?op=topologyGraphEdgeConfigList">
								<i class="menu-icon fa fa-caret-right"></i>应用依赖配置</a>
								<b class="arrow"></b></li>
							<li id="businessConfig"><a href="/cat/s/business?op=list">
								<i class="menu-icon fa fa-caret-right"></i>业务监控配置</a>
								<b class="arrow"></b></li>
							<li id="businessTag"><a href="/cat/s/business?op=tagConfig">
								<i class="menu-icon fa fa-caret-right"></i>业务标签配置</a>
								<b class="arrow"></b></li>
							<li id="displayPolicy"><a href="/cat/s/config?op=displayPolicy">
								<i class="menu-icon fa fa-caret-right"></i>HeartBeat视图</a>
								<b class="arrow"></b></li>
							 <li id="storageGroupConfigUpdate"><a href="/cat/s/config?op=storageGroupConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>存储分组配置</a>
								<b class="arrow"></b></li>
							 <li id="allReportConfig"><a href="/cat/s/config?op=allReportConfig">
								<i class="menu-icon fa fa-caret-right"></i>报表合并配置</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="browser-config" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-globe"></i> <span class="menu-text">Browser</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<%--<li id="urlPatternConfigUpdate"><a href="/cat/s/web?op=urlPatternConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>全局配置</a>
								<b class="arrow"></b></li>--%>
							<li id="code"><a href="/cat/s/web?op=codeList">
								<i class="menu-icon fa fa-caret-right"></i>返回码配置</a>
								<b class="arrow"></b></li>
							<li id="webConstants"><a href="/cat/s/web?op=webConstants">
								<i class="menu-icon fa fa-caret-right"></i>常量配置</a>
								<b class="arrow"></b></li>
							<li id="urlPatterns"><a href="/cat/s/web?op=urlPatterns">
								<i class="menu-icon fa fa-caret-right"></i>Ajax接口配置</a>
								<b class="arrow"></b></li>
							<li id="webRule"><a href="/cat/s/web?op=webRule">
								<i class="menu-icon fa fa-caret-right"></i>Ajax接口告警</a>
								<b class="arrow"></b></li>
							<li id="jsRule"><a href="/cat/s/web?op=jsRuleList">
								<i class="menu-icon fa fa-caret-right"></i>Js错误告警</a>
								<b class="arrow"></b></li>
							<li id="speed"><a href="/cat/s/web?op=speed">
								<i class="menu-icon fa fa-caret-right"></i>Web测速配置</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="userMonitor_config" class="hsub">
						<a href="#" class="dropdown-toggle"><i class="menu-icon glyphicon glyphicon-phone"></i><span class="menu-text">&nbsp;Mobile</span>
							<b class="arrow fa fa-angle-down"></b>
						</a><b class="arrow"></b>
						<ul class="submenu">
							<li id="appSources"><a href="/cat/s/app?op=appSources">
								<i class="menu-icon fa fa-caret-right"></i>AppId注册</a>
								<b class="arrow"></b></li>
							<li id="appList"><a href="/cat/s/app?op=appList">
								<i class="menu-icon fa fa-caret-right"></i>App命令字</a>
								<b class="arrow"></b></li>
							<li id="appCommandBatch"><a href="/cat/s/app?op=appCommandBatch">
								<i class="menu-icon fa fa-caret-right"></i>App批指令</a>
								<b class="arrow"></b></li>
							<li id="appCommandGroup"><a href="/cat/s/app?op=appCommandGroup">
								<i class="menu-icon fa fa-caret-right"></i>Api分组</a>
								<b class="arrow"></b></li>
							<li id="appCommandFormatConfig"><a href="/cat/s/app?op=appCommandFormatConfig">
								<i class="menu-icon fa fa-caret-right"></i>Api规则</a>
								<b class="arrow"></b></li>
							<li id="appCodes"><a href="/cat/s/app?op=appCodes">
								<i class="menu-icon fa fa-caret-right"></i>App返回码</a>
								<b class="arrow"></b></li>
							<li id="appSpeedList"><a href="/cat/s/app?op=appSpeedList">
								<i class="menu-icon fa fa-caret-right"></i>App测速</a>
								<b class="arrow"></b></li>
							<li id="appRule"><a href="/cat/s/app?op=appRule">
								<i class="menu-icon fa fa-caret-right"></i>App告警</a>
								<b class="arrow"></b>
							<li id="appConstants"><a href="/cat/s/app?op=appConstants">
								<i class="menu-icon fa fa-caret-right"></i>App常量</a>
								<b class="arrow"></b></li>
							<li id="appConfigUpdate"><a href="/cat/s/app?op=appConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>App全局</a>
								<b class="arrow"></b></li>
							<li id="brokerConfigUpdate"><a href="/cat/s/app?op=mobileConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>Mobile配置</a>
								<b class="arrow"></b></li>
							<li id="crashRule"><a href="/cat/s/app?op=crashRuleList">
								<i class="menu-icon fa fa-caret-right"></i>Crash告警</a>
								<b class="arrow"></b></li>
							<li id="crashLogConfigUpdate"><a href="/cat/s/app?op=crashLogConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>Crash配置</a>
								<b class="arrow"></b></li>
							<li id="sdkConfigUpdate"><a href="/cat/s/app?op=sdkConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>SDK配置</a>
								<b class="arrow"></b></li></li>
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
				<div id="rebuild-router-message" class="hide">
					<p>
						你确定吗？(不可恢复)
					</p>
				</div>
				<div style="padding-top:2px;padding-left:5px;padding-right:8px;overflow:auto;height: calc(100% - 69px);width:100%;">
 				<jsp:doBody/>
 				</div>
			</div>
		</div></a:base>
