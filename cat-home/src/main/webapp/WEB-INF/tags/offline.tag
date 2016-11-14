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
						<button class="btn btn-danger" id="tab_config">
							<i class="ace-icon fa fa-cogs"></i>&nbsp;&nbsp;配置
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
					<li id="overload_report" >
						<a href="/cat/r/overload?domain=${model.domain}&op=${payload.action.name}">
							<i class="menu-icon  fa  fa-flask"></i>
							<span class="menu-text">报表容量统计</span>
						</a>
					</li>
				</ul>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="bug_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=view">
							<i class="menu-icon  fa fa-bar-chart-o"></i>
							<span class="menu-text">全局统计异常</span>
						</a>
					</li>
				</ul>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="service_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service">
							<i class="menu-icon glyphicon glyphicon-check"></i>
							<span class="menu-text">服务可用排行</span>
						</a>
					</li>
				</ul>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="utilization_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization">
							<i class="menu-icon  fa fa-glass"></i>
							<span class="menu-text">线上容量规划</span>
						</a>
					</li>
				</ul>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="jar_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=jar">
							<i class="menu-icon  fa fa-briefcase"></i>
							<span class="menu-text">线上JAR版本</span>
						</a>
					</li>
				</ul>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="heavy_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy">
							<i class="menu-icon  fa fa-circle"></i>
							<span class="menu-text">重量访问排行</span>
						</a>
					</li>
				</ul>
				<ul class="nav  nav-list" style="top: 0px;">
					<li id="summary_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=summary">
							<i class="menu-icon  fa fa-lightbulb-o"></i>
							<span class="menu-text">告警智能分析</span>
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
		$("#tab_offtime").addClass("disabled");

</script>