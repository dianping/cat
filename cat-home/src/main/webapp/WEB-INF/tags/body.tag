<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="navBar"
	class="com.dianping.cat.report.view.NavigationBar" scope="page" />
<res:bean id="res" />
<html lang="en"><head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="description" content="Restyling jQuery UI Widgets and Elements">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
	 <link rel="stylesheet" href="${model.webapp}/assets/css/bootstrap.min.css">
<%-- 	 <res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
 --%>	<link rel="stylesheet" href="${model.webapp}/assets/css/font-awesome.min.css">
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
					<a href="#" class="navbar-brand">
						<small>
							CAT
						</small>
					</a>

					<!-- /section:basics/navbar.layout.brand -->

					<!-- #section:basics/navbar.toggle -->

					<!-- /section:basics/navbar.toggle -->
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
								<a href="#">
									<a href="/cat/s/login?op=logout" >注销</a>
								</a>
							</li>
						</ul>
					</li>
		
					<!-- /section:basics/navbar.user_menu -->
				</ul>
			</div> 
				<!-- /section:basics/navbar.dropdown -->
			</div><!-- /.navbar-container -->
		</div>

		<!-- /section:basics/navbar.layout -->
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
						<button class="btn btn-success">
							<i class="ace-icon fa fa-signal"></i>&nbsp;&nbsp;实时
						</button>
						<button class="btn btn-info">
							<i class="ace-icon fa fa-film"></i>&nbsp;&nbsp;离线
						</button>
						<!-- #section:basics/sidebar.layout.shortcuts -->
						<button class="btn btn-warning">
							<i class="ace-icon fa fa-users"></i>&nbsp;&nbsp;文档
						</button>
						<button class="btn btn-danger">
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
					<li id="Web_report" >
						<a href="/cat/r/web?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
							<i class="menu-icon fa fa-globe"></i>
							<span class="menu-text">Web</span>
						</a>
						<b class="arrow"></b>
					</li>
					</li><li id="App_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-user"></i> <span class="menu-text">App</span>
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
					<li id="Metric_report" >
						<a href="/cat/r/metric?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
							<i class="menu-icon glyphicon glyphicon-signal"></i>
							<span class="menu-text">Metric</span>
						</a>
						<b class="arrow"></b>
					</li>
					<li id="Transaction_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-time"></i> <span class="menu-text">Transaction</span>
							<b class="arrow fa fa-angle-down "></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="Transaction_report_hour"><a href="/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-angle-down "></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="Transaction_report_day"><a href="/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
							<li id="Transaction_report_week"><a href="/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=week&op=history">
								<i class="menu-icon fa fa-caret-right"></i>周模式</a>
								<b class="arrow"></b></li>
							<li id="Transaction_report_month"><a href="/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=month&op=history">
								<i class="menu-icon fa fa-caret-right"></i>月模式</a>
								<b class="arrow"></b></li>
						</ul>
				
					</li><li id="Event_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon  glyphicon glyphicon-check"></i> <span class="menu-text">Event</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="Event_report_hour"><a href="/cat/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-caret-right"></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="Event_report_day"><a href="/cat/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
							<li id="Event_report_week"><a href="/cat/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=week&op=history">
								<i class="menu-icon fa fa-caret-right"></i>周模式</a>
								<b class="arrow"></b></li>
							<li id="Event_report_month"><a href="/cat/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=month&op=history">
								<i class="menu-icon fa fa-caret-right"></i>月模式</a>
								<b class="arrow"></b></li>
						</ul>
				
					</li><li id="Problem_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-bolt"></i> <span class="menu-text">Problem</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="Problem_report_hour"><a href="/cat/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-caret-right"></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="Problem_report_day"><a href="/cat/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
							<li id="Problem_report_week"><a href="/cat/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=week&op=history">
								<i class="menu-icon fa fa-caret-right"></i>周模式</a>
								<b class="arrow"></b></li>
							<li id="Problem_report_month"><a href="/cat/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=month&op=history">
								<i class="menu-icon fa fa-caret-right"></i>月模式</a>
								<b class="arrow"></b></li>
						</ul>
				
					</li><li id="Heartbeat_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-heart"></i> <span class="menu-text">Heartbeat</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="Heartbeat_report_hour"><a href="/cat/r/h?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-caret-right"></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="Heartbeat_report_day"><a href="/cat/r/h?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
						</ul>
				
					</li><li id="Cross_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-exchange"></i> <span class="menu-text">Cross</span>
							<b class="arrow fa fa-angle-down "></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="Cross_report_hour"><a href="/cat/r/cross?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-caret-right"></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="Cross_report_day"><a href="/cat/r/cross?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
							<li id="Cross_report_week"><a href="/cat/r/cross?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=week&op=history">
								<i class="menu-icon fa fa-caret-right"></i>周模式</a>
								<b class="arrow"></b></li>
							<li id="Cross_report_month"><a href="/cat/r/cross?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=month&op=history">
								<i class="menu-icon fa fa-caret-right"></i>月模式</a>
								<b class="arrow"></b></li>
						</ul>
				
					</li><li id="Cache_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-coffee"></i> <span class="menu-text">Cache</span>
							<b class="arrow fa fa-angle-down "></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="Cache_report_hour"><a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-caret-right"></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="Cache_report_day"><a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
							<li id="Cache_report_week"><a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=week&op=history">
								<i class="menu-icon fa fa-caret-right"></i>周模式</a>
								<b class="arrow"></b></li>
							<li id="Cache_report_month"><a href="/cat/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=month&op=history">
								<i class="menu-icon fa fa-caret-right"></i>月模式</a>
								<b class="arrow"></b></li>
						</ul>
				
					</li><li id="Dependency_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-external-link"></i> <span class="menu-text">Dependency</span>
							<b class="arrow fa fa-angle-down "></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
						</ul>
				
					</li><li id="State_report" class="hsub"><a href="cat/r/t" class="dropdown-toggle"> <i class="menu-icon fa fa-cogs"></i> <span class="menu-text">State</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="State_report_hour"><a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
								<i class="menu-icon fa fa-caret-right"></i>小时模式</a>
								<b class="arrow"></b></li>
							<li id="State_report_day"><a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=day&op=history">
								<i class="menu-icon fa fa-caret-right"></i>天模式</a>
								<b class="arrow"></b></li>
							<li id="State_report_week"><a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=week&op=history">
								<i class="menu-icon fa fa-caret-right"></i>周模式</a>
								<b class="arrow"></b></li>
							<li id="State_report_month"><a href="/cat/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=month&op=history">
								<i class="menu-icon fa fa-caret-right"></i>月模式</a>
								<b class="arrow"></b></li>
						</ul>
					</li></ul>
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
				<!-- #section:basics/content.breadcrumbs -->
				<!-- /section:basics/content.breadcrumbs -->
			</div><!-- /.main-content -->


		</div><!-- /.main-container -->

		<!-- inline scripts related to this page -->
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
				loginInfo.innerHTML ='欢迎您,'+name;
			} else{
				var loginInfo=document.getElementById('loginInfo');
				loginInfo.innerHTML ='<a href="/cat/s/login" data-toggle="modal">登录</a>';
			}
			
			
			var page = '${model.page.title}';
			$('#'+page+"_report").addClass("active open");
			
			var op = '${payload.action.name}';
			
			if(op=='view'){
				$('#'+page+"_report_hour").addClass("active");
			}else{
				$('#'+page+"_report_"+'${payload.reportType}').addClass("active");
			}
		});
	</script>
</body>
</html>

