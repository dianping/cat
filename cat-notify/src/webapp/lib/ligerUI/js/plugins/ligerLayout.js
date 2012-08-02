/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerLayout = function (options)
    {
        return $.ligerui.run.call(this, "ligerLayout", arguments);
    };

    $.fn.ligerGetLayoutManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetLayoutManager", arguments);
    };


    $.ligerDefaults.Layout = {
        topHeight: 50,
        bottomHeight: 50,
        leftWidth: 110,
        centerWidth: 300,
        rightWidth: 170,
        InWindow: true,     //是否以窗口的高度为准 height设置为百分比时可用
        heightDiff: 0,     //高度补差
        height: '100%',      //高度
        onHeightChanged: null,
        isLeftCollapse: false,      //初始化时 左边是否隐藏
        isRightCollapse: false,     //初始化时 右边是否隐藏
        allowLeftCollapse: true,      //是否允许 左边可以隐藏
        allowRightCollapse: true,     //是否允许 右边可以隐藏
        allowLeftResize: true,      //是否允许 左边可以调整大小
        allowRightResize: true,     //是否允许 右边可以调整大小
        allowTopResize: true,      //是否允许 头部可以调整大小
        allowBottomResize: true,     //是否允许 底部可以调整大小
        space: 3 //间隔
    };

    $.ligerMethos.Layout = {};

    $.ligerui.controls.Layout = function (element, options)
    {
        $.ligerui.controls.Layout.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.Layout.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Layout';
        },
        __idPrev: function ()
        {
            return 'Layout';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Layout;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.layout = $(this.element);
            g.layout.addClass("l-layout");
            g.width = g.layout.width();
            //top
            if ($("> div[position=top]", g.layout).length > 0)
            {
                g.top = $("> div[position=top]", g.layout).wrap('<div class="l-layout-top" style="top:0px;"></div>').parent();
                g.top.content = $("> div[position=top]", g.top);
                if (!g.top.content.hasClass("l-layout-content"))
                    g.top.content.addClass("l-layout-content");
                g.topHeight = p.topHeight;
                if (g.topHeight)
                {
                    g.top.height(g.topHeight);
                }
            }
            //bottom
            if ($("> div[position=bottom]", g.layout).length > 0)
            {
                g.bottom = $("> div[position=bottom]", g.layout).wrap('<div class="l-layout-bottom"></div>').parent();
                g.bottom.content = $("> div[position=bottom]", g.top);
                if (!g.bottom.content.hasClass("l-layout-content"))
                    g.bottom.content.addClass("l-layout-content");

                g.bottomHeight = p.bottomHeight;
                if (g.bottomHeight)
                {
                    g.bottom.height(g.bottomHeight);
                }

            }
            //left
            if ($("> div[position=left]", g.layout).length > 0)
            {
                g.left = $("> div[position=left]", g.layout).wrap('<div class="l-layout-left" style="left:0px;"></div>').parent();
                g.left.header = $('<div class="l-layout-header"><div class="l-layout-header-toggle"></div><div class="l-layout-header-inner"></div></div>');
                g.left.prepend(g.left.header);
                g.left.header.toggle = $(".l-layout-header-toggle", g.left.header);
                g.left.content = $("> div[position=left]", g.left);
                if (!g.left.content.hasClass("l-layout-content"))
                    g.left.content.addClass("l-layout-content");
                if (!p.allowLeftCollapse) $(".l-layout-header-toggle", g.left.header).remove();
                //set title
                var lefttitle = g.left.content.attr("title");
                if (lefttitle)
                {
                    g.left.content.attr("title", "");
                    $(".l-layout-header-inner", g.left.header).html(lefttitle);
                }
                //set width
                g.leftWidth = p.leftWidth;
                if (g.leftWidth)
                    g.left.width(g.leftWidth);
            }
            //center
            if ($("> div[position=center]", g.layout).length > 0)
            {
                g.center = $("> div[position=center]", g.layout).wrap('<div class="l-layout-center" ></div>').parent();
                g.center.content = $("> div[position=center]", g.center);
                g.center.content.addClass("l-layout-content");
                //set title
                var centertitle = g.center.content.attr("title");
                if (centertitle)
                {
                    g.center.content.attr("title", "");
                    g.center.header = $('<div class="l-layout-header"></div>');
                    g.center.prepend(g.center.header);
                    g.center.header.html(centertitle);
                }
                //set width
                g.centerWidth = p.centerWidth;
                if (g.centerWidth)
                    g.center.width(g.centerWidth);
            }
            //right
            if ($("> div[position=right]", g.layout).length > 0)
            {
                g.right = $("> div[position=right]", g.layout).wrap('<div class="l-layout-right"></div>').parent();

                g.right.header = $('<div class="l-layout-header"><div class="l-layout-header-toggle"></div><div class="l-layout-header-inner"></div></div>');
                g.right.prepend(g.right.header);
                g.right.header.toggle = $(".l-layout-header-toggle", g.right.header);
                if (!p.allowRightCollapse) $(".l-layout-header-toggle", g.right.header).remove();
                g.right.content = $("> div[position=right]", g.right);
                if (!g.right.content.hasClass("l-layout-content"))
                    g.right.content.addClass("l-layout-content");

                //set title
                var righttitle = g.right.content.attr("title");
                if (righttitle)
                {
                    g.right.content.attr("title", "");
                    $(".l-layout-header-inner", g.right.header).html(righttitle);
                }
                //set width
                g.rightWidth = p.rightWidth;
                if (g.rightWidth)
                    g.right.width(g.rightWidth);
            }
            //lock
            g.layout.lock = $("<div class='l-layout-lock'></div>");
            g.layout.append(g.layout.lock);
            //DropHandle
            g._addDropHandle();

            //Collapse
            g.isLeftCollapse = p.isLeftCollapse;
            g.isRightCollapse = p.isRightCollapse;
            g.leftCollapse = $('<div class="l-layout-collapse-left" style="display: none; "><div class="l-layout-collapse-left-toggle"></div></div>');
            g.rightCollapse = $('<div class="l-layout-collapse-right" style="display: none; "><div class="l-layout-collapse-right-toggle"></div></div>');
            g.layout.append(g.leftCollapse).append(g.rightCollapse);
            g.leftCollapse.toggle = $("> .l-layout-collapse-left-toggle", g.leftCollapse);
            g.rightCollapse.toggle = $("> .l-layout-collapse-right-toggle", g.rightCollapse);
            g._setCollapse();
            //init
            g._bulid();
            $(window).resize(function ()
            {
                g._onResize();
            });
            g.set(p);
        },
        setLeftCollapse: function (isCollapse)
        {
            var g = this, p = this.options;
            if (!g.left) return false;
            g.isLeftCollapse = isCollapse;
            if (g.isLeftCollapse)
            {
                g.leftCollapse.show();
                g.leftDropHandle && g.leftDropHandle.hide();
                g.left.hide();
            }
            else
            {
                g.leftCollapse.hide();
                g.leftDropHandle && g.leftDropHandle.show();
                g.left.show();
            }
            g._onResize();
        },
        setRightCollapse: function (isCollapse)
        {
            var g = this, p = this.options;
            if (!g.right) return false;
            g.isRightCollapse = isCollapse;
            g._onResize();
            if (g.isRightCollapse)
            {
                g.rightCollapse.show();
                g.rightDropHandle && g.rightDropHandle.hide();
                g.right.hide();
            }
            else
            {
                g.rightCollapse.hide();
                g.rightDropHandle && g.rightDropHandle.show();
                g.right.show();
            }
            g._onResize();
        },
        _bulid: function ()
        {
            var g = this, p = this.options;
            $("> .l-layout-left .l-layout-header,> .l-layout-right .l-layout-header", g.layout).hover(function ()
            {
                $(this).addClass("l-layout-header-over");
            }, function ()
            {
                $(this).removeClass("l-layout-header-over");

            });
            $(".l-layout-header-toggle", g.layout).hover(function ()
            {
                $(this).addClass("l-layout-header-toggle-over");
            }, function ()
            {
                $(this).removeClass("l-layout-header-toggle-over");

            });
            $(".l-layout-header-toggle", g.left).click(function ()
            {
                g.setLeftCollapse(true);
            });
            $(".l-layout-header-toggle", g.right).click(function ()
            {
                g.setRightCollapse(true);
            });
            //set top
            g.middleTop = 0;
            if (g.top)
            {
                g.middleTop += g.top.height();
                g.middleTop += parseInt(g.top.css('borderTopWidth'));
                g.middleTop += parseInt(g.top.css('borderBottomWidth'));
                g.middleTop += p.space;
            }
            if (g.left)
            {
                g.left.css({ top: g.middleTop });
                g.leftCollapse.css({ top: g.middleTop });
            }
            if (g.center) g.center.css({ top: g.middleTop });
            if (g.right)
            {
                g.right.css({ top: g.middleTop });
                g.rightCollapse.css({ top: g.middleTop });
            }
            //set left
            if (g.left) g.left.css({ left: 0 });
            g._onResize();
            g._onResize();
        },
        _setCollapse: function ()
        {
            var g = this, p = this.options;
            g.leftCollapse.hover(function ()
            {
                $(this).addClass("l-layout-collapse-left-over");
            }, function ()
            {
                $(this).removeClass("l-layout-collapse-left-over");
            });
            g.leftCollapse.toggle.hover(function ()
            {
                $(this).addClass("l-layout-collapse-left-toggle-over");
            }, function ()
            {
                $(this).removeClass("l-layout-collapse-left-toggle-over");
            });
            g.rightCollapse.hover(function ()
            {
                $(this).addClass("l-layout-collapse-right-over");
            }, function ()
            {
                $(this).removeClass("l-layout-collapse-right-over");
            });
            g.rightCollapse.toggle.hover(function ()
            {
                $(this).addClass("l-layout-collapse-right-toggle-over");
            }, function ()
            {
                $(this).removeClass("l-layout-collapse-right-toggle-over");
            });
            g.leftCollapse.toggle.click(function ()
            {
                g.setLeftCollapse(false);
            });
            g.rightCollapse.toggle.click(function ()
            {
                g.setRightCollapse(false);
            });
            if (g.left && g.isLeftCollapse)
            {
                g.leftCollapse.show();
                g.leftDropHandle && g.leftDropHandle.hide();
                g.left.hide();
            }
            if (g.right && g.isRightCollapse)
            {
                g.rightCollapse.show();
                g.rightDropHandle && g.rightDropHandle.hide();
                g.right.hide();
            }
        },
        _addDropHandle: function ()
        {
            var g = this, p = this.options;
            if (g.left && p.allowLeftResize)
            {
                g.leftDropHandle = $("<div class='l-layout-drophandle-left'></div>");
                g.layout.append(g.leftDropHandle);
                g.leftDropHandle && g.leftDropHandle.show();
                g.leftDropHandle.mousedown(function (e)
                {
                    g._start('leftresize', e);
                });
            }
            if (g.right && p.allowRightResize)
            {
                g.rightDropHandle = $("<div class='l-layout-drophandle-right'></div>");
                g.layout.append(g.rightDropHandle);
                g.rightDropHandle && g.rightDropHandle.show();
                g.rightDropHandle.mousedown(function (e)
                {
                    g._start('rightresize', e);
                });
            }
            if (g.top && p.allowTopResize)
            {
                g.topDropHandle = $("<div class='l-layout-drophandle-top'></div>");
                g.layout.append(g.topDropHandle);
                g.topDropHandle.show();
                g.topDropHandle.mousedown(function (e)
                {
                    g._start('topresize', e);
                });
            }
            if (g.bottom && p.allowBottomResize)
            {
                g.bottomDropHandle = $("<div class='l-layout-drophandle-bottom'></div>");
                g.layout.append(g.bottomDropHandle);
                g.bottomDropHandle.show();
                g.bottomDropHandle.mousedown(function (e)
                {
                    g._start('bottomresize', e);
                });
            }
            g.draggingxline = $("<div class='l-layout-dragging-xline'></div>");
            g.draggingyline = $("<div class='l-layout-dragging-yline'></div>");
            g.layout.append(g.draggingxline).append(g.draggingyline);
        },
        _setDropHandlePosition: function ()
        {
            var g = this, p = this.options;
            if (g.leftDropHandle)
            {
                g.leftDropHandle.css({ left: g.left.width() + parseInt(g.left.css('left')), height: g.middleHeight, top: g.middleTop });
            }
            if (g.rightDropHandle)
            {
                g.rightDropHandle.css({ left: parseInt(g.right.css('left')) - p.space, height: g.middleHeight, top: g.middleTop });
            }
            if (g.topDropHandle)
            {
                g.topDropHandle.css({ top: g.top.height() + parseInt(g.top.css('top')), width: g.top.width() });
            }
            if (g.bottomDropHandle)
            {
                g.bottomDropHandle.css({ top: parseInt(g.bottom.css('top')) - p.space, width: g.bottom.width() });
            }
        },
        _onResize: function ()
        {
            var g = this, p = this.options;
            var oldheight = g.layout.height();
            //set layout height 
            var h = 0;
            var windowHeight = $(window).height();
            var parentHeight = null;
            if (typeof (p.height) == "string" && p.height.indexOf('%') > 0)
            {
                var layoutparent = g.layout.parent();
                if (p.InWindow || layoutparent[0].tagName.toLowerCase() == "body")
                {
                    parentHeight = windowHeight;
                    parentHeight -= parseInt($('body').css('paddingTop'));
                    parentHeight -= parseInt($('body').css('paddingBottom'));
                }
                else
                {
                    parentHeight = layoutparent.height();
                }
                h = parentHeight * parseFloat(p.height) * 0.01;
                if (p.InWindow || layoutparent[0].tagName.toLowerCase() == "body")
                    h -= (g.layout.offset().top - parseInt($('body').css('paddingTop')));
            }
            else
            {
                h = parseInt(p.height);
            }
            h += p.heightDiff;
            g.layout.height(h);
            g.layoutHeight = g.layout.height();
            g.middleWidth = g.layout.width();
            g.middleHeight = g.layout.height();
            if (g.top)
            {
                g.middleHeight -= g.top.height();
                g.middleHeight -= parseInt(g.top.css('borderTopWidth'));
                g.middleHeight -= parseInt(g.top.css('borderBottomWidth'));
                g.middleHeight -= p.space;
            }
            if (g.bottom)
            {
                g.middleHeight -= g.bottom.height();
                g.middleHeight -= parseInt(g.bottom.css('borderTopWidth'));
                g.middleHeight -= parseInt(g.bottom.css('borderBottomWidth'));
                g.middleHeight -= p.space;
            }
            //specific
            g.middleHeight -= 2;

            if (g.hasBind('heightChanged') && g.layoutHeight != oldheight)
            { 
                g.trigger('heightChanged', [{ layoutHeight: g.layoutHeight, diff: g.layoutHeight - oldheight, middleHeight: g.middleHeight}]);
            }

            if (g.center)
            {
                g.centerWidth = g.middleWidth;
                if (g.left)
                {
                    if (g.isLeftCollapse)
                    {
                        g.centerWidth -= g.leftCollapse.width();
                        g.centerWidth -= parseInt(g.leftCollapse.css('borderLeftWidth'));
                        g.centerWidth -= parseInt(g.leftCollapse.css('borderRightWidth'));
                        g.centerWidth -= parseInt(g.leftCollapse.css('left'));
                        g.centerWidth -= p.space;
                    }
                    else
                    {
                        g.centerWidth -= g.leftWidth;
                        g.centerWidth -= parseInt(g.left.css('borderLeftWidth'));
                        g.centerWidth -= parseInt(g.left.css('borderRightWidth'));
                        g.centerWidth -= parseInt(g.left.css('left'));
                        g.centerWidth -= p.space;
                    }
                }
                if (g.right)
                {
                    if (g.isRightCollapse)
                    {
                        g.centerWidth -= g.rightCollapse.width();
                        g.centerWidth -= parseInt(g.rightCollapse.css('borderLeftWidth'));
                        g.centerWidth -= parseInt(g.rightCollapse.css('borderRightWidth'));
                        g.centerWidth -= parseInt(g.rightCollapse.css('right'));
                        g.centerWidth -= p.space;
                    }
                    else
                    {
                        g.centerWidth -= g.rightWidth;
                        g.centerWidth -= parseInt(g.right.css('borderLeftWidth'));
                        g.centerWidth -= parseInt(g.right.css('borderRightWidth'));
                        g.centerWidth -= p.space;
                    }
                }
                g.centerLeft = 0;
                if (g.left)
                {
                    if (g.isLeftCollapse)
                    {
                        g.centerLeft += g.leftCollapse.width();
                        g.centerLeft += parseInt(g.leftCollapse.css('borderLeftWidth'));
                        g.centerLeft += parseInt(g.leftCollapse.css('borderRightWidth'));
                        g.centerLeft += parseInt(g.leftCollapse.css('left'));
                        g.centerLeft += p.space;
                    }
                    else
                    {
                        g.centerLeft += g.left.width();
                        g.centerLeft += parseInt(g.left.css('borderLeftWidth'));
                        g.centerLeft += parseInt(g.left.css('borderRightWidth'));
                        g.centerLeft += p.space;
                    }
                }
                g.center.css({ left: g.centerLeft });
                g.center.width(g.centerWidth);
                g.center.height(g.middleHeight);
                var contentHeight = g.middleHeight;
                if (g.center.header) contentHeight -= g.center.header.height();
                g.center.content.height(contentHeight);
            }
            if (g.left)
            {
                g.leftCollapse.height(g.middleHeight);
                g.left.height(g.middleHeight);
            }
            if (g.right)
            {
                g.rightCollapse.height(g.middleHeight);
                g.right.height(g.middleHeight);
                //set left
                g.rightLeft = 0;

                if (g.left)
                {
                    if (g.isLeftCollapse)
                    {
                        g.rightLeft += g.leftCollapse.width();
                        g.rightLeft += parseInt(g.leftCollapse.css('borderLeftWidth'));
                        g.rightLeft += parseInt(g.leftCollapse.css('borderRightWidth'));
                        g.rightLeft += p.space;
                    }
                    else
                    {
                        g.rightLeft += g.left.width();
                        g.rightLeft += parseInt(g.left.css('borderLeftWidth'));
                        g.rightLeft += parseInt(g.left.css('borderRightWidth'));
                        g.rightLeft += parseInt(g.left.css('left'));
                        g.rightLeft += p.space;
                    }
                }
                if (g.center)
                {
                    g.rightLeft += g.center.width();
                    g.rightLeft += parseInt(g.center.css('borderLeftWidth'));
                    g.rightLeft += parseInt(g.center.css('borderRightWidth'));
                    g.rightLeft += p.space;
                }
                g.right.css({ left: g.rightLeft });
            }
            if (g.bottom)
            {
                g.bottomTop = g.layoutHeight - g.bottom.height() - 2;
                g.bottom.css({ top: g.bottomTop });
            }
            g._setDropHandlePosition();

        },
        _start: function (dragtype, e)
        {
            var g = this, p = this.options;
            g.dragtype = dragtype;
            if (dragtype == 'leftresize' || dragtype == 'rightresize')
            {
                g.xresize = { startX: e.pageX };
                g.draggingyline.css({ left: e.pageX - g.layout.offset().left, height: g.middleHeight, top: g.middleTop }).show();
                $('body').css('cursor', 'col-resize');
            }
            else if (dragtype == 'topresize' || dragtype == 'bottomresize')
            {
                g.yresize = { startY: e.pageY };
                g.draggingxline.css({ top: e.pageY - g.layout.offset().top, width: g.layout.width() }).show();
                $('body').css('cursor', 'row-resize');
            }
            else
            {
                return;
            }

            g.layout.lock.width(g.layout.width());
            g.layout.lock.height(g.layout.height());
            g.layout.lock.show();
            if ($.browser.msie || $.browser.safari) $('body').bind('selectstart', function () { return false; }); // 不能选择

            $(document).bind('mouseup', function ()
            {
                g._stop.apply(g, arguments);
            });
            $(document).bind('mousemove', function ()
            {
                g._drag.apply(g, arguments);
            });
        },
        _drag: function (e)
        {
            var g = this, p = this.options;
            if (g.xresize)
            {
                g.xresize.diff = e.pageX - g.xresize.startX;
                g.draggingyline.css({ left: e.pageX - g.layout.offset().left });
                $('body').css('cursor', 'col-resize');
            }
            else if (g.yresize)
            {
                g.yresize.diff = e.pageY - g.yresize.startY;
                g.draggingxline.css({ top: e.pageY - g.layout.offset().top });
                $('body').css('cursor', 'row-resize');
            }
        },
        _stop: function (e)
        {
            var g = this, p = this.options;
            if (g.xresize && g.xresize.diff != undefined)
            {
                if (g.dragtype == 'leftresize')
                {
                    g.leftWidth += g.xresize.diff;
                    g.left.width(g.leftWidth);
                    if (g.center)
                        g.center.width(g.center.width() - g.xresize.diff).css({ left: parseInt(g.center.css('left')) + g.xresize.diff });
                    else if (g.right)
                        g.right.width(g.left.width() - g.xresize.diff).css({ left: parseInt(g.right.css('left')) + g.xresize.diff });
                }
                else if (g.dragtype == 'rightresize')
                {
                    g.rightWidth -= g.xresize.diff;
                    g.right.width(g.rightWidth).css({ left: parseInt(g.right.css('left')) + g.xresize.diff });
                    if (g.center)
                        g.center.width(g.center.width() + g.xresize.diff);
                    else if (g.left)
                        g.left.width(g.left.width() + g.xresize.diff);
                }
            }
            else if (g.yresize && g.yresize.diff != undefined)
            {
                if (g.dragtype == 'topresize')
                {
                    g.top.height(g.top.height() + g.yresize.diff);
                    g.middleTop += g.yresize.diff;
                    g.middleHeight -= g.yresize.diff;
                    if (g.left)
                    {
                        g.left.css({ top: g.middleTop }).height(g.middleHeight);
                        g.leftCollapse.css({ top: g.middleTop }).height(g.middleHeight);
                    }
                    if (g.center) g.center.css({ top: g.middleTop }).height(g.middleHeight);
                    if (g.right)
                    {
                        g.right.css({ top: g.middleTop }).height(g.middleHeight);
                        g.rightCollapse.css({ top: g.middleTop }).height(g.middleHeight);
                    }
                }
                else if (g.dragtype == 'bottomresize')
                {
                    g.bottom.height(g.bottom.height() - g.yresize.diff);
                    g.middleHeight += g.yresize.diff;
                    g.bottomTop += g.yresize.diff;
                    g.bottom.css({ top: g.bottomTop });
                    if (g.left)
                    {
                        g.left.height(g.middleHeight);
                        g.leftCollapse.height(g.middleHeight);
                    }
                    if (g.center) g.center.height(g.middleHeight);
                    if (g.right)
                    {
                        g.right.height(g.middleHeight);
                        g.rightCollapse.height(g.middleHeight);
                    }
                }
            }
            g._setDropHandlePosition();
            g.draggingxline.hide();
            g.draggingyline.hide();
            g.xresize = g.yresize = g.dragtype = false;
            g.layout.lock.hide();
            if ($.browser.msie || $.browser.safari)
                $('body').unbind('selectstart');
            $(document).unbind('mousemove', g._drag);
            $(document).unbind('mouseup', g._stop);
            $('body').css('cursor', '');
        }
    });

})(jQuery);