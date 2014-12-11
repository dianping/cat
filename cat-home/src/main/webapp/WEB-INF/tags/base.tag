<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="navBar" class="com.dianping.cat.report.view.NavigationBar" scope="page" />
<res:bean id="res" />
<html lang="en"><head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="description" content="Restyling jQuery UI Widgets and Elements">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
	<link rel="stylesheet" href="${model.webapp}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/jquery-ui.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace-fonts.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace-skins.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace-rtl.min.css">
	<script src="${model.webapp}/assets/js/ace-extra.min.js"></script>
	<script src="${model.webapp}/assets/js/bootstrap.min.js"></script>
	<res:useJs value="${res.js.local['highcharts.js']}" target="head-js" />
	<res:useCss value='${res.css.local.body_css}' target="head-css" />
	<script src="${model.webapp}/assets/js/jquery-ui.min.js"></script>
	<script src="${model.webapp}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${model.webapp}/assets/js/ace-elements.min.js"></script>
	<script src="${model.webapp}/assets/js/ace.min.js"></script>
	</head>
	<body class="no-skin">
		<!-- #section:basics/navbar.layout -->
		<div id="navbar" class="navbar navbar-default">
			<script type="text/javascript">
				try{ace.settings.check('navbar' , 'fixed')}catch(e){}
			</script>

			<div class="navbar-container" id="navbar-container">
				<!-- #section:basics/sidebar.mobile.toggle -->
				<button type="button" class="navbar-toggle menu-toggler pull-left" id="menu-toggler">
					<span class="sr-only">Toggle sidebar</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>

				<!-- /section:basics/sidebar.mobile.toggle -->
				<div class="navbar-header pull-left">
					<!-- #section:basics/navbar.layout.brand -->
					<a href="http://github.com/dianping/cat"  target="_blank" class="navbar-brand">
						<small>
							CAT
						</small>
					</a>
				</div>
				<!-- #section:basics/navbar.dropdown -->
				<div class="navbar-buttons navbar-header pull-right" role="navigation">
				<ul class="nav ace-nav" style="height:auto;">
					<!-- #section:basics/navbar.user_menu -->
					<li class="light-blue">
						<a data-toggle="dropdown" href="#" class="dropdown-toggle">
							<span class="user-info">
								<span id="loginInfo" ></span>
							</span>
							<i class="ace-icon fa fa-caret-down"></i>
						</a>
						<ul class="user-menu dropdown-menu-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close">
							<li>
									<a href="http://github.com/dianping/cat" target="_blank">关注</a>
									<a href="/cat/s/login?op=logout" >注销</a>
							</li>
						</ul>
					</li>
				</ul>
			</div> 
			</div>
		</div>
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
						<button class="btn btn-info" id="tab_offtime">
							<i class="ace-icon fa fa-film"></i>&nbsp;&nbsp;离线
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
					<li id="Dashboard_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-bar-chart-o"></i> <span class="menu-text">Dashboard</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="dashbord_system"><a href="/cat/r/dependency?op=metricDashboard&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>系统报错</a>
								<b class="arrow"></b></li>
							<li id="dashbord_metric"><a href="/cat/r/metric?op=dashboard&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>业务大盘</a>
								<b class="arrow"></b></li>
							<li id="dashbord_network"><a href="/cat/r/network?op=dashboard&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>网络大盘</a>
								<b class="arrow"></b></li>
							<li id="dashbord_application"><a href="/cat/r/dependency?op=dashboard&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>应用大盘</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="Web_report" >
						<a href="/cat/r/web?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
							<i class="menu-icon fa fa-globe"></i>
							<span class="menu-text">Web</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="App_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-earphone"></i> <span class="menu-text">App</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="trend"><a href="/cat/r/app?op=view&showActivity=false&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>API访问趋势</a>
								<b class="arrow"></b></li>
							<li id="activity_trend"><a href="/cat/r/app?op=view&showActivity=true&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>运营活动趋势</a>
								<b class="arrow"></b></li>
							<li id="speed"><a href="/cat/r/app?op=speed&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>访问速度趋势</a>
								<b class="arrow"></b></li>
							<li id="accessPiechart"><a href="/cat/r/app?op=piechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>访问量分布</a>
								<b class="arrow"></b></li>
							<li id="crashLog"><a href="/cat/r/app?op=crashLog&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>Crash日志</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<%-- <li id="Metric_report" >
						<a href="/cat/r/metric?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
							<i class="menu-icon glyphicon glyphicon-signal"></i>
							<span class="menu-text">Metric</span>
						</a>
					</li> --%>
					<li id="Transaction_report" >
						<a href="/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
							<i class="menu-icon glyphicon glyphicon-time"></i>
							<span class="menu-text">Transaction</span>
						</a>
					</li>
					<li id="Event_report" >
						<a href="/cat/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
							<i class="menu-icon glyphicon glyphicon-check"></i>
							<span class="menu-text">Event</span>
						</a>
					</li>					
					<li id="Problem_report" >
						<a href="/cat/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
							<i class="menu-icon fa fa-bolt"></i>
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
							<i class="menu-icon  fa fa-exchange"></i>
							<span class="menu-text">Cross</span>
						</a>
					</li>		
					<li id="Cache_report" >
						<a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
							<i class="menu-icon  fa fa-coffee"></i>
							<span class="menu-text">Cache</span>
						</a>
					</li>
					<li id="Dependency_report" class="hsub"><a href="/cat/r/dependency?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-road"></i> <span class="menu-text">Dependency</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="dependency_trend"><a href="/cat/r/dependency?op=lineChart&domain=${model.domain}&date=${model.date}">
								<i class="menu-icon fa fa-caret-right"></i>趋势图</a>
								<b class="arrow"></b></li>
							<li id="dependency_topo"><a href="/cat/r/dependency?op=dependencyGraph&domain=${model.domain}&date=${model.date}">
								<i class="menu-icon fa fa-caret-right"></i>拓扑图</a>
								<b class="arrow"></b></li>
							
						</ul>
					</li>
					<li id="State_report" >
						<a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
							<i class="menu-icon  fa fa-cogs"></i>
							<span class="menu-text">State</span>
						</a>
					</li>
					<li id="System_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-gavel"></i> <span class="menu-text">System</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="system_cdn"><a href="/cat/r/cdn?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
								<i class="menu-icon fa fa-caret-right"></i>CDN监控</a>
								<b class="arrow"></b></li>
							<li id="system_network"><a href="/cat/r/network?op=metric&product=f5-3600-2-dianping-com&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
								<i class="menu-icon fa fa-caret-right"></i>网络监控</a>
								<b class="arrow"></b></li>
							<li id="system_database"><a href="/cat/r/database?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
								<i class="menu-icon fa fa-caret-right"></i>数据库</a>
								<b class="arrow"></b></li>
							<li id="system_paas"><a href="/cat/r/system?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
								<i class="menu-icon fa fa-caret-right"></i>PAAS监控</a>
								<b class="arrow"></b></li>
							<li id="system_alteration"><a href="/cat/r/alteration?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
								<i class="menu-icon fa fa-caret-right"></i>线上变更</a>
								<b class="arrow"></b></li>
							<li id="system_alert"><a href="/cat/r/alert?domain=${model.domain}&op=${payload.action.name}">
								<i class="menu-icon fa fa-caret-right"></i>告警信息</a>
								<b class="arrow"></b></li>
							
						</ul>
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
 				<div style="padding-right:8px;">
 				<jsp:doBody/>
 				</div>
			</div><!-- /.main-content -->
		</div><!-- /.main-container -->
		<script type="text/javascript">
			jQuery(function($) {
				//override dialog's title function to allow for HTML titles
				$.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
					_title: function(title) {
						var $title = this.options.title || '&nbsp;'
						if( ("title_html" in this.options) && this.options.title_html == true )
							title.html($title);
						else title.text($title);
					}
				}));
			
				$(".delete").on('click', function(e) {
					e.preventDefault();
					var anchor = this;
					var dialog = $( "#dialog-message" ).removeClass('hide').dialog({
						modal: true,
						title: "<div class='widget-header widget-header-small'><h4 class='smaller'><i class='ace-icon fa fa-check'></i>CRM管理系统提示</h4></div>",
						title_html: true,
						buttons: [ 
							{
								text: "Cancel",
								"class" : "btn btn-xs",
								click: function() {
									$( this ).dialog( "close" ); 
								} 
							},
							{
								text: "OK",
								"class" : "btn btn-primary btn-xs",
								click: function() {
									window.location.href=anchor.href;
								} 
							}
						]
					});
				});
				//tooltips
				$( "#show-option" ).tooltip({
					show: {
						effect: "slideDown",
						delay: 250
					}
				});
			
				$( "#hide-option" ).tooltip({
					hide: {
						effect: "explode",
						delay: 250
					}
				});
				$( "#open-event" ).tooltip({
					show: null,
					position: {
						my: "left top",
						at: "left bottom"
					},
					open: function( event, ui ) {
						ui.tooltip.animate({ top: ui.tooltip.position().top + 10 }, "fast" );
					}
				});
				//Menu
				$( "#menu" ).menu();
			
				//spinner
				var spinner = $( "#spinner" ).spinner({
					create: function( event, ui ) {
						//add custom classes and icons
						$(this)
						.next().addClass('btn btn-success').html('<i class="ace-icon fa fa-plus"></i>')
						.next().addClass('btn btn-danger').html('<i class="ace-icon fa fa-minus"></i>')
						
						//larger buttons on touch devices
						if('touchstart' in document.documentElement) 
							$(this).closest('.ui-spinner').addClass('ui-spinner-touch');
					}
				});
			
				//slider example
				$( "#slider" ).slider({
					range: true,
					min: 0,
					max: 500,
					values: [ 75, 300 ]
				});
			
			
				//jquery accordion
				$( "#accordion" ).accordion({
					collapsible: true ,
					heightStyle: "content",
					animate: 250,
					header: ".accordion-header"
				}).sortable({
					axis: "y",
					handle: ".accordion-header",
					stop: function( event, ui ) {
						// IE doesn't register the blur when sorting
						// so trigger focusout handlers to remove .ui-state-focus
						ui.item.children( ".accordion-header" ).triggerHandler( "focusout" );
					}
				});
				//jquery tabs
				$( "#tabs" ).tabs();
			});
		</script>
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
				loginInfo.innerHTML ='欢迎'+name;
			} else{
				var loginInfo=document.getElementById('loginInfo');
				loginInfo.innerHTML ='<a href="/cat/s/login" data-toggle="modal">登录</a>';
			}
			var page = '${model.page.title}';
			$('#'+page+"_report").addClass("active open");
		});
	</script>
</body>
</html>

