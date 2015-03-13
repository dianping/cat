<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
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
					<a href="/cat/r/home"  class="navbar-brand">
						<span>CAT</span>
						<small style="font-size:65%">
							（Central Application Tracking）
						</small>
					</a>
				</div>
				<div class="navbar-header pull-left position" style="width:350px;padding-top:5px;">
					<form id="wrap_search" style="margin-bottom:0px;">
						<div class="input-group">
							<span class="input-group-btn "><button class="btn btn-sm btn-default" onclick="showDomain()" type="button"  id="switch">全部</button></span>
							<span class="input-group-btn "><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button"  id="frequent">常用</button></span>
							<span class="input-icon" style="width:300px;">
							<input id="search" type="text" value="${payload.id}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
							</span>
							<span class="input-group-btn">
								<button class="btn btn-sm btn-pink" type="button" id="search_go">
									Go
								</button> 
							</span>
						</div>
					</form>
				</div>
				
				<!-- #section:basics/navbar.dropdown -->
				<div class="navbar-buttons navbar-header pull-right" role="navigation">
				<ul class="nav ace-nav" style="height:auto;">
					<li class="light-blue">
						<a href="http://github.com/dianping/cat/" target="_blank">
							<i class="ace-icon glyphicon glyphicon-star"></i>
							<span>Star</span>
						</a>
					</li>
					<li class="light-blue" >
						<a data-toggle="dropdown" href="#" class="dropdown-toggle">
							<span class="user-info">
								<span id="loginInfo" ></span>
							</span>
							<i class="ace-icon fa fa-caret-down"></i>
						</a>
						<ul class="user-menu dropdown-menu-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close">
							<li>
								<a href="/cat/s/login?op=logout" ><i class="ace-icon fa fa-power-off"></i>
								注销</a>
							</li>
						</ul>
					</li>
				</ul>
				</div> 
			</div>
		</div>
		<jsp:doBody/>
		
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
			if (b == '全部') {
				$('.domainNavbar').slideDown();
				$('#switch').html("收起");
			} else {
				$('.domainNavbar').slideUp();
				$('#switch').html("全部");
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
				loginInfo.innerHTML ='欢迎，'+name;
			} else{
				var loginInfo=document.getElementById('loginInfo');
				loginInfo.innerHTML ='<a href="/cat/s/login" data-toggle="modal">登录</a>';
			}
			var page = '${model.page.title}';
			$('#'+page+"_report").addClass("active open");
			
			
			//custom autocomplete (category selection)
			$.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						if ( item.category != currentCategory ) {
							ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
							currentCategory = item.category;
						}
						that._renderItemData( ul, item );
					});
				}
			});
			
			var data = [];
			<c:forEach var="item" items="${model.departments}">
				<c:set var="department" value="${item.value}" />
					<c:forEach var="entry" items="${department.productlines}" varStatus="index">
					<c:set var="productline" value="${entry.value}" />
					<c:forEach var="storage" items="${productline.storages}">
							var item = {};
							item['label'] = '${storage}';
							item['category'] ='${entry.key}';
							
							data.push(item);
					</c:forEach>
			</c:forEach></c:forEach>
			
			$( "#search" ).catcomplete({
				delay: 0,
				source: data
			});
		});
	</script>
</body>
</html>

