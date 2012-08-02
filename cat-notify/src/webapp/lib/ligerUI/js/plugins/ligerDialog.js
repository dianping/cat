/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{
    var l = $.ligerui;

    $.ligerDialog = function ()
    {
        return l.run.call(null, "ligerDialog", arguments, { isStatic: true });
    };

    //dialog 图片文件夹的路径 预加载
    l.DialogImagePath = "../../lib/ligerUI/skins/Aqua/images/win/";

    function prevImage(paths)
    {
        for (var i in paths)
        {
            $('<img />').attr('src', l.DialogImagePath + paths[i]);
        }
    }
    prevImage(['dialog.gif', 'dialog-winbtns.gif', 'dialog-bc.gif', 'dialog-tc.gif']);

    $.ligerDefaults.Dialog = {
        cls: null,       //给dialog附加css class
        id: null,        //给dialog附加id
        buttons: null, //按钮集合 
        isDrag: true,   //是否拖动
        width: 280,     //宽度
        height: null,   //高度，默认自适应 
        content: '',    //内容
        target: null,   //目标对象，指定它将以appendTo()的方式载入
        url: null,      //目标页url，默认以iframe的方式载入
        load: false,     //是否以load()的方式加载目标页的内容
        onLoaded: null,
        type: 'none',   //类型 warn、success、error、question
        left: null,     //位置left
        top: null,      //位置top
        modal: true,    //是否模态对话框
        name: null,     //创建iframe时 作为iframe的name和id 
        isResize: false, // 是否调整大小
        allowClose: true, //允许关闭
        opener: null,
        timeParmName: null,  //是否给URL后面加上值为new Date().getTime()的参数，如果需要指定一个参数名即可
        closeWhenEnter: null, //回车时是否关闭dialog
        isHidden: true,        //关闭对话框时是否只是隐藏，还是销毁对话框
        show: true,          //初始化时是否马上显示
        title: '提示',        //头部 
        showMax: false,                             //是否显示最大化按钮 
        showToggle: false,                          //是否显示收缩窗口按钮
        showMin: false,                             //是否显示最小化按钮
        slide: $.browser.msie ? false : true,        //是否以动画的形式显示 
        fixedType: null,            //在固定的位置显示, 可以设置的值有n, e, s, w, ne, se, sw, nw
        showType: null             //显示类型,可以设置为slide(固定显示时有效)
    };
    $.ligerDefaults.DialogString = {
        titleMessage: '提示',                     //提示文本标题
        ok: '确定',
        yes: '是',
        no: '否',
        cancel: '取消',
        waittingMessage: '正在等待中,请稍候...'
    };

    $.ligerMethos.Dialog = $.ligerMethos.Dialog || {};


    l.controls.Dialog = function (options)
    {
        l.controls.Dialog.base.constructor.call(this, null, options);
    };
    l.controls.Dialog.ligerExtend(l.core.Win, {
        __getType: function ()
        {
            return 'Dialog';
        },
        __idPrev: function ()
        {
            return 'Dialog';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Dialog;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.set(p, true);
            var dialog = $('<div class="l-dialog"><table class="l-dialog-table" cellpadding="0" cellspacing="0" border="0"><tbody><tr><td class="l-dialog-tl"></td><td class="l-dialog-tc"><div class="l-dialog-tc-inner"><div class="l-dialog-icon"></div><div class="l-dialog-title"></div><div class="l-dialog-winbtns"><div class="l-dialog-winbtn l-dialog-close"></div></div></div></td><td class="l-dialog-tr"></td></tr><tr><td class="l-dialog-cl"></td><td class="l-dialog-cc"><div class="l-dialog-body"><div class="l-dialog-image"></div> <div class="l-dialog-content"></div><div class="l-dialog-buttons"><div class="l-dialog-buttons-inner"></div></td><td class="l-dialog-cr"></td></tr><tr><td class="l-dialog-bl"></td><td class="l-dialog-bc"></td><td class="l-dialog-br"></td></tr></tbody></table></div>');
            $('body').append(dialog);
            g.dialog = dialog;
            g.element = dialog[0];
            g.dialog.body = $(".l-dialog-body:first", g.dialog);
            g.dialog.header = $(".l-dialog-tc-inner:first", g.dialog);
            g.dialog.winbtns = $(".l-dialog-winbtns:first", g.dialog.header);
            g.dialog.buttons = $(".l-dialog-buttons:first", g.dialog);
            g.dialog.content = $(".l-dialog-content:first", g.dialog);
            g.set(p, false);

            if (p.allowClose == false) $(".l-dialog-close", g.dialog).remove();
            if (p.target || p.url || p.type == "none")
            {
                p.type = null;
                g.dialog.addClass("l-dialog-win");
            }
            if (p.cls) g.dialog.addClass(p.cls);
            if (p.id) g.dialog.attr("id", p.id);
            //设置锁定屏幕、拖动支持 和设置图片
            g.mask();
            if (p.isDrag)
                g._applyDrag();
            if (p.isResize)
                g._applyResize();
            if (p.type)
                g._setImage();
            else
            {
                $(".l-dialog-image", g.dialog).remove();
                g.dialog.content.addClass("l-dialog-content-noimage");
            }
            if (!p.show)
            {
                g.unmask();
                g.dialog.hide();
            }
            //设置主体内容
            if (p.target)
            {
                g.dialog.content.prepend(p.target);
                $(p.target).show();
            }
            else if (p.url)
            {
                if (p.timeParmName)
                {
                    p.url += p.url.indexOf('?') == -1 ? "?" : "&";
                    p.url += p.timeParmName + "=" + new Date().getTime();
                }
                if (p.load)
                {
                    g.dialog.body.load(p.url, function ()
                    {
                        g._saveStatus();
                        g.trigger('loaded');
                    });
                }
                else
                {
                    g.jiframe = $("<iframe frameborder='0'></iframe>");
                    var framename = p.name ? p.name : "ligerwindow" + new Date().getTime();
                    g.jiframe.attr("name", framename);
                    g.jiframe.attr("id", framename);
                    g.dialog.content.prepend(g.jiframe);
                    g.dialog.content.addClass("l-dialog-content-nopadding");
                    setTimeout(function ()
                    {
                        g.jiframe.attr("src", p.url);
                        g.frame = window.frames[g.jiframe.attr("name")];
                    }, 0);
                }
            }
            if (p.opener) g.dialog.opener = p.opener;
            //设置按钮
            if (p.buttons)
            {
                $(p.buttons).each(function (i, item)
                {
                    var btn = $('<div class="l-dialog-btn"><div class="l-dialog-btn-l"></div><div class="l-dialog-btn-r"></div><div class="l-dialog-btn-inner"></div></div>');
                    $(".l-dialog-btn-inner", btn).html(item.text);
                    $(".l-dialog-buttons-inner", g.dialog.buttons).prepend(btn);
                    item.width && btn.width(item.width);
                    item.onclick && btn.click(function () { item.onclick(item, g, i) });
                });
            } else
            {
                g.dialog.buttons.remove();
            }
            $(".l-dialog-buttons-inner", g.dialog.buttons).append("<div class='l-clear'></div>");


            $(".l-dialog-title", g.dialog)
            .bind("selectstart", function () { return false; });
            g.dialog.click(function ()
            {
                l.win.setFront(g);
            });

            //设置事件
            $(".l-dialog-btn", g.dialog.body).hover(function ()
            {
                $(this).addClass("l-dialog-btn-over");
            }, function ()
            {
                $(this).removeClass("l-dialog-btn-over");
            });
            $(".l-dialog-tc .l-dialog-close", g.dialog).hover(function ()
            {
                $(this).addClass("l-dialog-close-over");
            }, function ()
            {
                $(this).removeClass("l-dialog-close-over");
            }).click(function ()
            {
                if (p.isHidden)
                    g.hide();
                else
                    g.close();
            });
            if (!p.fixedType)
            {
                //位置初始化
                var left = 0;
                var top = 0;
                var width = p.width || g.dialog.width();
                if (p.slide == true) p.slide = 'fast';
                if (p.left != null) left = p.left;
                else p.left = left = 0.5 * ($(window).width() - width);
                if (p.top != null) top = p.top;
                else p.top = top = 0.5 * ($(window).height() - g.dialog.height()) + $(window).scrollTop() - 10;
                if (left < 0) p.left = left = 0;
                if (top < 0) p.top = top = 0;
                g.dialog.css({ left: left, top: top });
            }
            g.show();
            $('body').bind('keydown.dialog', function (e)
            {
                var key = e.which;
                if (key == 13)
                {
                    g.enter();
                }
                else if (key == 27)
                {
                    g.esc();
                }
            });

            g._updateBtnsWidth();
            g._saveStatus();
            g._onReisze();

        },
        _borderX: 12,
        _borderY: 32,
        doMax: function (slide)
        {
            var g = this, p = this.options;
            var width = $(window).width(), height = $(window).height(), left = 0, top = 0;
            if (l.win.taskbar)
            {
                height -= l.win.taskbar.outerHeight();
                if (l.win.top) top += l.win.taskbar.outerHeight();
            }
            if (slide)
            {
                g.dialog.body.animate({ width: width - g._borderX }, p.slide);
                g.dialog.animate({ left: left, top: top }, p.slide);
                g.dialog.content.animate({ height: height - g._borderY - g.dialog.buttons.outerHeight() }, p.slide, function ()
                {
                    g._onReisze();
                });
            }
            else
            {
                g.set({ width: width, height: height, left: left, top: top });
                g._onReisze();
            }
        },
        //最大化
        max: function ()
        {
            var g = this, p = this.options;
            if (g.winmax)
            {
                g.winmax.addClass("l-dialog-recover");
                g.doMax(p.slide);
                if (g.wintoggle)
                {
                    if (g.wintoggle.hasClass("l-dialog-extend"))
                        g.wintoggle.addClass("l-dialog-toggle-disabled l-dialog-extend-disabled");
                    else
                        g.wintoggle.addClass("l-dialog-toggle-disabled l-dialog-collapse-disabled");
                }
                if (g.resizable) g.resizable.set({ disabled: true });
                if (g.draggable) g.draggable.set({ disabled: true });
                g.maximum = true;

                $(window).bind('resize.dialogmax', function ()
                {
                    g.doMax(false);
                });
            }
        },

        //恢复
        recover: function ()
        {
            var g = this, p = this.options;
            if (g.winmax)
            {
                g.winmax.removeClass("l-dialog-recover");
                if (p.slide)
                {
                    g.dialog.body.animate({ width: g._width - g._borderX }, p.slide);
                    g.dialog.animate({ left: g._left, top: g._top }, p.slide);
                    g.dialog.content.animate({ height: g._height - g._borderY - g.dialog.buttons.outerHeight() }, p.slide, function ()
                    {
                        g._onReisze();
                    });
                }
                else
                {
                    g.set({ width: g._width, height: g._height, left: g._left, top: g._top });
                    g._onReisze();
                }
                if (g.wintoggle)
                {
                    g.wintoggle.removeClass("l-dialog-toggle-disabled l-dialog-extend-disabled l-dialog-collapse-disabled");
                }

                $(window).unbind('resize.dialogmax');
            }
            if (this.resizable) this.resizable.set({ disabled: false });
            if (g.draggable) g.draggable.set({ disabled: false });
            g.maximum = false;
        },

        //最小化
        min: function ()
        {
            var g = this, p = this.options;
            var task = l.win.getTask(this);
            if (p.slide)
            {
                g.dialog.body.animate({ width: 1 }, p.slide);
                task.y = task.offset().top + task.height();
                task.x = task.offset().left + task.width() / 2;
                g.dialog.animate({ left: task.x, top: task.y }, p.slide, function ()
                {
                    g.dialog.hide();
                });
            }
            else
            {
                g.dialog.hide();
            }
            g.unmask();
            g.minimize = true;
            g.actived = false;
        },

        active: function ()
        {
            var g = this, p = this.options;
            if (g.minimize)
            {
                var width = g._width, height = g._height, left = g._left, top = g._top;
                if (g.maximum)
                {
                    width = $(window).width();
                    height = $(window).height();
                    left = top = 0;
                    if (l.win.taskbar)
                    {
                        height -= l.win.taskbar.outerHeight();
                        if (l.win.top) top += l.win.taskbar.outerHeight();
                    }
                }
                if (p.slide)
                {
                    g.dialog.body.animate({ width: width - g._borderX }, p.slide);
                    g.dialog.animate({ left: left, top: top }, p.slide);
                }
                else
                {
                    g.set({ width: width, height: height, left: left, top: top });
                }
            }
            g.actived = true;
            g.minimize = false;
            l.win.setFront(g);
            g.show();
        },

        //展开 收缩
        toggle: function ()
        {

            var g = this, p = this.options;
            if (!g.wintoggle) return;
            if (g.wintoggle.hasClass("l-dialog-extend"))
                g.extend();
            else
                g.collapse();
        },

        //收缩
        collapse: function ()
        {
            var g = this, p = this.options;
            if (!g.wintoggle) return;
            if (p.slide)
                g.dialog.content.animate({ height: 1 }, p.slide);
            else
                g.dialog.content.height(1);
            if (this.resizable) this.resizable.set({ disabled: true });
        },

        //展开
        extend: function ()
        {
            var g = this, p = this.options;
            if (!g.wintoggle) return;
            var contentHeight = g._height - g._borderY - g.dialog.buttons.outerHeight();
            if (p.slide)
                g.dialog.content.animate({ height: contentHeight }, p.slide);
            else
                g.dialog.content.height(contentHeight);
            if (this.resizable) this.resizable.set({ disabled: false });
        },
        _updateBtnsWidth: function ()
        {
            var g = this;
            var btnscount = $(">div", g.dialog.winbtns).length;
            g.dialog.winbtns.width(22 * btnscount);
        },
        _setLeft: function (value)
        {
            if (!this.dialog) return;
            if (value != null)
                this.dialog.css({ left: value });
        },
        _setTop: function (value)
        {
            if (!this.dialog) return;
            if (value != null)
                this.dialog.css({ top: value });
        },
        _setWidth: function (value)
        {
            if (!this.dialog) return;
            if (value >= this._borderX)
            {
                this.dialog.body.width(value - this._borderX);
            }
        },
        _setHeight: function (value)
        {
            var g = this, p = this.options;
            if (!this.dialog) return;
            if (value >= this._borderY)
            {
                var height = value - this._borderY - g.dialog.buttons.outerHeight();
                g.dialog.content.height(height);
            }
        },
        _setShowMax: function (value)
        {
            var g = this, p = this.options;
            if (value)
            {
                if (!g.winmax)
                {
                    g.winmax = $('<div class="l-dialog-winbtn l-dialog-max"></div>').appendTo(g.dialog.winbtns)
                    .hover(function ()
                    {
                        if ($(this).hasClass("l-dialog-recover"))
                            $(this).addClass("l-dialog-recover-over");
                        else
                            $(this).addClass("l-dialog-max-over");
                    }, function ()
                    {
                        $(this).removeClass("l-dialog-max-over l-dialog-recover-over");
                    }).click(function ()
                    {
                        if ($(this).hasClass("l-dialog-recover"))
                            g.recover();
                        else
                            g.max();
                    });
                }
            }
            else if (g.winmax)
            {
                g.winmax.remove();
                g.winmax = null;
            }
            g._updateBtnsWidth();
        },
        _setShowMin: function (value)
        {
            var g = this, p = this.options;
            if (value)
            {
                if (!g.winmin)
                {
                    g.winmin = $('<div class="l-dialog-winbtn l-dialog-min"></div>').appendTo(g.dialog.winbtns)
                    .hover(function ()
                    {
                        $(this).addClass("l-dialog-min-over");
                    }, function ()
                    {
                        $(this).removeClass("l-dialog-min-over");
                    }).click(function ()
                    {
                        g.min();
                    });
                    l.win.addTask(g);
                }
            }
            else if (g.winmin)
            {
                g.winmin.remove();
                g.winmin = null;
            }
            g._updateBtnsWidth();
        },
        _setShowToggle: function (value)
        {
            var g = this, p = this.options;
            if (value)
            {
                if (!g.wintoggle)
                {
                    g.wintoggle = $('<div class="l-dialog-winbtn l-dialog-collapse"></div>').appendTo(g.dialog.winbtns)
                   .hover(function ()
                   {
                       if ($(this).hasClass("l-dialog-toggle-disabled")) return;
                       if ($(this).hasClass("l-dialog-extend"))
                           $(this).addClass("l-dialog-extend-over");
                       else
                           $(this).addClass("l-dialog-collapse-over");
                   }, function ()
                   {
                       $(this).removeClass("l-dialog-extend-over l-dialog-collapse-over");
                   }).click(function ()
                   {
                       if ($(this).hasClass("l-dialog-toggle-disabled")) return;
                       if (g.wintoggle.hasClass("l-dialog-extend"))
                       {
                           if (g.trigger('extend') == false) return;
                           g.wintoggle.removeClass("l-dialog-extend");
                           g.extend();
                           g.trigger('extended');
                       }
                       else
                       {
                           if (g.trigger('collapse') == false) return;
                           g.wintoggle.addClass("l-dialog-extend");
                           g.collapse();
                           g.trigger('collapseed')
                       }
                   });
                }
            }
            else if (g.wintoggle)
            {
                g.wintoggle.remove();
                g.wintoggle = null;
            }
        },
        //按下回车
        enter: function ()
        {
            var g = this, p = this.options;
            var isClose;
            if (p.closeWhenEnter != undefined)
            {
                isClose = p.closeWhenEnter;
            }
            else if (p.type == "warn" || p.type == "error" || p.type == "success" || p.type == "question")
            {
                isClose = true;
            }
            if (isClose)
            {
                g.close();
            }
        },
        esc: function ()
        {

        },
        _removeDialog: function ()
        {
            var g = this, p = this.options;
            if (p.showType && p.fixedType)
            {
                g.dialog.animate({ bottom: -1 * p.height }, function ()
                {
                    g.dialog.remove();
                });
            } else
            {
                g.dialog.remove();
            }
        },
        close: function ()
        {
            var g = this, p = this.options;
            l.win.removeTask(this);
            if (g.frame)
            {
                $(g.frame.document).ready(function ()
                {
                    g.unmask();
                    g._removeDialog();
                });
            }
            else
            {
                g.unmask();
                g._removeDialog();
            }
            $('body').unbind('keydown.dialog');
        },
        _getVisible: function ()
        {
            return this.dialog.is(":visible");
        },
        _setUrl: function (url)
        {
            var g = this, p = this.options;
            p.url = url;
            if (p.load)
            {
                g.dialog.body.html("").load(p.url, function ()
                {
                    g.trigger('loaded');
                });
            }
            else if (g.jiframe)
            {
                g.jiframe.attr("src", p.url);
            }
        },
        _setContent: function (content)
        {
            this.dialog.content.html(content);
        },
        _setTitle: function (value)
        {
            var g = this; var p = this.options;
            if (value)
            {
                $(".l-dialog-title", g.dialog).html(value);
            }
        },
        _hideDialog: function ()
        {
            var g = this, p = this.options;
            if (p.showType && p.fixedType)
            {
                g.dialog.animate({ bottom: -1 * p.height }, function ()
                {
                    g.dialog.hide();
                });
            } else
            {
                g.dialog.hide();
            }
        },
        hidden: function ()
        {
            var g = this;
            l.win.removeTask(this);
            if (g.frame)
            {
                $(g.frame.document).ready(function ()
                {
                    g.unmask();
                    g._hideDialog();

                });
            }
            else
            {
                g.unmask();
                g._hideDialog();
            }
        },
        show: function ()
        {
            var g = this, p = this.options;
            g.mask();
            if (p.fixedType)
            {
                if (p.showType)
                {
                    g.dialog.css({ bottom: -1 * p.height }).addClass("l-dialog-fixed");
                    g.dialog.show().animate({ bottom: 0 });
                }
                else
                {
                    g.dialog.show().css({ bottom: 0 });
                }
            }
            else
            {
                g.dialog.show();
            }
        },
        setUrl: function (url)
        {
            this._setUrl(url);
        },
        _saveStatus: function ()
        {
            var g = this;
            g._width = g.dialog.body.width();
            g._height = g.dialog.body.height();
            var top = 0;
            var left = 0;
            if (!isNaN(parseInt(g.dialog.css('top'))))
                top = parseInt(g.dialog.css('top'));
            if (!isNaN(parseInt(g.dialog.css('left'))))
                left = parseInt(g.dialog.css('left'));
            g._top = top;
            g._left = left;
        },
        _applyDrag: function ()
        {
            var g = this;
            if ($.fn.ligerDrag)
                g.draggable = g.dialog.ligerDrag({ handler: '.l-dialog-title', animate: false,
                    onStartDrag: function ()
                    {
                        l.win.setFront(g);
                    },
                    onStopDrag: function ()
                    {
                        g._saveStatus();
                    }
                });
        },
        _onReisze: function ()
        {
            var g = this, p = this.options;
            if (p.target)
            {
                var manager = $(p.target).liger();
                if (!manager) manager = $(p.target).find(":first").liger();
                if (!manager) return;
                var contentHeight = g.dialog.content.height();
                var contentWidth = g.dialog.content.width();
                manager.trigger('resize', [{ width: contentWidth, height: contentHeight}]);
            }
        },
        _applyResize: function ()
        {
            var g = this;
            if ($.fn.ligerResizable)
            {
                g.resizable = g.dialog.ligerResizable({
                    onStopResize: function (current, e)
                    {
                        var top = 0;
                        var left = 0;
                        if (!isNaN(parseInt(g.dialog.css('top'))))
                            top = parseInt(g.dialog.css('top'));
                        if (!isNaN(parseInt(g.dialog.css('left'))))
                            left = parseInt(g.dialog.css('left'));
                        if (current.diffLeft)
                        {
                            g.set({ left: left + current.diffLeft });
                        }
                        if (current.diffTop)
                        {
                            g.set({ top: top + current.diffTop });
                        }
                        if (current.newWidth)
                        {
                            g.set({ width: current.newWidth });
                            g.dialog.body.css({ width: current.newWidth - g._borderX });
                        }
                        if (current.newHeight)
                        {
                            g.set({ height: current.newHeight });
                        }
                        g._onReisze();
                        g._saveStatus();
                        return false;
                    }, animate: false
                });
            }
        },
        _setImage: function ()
        {
            var g = this;
            if (p.type)
            {
                if (p.type == 'success' || p.type == 'donne' || p.type == 'ok')
                {
                    $(".l-dialog-image", g.dialog).addClass("l-dialog-image-donne").show();
                    g.dialog.content.css({ paddingLeft: 64, paddingBottom: 30 });
                }
                else if (p.type == 'error')
                {
                    $(".l-dialog-image", g.dialog).addClass("l-dialog-image-error").show();
                    g.dialog.content.css({ paddingLeft: 64, paddingBottom: 30 });
                }
                else if (p.type == 'warn')
                {
                    $(".l-dialog-image", g.dialog).addClass("l-dialog-image-warn").show();
                    g.dialog.content.css({ paddingLeft: 64, paddingBottom: 30 });
                }
                else if (p.type == 'question')
                {
                    $(".l-dialog-image", g.dialog).addClass("l-dialog-image-question").show();
                    g.dialog.content.css({ paddingLeft: 64, paddingBottom: 40 });
                }
            }
        }
    });
    l.controls.Dialog.prototype.hide = l.controls.Dialog.prototype.hidden;



    $.ligerDialog.open = function (p)
    {
        return $.ligerDialog(p);
    };
    $.ligerDialog.close = function ()
    {
        var dialogs = l.find(l.controls.Dialog.prototype.__getType());
        for (var i in dialogs)
        {
            var d = dialogs[i];
            d.destroy.ligerDefer(d, 5);
        }
    };
    $.ligerDialog.show = function (p)
    {
        var dialogs = l.find(l.controls.Dialog.prototype.__getType());
        if (dialogs.length)
        {
            for (var i in dialogs)
            {
                dialogs[i].show();
                return;
            }
        }
        return $.ligerDialog(p);
    };
    $.ligerDialog.hide = function ()
    {
        var dialogs = l.find(l.controls.Dialog.prototype.__getType());
        for (var i in dialogs)
        {
            var d = dialogs[i];
            d.hide();
        }
    };
    $.ligerDialog.tip = function (options)
    {
        options = $.extend({
            showType: 'slide',
            width: 240,
            modal: false,
            height: 100
        }, options || {});

        $.extend(options, {
            fixedType: 'se',
            type: 'none',
            isDrag: false,
            isResize: false,
            showMax: false,
            showToggle: false,
            showMin: false
        });
        return $.ligerDialog.open(options);
    };
    $.ligerDialog.alert = function (content, title, type, callback)
    {
        content = content || "";
        if (typeof (title) == "function")
        {

            callback = title;
            type = null;
        }
        else if (typeof (type) == "function")
        {
            callback = type;
        }
        var btnclick = function (item, Dialog, index)
        {
            Dialog.close();
            if (callback)
                callback(item, Dialog, index);
        };
        p = {
            content: content,
            buttons: [{ text: $.ligerDefaults.DialogString.ok, onclick: btnclick}]
        };
        if (typeof (title) == "string" && title != "") p.title = title;
        if (typeof (type) == "string" && type != "") p.type = type;
        $.extend(p, {
            showMax: false,
            showToggle: false,
            showMin: false
        });
        return $.ligerDialog(p);
    };

    $.ligerDialog.confirm = function (content, title, callback)
    {
        if (typeof (title) == "function")
        {
            callback = title;
            type = null;
        }
        var btnclick = function (item, Dialog)
        {
            Dialog.close();
            if (callback)
            {
                callback(item.type == 'ok');
            }
        };
        p = {
            type: 'question',
            content: content,
            buttons: [{ text: $.ligerDefaults.DialogString.yes, onclick: btnclick, type: 'ok' }, { text: $.ligerDefaults.DialogString.no, onclick: btnclick, type: 'no'}]
        };
        if (typeof (title) == "string" && title != "") p.title = title;
        $.extend(p, {
            fixedType: 'se',
            showMax: false,
            showToggle: false,
            showMin: false
        });
        return $.ligerDialog(p);
    };
    $.ligerDialog.warning = function (content, title, callback)
    {
        if (typeof (title) == "function")
        {
            callback = title;
            type = null;
        }
        var btnclick = function (item, Dialog)
        {
            Dialog.close();
            if (callback)
            {
                callback(item.type);
            }
        };
        p = {
            type: 'question',
            content: content,
            buttons: [{ text: $.ligerDefaults.DialogString.yes, onclick: btnclick, type: 'yes' }, { text: $.ligerDefaults.DialogString.no, onclick: btnclick, type: 'no' }, { text: $.ligerDefaults.DialogString.cancel, onclick: btnclick, type: 'cancel'}]
        };
        if (typeof (title) == "string" && title != "") p.title = title;
        $.extend(p, {
            fixedType: 'se',
            showMax: false,
            showToggle: false,
            showMin: false
        });
        return $.ligerDialog(p);
    };
    $.ligerDialog.waitting = function (title)
    {
        title = title || $.ligerDefaults.Dialog.waittingMessage;
        return $.ligerDialog.open({ cls: 'l-dialog-waittingdialog', type: 'none', content: '<div style="padding:4px">' + title + '</div>', allowClose: false });
    };
    $.ligerDialog.closeWaitting = function ()
    {
        var dialogs = l.find(l.controls.Dialog.prototype.__getType());
        for (var i in dialogs)
        {
            var d = dialogs[i];
            if (d.dialog.hasClass("l-dialog-waittingdialog"))
                d.close();
        }
    };
    $.ligerDialog.success = function (content, title, onBtnClick)
    {
        return $.ligerDialog.alert(content, title, 'success', onBtnClick);
    };
    $.ligerDialog.error = function (content, title, onBtnClick)
    {
        return $.ligerDialog.alert(content, title, 'error', onBtnClick);
    };
    $.ligerDialog.warn = function (content, title, onBtnClick)
    {
        return $.ligerDialog.alert(content, title, 'warn', onBtnClick);
    };
    $.ligerDialog.question = function (content, title)
    {
        return $.ligerDialog.alert(content, title, 'question');
    };


    $.ligerDialog.prompt = function (title, value, multi, callback)
    {
        var target = $('<input type="text" class="l-dialog-inputtext"/>');
        if (typeof (multi) == "function")
        {
            callback = multi;
        }
        if (typeof (value) == "function")
        {
            callback = value;
        }
        else if (typeof (value) == "boolean")
        {
            multi = value;
        }
        if (typeof (multi) == "boolean" && multi)
        {
            target = $('<textarea class="l-dialog-textarea"></textarea>');
        }
        if (typeof (value) == "string" || typeof (value) == "int")
        {
            target.val(value);
        }
        var btnclick = function (item, Dialog, index)
        {
            Dialog.close();
            if (callback)
            {
                callback(item.type == 'yes', target.val());
            }
        }
        p = {
            title: title,
            target: target,
            width: 320,
            buttons: [{ text: $.ligerDefaults.DialogString.ok, onclick: btnclick, type: 'yes' }, { text: $.ligerDefaults.DialogString.cancel, onclick: btnclick, type: 'cancel'}]
        };
        return $.ligerDialog(p);
    };


})(jQuery);