/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{

    $.fn.ligerTab = function (options)
    {
        return $.ligerui.run.call(this, "ligerTab", arguments);
    };

    $.fn.ligerGetTabManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetTabManager", arguments);
    };

    $.ligerDefaults.Tab = {
        height: null,
        heightDiff: 0, // 高度补差 
        changeHeightOnResize: false,
        contextmenu: true,
        dblClickToClose: false, //是否双击时关闭
        dragToMove: true,    //是否允许拖动时改变tab项的位置
        onBeforeOverrideTabItem: null,
        onAfterOverrideTabItem: null,
        onBeforeRemoveTabItem: null,
        onAfterRemoveTabItem: null,
        onBeforeAddTabItem: null,
        onAfterAddTabItem: null,
        onBeforeSelectTabItem: null,
        onAfterSelectTabItem: null
    };
    $.ligerDefaults.TabString = {
        closeMessage: "关闭当前页",
        closeOtherMessage: "关闭其他",
        closeAllMessage: "关闭所有",
        reloadMessage: "刷新"
    };

    $.ligerMethos.Tab = {};

    $.ligerui.controls.Tab = function (element, options)
    {
        $.ligerui.controls.Tab.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.Tab.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Tab';
        },
        __idPrev: function ()
        {
            return 'Tab';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Tab;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            if (p.height) g.makeFullHeight = true;
            g.tab = $(this.element);
            g.tab.addClass("l-tab");
            if (p.contextmenu && $.ligerMenu)
            {
                g.tab.menu = $.ligerMenu({ width: 100, items: [
                    { text: p.closeMessage, id: 'close', click: function ()
                    {
                        g._menuItemClick.apply(g, arguments);
                    }
                    },
                    { text: p.closeOtherMessage, id: 'closeother', click: function ()
                    {
                        g._menuItemClick.apply(g, arguments);
                    }
                    },
                    { text: p.closeAllMessage, id: 'closeall', click: function ()
                    {
                        g._menuItemClick.apply(g, arguments);
                    }
                    },
                    { text: p.reloadMessage, id: 'reload', click: function ()
                    {
                        g._menuItemClick.apply(g, arguments);
                    }
                    }
                ]
                });
            }
            g.tab.content = $('<div class="l-tab-content"></div>');
            $("> div", g.tab).appendTo(g.tab.content);
            g.tab.content.appendTo(g.tab);
            g.tab.links = $('<div class="l-tab-links"><ul style="left: 0px; "></ul></div>');
            g.tab.links.prependTo(g.tab);
            g.tab.links.ul = $("ul", g.tab.links);
            var haslselected = $("> div[lselected=true]", g.tab.content).length > 0;
            g.selectedTabId = $("> div[lselected=true]", g.tab.content).attr("tabid");
            $("> div", g.tab.content).each(function (i, box)
            {
                var li = $('<li class=""><a></a><div class="l-tab-links-item-left"></div><div class="l-tab-links-item-right"></div></li>');
                if ($(box).attr("title"))
                {
                    $("> a", li).html($(box).attr("title"));
                }
                var tabid = $(box).attr("tabid");
                if (tabid == undefined)
                {
                    tabid = g.getNewTabid();
                    $(box).attr("tabid", tabid);
                    if ($(box).attr("lselected"))
                    {
                        g.selectedTabId = tabid;
                    }
                }
                li.attr("tabid", tabid);
                if (!haslselected && i == 0) g.selectedTabId = tabid;
                var showClose = $(box).attr("showClose");
                if (showClose)
                {
                    li.append("<div class='l-tab-links-item-close'></div>");
                }
                $("> ul", g.tab.links).append(li);
                if (!$(box).hasClass("l-tab-content-item")) $(box).addClass("l-tab-content-item");
            });
            //init 
            g.selectTabItem(g.selectedTabId);
            //set content height
            if (p.height)
            {
                if (typeof (p.height) == 'string' && p.height.indexOf('%') > 0)
                {
                    g.onResize();
                    if (p.changeHeightOnResize)
                    {
                        $(window).resize(function ()
                        {
                            g.onResize.call(g);
                        });
                    }
                } else
                {
                    g.setHeight(p.height);
                }
            }
            if (g.makeFullHeight)
                g.setContentHeight();
            //add even 
            $("li", g.tab.links).each(function ()
            {
                g._addTabItemEvent($(this));
            });
            g.tab.bind('dblclick.tab', function (e)
            {
                if (!p.dblClickToClose) return;
                g.dblclicking = true;
                var obj = (e.target || e.srcElement);
                var tagName = obj.tagName.toLowerCase();
                if (tagName == "a")
                {
                    var tabid = $(obj).parent().attr("tabid");
                    var allowClose = $(obj).parent().find("div.l-tab-links-item-close").length ? true : false;
                    if (allowClose)
                    {
                        g.removeTabItem(tabid);
                    }
                }
                g.dblclicking = false;
            });

            g.set(p);
        },
        _applyDrag: function (tabItemDom)
        {
            var g = this, p = this.options;
            g.droptip = g.droptip || $("<div class='l-tab-drag-droptip' style='display:none'><div class='l-drop-move-up'></div><div class='l-drop-move-down'></div></div>").appendTo('body');
            var drag = $(tabItemDom).ligerDrag(
            {
                revert: true, animate: false,
                proxy: function ()
                {
                    var name = $(this).find("a").html();
                    g.dragproxy = $("<div class='l-tab-drag-proxy' style='display:none'><div class='l-drop-icon l-drop-no'></div></div>").appendTo('body');
                    g.dragproxy.append(name);
                    return g.dragproxy;
                },
                onRendered: function ()
                {
                    this.set('cursor', 'pointer');
                },
                onStartDrag: function (current, e)
                {
                    if (!$(tabItemDom).hasClass("l-selected")) return false;
                    if (e.button == 2) return false;
                    var obj = e.srcElement || e.target;
                    if ($(obj).hasClass("l-tab-links-item-close")) return false;
                },
                onDrag: function (current, e)
                {
                    if (g.dropIn == null)
                        g.dropIn = -1;
                    var tabItems = g.tab.links.ul.find('>li');
                    var targetIndex = tabItems.index(current.target);
                    tabItems.each(function (i, item)
                    {
                        if (targetIndex == i)
                        {
                            return;
                        }
                        var isAfter = i > targetIndex;
                        if (g.dropIn != -1 && g.dropIn != i) return;
                        var offset = $(this).offset();
                        var range = {
                            top: offset.top,
                            bottom: offset.top + $(this).height(),
                            left: offset.left - 10,
                            right: offset.left + 10
                        };
                        if (isAfter)
                        {
                            range.left += $(this).width();
                            range.right += $(this).width();
                        }
                        var pageX = e.pageX || e.screenX;
                        var pageY = e.pageY || e.screenY;
                        if (pageX > range.left && pageX < range.right && pageY > range.top && pageY < range.bottom)
                        {
                            g.droptip.css({
                                left: range.left + 5,
                                top: range.top - 9
                            }).show();
                            g.dropIn = i;
                            g.dragproxy.find(".l-drop-icon").removeClass("l-drop-no").addClass("l-drop-yes");
                        }
                        else
                        {
                            g.dropIn = -1;
                            g.droptip.hide();
                            g.dragproxy.find(".l-drop-icon").removeClass("l-drop-yes").addClass("l-drop-no");
                        }
                    });
                },
                onStopDrag: function (current, e)
                {
                    if (g.dropIn > -1)
                    {
                        var to = g.tab.links.ul.find('>li:eq(' + g.dropIn + ')').attr("tabid");
                        var from = $(current.target).attr("tabid");
                        setTimeout(function ()
                        {
                            g.moveTabItem(from, to);
                        }, 0);
                        g.dropIn = -1;
                        g.dragproxy.remove();
                    }
                    g.droptip.hide();
                    this.set('cursor', 'default');
                }
            });
            return drag;
        },
        _setDragToMove: function (value)
        {
            if (!$.fn.ligerDrag) return; //需要ligerDrag的支持
            var g = this, p = this.options;
            if (value)
            {
                if (g.drags) return;
                g.drags = g.drags || [];
                g.tab.links.ul.find('>li').each(function ()
                {
                    g.drags.push(g._applyDrag(this));
                });
            }
        },
        moveTabItem: function (fromTabItemID, toTabItemID)
        {
            var g = this;
            var from = g.tab.links.ul.find(">li[tabid=" + fromTabItemID + "]");
            var to = g.tab.links.ul.find(">li[tabid=" + toTabItemID + "]");
            var index1 = g.tab.links.ul.find(">li").index(from);
            var index2 = g.tab.links.ul.find(">li").index(to);
            if (index1 < index2)
            {
                to.after(from);
            }
            else
            {
                to.before(from);
            }
        },
        //设置tab按钮(左和右),显示返回true,隐藏返回false
        setTabButton: function ()
        {
            var g = this, p = this.options;
            var sumwidth = 0;
            $("li", g.tab.links.ul).each(function ()
            {
                sumwidth += $(this).width() + 2;
            });
            var mainwidth = g.tab.width();
            if (sumwidth > mainwidth)
            {
                g.tab.links.append('<div class="l-tab-links-left"></div><div class="l-tab-links-right"></div>');
                g.setTabButtonEven();
                return true;
            } else
            {
                g.tab.links.ul.animate({ left: 0 });
                $(".l-tab-links-left,.l-tab-links-right", g.tab.links).remove();
                return false;
            }
        },
        //设置左右按钮的事件 标签超出最大宽度时，可左右拖动
        setTabButtonEven: function ()
        {
            var g = this, p = this.options;
            $(".l-tab-links-left", g.tab.links).hover(function ()
            {
                $(this).addClass("l-tab-links-left-over");
            }, function ()
            {
                $(this).removeClass("l-tab-links-left-over");
            }).click(function ()
            {
                g.moveToPrevTabItem();
            });
            $(".l-tab-links-right", g.tab.links).hover(function ()
            {
                $(this).addClass("l-tab-links-right-over");
            }, function ()
            {
                $(this).removeClass("l-tab-links-right-over");
            }).click(function ()
            {
                g.moveToNextTabItem();
            });
        },
        //切换到上一个tab
        moveToPrevTabItem: function ()
        {
            var g = this, p = this.options;
            var btnWitdth = $(".l-tab-links-left", g.tab.links).width();
            var leftList = new Array(); //记录每个tab的left,由左到右
            $("li", g.tab.links).each(function (i, item)
            {
                var currentItemLeft = -1 * btnWitdth;
                if (i > 0)
                {
                    currentItemLeft = parseInt(leftList[i - 1]) + $(this).prev().width() + 2;
                }
                leftList.push(currentItemLeft);
            });
            var currentLeft = -1 * parseInt(g.tab.links.ul.css("left"));
            for (var i = 0; i < leftList.length - 1; i++)
            {
                if (leftList[i] < currentLeft && leftList[i + 1] >= currentLeft)
                {
                    g.tab.links.ul.animate({ left: -1 * parseInt(leftList[i]) });
                    return;
                }
            }
        },
        //切换到下一个tab
        moveToNextTabItem: function ()
        {
            var g = this, p = this.options;
            var btnWitdth = $(".l-tab-links-right", g.tab).width();
            var sumwidth = 0;
            var tabItems = $("li", g.tab.links.ul);
            tabItems.each(function ()
            {
                sumwidth += $(this).width() + 2;
            });
            var mainwidth = g.tab.width();
            var leftList = new Array(); //记录每个tab的left,由右到左 
            for (var i = tabItems.length - 1; i >= 0; i--)
            {
                var currentItemLeft = sumwidth - mainwidth + btnWitdth + 2;
                if (i != tabItems.length - 1)
                {
                    currentItemLeft = parseInt(leftList[tabItems.length - 2 - i]) - $(tabItems[i + 1]).width() - 2;
                }
                leftList.push(currentItemLeft);
            }
            var currentLeft = -1 * parseInt(g.tab.links.ul.css("left"));
            for (var j = 1; j < leftList.length; j++)
            {
                if (leftList[j] <= currentLeft && leftList[j - 1] > currentLeft)
                {
                    g.tab.links.ul.animate({ left: -1 * parseInt(leftList[j - 1]) });
                    return;
                }
            }
        },
        getTabItemCount: function ()
        {
            var g = this, p = this.options;
            return $("li", g.tab.links.ul).length;
        },
        getSelectedTabItemID: function ()
        {
            var g = this, p = this.options;
            return $("li.l-selected", g.tab.links.ul).attr("tabid");
        },
        removeSelectedTabItem: function ()
        {
            var g = this, p = this.options;
            g.removeTabItem(g.getSelectedTabItemID());
        },
        //覆盖选择的tabitem
        overrideSelectedTabItem: function (options)
        {
            var g = this, p = this.options;
            g.overrideTabItem(g.getSelectedTabItemID(), options);
        },
        //覆盖
        overrideTabItem: function (targettabid, options)
        {
            var g = this, p = this.options;
            if (g.trigger('beforeOverrideTabItem', [targettabid]) == false)
                return false;
            var tabid = options.tabid;
            if (tabid == undefined) tabid = g.getNewTabid();
            var url = options.url;
            var content = options.content;
            var target = options.target;
            var text = options.text;
            var showClose = options.showClose;
            var height = options.height;
            //如果已经存在
            if (g.isTabItemExist(tabid))
            {
                return;
            }
            var tabitem = $("li[tabid=" + targettabid + "]", g.tab.links.ul);
            var contentitem = $(".l-tab-content-item[tabid=" + targettabid + "]", g.tab.content);
            if (!tabitem || !contentitem) return;
            tabitem.attr("tabid", tabid);
            contentitem.attr("tabid", tabid);
            if ($("iframe", contentitem).length == 0 && url)
            {
                contentitem.html("<iframe frameborder='0'></iframe>");
            }
            else if (content)
            {
                contentitem.html(content);
            }
            $("iframe", contentitem).attr("name", tabid);
            if (showClose == undefined) showClose = true;
            if (showClose == false) $(".l-tab-links-item-close", tabitem).remove();
            else
            {
                if ($(".l-tab-links-item-close", tabitem).length == 0)
                    tabitem.append("<div class='l-tab-links-item-close'></div>");
            }
            if (text == undefined) text = tabid;
            if (height) contentitem.height(height);
            $("a", tabitem).text(text);
            $("iframe", contentitem).attr("src", url);


            g.trigger('afterOverrideTabItem', [targettabid]);
        },
        //选中tab项
        selectTabItem: function (tabid)
        {
            var g = this, p = this.options;
            if (g.trigger('beforeSelectTabItem', [tabid]) == false)
                return false;
            g.selectedTabId = tabid;
            $("> .l-tab-content-item[tabid=" + tabid + "]", g.tab.content).show().siblings().hide();
            $("li[tabid=" + tabid + "]", g.tab.links.ul).addClass("l-selected").siblings().removeClass("l-selected");
            g.trigger('afterSelectTabItem', [tabid]);
        },
        //移动到最后一个tab
        moveToLastTabItem: function ()
        {
            var g = this, p = this.options;
            var sumwidth = 0;
            $("li", g.tab.links.ul).each(function ()
            {
                sumwidth += $(this).width() + 2;
            });
            var mainwidth = g.tab.width();
            if (sumwidth > mainwidth)
            {
                var btnWitdth = $(".l-tab-links-right", g.tab.links).width();
                g.tab.links.ul.animate({ left: -1 * (sumwidth - mainwidth + btnWitdth + 2) });
            }
        },
        //判断tab是否存在
        isTabItemExist: function (tabid)
        {
            var g = this, p = this.options;
            return $("li[tabid=" + tabid + "]", g.tab.links.ul).length > 0;
        },
        //增加一个tab
        addTabItem: function (options)
        {
            var g = this, p = this.options;
            if (g.trigger('beforeAddTabItem', [tabid]) == false)
                return false;
            var tabid = options.tabid;
            if (tabid == undefined) tabid = g.getNewTabid();
            var url = options.url;
            var content = options.content;
            var text = options.text;
            var showClose = options.showClose;
            var height = options.height;
            //如果已经存在
            if (g.isTabItemExist(tabid))
            {
                g.selectTabItem(tabid);
                return;
            }
            var tabitem = $("<li><a></a><div class='l-tab-links-item-left'></div><div class='l-tab-links-item-right'></div><div class='l-tab-links-item-close'></div></li>");
            var contentitem = $("<div class='l-tab-content-item'><iframe frameborder='0'></iframe></div>");
            if (g.makeFullHeight)
            {
                var newheight = g.tab.height() - g.tab.links.height();
                contentitem.height(newheight);
            }
            tabitem.attr("tabid", tabid);
            contentitem.attr("tabid", tabid);
            $("iframe", contentitem).attr("name", tabid);
            $("iframe", contentitem).attr("id", tabid);
            if (showClose == undefined) showClose = true;
            if (showClose == false) $(".l-tab-links-item-close", tabitem).remove();
            if (text == undefined) text = tabid;
            if (height) contentitem.height(height);
            $("a", tabitem).text(text);
            $("iframe", contentitem).attr("src", url);
            g.tab.links.ul.append(tabitem);
            g.tab.content.append(contentitem);
            g.selectTabItem(tabid);
            if (g.setTabButton())
            {
                g.moveToLastTabItem();
            }
            //增加事件
            g._addTabItemEvent(tabitem);
            if (p.dragToMove && $.fn.ligerDrag)
            {
                g.drags = g.drags || [];
                tabitem.each(function ()
                {
                    g.drags.push(g._applyDrag(this));
                });
            }
            g.trigger('afterAddTabItem', [tabid]);
        },
        _addTabItemEvent: function (tabitem)
        {
            var g = this, p = this.options;
            tabitem.click(function ()
            {
                var tabid = $(this).attr("tabid");
                g.selectTabItem(tabid);
            });
            //右键事件支持
            g.tab.menu && g._addTabItemContextMenuEven(tabitem);
            $(".l-tab-links-item-close", tabitem).hover(function ()
            {
                $(this).addClass("l-tab-links-item-close-over");
            }, function ()
            {
                $(this).removeClass("l-tab-links-item-close-over");
            }).click(function ()
            {
                var tabid = $(this).parent().attr("tabid");
                g.removeTabItem(tabid);
            });

        },
        //移除tab项
        removeTabItem: function (tabid)
        {
            var g = this, p = this.options;
            if (g.trigger('beforeRemoveTabItem', [tabid]) == false)
                return false;
            var currentIsSelected = $("li[tabid=" + tabid + "]", g.tab.links.ul).hasClass("l-selected");
            if (currentIsSelected)
            {
                $(".l-tab-content-item[tabid=" + tabid + "]", g.tab.content).prev().show();
                $("li[tabid=" + tabid + "]", g.tab.links.ul).prev().addClass("l-selected").siblings().removeClass("l-selected");
            }
            $(".l-tab-content-item[tabid=" + tabid + "]", g.tab.content).remove();
            $("li[tabid=" + tabid + "]", g.tab.links.ul).remove();
            g.setTabButton();
            g.trigger('afterRemoveTabItem', [tabid]);
        },
        addHeight: function (heightDiff)
        {
            var g = this, p = this.options;
            var newHeight = g.tab.height() + heightDiff;
            g.setHeight(newHeight);
        },
        setHeight: function (height)
        {
            var g = this, p = this.options;
            g.tab.height(height);
            g.setContentHeight();
        },
        setContentHeight: function ()
        {
            var g = this, p = this.options;
            var newheight = g.tab.height() - g.tab.links.height();
            g.tab.content.height(newheight);
            $("> .l-tab-content-item", g.tab.content).height(newheight);
        },
        getNewTabid: function ()
        {
            var g = this, p = this.options;
            g.getnewidcount = g.getnewidcount || 0;
            return 'tabitem' + (++g.getnewidcount);
        },
        //notabid 过滤掉tabid的
        //noclose 过滤掉没有关闭按钮的
        getTabidList: function (notabid, noclose)
        {
            var g = this, p = this.options;
            var tabidlist = [];
            $("> li", g.tab.links.ul).each(function ()
            {
                if ($(this).attr("tabid")
                        && $(this).attr("tabid") != notabid
                        && (!noclose || $(".l-tab-links-item-close", this).length > 0))
                {
                    tabidlist.push($(this).attr("tabid"));
                }
            });
            return tabidlist;
        },
        removeOther: function (tabid, compel)
        {
            var g = this, p = this.options;
            var tabidlist = g.getTabidList(tabid, true);
            $(tabidlist).each(function ()
            {
                g.removeTabItem(this);
            });
        },
        reload: function (tabid)
        {
            var g = this, p = this.options;
            $(".l-tab-content-item[tabid=" + tabid + "] iframe", g.tab.content).each(function (i, iframe)
            {
                $(iframe).attr("src", $(iframe).attr("src"));
            });
        },
        removeAll: function (compel)
        {
            var g = this, p = this.options;
            var tabidlist = g.getTabidList(null, true);
            $(tabidlist).each(function ()
            {
                g.removeTabItem(this);
            });
        },
        onResize: function ()
        {
            var g = this, p = this.options;
            if (!p.height || typeof (p.height) != 'string' || p.height.indexOf('%') == -1) return false;
            //set tab height
            if (g.tab.parent()[0].tagName.toLowerCase() == "body")
            {
                var windowHeight = $(window).height();
                windowHeight -= parseInt(g.tab.parent().css('paddingTop'));
                windowHeight -= parseInt(g.tab.parent().css('paddingBottom'));
                g.height = p.heightDiff + windowHeight * parseFloat(g.height) * 0.01;
            }
            else
            {
                g.height = p.heightDiff + (g.tab.parent().height() * parseFloat(p.height) * 0.01);
            }
            g.tab.height(g.height);
            g.setContentHeight();
        },
        _menuItemClick: function (item)
        {
            var g = this, p = this.options;
            if (!item.id || !g.actionTabid) return;
            switch (item.id)
            {
                case "close":
                    g.removeTabItem(g.actionTabid);
                    g.actionTabid = null;
                    break;
                case "closeother":
                    g.removeOther(g.actionTabid);
                    break;
                case "closeall":
                    g.removeAll();
                    g.actionTabid = null;
                    break;
                case "reload":
                    g.selectTabItem(g.actionTabid);
                    g.reload(g.actionTabid);
                    break;
            }
        },
        _addTabItemContextMenuEven: function (tabitem)
        {
            var g = this, p = this.options;
            tabitem.bind("contextmenu", function (e)
            {
                if (!g.tab.menu) return;
                g.actionTabid = tabitem.attr("tabid");
                g.tab.menu.show({ top: e.pageY, left: e.pageX });
                if ($(".l-tab-links-item-close", this).length == 0)
                {
                    g.tab.menu.setDisabled('close');
                }
                else
                {
                    g.tab.menu.setEnabled('close');
                }
                return false;
            });
        }
    });



})(jQuery);