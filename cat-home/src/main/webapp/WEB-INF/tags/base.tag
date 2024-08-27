<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core" %>
<jsp:useBean id="navBar" class="com.dianping.cat.report.view.NavigationBar" scope="page"/>
<res:bean id="res"/>
<html lang="en">
<head>
    <title>CAT</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta charset="utf-8">
    <meta name="description" content="Restyling jQuery UI Widgets and Elements">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <link rel="icon" href="${model.webapp}/favicon.ico" type ="image/x-icon">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/jquery-ui.min.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/ace-fonts.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/ace.min.css" id="main-ace-style">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/ace-skins.min.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/ace-rtl.min.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/css/body.css">
    <link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>

    <script src='${model.webapp}/assets/js/jquery.min.js'></script>
    <script src="${model.webapp}/assets/js/ace-extra.min.js"></script>
    <script src="${model.webapp}/assets/js/bootstrap.min.js"></script>
    <script src="${model.webapp}/js/highcharts.js"></script>
    <script src="${model.webapp}/js/baseGraph.js"></script>
    <script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
    <script src="${model.webapp}/assets/js/jquery-ui.min.js"></script>
    <script src="${model.webapp}/assets/js/jquery.ui.touch-punch.min.js"></script>
    <script src="${model.webapp}/assets/js/ace-elements.min.js"></script>
    <script src="${model.webapp}/assets/js/ace.min.js"></script>
</head>
<style>
	.ace-nav>li {
		border-left: 0px;
		line-height: 66px;
	}

	.ace-nav>li>a {
		background: #333333;
		width: 80px;
	}

	.ace-nav>li.open>a, .ace-nav>li>a:focus, .ace-nav>li>a:hover {
		background: #333333;
		color: #0e90d2;
		cursor: hand;
	}

	.nav .open>a, .nav .open>a:focus, .nav .open>a:hover {
		background: #333333;
		border-color: #333333;
	}
</style>
<body class="no-skin" style="overflow: hidden">
<!-- #section:basics/navbar.layout -->
<div id="navbar" class="navbar navbar-default">
    <script type="text/javascript">
        try {
            ace.settings.check('navbar', 'fixed')
        } catch (e) {
        }
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
            <img src="${model.webapp}/images/logo/cat_logo02.png" width="80px" style="float:left;margin: 14px 8px;">
            <!-- #section:basics/navbar.layout.brand -->
            <i class="navbar-brand" style="padding: 12px 3px;">
<%--                <span>CAT</span>--%>
                <small style="font-size:80%;line-height:45px;">
                   v3.4.1
                </small>
                &nbsp;&nbsp;
               <%-- <button class="btn btn-primary btn-sm" id="nav_application">
                    <i class="ace-icon fa fa-home"></i>应用
                </button>--%>
                <%--<button class="btn btn-grey btn-sm" id="nav_mobile">
                    <i class="menu-icon glyphicon glyphicon-phone"></i>Mobile
                </button>
                <button class="btn btn-warning btn-sm" id="nav_browser">
                    <i class="ace-icon fa fa-users"></i>Browser
                </button>--%>
                <%--<button class="btn btn-success btn-sm" id="nav_config">
                    <i class="ace-icon fa fa-cogs"></i>配置
                </button>--%>
                <%--<button class="btn btn-purple btn-sm" id="nav_server">
                    <i class="ace-icon fa fa-cogs"></i>服务器
                </button>--%>
                <%--<button class="btn btn-yellow btn-sm" id="nav_document">
                    <i class="ace-icon fa fa-cogs"></i>文档
                </button>--%>
            </i>
        </div>
        <!-- #section:basics/navbar.dropdown -->
        <div class="navbar-buttons navbar-header pull-right" role="navigation">
            <ul class="nav ace-nav" style="height:auto;">
                <li>
					<a id="nav_application">
						<i class="ace-icon glyphicon glyphicon-home"></i>
						<span>首页</span>
					</a>
                </li>
				<li>
					<a id="nav_server" style="width: 95px;">
						<i class="ace-icon glyphicon glyphicon-cloud" style="top:3px"></i>
						<span>Server</span>
					</a>
				</li>
				<li>
					<a id="nav_browser" style="width: 95px">
						<i class="ace-icon glyphicon glyphicon-globe"></i>
						<span>Browser</span>
					</a>
				</li>
				<li>
					<a id="nav_mobile" style="width: 95px">
						<i class="ace-icon glyphicon glyphicon-phone"></i>
						<span>Mobile</span>
					</a>
				</li>
				<li>
					<a id="nav_config">
						<i class="ace-icon glyphicon glyphicon-cog"></i>
						<span>配置</span>
					</a>
				</li>
				<li>
					<a id="nav_document">
						<i class="ace-icon glyphicon glyphicon-question-sign"></i>
						<span>文档</span>
					</a>
				</li>
				<li>
					<a id="nav_github" href="https://github.com/shiyindaxiaojie/cat" target="_blank">
						<i class="ace-icon fa fa-github"></i>
						<span>开源</span>
					</a>
				</li>
                <li style="margin:7px 3px;">
                    <a data-toggle="dropdown" href="#" class="dropdown-toggle" style="width: 80px">
							<span class="user-info" style="max-width:200px;font-size: 16px;line-height:40px;">
								<span id="loginInfo"></span>
							</span>
                        <%--<i id="forward-logout" class="ace-icon fa fa-caret-down"></i>--%>
                    </a>
                    <ul id="logout" style="margin-right:20px;"
                        class="user-menu dropdown-menu-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close">
                        <li>
							<a href="/cat/s/login?op=logout"><i class="ace-icon fa fa-power-off"></i>注销
							</a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</div>
