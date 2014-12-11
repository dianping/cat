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
					<li id="projects_config" class="hsub">
					<a href="/cat/s/config?op=projects" class="dropdown-toggle"> <i class="menu-icon fa fa-bar-chart-o"></i> <span class="menu-text">项目配置</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="projects"><a href="/cat/s/config?op=projects">
								<i class="menu-icon fa fa-caret-right"></i>项目信息配置</a>
								<b class="arrow"></b></li>
							<li id="topologyProductLines"><a href="/cat/s/config?op=topologyProductLines">
								<i class="menu-icon fa fa-caret-right"></i>监控分组配置</a>
								<b class="arrow"></b></li>
							<li id="domainGroupConfigUpdate"><a href="/cat/s/config?op=domainGroupConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>机器分组配置</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="userMonitor_config" class="hsub" >
						<a href="/cat/s/config?op=aggregations"  class="dropdown-toggle"><i class="menu-icon fa fa-cogs"></i><span class="menu-text">用户监控配置</span>
						<b class="arrow fa fa-angle-down"></b>
						</a><b class="arrow"></b>
						<ul class="submenu">
							<li id="aggregations"><a href="/cat/s/config?op=aggregations">
								<i class="menu-icon fa fa-caret-right"></i>Js报错</a>
								<b class="arrow"></b></li>
							<li id="urlPatterns"><a href="/cat/s/config?op=urlPatterns">
								<i class="menu-icon fa fa-caret-right"></i>Web监控</a>
								<b class="arrow"></b></li>
							<li id="webRule"><a href="/cat/s/config?op=webRule">
								<i class="menu-icon fa fa-caret-right"></i>Web告警</a>
								<b class="arrow"></b></li>
							<li id="appList"><a href="/cat/s/config?op=appList">
								<i class="menu-icon fa fa-caret-right"></i>App监控</a>
								<b class="arrow"></b></li>
							<li id="appConfigUpdate"><a href="/cat/s/config?op=appConfigUpdate" style="display:none">
								<i class="menu-icon fa fa-caret-right"></i>App全局</a>
								<b class="arrow"></b></li>
							<li id="appRule"><a href="/cat/s/config?op=appRule">
								<i class="menu-icon fa fa-caret-right"></i>App告警</a>
								<b class="arrow"></b></li>
							<li id="appComparisonConfigUpdate"><a href="/cat/s/config?op=appComparisonConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>美团对比报表</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="application_config" class="hsub">
					<a href="/cat/s/config?op=metricConfigList" class="dropdown-toggle"> <i class="menu-icon fa fa-cogs"></i> <span class="menu-text">应用监控配置</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="metricConfigList"><a href="/cat/s/config?op=metricConfigList">
								<i class="menu-icon fa fa-caret-right"></i>业务监控规则</a>
								<b class="arrow"></b></li>
							<li id="transactionRule"><a href="/cat/s/config?op=transactionRule">
								<i class="menu-icon fa fa-caret-right"></i>响应时间告警</a>
								<b class="arrow"></b></li>
							<li id="exception"><a href="/cat/s/config?op=exception">
								<i class="menu-icon fa fa-caret-right"></i>异常告警</a>
								<b class="arrow"></b></li>
							<li id="displayPolicy"><a href="/cat/s/config?op=displayPolicy">
								<i class="menu-icon fa fa-caret-right"></i>心跳报表</a>
								<b class="arrow"></b></li>
							<li id="heartbeatRuleConfigList"><a href="/cat/s/config?op=heartbeatRuleConfigList">
								<i class="menu-icon fa fa-caret-right"></i>心跳告警</a>
								<b class="arrow"></b></li>
							<li id="thirdPartyConfigUpdate"><a href="/cat/s/config?op=thirdPartyConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>第三方告警</a>
								<b class="arrow"></b></li>
							<li id="topologyGraphNodeConfigList"><a href="/cat/s/config?op=topologyGraphNodeConfigList">
								<i class="menu-icon fa fa-caret-right"></i>应用阈值</a>
								<b class="arrow"></b></li>
							<li id="topologyGraphEdgeConfigList"><a href="/cat/s/config?op=topologyGraphEdgeConfigList">
								<i class="menu-icon fa fa-caret-right"></i>应用依赖</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
					<li id="alert_config" class="hsub">
					<a href="/cat/s/config?op=networkRuleConfigList" class="dropdown-toggle"> <i class="menu-icon fa fa-cogs"></i> <span class="menu-text">监控告警配置</span>
						<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="networkRuleConfigList"><a href="/cat/s/config?op=networkRuleConfigList">
								<i class="menu-icon fa fa-caret-right"></i>网络告警规则</a>
								<b class="arrow"></b></li>
							<li id="netGraphConfigUpdate"><a href="/cat/s/config?op=netGraphConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>网络拓扑</a>
								<b class="arrow"></b></li>
							<li id="databaseRuleConfigList"><a href="/cat/s/config?op=databaseRuleConfigList">
								<i class="menu-icon fa fa-caret-right"></i>数据库告警</a>
								<b class="arrow"></b></li>
							<li id="systemRuleConfigList"><a href="/cat/s/config?op=systemRuleConfigList">
								<i class="menu-icon fa fa-caret-right"></i>系统告警</a>
								<b class="arrow"></b></li>
							<li id="alertPolicy"><a href="/cat/s/config?op=alertPolicy">
								<i class="menu-icon fa fa-caret-right"></i>告警策略</a>
								<b class="arrow"></b></li>
							<li id="alertDefaultReceivers"><a href="/cat/s/config?op=alertDefaultReceivers">
								<i class="menu-icon fa fa-caret-right"></i>默认告警人</a>
								<b class="arrow"></b></li>
							<li id="routerConfigUpdate"><a href="/cat/s/config?op=routerConfigUpdate">
								<i class="menu-icon fa fa-caret-right"></i>客户端路由</a>
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
						title: "<div class='widget-header widget-header-small'><h4 class='smaller'><i class='ace-icon fa fa-check'></i>CAT提示</h4></div>",
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
		});
	</script>
</body>
</html>

