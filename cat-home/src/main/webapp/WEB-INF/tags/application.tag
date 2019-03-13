<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#nav_application").addClass("disabled");
		});
	</script>
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try{ace.settings.check('main-container' , 'fixed')}catch(e){}
		</script>
		<div id="sidebar" class="sidebar   responsive">
			<script type="text/javascript">
				try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
			</script>
			<ul class="nav nav-list" style="top: 0px;">
				<%--<li id="Dashboard_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-tachometer"></i> <span class="menu-text">Dashboard</span>--%>
						<%--<b class="arrow fa fa-angle-down"></b>--%>
				<%--</a> <b class="arrow"></b>--%>
					<%--<ul class="submenu">--%>
						<%--<li id="dashbord_system"><a href="/cat/r/top?op=view&domain=${model.domain}">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>报错大盘</a>--%>
							<%--<b class="arrow"></b></li>--%>
						<%--<li id="dashbord_metric"><a href="/cat/r/business?name=业务大盘&type=tag">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>业务大盘</a>--%>
							<%--<b class="arrow"></b></li>--%>
						<%--&lt;%&ndash; <li id="dashbord_network"><a href="/cat/r/network?op=dashboard&domain=${model.domain}">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>网络大盘</a>--%>
							<%--<b class="arrow"></b></li>--%>
						<%--<li id="dashbord_rpc"><a href="/cat/r/storage?op=dashboard&domain=${model.domain}&type=RPC">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>服务大盘</a>--%>
							<%--<b class="arrow"></b></li>--%>
						<%--<li id="dashbord_database"><a href="/cat/r/storage?op=dashboard&domain=${model.domain}">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>数据库大盘</a>--%>
							<%--<b class="arrow"></b></li>--%>
						<%--<li id="dashbord_cache"><a href="/cat/r/storage?op=dashboard&domain=${model.domain}&type=Cache">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>缓存大盘</a> &ndash;%&gt;--%>
							<%--<b class="arrow"></b></li>--%>
					<%--</ul>--%>
				<%--</li>--%>
                <li id="Dashboard_report"  class="hsub">
                    <a href="/cat/r/top?op=view&domain=${model.domain}">
                        <i class="menu-icon fa fa-tachometer"></i>
                        <span class="menu-text">Dashboard</span>
                    </a>
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
				<li id="Business_report" >
					<a href="/cat/r/business?name=${model.domain}&type=domain">
						<i class="menu-icon fa fa-list-alt"></i>
						<span class="menu-text">Business</span>
					</a>
				</li>
				<%--<li id="RPC_report" >--%>
					<%--<a href="/cat/r/storage?id=shop-server&type=RPC&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">--%>
						<%--<i class="menu-icon fa fa-cloud"></i>--%>
						<%--<span class="menu-text">Service</span>--%>
					<%--</a>--%>
				<%--</li>--%>
				<%--<li id="Database_report" >--%>
					<%--<a href="/cat/r/storage?id=cat&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">--%>
						<%--<i class="menu-icon fa fa-lemon-o"></i>--%>
						<%--<span class="menu-text">Database</span>--%>
					<%--</a>--%>
				<%--</li>--%>
				<%--<li id="Cache_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-flash"></i> <span class="menu-text">Cache</span>--%>
						<%--<b class="arrow fa fa-angle-down"></b>--%>
					<%--</a> <b class="arrow"></b>--%>
					<%--<ul class="submenu">--%>
						<%--<li id="cache_operation"><a href="/cat/r/storage?id=memcached&type=Cache&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>访问趋势</a>--%>
							<%--<b class="arrow"></b></li>--%>
						<%--&lt;%&ndash; <li id="cache_info"><a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=view">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>访问情况</a>--%>
							<%--<b class="arrow"></b></li> &ndash;%&gt;--%>
					<%--</ul>--%>
				<%--</li>--%>
				<%-- <li id="Dependency_report" class="hsub"><a href="/cat/r/dependency?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-road"></i> <span class="menu-text">Dependency</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="dependency_dashboard"><a href="/cat/r/dependency?op=dashboard&domain=${model.domain}&date=${model.date}">
							<i class="menu-icon fa fa-caret-right"></i>综合大盘</a>
							<b class="arrow"></b></li>
						<li id="dependency_trend"><a href="/cat/r/dependency?op=lineChart&domain=${model.domain}&date=${model.date}">
							<i class="menu-icon fa fa-caret-right"></i>趋势图</a>
							<b class="arrow"></b></li>
						<li id="dependency_topo"><a href="/cat/r/dependency?op=dependencyGraph&domain=${model.domain}&date=${model.date}">
							<i class="menu-icon fa fa-caret-right"></i>拓扑图</a>
							<b class="arrow"></b></li>
						
					</ul>
				</li> --%>
				<%--<li id="Matrix_report" >--%>
					<%--<a href="/cat/r/matrix?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">--%>
						<%--<i class="menu-icon  fa  fa-flask"></i>--%>
						<%--<span class="menu-text">Matrix</span>--%>
					<%--</a>--%>
				<%--</li>--%>
				<li id="State_report" >
					<a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
						<i class="menu-icon fa fa-bar-chart-o"></i>
						<span class="menu-text">State</span>
					</a>
				</li>
				<%-- <li id="Offline_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-film"></i> <span class="menu-text">Offline</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="overload_report" >
						<a href="/cat/r/overload?domain=${model.domain}&op=${payload.action.name}">
							<i class="menu-icon  fa  fa-flask"></i>
							<span class="menu-text">报表容量统计</span>
						</a>
					</li>
					<li id="service_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service">
							<i class="menu-icon glyphicon glyphicon-check"></i>
							<span class="menu-text">服务可用排行</span>
						</a>
					</li>
					<li id="client_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=client">
							<i class="menu-icon fa  fa-exchange"></i>
							<span class="menu-text">服务调用排行</span>
						</a>
					</li>
					<li id="utilization_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization">
							<i class="menu-icon  fa fa-glass"></i>
							<span class="menu-text">线上容量规划</span>
						</a>
					</li>
					<li id="jar_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=jar">
							<i class="menu-icon  fa fa-briefcase"></i>
							<span class="menu-text">线上JAR版本</span>
						</a>
					</li>
					<li id="heavy_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy">
							<i class="menu-icon  fa fa-circle"></i>
							<span class="menu-text">重量访问排行</span>
						</a>
					</li>
					<li id="summary_report" >
						<a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=summary">
							<i class="menu-icon  fa fa-lightbulb-o"></i>
							<span class="menu-text">告警智能分析</span>
						</a>
					</li>
					</ul>
				</li> --%>
				<%--<li id="System_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-gavel"></i> <span class="menu-text">System</span>--%>
						<%--<b class="arrow fa fa-angle-down"></b>--%>
				<%--</a> <b class="arrow"></b>--%>
					<%--<ul class="submenu">--%>
						<%--&lt;%&ndash; <li id="system_alteration"><a href="/cat/r/alteration?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>线上变更</a>--%>
							<%--<b class="arrow"></b></li> &ndash;%&gt;--%>
						<%--<li id="system_alert"><a href="/cat/r/alert?domain=${model.domain}&op=${payload.action.name}">--%>
							<%--<i class="menu-icon fa fa-caret-right"></i>告警信息</a>--%>
							<%--<b class="arrow"></b></li>--%>
					<%--</ul>--%>
				<%--</li>--%>
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
</a:base>


