<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<div class="main-container" id="main-container">
			<script type="text/javascript">
				try{ace.settings.check('main-container' , 'fixed')}catch(e){}
			</script>
			<!-- #section:basics/sidebar -->
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
					<li id="deployButton" >
						<a href="/cat/r/home?op=view&docName=deploy">
							<i class="menu-icon fa fa-cogs"></i>
							<span class="menu-text">部署文档</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="userButton" >
						<a href="/cat/r/home?op=view&docName=user">
							<i class="menu-icon fa fa-users"></i>
							<span class="menu-text">用户文档</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="alertButton" >
						<a href="/cat/r/home?op=view&docName=alert">
							<i class="menu-icon fa fa-bell"></i>
							<span class="menu-text">告警文档</span>
						</a>
						<b class="arrow"></b>
					</li><li id="integrationButton" >
						<a href="/cat/r/home?op=view&docName=integration">
							<i class="menu-icon fa fa-cutlery"></i>
							<span class="menu-text">集成文档</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="developButton" >
						<a href="/cat/r/home?op=view&docName=develop">
							<i class="menu-icon glyphicon glyphicon-refresh"></i>
							<span class="menu-text">开发者文档</span>
						</a>
						<b class="arrow"></b>
					</li><li id="designButton" >
						<a href="/cat/r/home?op=view&docName=design">
							<i class="menu-icon fa fa-book"></i>
							<span class="menu-text">设计文档</span>
						</a>
						<b class="arrow"></b>
					</li><li id="problemButton" >
						<a href="/cat/r/home?op=view&docName=problem">
							<i class="menu-icon fa fa-inbox"></i>
							<span class="menu-text">常见问题</span>
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
		$("#tab_document").addClass("disabled");

</script>