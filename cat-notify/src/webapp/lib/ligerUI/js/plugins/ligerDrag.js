/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{
    var l = $.ligerui;

    $.fn.ligerDrag = function (options)
    {
        return l.run.call(this, "ligerDrag", arguments,
        {
            idAttrName: 'ligeruidragid', hasElement: false, propertyToElemnt: 'target'
        }
        );
    };

    $.fn.ligerGetDragManager = function ()
    {
        return l.run.call(this, "ligerGetDragManager", arguments,
        {
            idAttrName: 'ligeruidragid', hasElement: false, propertyToElemnt: 'target'
        });
    };

    $.ligerDefaults.Drag = {
        onStartDrag: false,
        onDrag: false,
        onStopDrag: false,
        handler: null,
        //代理 拖动时的主体,可以是'clone'或者是函数,放回jQuery 对象
        proxy: true,
        revert: false,
        animate: true,
        onRevert: null,
        onEndRevert: null,
        //接收区域 jQuery对象或者jQuery选择字符
        receive: null,
        //进入区域
        onDragEnter: null,
        //在区域移动
        onDragOver: null,
        //离开区域
        onDragLeave: null,
        //在区域释放
        onDrop: null,
        disabled: false,
        proxyX: null,     //代理相对鼠标指针的位置,如果不设置则对应target的left
        proxyY: null
    };


    l.controls.Drag = function (options)
    {
        l.controls.Drag.base.constructor.call(this, null, options);
    };

    l.controls.Drag.ligerExtend(l.core.UIComponent, {
        __getType: function ()
        {
            return 'Drag';
        },
        __idPrev: function ()
        {
            return 'Drag';
        },
        _render: function ()
        {
            var g = this, p = this.options;
            this.set(p);
            g.cursor = "move";
            g.handler.css('cursor', g.cursor);
            g.handler.bind('mousedown.drag', function (e)
            {
                if (p.disabled) return;
                if (e.button == 2) return; 
                g._start.call(g, e);
            }).bind('mousemove.drag', function ()
            {
                if (p.disabled) return;
                g.handler.css('cursor', g.cursor);
            });
        },
        _rendered: function ()
        {
            this.options.target.ligeruidragid = this.id;
        },
        _start: function (e)
        {
            var g = this, p = this.options;
            if (g.reverting) return;
            if (p.disabled) return;
            g.current = {
                target: g.target,
                left: g.target.offset().left,
                top: g.target.offset().top,
                startX: e.pageX || e.screenX,
                startY: e.pageY || e.clientY
            };
            if (g.trigger('startDrag', [g.current, e]) == false) return false;
            g.cursor = "move";
            g._createProxy(p.proxy, e);
            //代理没有创建成功
            if (p.proxy && !g.proxy) return false;
            (g.proxy || g.handler).css('cursor', g.cursor);
            $(document).bind("selectstart.drag", function () { return false; });
            $(document).bind('mousemove.drag', function ()
            {
                g._drag.apply(g, arguments);
            });
            l.draggable.dragging = true;
            $(document).bind('mouseup.drag', function ()
            {
                l.draggable.dragging = false;
                g._stop.apply(g, arguments);
            });
        },
        _drag: function (e)
        {
            var g = this, p = this.options;
            if (!g.current) return;
            var pageX = e.pageX || e.screenX;
            var pageY = e.pageY || e.screenY;
            g.current.diffX = pageX - g.current.startX;
            g.current.diffY = pageY - g.current.startY;
            (g.proxy || g.handler).css('cursor', g.cursor);
            if (g.receive)
            {
                g.receive.each(function (i, obj)
                {
                    var receive = $(obj);
                    var xy = receive.offset();
                    if (pageX > xy.left && pageX < xy.left + receive.width()
                    && pageY > xy.top && pageY < xy.top + receive.height())
                    {
                        if (!g.receiveEntered[i])
                        {
                            g.receiveEntered[i] = true;
                            g.trigger('dragEnter', [obj, g.proxy || g.target, e]);
                        }
                        else
                        {
                            g.trigger('dragOver', [obj, g.proxy || g.target, e]);
                        }
                    }
                    else if (g.receiveEntered[i])
                    {
                        g.receiveEntered[i] = false;
                        g.trigger('dragLeave', [obj, g.proxy || g.target, e]);
                    }
                });
            }
            if (g.hasBind('drag'))
            {
                if (g.trigger('drag', [g.current, e]) != false)
                {
                    g._applyDrag();
                }
                else
                {
                    g._removeProxy();
                }
            }
            else
            {
                g._applyDrag();
            }
        },
        _stop: function (e)
        {
            var g = this, p = this.options;
            $(document).unbind('mousemove.drag');
            $(document).unbind('mouseup.drag');
            $(document).unbind("selectstart.drag");
            if (g.receive)
            {
                g.receive.each(function (i, obj)
                {
                    if (g.receiveEntered[i])
                    {
                        g.trigger('drop', [obj, g.proxy || g.target, e]);
                    }
                });
            }
            if (g.proxy)
            {
                if (p.revert)
                {
                    if (g.hasBind('revert'))
                    {
                        if (g.trigger('revert', [g.current, e]) != false)
                            g._revert(e);
                        else
                            g._removeProxy();
                    }
                    else
                    {
                        g._revert(e);
                    }
                }
                else
                {
                    g._applyDrag(g.target);
                    g._removeProxy();
                }
            }
            g.cursor = 'move';
            g.trigger('stopDrag', [g.current, e]);
            g.current = null;
            g.handler.css('cursor', g.cursor);
        },
        _revert: function (e)
        {
            var g = this;
            g.reverting = true;
            g.proxy.animate({
                left: g.current.left,
                top: g.current.top
            }, function ()
            {
                g.reverting = false;
                g._removeProxy();
                g.trigger('endRevert', [g.current, e]);
                g.current = null;
            });
        },
        _applyDrag: function (applyResultBody)
        {
            var g = this, p = this.options;
            applyResultBody = applyResultBody || g.proxy || g.target;
            var cur = {}, changed = false;
            var noproxy = applyResultBody == g.target;
            if (g.current.diffX)
            {
                if (noproxy || p.proxyX == null)
                    cur.left = g.current.left + g.current.diffX;
                else
                    cur.left = g.current.startX + p.proxyX + g.current.diffX;
                changed = true;
            }
            if (g.current.diffY)
            {
                if (noproxy || p.proxyY == null)
                    cur.top = g.current.top + g.current.diffY;
                else
                    cur.top = g.current.startY + p.proxyY + g.current.diffY;
                changed = true;
            }
            if (applyResultBody == g.target && g.proxy && p.animate)
            {
                g.reverting = true;
                applyResultBody.animate(cur, function ()
                {
                    g.reverting = false;
                });
            }
            else
            {
                applyResultBody.css(cur);
            }
        },
        _setReceive: function (receive)
        {
            this.receiveEntered = {};
            if (!receive) return;
            if (typeof receive == 'string')
                this.receive = $(receive);
            else
                this.receive = receive;
        },
        _setHandler: function (handler)
        {
            var g = this, p = this.options;
            if (!handler)
                g.handler = $(p.target);
            else
                g.handler = (typeof handler == 'string' ? $(handler, p.target) : handler);
        },
        _setTarget: function (target)
        {
            this.target = $(target);
        },
        _setCursor: function (cursor)
        {
            this.cursor = cursor;
            (this.proxy || this.handler).css('cursor', cursor);
        },
        _createProxy: function (proxy, e)
        {
            if (!proxy) return;
            var g = this, p = this.options;
            if (typeof proxy == 'function')
            {
                g.proxy = proxy.call(this.options.target, g, e);
            }
            else if (proxy == 'clone')
            {
                g.proxy = g.target.clone().css('position', 'absolute');
                g.proxy.appendTo('body');
            }
            else
            {
                g.proxy = $("<div class='l-draggable'></div>");
                g.proxy.width(g.target.width()).height(g.target.height())
                g.proxy.attr("dragid", g.id).appendTo('body');
            }
            g.proxy.css({
                left: p.proxyX == null ? g.current.left : g.current.startX + p.proxyX,
                top: p.proxyY == null ? g.current.top : g.current.startY + p.proxyY
            }).show();
        },
        _removeProxy: function ()
        {
            var g = this;
            if (g.proxy)
            {
                g.proxy.remove();
                g.proxy = null;
            }
        }

    });

})(jQuery);