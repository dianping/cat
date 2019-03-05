<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#nav_document").addClass("disabled");
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
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="indexButton" >
						<a href="/cat/r/home?op=view&docName=index">
							<i class="menu-icon glyphicon glyphicon-home"></i>
							<span class="menu-text">项目首页</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="releaseButton" >
						<a href="/cat/r/home?op=view&docName=release">
							<i class="menu-icon glyphicon glyphicon-book"></i>
							<span class="menu-text">版本说明</span>
						</a>
						<b class="arrow"></b>
					</li>
					<%--<li id="deployButton" >--%>
						<%--<a href="/cat/r/home?op=view&docName=deploy">--%>
							<%--<i class="menu-icon fa fa-cogs"></i>--%>
							<%--<span class="menu-text">部署文档</span>--%>
						<%--</a>--%>
						<%--<b class="arrow"></b>--%>
					<%--</li><li id="integrationButton" >--%>
						<%--<a href="/cat/r/home?op=view&docName=integration">--%>
							<%--<i class="menu-icon fa fa-cutlery"></i>--%>
							<%--<span class="menu-text">集成文档</span>--%>
						<%--</a>--%>
						<%--<b class="arrow"></b>--%>
					<%--</li>--%>
					<%--<li id="userButton" >--%>
						<%--<a href="/cat/r/home?op=view&docName=user">--%>
							<%--<i class="menu-icon fa fa-users"></i>--%>
							<%--<span class="menu-text">应用监控</span>--%>
						<%--</a>--%>
						<%--<b class="arrow"></b>--%>
					<%--</li>--%>
					<%-- <li id="mobileMonitorButton" >
						<a href="/cat/r/home?op=view&docName=mobileMonitor">
							<i class="menu-icon glyphicon glyphicon-phone"></i>
							<span class="menu-text">移动监控</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="browserMonitorButton">
						<a href="/cat/r/home?op=view&docName=browserMonitor">
							<i class="menu-icon fa fa-globe"></i>
							<span class="menu-text">浏览器监控</span></a>
							<b class="arrow"></b></li>
					<li id="serverMonitorButton">
						<a href="/cat/r/home?op=view&docName=serverMonitor">
							<i class="menu-icon fa fa-desktop"></i>
							<span class="menu-text">服务端监控</span></a>
							<b class="arrow"></b></li> --%>
					<%--<li id="alertButton" >--%>
						<%--<a href="/cat/r/home?op=view&docName=alert">--%>
							<%--<i class="menu-icon fa fa-bell"></i>--%>
							<%--<span class="menu-text">告警文档</span>--%>
						<%--</a>--%>
						<%--<b class="arrow"></b>--%>
					<%--</li>--%>
						<%-- <li id="interfaceButton" >
						<a href="/cat/r/home?op=view&docName=interface">
							<i class="menu-icon glyphicon glyphicon-align-left"></i>
							<span class="menu-text">接口文档</span>
						</a>
						<b class="arrow"></b>
					</li> --%>
					<%--<li id="developButton" >--%>
						<%--<a href="/cat/r/home?op=view&docName=develop">--%>
							<%--<i class="menu-icon glyphicon glyphicon-refresh"></i>--%>
							<%--<span class="menu-text">开发者文档</span>--%>
						<%--</a>--%>
						<%--<b class="arrow"></b>--%>
					<%--</li>--%>
					<%--<li id="problemButton" >--%>
						<%--<a href="/cat/r/home?op=view&docName=problem">--%>
							<%--<i class="menu-icon fa fa-inbox"></i>--%>
							<%--<span class="menu-text">常见问题</span>--%>
						<%--</a>--%>
						<%--<b class="arrow"></b>--%>
					<%--</li>--%>
					<li id="pluginButton" >
						<a href="/cat/r/home?op=view&docName=plugin">
							<i class="menu-icon fa fa-key"></i>
							<span class="menu-text">插件扩展</span>
						</a>
						<b class="arrow"></b>
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
				
			<!-- /section:basics/sidebar -->
			<div class="main-content">
 				<div style="padding-top:2px;padding-right:8px;">
 				<jsp:doBody/>
 				</div>
			</div><!-- /.main-content -->
		</div></a:base>