<jsp:doBody/>

<script type="text/javascript">
    jQuery(function ($) {
        //override dialog's title function to allow for HTML titles
        $.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
            _title: function (title) {
                var $title = this.options.title || '&nbsp;'
                if (("title_html" in this.options) && this.options.title_html == true)
                    title.html($title);
                else title.text($title);
            }
        }));

        $(".delete").on('click', function (e) {
            e.preventDefault();
            var anchor = this;
            var dialog = $("#dialog-message").removeClass('hide').dialog({
                modal: true,
                title: "<div class='widget-header widget-header-small'><h4 class='smaller'><i class='ace-icon fa fa-check'></i>CAT提示</h4></div>",
                title_html: true,
                buttons: [
                    {
                        text: "Cancel",
                        "class": "btn btn-xs",
                        click: function () {
                            $(this).dialog("close");
                        }
                    },
                    {
                        text: "OK",
                        "class": "btn btn-primary btn-xs",
                        click: function () {
                            window.location.href = anchor.href;
                        }
                    }
                ]
            });
        });
        //tooltips
        $("#show-option").tooltip({
            show: {
                effect: "slideDown",
                delay: 250
            }
        });

        $("#hide-option").tooltip({
            hide: {
                effect: "explode",
                delay: 250
            }
        });
        $("#open-event").tooltip({
            show: null,
            position: {
                my: "left top",
                at: "left bottom"
            },
            open: function (event, ui) {
                ui.tooltip.animate({top: ui.tooltip.position().top + 10}, "fast");
            }
        });
        //Menu
        $("#menu").menu();

        //spinner
        var spinner = $("#spinner").spinner({
            create: function (event, ui) {
                //add custom classes and icons
                $(this)
                    .next().addClass('btn btn-success').html('<i class="ace-icon fa fa-plus"></i>')
                    .next().addClass('btn btn-danger').html('<i class="ace-icon fa fa-minus"></i>')

                //larger buttons on touch devices
                if ('touchstart' in document.documentElement)
                    $(this).closest('.ui-spinner').addClass('ui-spinner-touch');
            }
        });

        //slider example
        $("#slider").slider({
            range: true,
            min: 0,
            max: 500,
            values: [75, 300]
        });


        //jquery accordion
        $("#accordion").accordion({
            collapsible: true,
            heightStyle: "content",
            animate: 250,
            header: ".accordion-header"
        }).sortable({
            axis: "y",
            handle: ".accordion-header",
            stop: function (event, ui) {
                // IE doesn't register the blur when sorting
                // so trigger focusout handlers to remove .ui-state-focus
                ui.item.children(".accordion-header").triggerHandler("focusout");
            }
        });
        //jquery tabs
        $("#tabs").tabs();
    });
</script>
<script>
    function getcookie(objname) {
        var arrstr = document.cookie.split("; ");
        for (var i = 0; i < arrstr.length; i++) {
            var temp = arrstr[i].split("=");
            if (temp[0] == objname) {
                return temp[1];
            }
        }
        return "";
    }

    $(document).ready(function () {
        var ct = getcookie("ct");
        if (ct != "") {
            var length = ct.length;
            var realName = ct.split("|");
            var temp = realName[0];

            if (temp.charAt(0) == '"') {
                temp = temp.substring(1, temp.length);
            }
            var name = decodeURI(temp);
            var loginInfo = document.getElementById('loginInfo');
            loginInfo.innerHTML = name;
        } else {
			$('#logout').hide();
			$('#forward-logout').hide();
            var loginInfo = document.getElementById('loginInfo');
            loginInfo.innerHTML =
                '<i class="ace-icon fa fa-user"></i>&nbsp;&nbsp;<a href="/cat/s/login" style="color: #FFF;background: #333333" data-toggle="modal">登录</a>';
        }

        if ("${model.moduleUri}" != "/cat/s") {
            var page = '${model.page.title}';
            $('#' + page + "_report").addClass("active open");
        }

        //custom autocomplete (category selection)
        $.widget("custom.catcomplete", $.ui.autocomplete, {
            _renderMenu: function (ul, items) {
                var that = this,
                    currentCategory = "";
                $.each(items, function (index, item) {
                    if (item.category != currentCategory) {
                        ul.append("<li class='ui-autocomplete-category'>" + item.category + "</li>");
                        currentCategory = item.category;
                    }
                    that._renderItemData(ul, item);
                });
            }
        });
    });
</script>
<script type="text/javascript">
    $(document).ready(function () {
        $("#nav_application").click(function () {
            window.location.href = "/cat/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
        });
        $("#nav_mobile").click(function () {
            window.location.href = "/cat/r/app?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
        });
        $("#nav_browser").click(function () {
            window.location.href = "/cat/r/browser?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
        });
        $("#nav_server").click(function () {
            window.location.href = "/cat/r/server?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}";
        });
        $("#nav_document").click(function () {
            window.location.href = "/cat/r/home?op=view&docName=index";
        });
        $("#nav_config").click(function () {
            window.location.href = "/cat/s/config?op=projects";
        });
    });
</script>
</body>
</html>

