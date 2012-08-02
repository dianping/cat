/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{


    //ligerui 继承方法
    Function.prototype.ligerExtend = function (parent, overrides)
    {
        if (typeof parent != 'function') return this;
        //保存对父类的引用
        this.base = parent.prototype;
        this.base.constructor = parent;
        //继承
        var f = function () { };
        f.prototype = parent.prototype;
        this.prototype = new f();
        this.prototype.constructor = this;
        //附加属性方法
        if (overrides) $.extend(this.prototype, overrides);
    };
    //延时加载
    Function.prototype.ligerDefer = function (o, defer, args)
    {
        var fn = this;
        return setTimeout(function () { fn.apply(o, args || []); }, defer);
    };

    // 核心对象
    $.ligerui = {
        version: 'V1.1.6',
        managerCount: 0,
        //组件管理器池
        managers: {},
        managerIdPrev: 'ligerui',
        //错误提示
        error: {
            managerIsExist: '管理器id已经存在'
        },
        getId: function (prev)
        {
            prev = prev || this.managerIdPrev;
            var id = prev + (1000 + this.managerCount);
            this.managerCount++;
            return id;
        },
        add: function (manager)
        {
            if (arguments.length == 2)
            {
                var m = arguments[1];
                m.id = m.id || m.options.id || arguments[0].id;
                this.addManager(m);
                return;
            }
            if (!manager.id) manager.id = this.getId(manager.__idPrev());
            if (this.managers[manager.id])
                throw new Error(this.error.managerIsExist);
            this.managers[manager.id] = manager;
        },

        //获取ligerui对象
        //1,传入ligerui ID
        //2,传入Dom Object Array(jQuery)
        get: function (arg, idAttrName)
        {
            idAttrName = idAttrName || "ligeruiid";
            if (typeof arg == "string" || typeof arg == "number")
            {
                return $.ligerui.managers[arg];
            }
            else if (typeof arg == "object" && arg.length)
            {
                if (!arg[0][idAttrName] && !$(arg[0]).attr(idAttrName)) return null;
                return $.ligerui.managers[arg[0][idAttrName] || $(arg[0]).attr(idAttrName)];
            }
            return null;
        },
        //根据类型查找某一个对象
        find: function (type)
        {
            var arr = [];
            for (var id in this.managers)
            {
                var manager = this.managers[id];
                if (type instanceof Function)
                {
                    if (manager instanceof type)
                    {
                        arr.push(manager);
                    }
                }
                else if (type instanceof Array)
                {
                    if ($.inArray(manager.__getType(), type) != -1)
                    {
                        arr.push(manager);
                    }
                }
                else
                {
                    if (manager.__getType() == type)
                    {
                        arr.push(manager);
                    }
                }
            }
            return arr;
        },
        //$.fn.liger{Plugin} 和 $.fn.ligerGet{Plugin}Manager
        //会调用这个方法,并传入作用域(this)
        //@parm [plugin]  插件名
        //@parm [args] 参数(数组)
        //@parm [ext] 扩展参数,定义命名空间或者id属性名
        run: function (plugin, args, ext)
        {
            if (!plugin) return;
            ext = $.extend({
                defaultsNamespace: 'ligerDefaults',
                methodsNamespace: 'ligerMethods',
                controlNamespace: 'controls',
                idAttrName: 'ligeruiid',
                isStatic: false,
                hasElement: true,           //是否拥有element主体(比如drag、resizable等辅助性插件就不拥有)
                propertyToElemnt: null      //链接到element的属性名
            }, ext || {});
            plugin = plugin.replace(/^ligerGet/, '');
            plugin = plugin.replace(/^liger/, '');
            if (this == null || this == window || ext.isStatic)
            {
                if (!$.ligerui.plugins[plugin])
                {
                    $.ligerui.plugins[plugin] = {
                        fn: $['liger' + plugin],
                        isStatic: true
                    };
                }
                return new $.ligerui[ext.controlNamespace][plugin]($.extend({}, $[ext.defaultsNamespace][plugin] || {}, $[ext.defaultsNamespace][plugin + 'String'] || {}, args.length > 0 ? args[0] : {}));
            }
            if (!$.ligerui.plugins[plugin])
            {
                $.ligerui.plugins[plugin] = {
                    fn: $.fn['liger' + plugin],
                    isStatic: false
                };
            }
            if (/Manager$/.test(plugin)) return $.ligerui.get(this, ext.idAttrName);
            this.each(function ()
            {
                if (this[ext.idAttrName] || $(this).attr(ext.idAttrName)) return;  //已经执行过 
                if (args.length >= 1 && typeof args[0] == 'string') return;
                //只要第一个参数不是string类型,都执行组件的实例化工作
                var options = args.length > 0 ? args[0] : null;
                var p = $.extend({}, $[ext.defaultsNamespace][plugin] || {}
                , $[ext.defaultsNamespace][plugin + 'String'] || {}, options || {});
                if (ext.propertyToElemnt) p[ext.propertyToElemnt] = this;
                if (ext.hasElement)
                {
                    new $.ligerui[ext.controlNamespace][plugin](this, p);
                }
                else
                {
                    new $.ligerui[ext.controlNamespace][plugin](p);
                }
            });
            if (this.length == 0) return null;
            if (args.length == 0) return $.ligerui.get(this, ext.idAttrName);
            if (typeof args[0] == 'object') return $.ligerui.get(this, ext.idAttrName);
            if (typeof args[0] == 'string')
            {
                var manager = $.ligerui.get(this, ext.idAttrName);
                if (manager == null) return;
                if (args[0] == "option")
                {
                    if (args.length == 2)
                        return manager.get(args[1]);  //manager get
                    else if (args.length >= 3)
                        return manager.set(args[1], args[2]);  //manager set
                }
                else
                {
                    var method = args[0];
                    if (!manager[method]) return; //不存在这个方法
                    var parms = Array.apply(null, args);
                    parms.shift();
                    return manager[method].apply(manager, parms);  //manager method
                }
            }
            return null;
        },

        //扩展
        //1,默认参数     
        //2,本地化扩展 
        defaults: {},
        //3,方法接口扩展
        methods: {},
        //命名空间
        //核心控件,封装了一些常用方法
        core: {},
        //命名空间
        //组件的集合
        controls: {},
        //plugin 插件的集合
        plugins: {}
    };


    //扩展对象
    $.ligerDefaults = {};

    //扩展对象
    $.ligerMethos = {};

    //关联起来
    $.ligerui.defaults = $.ligerDefaults;
    $.ligerui.methods = $.ligerMethos;

    //获取ligerui对象
    //@parm [plugin]  插件名,可为空
    $.fn.liger = function (plugin)
    {
        if (plugin)
        {
            return $.ligerui.run.call(this, plugin, arguments);
        }
        else
        {
            return $.ligerui.get(this);
        }
    };


    //组件基类
    //1,完成定义参数处理方法和参数属性初始化的工作
    //2,完成定义事件处理方法和事件属性初始化的工作
    $.ligerui.core.Component = function (options)
    {
        //事件容器
        this.events = this.events || {};
        //配置参数
        this.options = options || {};
        //子组件集合索引
        this.children = {};
    };
    $.extend($.ligerui.core.Component.prototype, {
        __getType: function ()
        {
            return '$.ligerui.core.Component';
        },
        __idPrev: function ()
        {
            return 'ligerui';
        },

        //设置属性
        // arg 属性名    value 属性值 
        // arg 属性/值   value 是否只设置事件
        set: function (arg, value)
        {
            if (!arg) return;
            if (typeof arg == 'object')
            {
                var tmp;
                if (this.options != arg)
                {
                    $.extend(this.options, arg);
                    tmp = arg;
                }
                else
                {
                    tmp = $.extend({}, arg);
                }
                if (value == undefined || value == true)
                {
                    for (var p in tmp)
                    {
                        if (p.indexOf('on') == 0)
                            this.set(p, tmp[p]);
                    }
                }
                if (value == undefined || value == false)
                {
                    for (var p in tmp)
                    {
                        if (p.indexOf('on') != 0)
                            this.set(p, tmp[p]);
                    }
                }
                return;
            }
            var name = arg;
            //事件参数
            if (name.indexOf('on') == 0)
            {
                if (typeof value == 'function')
                    this.bind(name.substr(2), value);
                return;
            }
            this.trigger('propertychange', arg, value);
            this.options[name] = value;
            var pn = '_set' + name.substr(0, 1).toUpperCase() + name.substr(1);
            if (this[pn])
            {
                this[pn].call(this, value);
            }
            this.trigger('propertychanged', arg, value);
        },

        //获取属性
        get: function (name)
        {
            var pn = '_get' + name.substr(0, 1).toUpperCase() + name.substr(1);
            if (this[pn])
            {
                return this[pn].call(this, name);
            }
            return this.options[name];
        },

        hasBind: function (arg)
        {
            var name = arg.toLowerCase();
            var event = this.events[name];
            if (event && event.length) return true;
            return false;
        },

        //触发事件
        //data (可选) Array(可选)传递给事件处理函数的附加参数
        trigger: function (arg, data)
        {
            var name = arg.toLowerCase();
            var event = this.events[name];
            if (!event) return;
            data = data || [];
            if ((data instanceof Array) == false)
            {
                data = [data];
            }
            for (var i = 0; i < event.length; i++)
            {
                var ev = event[i];
                if (ev.handler.apply(ev.context, data) == false)
                    return false;
            }
        },

        //绑定事件
        bind: function (arg, handler, context)
        {
            if (typeof arg == 'object')
            {
                for (var p in arg)
                {
                    this.bind(p, arg[p]);
                }
                return;
            }
            if (typeof handler != 'function') return false;
            var name = arg.toLowerCase();
            var event = this.events[name] || [];
            context = context || this;
            event.push({ handler: handler, context: context });
            this.events[name] = event;
        },

        //取消绑定
        unbind: function (arg, handler)
        {
            if (!arg)
            {
                this.events = {};
                return;
            }
            var name = arg.toLowerCase();
            var event = this.events[name];
            if (!event || !event.length) return;
            if (!handler)
            {
                delete this.events[name];
            }
            else
            {
                for (var i = 0, l = event.length; i < l; i++)
                {
                    if (event[i].handler == handler)
                    {
                        event.splice(i, 1);
                        break;
                    }
                }
            }
        },
        destroy: function () { }
    });


    //界面组件基类, 
    //1,完成界面初始化:设置组件id并存入组件管理器池,初始化参数
    //2,渲染的工作,细节交给子类实现
    //@parm [element] 组件对应的dom element对象
    //@parm [options] 组件的参数
    $.ligerui.core.UIComponent = function (element, options)
    {
        $.ligerui.core.UIComponent.base.constructor.call(this, options);
        var extendMethods = this._extendMethods();
        if (extendMethods) $.extend(this, extendMethods);
        this.element = element;
        this._init();
        this._preRender();
        this.trigger('render');
        this._render();
        this.trigger('rendered');
        this._rendered();
    };
    $.ligerui.core.UIComponent.ligerExtend($.ligerui.core.Component, {
        __getType: function ()
        {
            return '$.ligerui.core.UIComponent';
        },
        //扩展方法
        _extendMethods: function ()
        {

        },
        _init: function ()
        {
            this.type = this.__getType();
            if (!this.element)
            {
                this.id = this.options.id || $.ligerui.getId(this.__idPrev());
            }
            else
            {
                this.id = this.options.id || this.element.id || $.ligerui.getId(this.__idPrev());
            }
            //存入管理器池
            $.ligerui.add(this);

            if (!this.element) return;

            //读取attr方法,并加载到参数,比如['url']
            var attributes = this.attr();
            if (attributes && attributes instanceof Array)
            {
                for (var i = 0; i < attributes.length; i++)
                {
                    var name = attributes[i];
                    this.options[name] = $(this.element).attr(name);
                }
            }
            //读取ligerui这个属性，并加载到参数，比如 ligerui = "width:120,heigth:100"
            var p = this.options;
            if ($(this.element).attr("ligerui"))
            {
                try
                {
                    var attroptions = $(this.element).attr("ligerui");
                    if (attroptions.indexOf('{') != 0) attroptions = "{" + attroptions + "}";
                    eval("attroptions = " + attroptions + ";");
                    if (attroptions) $.extend(p, attroptions);
                }
                catch (e) { }
            }
        },
        //预渲染,可以用于继承扩展
        _preRender: function ()
        {

        },
        _render: function ()
        {

        },
        _rendered: function ()
        {
            if (this.element)
            {
                $(this.element).attr("ligeruiid", this.id);
            }
        },
        //返回要转换成ligerui参数的属性,比如['url']
        attr: function ()
        {
            return [];
        },
        destroy: function ()
        {
            if (this.element) $(this.element).remove();
            this.options = null;
        }
    });


    //表单控件基类
    $.ligerui.controls.Input = function (element, options)
    {
        $.ligerui.controls.Input.base.constructor.call(this, element, options);
    };

    $.ligerui.controls.Input.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return '$.ligerui.controls.Input';
        },
        setValue: function (value)
        {
            return this.set('value', value);
        },
        getValue: function ()
        {
            return this.get('value');
        },
        setEnabled: function ()
        {
            return this.set('disabled', false);
        },
        setDisabled: function ()
        {
            return this.set('disabled', true);
        },
        updateStyle: function ()
        {

        }
    });

    //全局窗口对象
    $.ligerui.win = {
        //顶端显示
        top: false,

        //遮罩
        mask: function ()
        {
            function setHeight()
            {
                if (!$.ligerui.win.windowMask) return;
                var h = $(window).height() + $(window).scrollTop();
                $.ligerui.win.windowMask.height(h);
            }
            if (!this.windowMask)
            {
                this.windowMask = $("<div class='l-window-mask' style='display: block;'></div>").appendTo('body');
                $(window).bind('resize.ligeruiwin', setHeight);
                $(window).bind('scroll', setHeight);
            }
            this.windowMask.show();
            setHeight();
            this.masking = true;
        },

        //取消遮罩
        unmask: function ()
        {
            if (this.windowMask)
                this.windowMask.hide();
            this.masking = false;
        },

        //显示任务栏
        createTaskbar: function ()
        {
            if (!this.taskbar)
            {
                this.taskbar = $('<div class="l-taskbar"><div class="l-taskbar-tasks"></div><div class="l-clear"></div></div>').appendTo('body');
                if (this.top) this.taskbar.addClass("l-taskbar-top");
                this.taskbar.tasks = $(".l-taskbar-tasks:first", this.taskbar);
                this.tasks = {};
            }
            this.taskbar.show();
            this.taskbar.animate({ bottom: 0 });
            return this.taskbar;
        },

        //关闭任务栏
        removeTaskbar: function ()
        {
            var self = this;
            self.taskbar.animate({ bottom: -32 }, function ()
            {
                self.taskbar.remove();
                self.taskbar = null;
            });
        },
        activeTask: function (win)
        {
            for (var winid in this.tasks)
            {
                var t = this.tasks[winid];
                if (winid == win.id)
                {
                    t.addClass("l-taskbar-task-active");
                }
                else
                {
                    t.removeClass("l-taskbar-task-active");
                }
            }
        },

        //获取任务
        getTask: function (win)
        {
            var self = this;
            if (!self.taskbar) return;
            if (self.tasks[win.id]) return self.tasks[win.id];
            return null;
        },


        //增加任务
        addTask: function (win)
        {
            var self = this;
            if (!self.taskbar) self.createTaskbar();
            if (self.tasks[win.id]) return self.tasks[win.id];
            var title = win.get('title');
            var task = self.tasks[win.id] = $('<div class="l-taskbar-task"><div class="l-taskbar-task-icon"></div><div class="l-taskbar-task-content">' + title + '</div></div>');
            self.taskbar.tasks.append(task);
            self.activeTask(win);
            task.bind('click', function ()
            {
                self.activeTask(win);
                if (win.actived)
                    win.min();
                else
                    win.active();
            }).hover(function ()
            {
                $(this).addClass("l-taskbar-task-over");
            }, function ()
            {
                $(this).removeClass("l-taskbar-task-over");
            });
            return task;
        },

        hasTask: function ()
        {
            for (var p in this.tasks)
            {
                if (this.tasks[p])
                    return true;
            }
            return false;
        },

        //移除任务
        removeTask: function (win)
        {
            var self = this;
            if (!self.taskbar) return;
            if (self.tasks[win.id])
            {
                self.tasks[win.id].unbind();
                self.tasks[win.id].remove();
                delete self.tasks[win.id];
            }
            if (!self.hasTask())
            {
                self.removeTaskbar();
            }
        },

        //前端显示
        setFront: function (win)
        {
            var wins = $.ligerui.find($.ligerui.core.Win);
            for (var i in wins)
            {
                var w = wins[i];
                if (w == win)
                {
                    $(w.element).css("z-index", "9200");
                    this.activeTask(w);
                }
                else
                {
                    $(w.element).css("z-index", "9100");
                }
            }
        }
    };


    //窗口基类 window、dialog
    $.ligerui.core.Win = function (element, options)
    {
        $.ligerui.core.Win.base.constructor.call(this, element, options);
    };

    $.ligerui.core.Win.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return '$.ligerui.controls.Win';
        },
        mask: function ()
        {
            if (this.options.modal)
                $.ligerui.win.mask();
        },
        unmask: function ()
        {
            if (this.options.modal)
                $.ligerui.win.unmask();
        },
        min: function ()
        {
        },
        max: function ()
        {
        },
        active: function ()
        {
        }
    });


    $.ligerui.draggable = {
        dragging: false
    };

    $.ligerui.resizable = {
        reszing: false
    };
})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerAccordion = function (options)
    { 
        return $.ligerui.run.call(this, "ligerAccordion", arguments);
    };

    $.fn.ligerGetAccordionManager = function ()
    {
        return $.ligerui.get(this);
    };

    $.ligerDefaults.Accordion = {
        height: null,
        speed: "normal",
        changeHeightOnResize: false,
        heightDiff: 0 // 高度补差  
    };
    $.ligerMethos.Accordion = {};

    $.ligerui.controls.Accordion = function (element, options)
    {
        $.ligerui.controls.Accordion.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.Accordion.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Accordion';
        },
        __idPrev: function ()
        {
            return 'Accordion';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Accordion;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.accordion = $(g.element);
            if (!g.accordion.hasClass("l-accordion-panel")) g.accordion.addClass("l-accordion-panel");
            var selectedIndex = 0;
            if ($("> div[lselected=true]", g.accordion).length > 0)
                selectedIndex = $("> div", g.accordion).index($("> div[lselected=true]", g.accordion));

            $("> div", g.accordion).each(function (i, box)
            {
                var header = $('<div class="l-accordion-header"><div class="l-accordion-toggle"></div><div class="l-accordion-header-inner"></div></div>');
                if (i == selectedIndex)
                    $(".l-accordion-toggle", header).addClass("l-accordion-toggle-open");
                if ($(box).attr("title"))
                {
                    $(".l-accordion-header-inner", header).html($(box).attr("title"));
                    $(box).attr("title", "");
                }
                $(box).before(header);
                if (!$(box).hasClass("l-accordion-content")) $(box).addClass("l-accordion-content");
            });

            //add Even
            $(".l-accordion-toggle", g.accordion).each(function ()
            {
                if (!$(this).hasClass("l-accordion-toggle-open") && !$(this).hasClass("l-accordion-toggle-close"))
                {
                    $(this).addClass("l-accordion-toggle-close");
                }
                if ($(this).hasClass("l-accordion-toggle-close"))
                {
                    $(this).parent().next(".l-accordion-content:visible").hide();
                }
            });
            $(".l-accordion-header", g.accordion).hover(function ()
            {
                $(this).addClass("l-accordion-header-over");
            }, function ()
            {
                $(this).removeClass("l-accordion-header-over");
            });
            $(".l-accordion-toggle", g.accordion).hover(function ()
            {
                if ($(this).hasClass("l-accordion-toggle-open"))
                    $(this).addClass("l-accordion-toggle-open-over");
                else if ($(this).hasClass("l-accordion-toggle-close"))
                    $(this).addClass("l-accordion-toggle-close-over");
            }, function ()
            {
                if ($(this).hasClass("l-accordion-toggle-open"))
                    $(this).removeClass("l-accordion-toggle-open-over");
                else if ($(this).hasClass("l-accordion-toggle-close"))
                    $(this).removeClass("l-accordion-toggle-close-over");
            });
            $(">.l-accordion-header", g.accordion).click(function ()
            {
                var togglebtn = $(".l-accordion-toggle:first", this);
                if (togglebtn.hasClass("l-accordion-toggle-close"))
                {
                    togglebtn.removeClass("l-accordion-toggle-close")
                    .removeClass("l-accordion-toggle-close-over l-accordion-toggle-open-over")
                    togglebtn.addClass("l-accordion-toggle-open");
                    $(this).next(".l-accordion-content")
                    .show(p.speed)
                    .siblings(".l-accordion-content:visible").hide(p.speed);
                    $(this).siblings(".l-accordion-header").find(".l-accordion-toggle").removeClass("l-accordion-toggle-open").addClass("l-accordion-toggle-close");
                }
                else
                {
                    togglebtn.removeClass("l-accordion-toggle-open")
                    .removeClass("l-accordion-toggle-close-over l-accordion-toggle-open-over")
                    .addClass("l-accordion-toggle-close");
                    $(this).next(".l-accordion-content").hide(p.speed);
                }
            });
            //init
            g.headerHoldHeight = 0;
            $("> .l-accordion-header", g.accordion).each(function ()
            {
                g.headerHoldHeight += $(this).height();
            });
            if (p.height && typeof (p.height) == 'string' && p.height.indexOf('%') > 0)
            {
                g.onResize();
                if (p.changeHeightOnResize)
                {
                    $(window).resize(function ()
                    {
                        g.onResize();
                    });
                }
            }
            else
            {
                if (p.height)
                {
                    g.height = p.heightDiff + p.height;
                    g.accordion.height(g.height);
                    g.setHeight(p.height);
                }
                else
                {
                    g.header = g.accordion.height();
                }
            }

            g.set(p);
        },
        onResize: function ()
        {
            var g = this, p = this.options;
            if (!p.height || typeof (p.height) != 'string' || p.height.indexOf('%') == -1) return false;
            //set accordion height
            if (g.accordion.parent()[0].tagName.toLowerCase() == "body")
            {
                var windowHeight = $(window).height();
                windowHeight -= parseInt(g.layout.parent().css('paddingTop'));
                windowHeight -= parseInt(g.layout.parent().css('paddingBottom'));
                g.height = p.heightDiff + windowHeight * parseFloat(g.height) * 0.01;
            }
            else
            {
                g.height = p.heightDiff + (g.accordion.parent().height() * parseFloat(p.height) * 0.01);
            }
            g.accordion.height(g.height);
            g.setContentHeight(g.height - g.headerHoldHeight);
        },
        setHeight: function (height)
        {
            var g = this, p = this.options;
            g.accordion.height(height);
            height -= g.headerHoldHeight;
            $("> .l-accordion-content", g.accordion).height(height);
        }
    });


})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{

    $.fn.ligerButton = function (options)
    {
        return $.ligerui.run.call(this, "ligerButton", arguments);
    };
    $.fn.ligerGetButtonManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetButtonManager", arguments);
    };

    $.ligerDefaults.Button = { width: 100, text: 'Button', disabled: false };

    $.ligerMethos.Button = {};

    $.ligerui.controls.Button = function (element, options)
    {
        $.ligerui.controls.Button.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.Button.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'Button';
        },
        __idPrev: function ()
        {
            return 'Button';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Button;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.button = $(g.element);
            g.button.addClass("l-btn");
            g.button.append('<div class="l-btn-l"></div><div class="l-btn-r"></div><span></span>');
            p.click && g.button.click(function ()
            {
                if (!p.disabled)
                    p.click();
            });
            g.set(p);
        },
        _setEnabled: function (value)
        {
            if (value)
                this.button.removeClass("l-btn-disabled");
        },
        _setDisabled: function (value)
        {
            if (value)
            {
                this.button.addClass("l-btn-disabled");
                this.options.disabled = true;
            }
        },
        _setWidth: function (value)
        {
            this.button.width(value);
        },
        _setText: function (value)
        {
            $("span", this.button).html(value);
        },
        setValue: function (value)
        {
            this.set('text', value);
        },
        getValue: function ()
        {
            return this.options.text;
        },
        setEnabled: function ()
        {
            this.set('disabled', false);
        },
        setDisabled: function ()
        {
            this.set('disabled', true);
        }
    });


 

})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerCheckBox = function (options)
    {
        return $.ligerui.run.call(this, "ligerCheckBox", arguments);
    };
    $.fn.ligerGetCheckBoxManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetCheckBoxManager", arguments);
    };
    $.ligerDefaults.CheckBox = { disabled: false };

    $.ligerMethos.CheckBox = {};

    $.ligerui.controls.CheckBox = function (element, options)
    {
        $.ligerui.controls.CheckBox.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.CheckBox.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'CheckBox';
        },
        __idPrev: function ()
        {
            return 'CheckBox';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.CheckBox;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.input = $(g.element);
            g.link = $('<a class="l-checkbox"></a>');
            g.wrapper = g.input.addClass('l-hidden').wrap('<div class="l-checkbox-wrapper"></div>').parent();
            g.wrapper.prepend(g.link);
            g.link.click(function ()
            {
                if (g.input.attr('disabled')) { return false; }
                if (p.disabled) return false;
                if (g.trigger('beforeClick', [g.element]) == false) return false;
                if ($(this).hasClass("l-checkbox-checked"))
                {
                    g.setValue(false);
                }
                else
                {
                    g.setValue(true);
                }
                g.input.trigger("change");
            });
            g.wrapper.hover(function ()
            {
                if (!p.disabled)
                    $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
            this.set(p);
            this.updateStyle();
        },
        _setCss: function (value)
        {
            this.wrapper.css(value);
        },
        _setValue: function (value)
        {
            var g = this, p = this.options;
            if (!value)
            {
                g.input[0].checked = false;
                g.link.removeClass('l-checkbox-checked');
            }
            else
            {
                g.input[0].checked = true;
                g.link.addClass('l-checkbox-checked');
            }
        }, 
        _setDisabled: function (value)
        { 
            if (value)
            {
                this.input.attr('disabled', true);
                this.wrapper.addClass("l-disabled"); 
            }
            else
            {
                this.input.attr('disabled', false);
                this.wrapper.removeClass("l-disabled"); 
            }
        },
        _getValue: function ()
        {
            return this.element.checked;
        },
        updateStyle: function ()
        {
            if (this.input.attr('disabled'))
            {
                this.wrapper.addClass("l-disabled");
                this.options.disabled = true;
            }
            if (this.input[0].checked)
            {
                this.link.addClass('l-checkbox-checked');
            }
            else
            {
                this.link.removeClass('l-checkbox-checked');
            }
        }
    });
})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{

    $.fn.ligerComboBox = function (options)
    {
        return $.ligerui.run.call(this, "ligerComboBox", arguments);
    };

    $.fn.ligerGetComboBoxManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetComboBoxManager", arguments);
    };

    $.ligerDefaults.ComboBox = {
        resize: true,           //是否调整大小
        isMultiSelect: false,   //是否多选
        isShowCheckBox: false,  //是否选择复选框
        columns: false,       //表格状态
        selectBoxWidth: false, //宽度
        selectBoxHeight: false, //高度
        onBeforeSelect: false, //选择前事件
        onSelected: null, //选择值事件 
        initValue: null,
        initText: null,
        valueField: 'id',
        textField: 'text',
        valueFieldID: null,
        slide: true,           //是否以动画的形式显示
        split: ";",
        data: null,
        tree: null,            //下拉框以树的形式显示，tree的参数跟LigerTree的参数一致 
        treeLeafOnly: true,   //是否只选择叶子
        grid: null,              //表格
        onStartResize: null,
        onEndResize: null,
        hideOnLoseFocus: true,
        url: null,              //数据源URL(需返回JSON)
        onSuccess: null,
        onError: null,
        onBeforeOpen: null,      //打开下拉框前事件，可以通过return false来阻止继续操作，利用这个参数可以用来调用其他函数，比如打开一个新窗口来选择值
        render: null,            //文本框显示html函数
        absolute: true         //选择框是否在附加到body,并绝对定位
    };

    //扩展方法
    $.ligerMethos.ComboBox = $.ligerMethos.ComboBox || {};


    $.ligerui.controls.ComboBox = function (element, options)
    {
        $.ligerui.controls.ComboBox.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.ComboBox.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'ComboBox';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.ComboBox;
        },
        _init: function ()
        {
            $.ligerui.controls.ComboBox.base._init.call(this);
            var p = this.options;
            if (p.columns)
            {
                p.isShowCheckBox = true;
            }
            if (p.isMultiSelect)
            {
                p.isShowCheckBox = true;
            }
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.data = p.data;
            g.inputText = null;
            g.select = null;
            g.textFieldID = "";
            g.valueFieldID = "";
            g.valueField = null; //隐藏域(保存值)
            //文本框初始化
            if (this.element.tagName.toLowerCase() == "input")
            {
                this.element.readOnly = true;
                g.inputText = $(this.element);
                g.textFieldID = this.element.id;
            }
            else if (this.element.tagName.toLowerCase() == "select")
            {
                $(this.element).hide();
                g.select = $(this.element);
                p.isMultiSelect = false;
                p.isShowCheckBox = false;
                g.textFieldID = this.element.id + "_txt";
                g.inputText = $('<input type="text" readonly="true"/>');
                g.inputText.attr("id", g.textFieldID).insertAfter($(this.element));
            } else
            {
                //不支持其他类型
                return;
            }
            if (g.inputText[0].name == undefined) g.inputText[0].name = g.textFieldID;
            //隐藏域初始化
            g.valueField = null;
            if (p.valueFieldID)
            {
                g.valueField = $("#" + p.valueFieldID + ":input");
                if (g.valueField.length == 0) g.valueField = $('<input type="hidden"/>');
                g.valueField[0].id = g.valueField[0].name = p.valueFieldID;
            }
            else
            {
                g.valueField = $('<input type="hidden"/>');
                g.valueField[0].id = g.valueField[0].name = g.textFieldID + "_val";
            }
            if (g.valueField[0].name == undefined) g.valueField[0].name = g.valueField[0].id;
            //开关
            g.link = $('<div class="l-trigger"><div class="l-trigger-icon"></div></div>');
            //下拉框
            g.selectBox = $('<div class="l-box-select"><div class="l-box-select-inner"><table cellpadding="0" cellspacing="0" border="0" class="l-box-select-table"></table></div></div>');
            g.selectBox.table = $("table:first", g.selectBox);
            //外层
            g.wrapper = g.inputText.wrap('<div class="l-text l-text-combobox"></div>').parent();
            g.wrapper.append('<div class="l-text-l"></div><div class="l-text-r"></div>');
            g.wrapper.append(g.link);
            //添加个包裹，
            g.textwrapper = g.wrapper.wrap('<div class="l-text-wrapper"></div>').parent();

            if (p.absolute)
                g.selectBox.appendTo('body').addClass("l-box-select-absolute");
            else
                g.textwrapper.append(g.selectBox);

            g.textwrapper.append(g.valueField);
            g.inputText.addClass("l-text-field");
            if (p.isShowCheckBox && !g.select)
            {
                $("table", g.selectBox).addClass("l-table-checkbox");
            } else
            {
                p.isShowCheckBox = false;
                $("table", g.selectBox).addClass("l-table-nocheckbox");
            }
            //开关 事件
            g.link.hover(function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger-hover";
            }, function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger";
            }).mousedown(function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger-pressed";
            }).mouseup(function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger-hover";
            }).click(function ()
            {
                if (p.disabled) return;
                if (g.trigger('beforeOpen') == false) return false;
                g._toggleSelectBox(g.selectBox.is(":visible"));
            });
            g.inputText.click(function ()
            {
                if (p.disabled) return;
                if (g.trigger('beforeOpen') == false) return false;
                g._toggleSelectBox(g.selectBox.is(":visible"));
            }).blur(function ()
            {
                if (p.disabled) return;
                g.wrapper.removeClass("l-text-focus");
            }).focus(function ()
            {
                if (p.disabled) return;
                g.wrapper.addClass("l-text-focus");
            });
            g.wrapper.hover(function ()
            {
                if (p.disabled) return;
                g.wrapper.addClass("l-text-over");
            }, function ()
            {
                if (p.disabled) return;
                g.wrapper.removeClass("l-text-over");
            });
            g.resizing = false;
            g.selectBox.hover(null, function (e)
            {
                if (p.hideOnLoseFocus && g.selectBox.is(":visible") && !g.boxToggling && !g.resizing)
                {
                    g._toggleSelectBox(true);
                }
            });
            //下拉框宽度、高度初始化
            if (p.selectBoxWidth)
            {
                g.selectBox.width(p.selectBoxWidth);
            }
            else
            {
                g.selectBox.css('width', g.wrapper.css('width'));
            }
            var itemsleng = $("tr", g.selectBox.table).length;
            if (!p.selectBoxHeight && itemsleng < 8) p.selectBoxHeight = itemsleng * 30;
            if (p.selectBoxHeight)
            {
                g.selectBox.height(p.selectBoxHeight);
            }
            //下拉框内容初始化
            g.bulidContent();

            g.set(p);
        },
        destroy: function ()
        {
            if (this.wrapper) this.wrapper.remove();
            if (this.selectBox) this.selectBox.remove();
            this.options = null;
        },
        _setDisabled: function (value)
        {
            //禁用样式
            if (value)
            {
                this.wrapper.addClass('l-text-disabled');
            } else
            {
                this.wrapper.removeClass('l-text-disabled');
            }
        },
        _setLable: function (label)
        {
            var g = this, p = this.options;
            if (label)
            {
                if (g.labelwrapper)
                {
                    g.labelwrapper.find(".l-text-label:first").html(label + ':&nbsp');
                }
                else
                {
                    g.labelwrapper = g.textwrapper.wrap('<div class="l-labeltext"></div>').parent();
                    g.labelwrapper.prepend('<div class="l-text-label" style="float:left;display:inline;">' + label + ':&nbsp</div>');
                    g.textwrapper.css('float', 'left');
                }
                if (!p.labelWidth)
                {
                    p.labelWidth = $('.l-text-label', g.labelwrapper).outerWidth();
                }
                else
                {
                    $('.l-text-label', g.labelwrapper).outerWidth(p.labelWidth);
                }
                $('.l-text-label', g.labelwrapper).width(p.labelWidth);
                $('.l-text-label', g.labelwrapper).height(g.wrapper.height());
                g.labelwrapper.append('<br style="clear:both;" />');
                if (p.labelAlign)
                {
                    $('.l-text-label', g.labelwrapper).css('text-align', p.labelAlign);
                }
                g.textwrapper.css({ display: 'inline' });
                g.labelwrapper.width(g.wrapper.outerWidth() + p.labelWidth + 2);
            }
        },
        _setWidth: function (value)
        {
            var g = this;
            if (value > 20)
            {
                g.wrapper.css({ width: value });
                g.inputText.css({ width: value - 20 });
                g.textwrapper.css({ width: value });
            }
        },
        _setHeight: function (value)
        {
            var g = this;
            if (value > 10)
            {
                g.wrapper.height(value);
                g.inputText.height(value - 2);
                g.link.height(value - 4);
                g.textwrapper.css({ width: value });
            }
        },
        _setResize: function (resize)
        {
            //调整大小支持
            if (resize && $.fn.ligerResizable)
            {
                var g = this;
                g.selectBox.ligerResizable({ handles: 'se,s,e', onStartResize: function ()
                {
                    g.resizing = true;
                    g.trigger('startResize');
                }
                , onEndResize: function ()
                {
                    g.resizing = false;
                    if (g.trigger('endResize') == false)
                        return false;
                }
                });
                g.selectBox.append("<div class='l-btn-nw-drop'></div>");
            }
        },
        //查找Text,适用多选和单选
        findTextByValue: function (value)
        {
            var g = this, p = this.options;
            if (value == undefined) return "";
            var texts = "";
            var contain = function (checkvalue)
            {
                var targetdata = value.toString().split(p.split);
                for (var i = 0; i < targetdata.length; i++)
                {
                    if (targetdata[i] == checkvalue) return true;
                }
                return false;
            };
            $(g.data).each(function (i, item)
            {
                var val = item[p.valueField];
                var txt = item[p.textField];
                if (contain(val))
                {
                    texts += txt + p.split;
                }
            });
            if (texts.length > 0) texts = texts.substr(0, texts.length - 1);
            return texts;
        },
        //查找Value,适用多选和单选
        findValueByText: function (text)
        {
            var g = this, p = this.options;
            if (!text && text == "") return "";
            var contain = function (checkvalue)
            {
                var targetdata = text.toString().split(p.split);
                for (var i = 0; i < targetdata.length; i++)
                {
                    if (targetdata[i] == checkvalue) return true;
                }
                return false;
            };
            var values = "";
            $(g.data).each(function (i, item)
            {
                var val = item[p.valueField];
                var txt = item[p.textField];
                if (contain(txt))
                {
                    values += val + p.split;
                }
            });
            if (values.length > 0) values = values.substr(0, values.length - 1);
            return values;
        },
        removeItem: function ()
        {
        },
        insertItem: function ()
        {
        },
        addItem: function ()
        {

        },
        _setValue: function (value)
        {
            var g = this, p = this.options;
            var text = g.findTextByValue(value);
            if (p.tree)
            {
                g.selectValueByTree(value);
            }
            else if (!p.isMultiSelect)
            {
                g._changeValue(value, text);
                $("tr[value=" + value + "] td", g.selectBox).addClass("l-selected");
                $("tr[value!=" + value + "] td", g.selectBox).removeClass("l-selected");
            }
            else
            {
                g._changeValue(value, text);
                var targetdata = value.toString().split(p.split);
                $("table.l-table-checkbox :checkbox", g.selectBox).each(function () { this.checked = false; });
                for (var i = 0; i < targetdata.length; i++)
                {
                    $("table.l-table-checkbox tr[value=" + targetdata[i] + "] :checkbox", g.selectBox).each(function () { this.checked = true; });
                }
            }
        },
        selectValue: function (value)
        {
            this._setValue(value);
        },
        bulidContent: function ()
        {
            var g = this, p = this.options;
            this.clearContent();
            if (g.select)
            {
                g.setSelect();
            }
            else if (g.data)
            {
                g.setData(g.data);
            }
            else if (p.tree)
            {
                g.setTree(p.tree);
            }
            else if (p.grid)
            {
                g.setGrid(p.grid);
            }
            else if (p.url)
            {
                $.ajax({
                    type: 'post',
                    url: p.url,
                    cache: false,
                    dataType: 'json',
                    success: function (data)
                    {
                        g.data = data;
                        g.setData(g.data);
                        g.trigger('success', [g.data]);
                    },
                    error: function (XMLHttpRequest, textStatus)
                    {
                        g.trigger('error', [XMLHttpRequest, textStatus]);
                    }
                });
            }
        },
        clearContent: function ()
        {
            var g = this, p = this.options;
            $("table", g.selectBox).html("");
            //g.inputText.val("");
            //g.valueField.val("");
        },
        setSelect: function ()
        {
            var g = this, p = this.options;
            this.clearContent();
            $('option', g.select).each(function (i)
            {
                var val = $(this).val();
                var txt = $(this).html();
                var tr = $("<tr><td index='" + i + "' value='" + val + "'>" + txt + "</td>");
                $("table.l-table-nocheckbox", g.selectBox).append(tr);
                $("td", tr).hover(function ()
                {
                    $(this).addClass("l-over");
                }, function ()
                {
                    $(this).removeClass("l-over");
                });
            });
            $('td:eq(' + g.select[0].selectedIndex + ')', g.selectBox).each(function ()
            {
                if ($(this).hasClass("l-selected"))
                {
                    g.selectBox.hide();
                    return;
                }
                $(".l-selected", g.selectBox).removeClass("l-selected");
                $(this).addClass("l-selected");
                if (g.select[0].selectedIndex != $(this).attr('index') && g.select[0].onchange)
                {
                    g.select[0].selectedIndex = $(this).attr('index'); g.select[0].onchange();
                }
                var newIndex = parseInt($(this).attr('index'));
                g.select[0].selectedIndex = newIndex;
                g.select.trigger("change");
                g.selectBox.hide();
                var value = $(this).attr("value");
                var text = $(this).html();
                if (p.render)
                {
                    g.inputText.val(p.render(value, text));
                }
                else
                {
                    g.inputText.val(text);
                }
            });
            g._addClickEven();
        },
        setData: function (data)
        {
            var g = this, p = this.options;
            this.clearContent();
            if (!data || !data.length) return;
            if (g.data != data) g.data = data;
            if (p.columns)
            {
                g.selectBox.table.headrow = $("<tr class='l-table-headerow'><td width='18px'></td></tr>");
                g.selectBox.table.append(g.selectBox.table.headrow);
                g.selectBox.table.addClass("l-box-select-grid");
                for (var j = 0; j < p.columns.length; j++)
                {
                    var headrow = $("<td columnindex='" + j + "' columnname='" + p.columns[j].name + "'>" + p.columns[j].header + "</td>");
                    if (p.columns[j].width)
                    {
                        headrow.width(p.columns[j].width);
                    }
                    g.selectBox.table.headrow.append(headrow);

                }
            }
            for (var i = 0; i < data.length; i++)
            {
                var val = data[i][p.valueField];
                var txt = data[i][p.textField];
                if (!p.columns)
                {
                    $("table.l-table-checkbox", g.selectBox).append("<tr value='" + val + "'><td style='width:18px;'  index='" + i + "' value='" + val + "' text='" + txt + "' ><input type='checkbox' /></td><td index='" + i + "' value='" + val + "' align='left'>" + txt + "</td>");
                    $("table.l-table-nocheckbox", g.selectBox).append("<tr value='" + val + "'><td index='" + i + "' value='" + val + "' align='left'>" + txt + "</td>");
                } else
                {
                    var tr = $("<tr value='" + val + "'><td style='width:18px;'  index='" + i + "' value='" + val + "' text='" + txt + "' ><input type='checkbox' /></td></tr>");
                    $("td", g.selectBox.table.headrow).each(function ()
                    {
                        var columnname = $(this).attr("columnname");
                        if (columnname)
                        {
                            var td = $("<td>" + data[i][columnname] + "</td>");
                            tr.append(td);
                        }
                    });
                    g.selectBox.table.append(tr);
                }
            }
            //自定义复选框支持
            if (p.isShowCheckBox && $.fn.ligerCheckBox)
            {
                $("table input:checkbox", g.selectBox).ligerCheckBox();
            }
            $(".l-table-checkbox input:checkbox", g.selectBox).change(function ()
            {
                if (this.checked && g.hasBind('beforeSelect'))
                {
                    var parentTD = null;
                    if ($(this).parent().get(0).tagName.toLowerCase() == "div")
                    {
                        parentTD = $(this).parent().parent();
                    } else
                    {
                        parentTD = $(this).parent();
                    }
                    if (parentTD != null && g.trigger('beforeSelect', [parentTD.attr("value"), parentTD.attr("text")]) == false)
                    {
                        g.selectBox.slideToggle("fast");
                        return false;
                    }
                }
                if (!p.isMultiSelect)
                {
                    if (this.checked)
                    {
                        $("input:checked", g.selectBox).not(this).each(function ()
                        {
                            this.checked = false;
                            $(".l-checkbox-checked", $(this).parent()).removeClass("l-checkbox-checked");
                        });
                        g.selectBox.slideToggle("fast");
                    }
                }
                g._checkboxUpdateValue();
            });
            $("table.l-table-nocheckbox td", g.selectBox).hover(function ()
            {
                $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
            g._addClickEven();
            //选择项初始化
            g._dataInit();
        },
        //树
        setTree: function (tree)
        {
            var g = this, p = this.options;
            this.clearContent();
            g.selectBox.table.remove();
            if (tree.checkbox != false)
            {
                tree.onCheck = function ()
                {
                    var nodes = g.treeManager.getChecked();
                    var value = [];
                    var text = [];
                    $(nodes).each(function (i, node)
                    {
                        if (p.treeLeafOnly && node.data.children) return;
                        value.push(node.data[p.valueField]);
                        text.push(node.data[p.textField]);
                    });
                    g._changeValue(value.join(p.split), text.join(p.split));
                };
            }
            else
            {
                tree.onSelect = function (node)
                {
                    if (p.treeLeafOnly && node.data.children) return;
                    var value = node.data[p.valueField];
                    var text = node.data[p.textField];
                    g._changeValue(value, text);
                };
                tree.onCancelSelect = function (node)
                {
                    g._changeValue("", "");
                };
            }
            tree.onAfterAppend = function (domnode, nodedata)
            {
                if (!g.treeManager) return;
                var value = null;
                if (p.initValue) value = p.initValue;
                else if (g.valueField.val() != "") value = g.valueField.val();
                g.selectValueByTree(value);
            };
            g.tree = $("<ul></ul>");
            $("div:first", g.selectBox).append(g.tree);
            g.tree.ligerTree(tree);
            g.treeManager = g.tree.ligerGetTreeManager();
        },
        selectValueByTree: function (value)
        {
            var g = this, p = this.options;
            if (value != null)
            {
                var text = "";
                var valuelist = value.toString().split(p.split);
                $(valuelist).each(function (i, item)
                {
                    g.treeManager.selectNode(item.toString());
                    text += g.treeManager.getTextByID(item);
                    if (i < valuelist.length - 1) text += p.split;
                });
                g._changeValue(value, text);
            }
        },
        //表格
        setGrid: function (grid)
        {
            var g = this, p = this.options;
            this.clearContent();
            g.selectBox.table.remove();
            g.grid = $("div:first", g.selectBox);
            grid.columnWidth = grid.columnWidth || 120;
            grid.width = "100%";
            grid.height = "100%";
            grid.heightDiff = -2;
            grid.InWindow = false;
            g.gridManager = g.grid.ligerGrid(grid);
            p.hideOnLoseFocus = false;
            if (grid.checkbox != false)
            {
                var onCheckRow = function ()
                {
                    var rowsdata = g.gridManager.getCheckedRows();
                    var value = [];
                    var text = [];
                    $(rowsdata).each(function (i, rowdata)
                    {
                        value.push(rowdata[p.valueField]);
                        text.push(rowdata[p.textField]);
                    });
                    g._changeValue(value.join(p.split), text.join(p.split));
                };
                g.gridManager.bind('CheckAllRow', onCheckRow);
                g.gridManager.bind('CheckRow', onCheckRow);
            }
            else
            {
                g.gridManager.bind('SelectRow', function (rowdata, rowobj, index)
                {
                    var value = rowdata[p.valueField];
                    var text = rowdata[p.textField];
                    g._changeValue(value, text);
                });
                g.gridManager.bind('UnSelectRow', function (rowdata, rowobj, index)
                {
                    g._changeValue("", "");
                });
            }
            g.bind('show', function ()
            {
                if (g.gridManager)
                {
                    g.gridManager._updateFrozenWidth();
                }
            });
            g.bind('endResize', function ()
            {
                if (g.gridManager)
                {
                    g.gridManager._updateFrozenWidth();
                    g.gridManager.setHeight(g.selectBox.height() - 2);
                }
            });
        },
        _getValue: function ()
        {
            return $(this.valueField).val();
        },
        getValue: function ()
        {
            //获取值
            return this._getValue();
        },
        _dataInit: function ()
        {
            var g = this, p = this.options;
            var value = null;
            if (p.initValue != undefined && p.initValue != null
                    && p.initText != undefined && p.initText != null
                    )
            {
                g._changeValue(p.initValue, p.initText);
            }
            //根据值来初始化
            if (p.initValue != undefined && p.initValue != null)
            {
                value = p.initValue;
                var text = g.findTextByValue(value);
                g._changeValue(value, text);
            }
            //根据文本来初始化 
            else if (p.initText != undefined && p.initText != null)
            {
                value = g.findValueByText(p.initText);
                g._changeValue(value, p.initText);
            }
            else if (g.valueField.val() != "")
            {
                value = g.valueField.val();
                var text = g.findTextByValue(value);
                g._changeValue(value, text);
            }
            if (!p.isShowCheckBox && value != null)
            {
                $("table tr", g.selectBox).find("td:first").each(function ()
                {
                    if (value == $(this).attr("value"))
                    {
                        $(this).addClass("l-selected");
                    }
                });
            }
            if (p.isShowCheckBox && value != null)
            {
                $(":checkbox", g.selectBox).each(function ()
                {
                    var parentTD = null;
                    var checkbox = $(this);
                    if (checkbox.parent().get(0).tagName.toLowerCase() == "div")
                    {
                        parentTD = checkbox.parent().parent();
                    } else
                    {
                        parentTD = checkbox.parent();
                    }
                    if (parentTD == null) return;
                    var valuearr = value.toString().split(p.split);
                    $(valuearr).each(function (i, item)
                    {
                        if (item == parentTD.attr("value"))
                        {
                            $(".l-checkbox", parentTD).addClass("l-checkbox-checked");
                            checkbox[0].checked = true;
                        }
                    });
                });
            }
        },
        _changeValue: function (newValue, newText)
        {
            var g = this, p = this.options;
            g.valueField.val(newValue);
            if (p.render)
            {
                g.inputText.val(p.render(newValue, newText));
            }
            else
            {
                g.inputText.val(newText);
            }
            g.selectedValue = newValue;
            g.selectedText = newText;
            g.inputText.trigger("change").focus();
            g.trigger('selected', [newValue, newText]);
        },
        //更新选中的值(复选框)
        _checkboxUpdateValue: function ()
        {
            var g = this, p = this.options;
            var valueStr = "";
            var textStr = "";
            $("input:checked", g.selectBox).each(function ()
            {
                var parentTD = null;
                if ($(this).parent().get(0).tagName.toLowerCase() == "div")
                {
                    parentTD = $(this).parent().parent();
                } else
                {
                    parentTD = $(this).parent();
                }
                if (!parentTD) return;
                valueStr += parentTD.attr("value") + p.split;
                textStr += parentTD.attr("text") + p.split;
            });
            if (valueStr.length > 0) valueStr = valueStr.substr(0, valueStr.length - 1);
            if (textStr.length > 0) textStr = textStr.substr(0, textStr.length - 1);
            g._changeValue(valueStr, textStr);
        },
        _addClickEven: function ()
        {
            var g = this, p = this.options;
            //选项点击
            $(".l-table-nocheckbox td", g.selectBox).click(function ()
            {
                var value = $(this).attr("value");
                var index = parseInt($(this).attr('index'));
                var text = $(this).html();
                if (g.hasBind('beforeSelect') && g.trigger('beforeSelect', [value, text]) == false)
                {
                    if (p.slide) g.selectBox.slideToggle("fast");
                    else g.selectBox.hide();
                    return false;
                }
                if ($(this).hasClass("l-selected"))
                {
                    if (p.slide) g.selectBox.slideToggle("fast");
                    else g.selectBox.hide();
                    return;
                }
                $(".l-selected", g.selectBox).removeClass("l-selected");
                $(this).addClass("l-selected");
                if (g.select)
                {
                    if (g.select[0].selectedIndex != index)
                    {
                        g.select[0].selectedIndex = index;
                        g.select.trigger("change");
                    }
                }
                if (p.slide)
                {
                    g.boxToggling = true;
                    g.selectBox.hide("fast", function ()
                    {
                        g.boxToggling = false;
                    })
                } else g.selectBox.hide();
                g._changeValue(value, text);
            });
        },
        _toggleSelectBox: function (isHide)
        {
            var g = this, p = this.options;
            var textHeight = g.wrapper.height();
            g.boxToggling = true;
            if (isHide)
            {
                if (p.slide)
                {
                    g.selectBox.slideToggle('fast', function ()
                    {
                        g.boxToggling = false;
                    });
                }
                else
                {
                    g.selectBox.hide();
                    g.boxToggling = false;
                }
            }
            else
            {
                if (p.absolute)
                {
                    g.selectBox.css({ left: g.wrapper.offset().left, top: g.wrapper.offset().top + 1 + g.wrapper.outerHeight() });
                }
                else
                {
                    var topheight = g.wrapper.offset().top - $(window).scrollTop();
                    var selfheight = g.selectBox.height() + textHeight + 4;
                    if (topheight + selfheight > $(window).height() && topheight > selfheight)
                    {
                        g.selectBox.css("marginTop", -1 * (g.selectBox.height() + textHeight + 5));
                    }
                }
                if (p.slide)
                {
                    g.selectBox.slideToggle('fast', function ()
                    {
                        g.boxToggling = false;
                        if (!p.isShowCheckBox && $('td.l-selected', g.selectBox).length > 0)
                        {
                            var offSet = ($('td.l-selected', g.selectBox).offset().top - g.selectBox.offset().top);
                            $(".l-box-select-inner", g.selectBox).animate({ scrollTop: offSet });
                        }
                    });
                }
                else
                {
                    g.selectBox.show();
                    g.boxToggling = false;
                    if (!g.tree && !g.grid && !p.isShowCheckBox && $('td.l-selected', g.selectBox).length > 0)
                    {
                        var offSet = ($('td.l-selected', g.selectBox).offset().top - g.selectBox.offset().top);
                        $(".l-box-select-inner", g.selectBox).animate({ scrollTop: offSet });
                    }
                }
            }
            g.isShowed = g.selectBox.is(":visible");
            g.trigger('toggle', [isHide]);
            g.trigger(isHide ? 'hide' : 'show');
        }
    });
    $.ligerui.controls.ComboBox.prototype.setValue = $.ligerui.controls.ComboBox.prototype.selectValue;



})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerDateEditor = function ()
    {
        return $.ligerui.run.call(this, "ligerDateEditor", arguments);
    };

    $.fn.ligerGetDateEditorManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetDateEditorManager", arguments);
    };

    $.ligerDefaults.DateEditor = {
        format: "yyyy-MM-dd hh:mm",
        showTime: false,
        onChangeDate: false,
        absolute: true  //选择框是否在附加到body,并绝对定位
    };
    $.ligerDefaults.DateEditorString = {
        dayMessage: ["日", "一", "二", "三", "四", "五", "六"],
        monthMessage: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        todayMessage: "今天",
        closeMessage: "关闭"
    };
    $.ligerMethos.DateEditor = {};

    $.ligerui.controls.DateEditor = function (element, options)
    {
        $.ligerui.controls.DateEditor.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.DateEditor.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'DateEditor';
        },
        __idPrev: function ()
        {
            return 'DateEditor';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.DateEditor;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            if (!p.showTime && p.format.indexOf(" hh:mm") > -1)
                p.format = p.format.replace(" hh:mm", "");
            if (this.element.tagName.toLowerCase() != "input" || this.element.type != "text")
                return;
            g.inputText = $(this.element);
            if (!g.inputText.hasClass("l-text-field"))
                g.inputText.addClass("l-text-field");
            g.link = $('<div class="l-trigger"><div class="l-trigger-icon"></div></div>');
            g.text = g.inputText.wrap('<div class="l-text l-text-date"></div>').parent();
            g.text.append('<div class="l-text-l"></div><div class="l-text-r"></div>');
            g.text.append(g.link);
            //添加个包裹，
            g.textwrapper = g.text.wrap('<div class="l-text-wrapper"></div>').parent();
            var dateeditorHTML = "";
            dateeditorHTML += "<div class='l-box-dateeditor' style='display:none'>";
            dateeditorHTML += "    <div class='l-box-dateeditor-header'>";
            dateeditorHTML += "        <div class='l-box-dateeditor-header-btn l-box-dateeditor-header-prevyear'><span></span></div>";
            dateeditorHTML += "        <div class='l-box-dateeditor-header-btn l-box-dateeditor-header-prevmonth'><span></span></div>";
            dateeditorHTML += "        <div class='l-box-dateeditor-header-text'><a class='l-box-dateeditor-header-month'></a> , <a  class='l-box-dateeditor-header-year'></a></div>";
            dateeditorHTML += "        <div class='l-box-dateeditor-header-btn l-box-dateeditor-header-nextmonth'><span></span></div>";
            dateeditorHTML += "        <div class='l-box-dateeditor-header-btn l-box-dateeditor-header-nextyear'><span></span></div>";
            dateeditorHTML += "    </div>";
            dateeditorHTML += "    <div class='l-box-dateeditor-body'>";
            dateeditorHTML += "        <table cellpadding='0' cellspacing='0' border='0' class='l-box-dateeditor-calendar'>";
            dateeditorHTML += "            <thead>";
            dateeditorHTML += "                <tr><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr>";
            dateeditorHTML += "            </thead>";
            dateeditorHTML += "            <tbody>";
            dateeditorHTML += "                <tr class='l-first'><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr><tr><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr><tr><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr><tr><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr><tr><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr><tr><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td><td align='center'></td></tr>";
            dateeditorHTML += "            </tbody>";
            dateeditorHTML += "        </table>";
            dateeditorHTML += "        <ul class='l-box-dateeditor-monthselector'><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li></ul>";
            dateeditorHTML += "        <ul class='l-box-dateeditor-yearselector'><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li></ul>";
            dateeditorHTML += "        <ul class='l-box-dateeditor-hourselector'><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li></ul>";
            dateeditorHTML += "        <ul class='l-box-dateeditor-minuteselector'><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li><li></li></ul>";
            dateeditorHTML += "    </div>";
            dateeditorHTML += "    <div class='l-box-dateeditor-toolbar'>";
            dateeditorHTML += "        <div class='l-box-dateeditor-time'></div>";
            dateeditorHTML += "        <div class='l-button l-button-today'></div>";
            dateeditorHTML += "        <div class='l-button l-button-close'></div>";
            dateeditorHTML += "        <div class='l-clear'></div>";
            dateeditorHTML += "    </div>";
            dateeditorHTML += "</div>";
            g.dateeditor = $(dateeditorHTML);
            if (p.absolute)
                g.dateeditor.appendTo('body').addClass("l-box-dateeditor-absolute");
            else
                g.textwrapper.append(g.dateeditor);
            g.header = $(".l-box-dateeditor-header", g.dateeditor);
            g.body = $(".l-box-dateeditor-body", g.dateeditor);
            g.toolbar = $(".l-box-dateeditor-toolbar", g.dateeditor);

            g.body.thead = $("thead", g.body);
            g.body.tbody = $("tbody", g.body);
            g.body.monthselector = $(".l-box-dateeditor-monthselector", g.body);
            g.body.yearselector = $(".l-box-dateeditor-yearselector", g.body);
            g.body.hourselector = $(".l-box-dateeditor-hourselector", g.body);
            g.body.minuteselector = $(".l-box-dateeditor-minuteselector", g.body);

            g.toolbar.time = $(".l-box-dateeditor-time", g.toolbar);
            g.toolbar.time.hour = $("<a></a>");
            g.toolbar.time.minute = $("<a></a>");
            g.buttons = {
                btnPrevYear: $(".l-box-dateeditor-header-prevyear", g.header),
                btnNextYear: $(".l-box-dateeditor-header-nextyear", g.header),
                btnPrevMonth: $(".l-box-dateeditor-header-prevmonth", g.header),
                btnNextMonth: $(".l-box-dateeditor-header-nextmonth", g.header),
                btnYear: $(".l-box-dateeditor-header-year", g.header),
                btnMonth: $(".l-box-dateeditor-header-month", g.header),
                btnToday: $(".l-button-today", g.toolbar),
                btnClose: $(".l-button-close", g.toolbar)
            };
            var nowDate = new Date();
            g.now = {
                year: nowDate.getFullYear(),
                month: nowDate.getMonth() + 1, //注意这里
                day: nowDate.getDay(),
                date: nowDate.getDate(),
                hour: nowDate.getHours(),
                minute: nowDate.getMinutes()
            };
            //当前的时间
            g.currentDate = {
                year: nowDate.getFullYear(),
                month: nowDate.getMonth() + 1,
                day: nowDate.getDay(),
                date: nowDate.getDate(),
                hour: nowDate.getHours(),
                minute: nowDate.getMinutes()
            };
            //选择的时间
            g.selectedDate = null;
            //使用的时间
            g.usedDate = null;



            //初始化数据
            //设置周日至周六
            $("td", g.body.thead).each(function (i, td)
            {
                $(td).html(p.dayMessage[i]);
            });
            //设置一月到十一二月
            $("li", g.body.monthselector).each(function (i, li)
            {
                $(li).html(p.monthMessage[i]);
            });
            //设置按钮
            g.buttons.btnToday.html(p.todayMessage);
            g.buttons.btnClose.html(p.closeMessage);
            //设置时间
            if (p.showTime)
            {
                g.toolbar.time.show();
                g.toolbar.time.append(g.toolbar.time.hour).append(":").append(g.toolbar.time.minute);
                $("li", g.body.hourselector).each(function (i, item)
                {
                    var str = i;
                    if (i < 10) str = "0" + i.toString();
                    $(this).html(str);
                });
                $("li", g.body.minuteselector).each(function (i, item)
                {
                    var str = i;
                    if (i < 10) str = "0" + i.toString();
                    $(this).html(str);
                });
            }
            //设置主体
            g.bulidContent();
            //初始化   
            if (g.inputText.val() != "")
                g.onTextChange();
            /**************
            **bulid evens**
            *************/
            g.dateeditor.hover(null, function (e)
            {
                if (g.dateeditor.is(":visible") && !g.editorToggling)
                {
                    g.toggleDateEditor(true);
                }
            });
            //toggle even
            g.link.hover(function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger-hover";
            }, function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger";
            }).mousedown(function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger-pressed";
            }).mouseup(function ()
            {
                if (p.disabled) return;
                this.className = "l-trigger-hover";
            }).click(function ()
            {
                if (p.disabled) return;
                g.bulidContent();
                g.toggleDateEditor(g.dateeditor.is(":visible"));
            });
            //不可用属性时处理
            if (p.disabled)
            {
                g.inputText.attr("readonly", "readonly");
                g.text.addClass('l-text-disabled');
            }
            //初始值
            if (p.initValue)
            {
                g.inputText.val(p.initValue);
            }
            g.buttons.btnClose.click(function ()
            {
                g.toggleDateEditor(true);
            });
            //日期 点击
            $("td", g.body.tbody).hover(function ()
            {
                if ($(this).hasClass("l-box-dateeditor-today")) return;
                $(this).addClass("l-box-dateeditor-over");
            }, function ()
            {
                $(this).removeClass("l-box-dateeditor-over");
            }).click(function ()
            {
                $(".l-box-dateeditor-selected", g.body.tbody).removeClass("l-box-dateeditor-selected");
                if (!$(this).hasClass("l-box-dateeditor-today"))
                    $(this).addClass("l-box-dateeditor-selected");
                g.currentDate.date = parseInt($(this).html());
                g.currentDate.day = new Date(g.currentDate.year, g.currentDate.month - 1, 1).getDay();
                if ($(this).hasClass("l-box-dateeditor-out"))
                {
                    if ($("tr", g.body.tbody).index($(this).parent()) == 0)
                    {
                        if (--g.currentDate.month == 0)
                        {
                            g.currentDate.month = 12;
                            g.currentDate.year--;
                        }
                    } else
                    {
                        if (++g.currentDate.month == 13)
                        {
                            g.currentDate.month = 1;
                            g.currentDate.year++;
                        }
                    }
                }
                g.selectedDate = {
                    year: g.currentDate.year,
                    month: g.currentDate.month,
                    date: g.currentDate.date
                };
                g.showDate();
                g.editorToggling = true;
                g.dateeditor.slideToggle('fast', function ()
                {
                    g.editorToggling = false;
                });
            });

            $(".l-box-dateeditor-header-btn", g.header).hover(function ()
            {
                $(this).addClass("l-box-dateeditor-header-btn-over");
            }, function ()
            {
                $(this).removeClass("l-box-dateeditor-header-btn-over");
            });
            //选择年份
            g.buttons.btnYear.click(function ()
            {
                //build year list
                if (!g.body.yearselector.is(":visible"))
                {
                    $("li", g.body.yearselector).each(function (i, item)
                    {
                        var currentYear = g.currentDate.year + (i - 4);
                        if (currentYear == g.currentDate.year)
                            $(this).addClass("l-selected");
                        else
                            $(this).removeClass("l-selected");
                        $(this).html(currentYear);
                    });
                }

                g.body.yearselector.slideToggle();
            });
            g.body.yearselector.hover(function () { }, function ()
            {
                $(this).slideUp();
            });
            $("li", g.body.yearselector).click(function ()
            {
                g.currentDate.year = parseInt($(this).html());
                g.body.yearselector.slideToggle();
                g.bulidContent();
            });
            //select month
            g.buttons.btnMonth.click(function ()
            {
                $("li", g.body.monthselector).each(function (i, item)
                {
                    //add selected style
                    if (g.currentDate.month == i + 1)
                        $(this).addClass("l-selected");
                    else
                        $(this).removeClass("l-selected");
                });
                g.body.monthselector.slideToggle();
            });
            g.body.monthselector.hover(function () { }, function ()
            {
                $(this).slideUp("fast");
            });
            $("li", g.body.monthselector).click(function ()
            {
                var index = $("li", g.body.monthselector).index(this);
                g.currentDate.month = index + 1;
                g.body.monthselector.slideToggle();
                g.bulidContent();
            });

            //选择小时
            g.toolbar.time.hour.click(function ()
            {
                $("li", g.body.hourselector).each(function (i, item)
                {
                    //add selected style
                    if (g.currentDate.hour == i)
                        $(this).addClass("l-selected");
                    else
                        $(this).removeClass("l-selected");
                });
                g.body.hourselector.slideToggle();
            });
            g.body.hourselector.hover(function () { }, function ()
            {
                $(this).slideUp("fast");
            });
            $("li", g.body.hourselector).click(function ()
            {
                var index = $("li", g.body.hourselector).index(this);
                g.currentDate.hour = index;
                g.body.hourselector.slideToggle();
                g.bulidContent();
            });
            //选择分钟
            g.toolbar.time.minute.click(function ()
            {
                $("li", g.body.minuteselector).each(function (i, item)
                {
                    //add selected style
                    if (g.currentDate.minute == i)
                        $(this).addClass("l-selected");
                    else
                        $(this).removeClass("l-selected");
                });
                g.body.minuteselector.slideToggle("fast", function ()
                {
                    var index = $("li", this).index($('li.l-selected', this));
                    if (index > 29)
                    {
                        var offSet = ($('li.l-selected', this).offset().top - $(this).offset().top);
                        $(this).animate({ scrollTop: offSet });
                    }
                });
            });
            g.body.minuteselector.hover(function () { }, function ()
            {
                $(this).slideUp("fast");
            });
            $("li", g.body.minuteselector).click(function ()
            {
                var index = $("li", g.body.minuteselector).index(this);
                g.currentDate.minute = index;
                g.body.minuteselector.slideToggle("fast");
                g.bulidContent();
            });

            //上个月
            g.buttons.btnPrevMonth.click(function ()
            {
                if (--g.currentDate.month == 0)
                {
                    g.currentDate.month = 12;
                    g.currentDate.year--;
                }
                g.bulidContent();
            });
            //下个月
            g.buttons.btnNextMonth.click(function ()
            {
                if (++g.currentDate.month == 13)
                {
                    g.currentDate.month = 1;
                    g.currentDate.year++;
                }
                g.bulidContent();
            });
            //上一年
            g.buttons.btnPrevYear.click(function ()
            {
                g.currentDate.year--;
                g.bulidContent();
            });
            //下一年
            g.buttons.btnNextYear.click(function ()
            {
                g.currentDate.year++;
                g.bulidContent();
            });
            //今天
            g.buttons.btnToday.click(function ()
            {
                g.currentDate = {
                    year: g.now.year,
                    month: g.now.month,
                    day: g.now.day,
                    date: g.now.date
                };
                g.selectedDate = {
                    year: g.now.year,
                    month: g.now.month,
                    day: g.now.day,
                    date: g.now.date
                };
                g.showDate();
                g.dateeditor.slideToggle("fast");
            });
            //文本框
            g.inputText.change(function ()
            {
                g.onTextChange();
            }).blur(function ()
            {
                g.text.removeClass("l-text-focus");
            }).focus(function ()
            {
                g.text.addClass("l-text-focus");
            });
            g.text.hover(function ()
            {
                g.text.addClass("l-text-over");
            }, function ()
            {
                g.text.removeClass("l-text-over");
            });
            //LEABEL 支持
            if (p.label)
            {
                g.labelwrapper = g.textwrapper.wrap('<div class="l-labeltext"></div>').parent();
                g.labelwrapper.prepend('<div class="l-text-label" style="float:left;display:inline;">' + p.label + ':&nbsp</div>');
                g.textwrapper.css('float', 'left');
                if (!p.labelWidth)
                {
                    p.labelWidth = $('.l-text-label', g.labelwrapper).outerWidth();
                } else
                {
                    $('.l-text-label', g.labelwrapper).outerWidth(p.labelWidth);
                }
                $('.l-text-label', g.labelwrapper).width(p.labelWidth);
                $('.l-text-label', g.labelwrapper).height(g.text.height());
                g.labelwrapper.append('<br style="clear:both;" />');
                if (p.labelAlign)
                {
                    $('.l-text-label', g.labelwrapper).css('text-align', p.labelAlign);
                }
                g.textwrapper.css({ display: 'inline' });
                g.labelwrapper.width(g.text.outerWidth() + p.labelWidth + 2);
            }

            g.set(p);
        },
        destroy: function ()
        { 
            if (this.textwrapper) this.textwrapper.remove();
            if (this.dateeditor) this.dateeditor.remove();
            this.options = null;
        },
        bulidContent: function ()
        {
            var g = this, p = this.options;
            //当前月第一天星期
            var thismonthFirstDay = new Date(g.currentDate.year, g.currentDate.month - 1, 1).getDay();
            //当前月天数
            var nextMonth = g.currentDate.month;
            var nextYear = g.currentDate.year;
            if (++nextMonth == 13)
            {
                nextMonth = 1;
                nextYear++;
            }
            var monthDayNum = new Date(nextYear, nextMonth - 1, 0).getDate();
            //当前上个月天数
            var prevMonthDayNum = new Date(g.currentDate.year, g.currentDate.month - 1, 0).getDate();

            g.buttons.btnMonth.html(p.monthMessage[g.currentDate.month - 1]);
            g.buttons.btnYear.html(g.currentDate.year);
            g.toolbar.time.hour.html(g.currentDate.hour);
            g.toolbar.time.minute.html(g.currentDate.minute);
            if (g.toolbar.time.hour.html().length == 1)
                g.toolbar.time.hour.html("0" + g.toolbar.time.hour.html());
            if (g.toolbar.time.minute.html().length == 1)
                g.toolbar.time.minute.html("0" + g.toolbar.time.minute.html());
            $("td", this.body.tbody).each(function () { this.className = "" });
            $("tr", this.body.tbody).each(function (i, tr)
            {
                $("td", tr).each(function (j, td)
                {
                    var id = i * 7 + (j - thismonthFirstDay);
                    var showDay = id + 1;
                    if (g.selectedDate && g.currentDate.year == g.selectedDate.year &&
                            g.currentDate.month == g.selectedDate.month &&
                            id + 1 == g.selectedDate.date)
                    {
                        if (j == 0 || j == 6)
                        {
                            $(td).addClass("l-box-dateeditor-holiday")
                        }
                        $(td).addClass("l-box-dateeditor-selected");
                        $(td).siblings().removeClass("l-box-dateeditor-selected");
                    }
                    else if (g.currentDate.year == g.now.year &&
                            g.currentDate.month == g.now.month &&
                            id + 1 == g.now.date)
                    {
                        if (j == 0 || j == 6)
                        {
                            $(td).addClass("l-box-dateeditor-holiday")
                        }
                        $(td).addClass("l-box-dateeditor-today");
                    }
                    else if (id < 0)
                    {
                        showDay = prevMonthDayNum + showDay;
                        $(td).addClass("l-box-dateeditor-out")
                                .removeClass("l-box-dateeditor-selected");
                    }
                    else if (id > monthDayNum - 1)
                    {
                        showDay = showDay - monthDayNum;
                        $(td).addClass("l-box-dateeditor-out")
                                .removeClass("l-box-dateeditor-selected");
                    }
                    else if (j == 0 || j == 6)
                    {
                        $(td).addClass("l-box-dateeditor-holiday")
                                .removeClass("l-box-dateeditor-selected");
                    }
                    else
                    {
                        td.className = "";
                    }

                    $(td).html(showDay);
                });
            });
        },
        toggleDateEditor: function (isHide)
        {
            var g = this, p = this.options;
            var textHeight = g.text.height();
            g.editorToggling = true;
            if (isHide)
            {
                g.dateeditor.hide('fast', function ()
                {
                    g.editorToggling = false;
                });
            }
            else
            {
                if (p.absolute)
                {
                    g.dateeditor.css({ left: g.text.offset().left, top: g.text.offset().top + 1 + g.text.outerHeight() });
                }
                else
                {
                    if (g.text.offset().top + 4 > g.dateeditor.height() && g.text.offset().top + g.dateeditor.height() + textHeight + 4 - $(window).scrollTop() > $(window).height())
                    {
                        g.dateeditor.css("marginTop", -1 * (g.dateeditor.height() + textHeight + 5));
                        g.showOnTop = true;
                    }
                    else
                    {
                        g.showOnTop = false;
                    }
                }
                g.dateeditor.slideDown('fast', function ()
                {
                    g.editorToggling = false;
                });
            }
        },
        showDate: function ()
        {
            var g = this, p = this.options;
            if (!this.selectedDate) return;
            var dateStr = g.selectedDate.year + "/" + g.selectedDate.month + "/" + g.selectedDate.date;
            this.currentDate.hour = parseInt(g.toolbar.time.hour.html(), 10);
            this.currentDate.minute = parseInt(g.toolbar.time.minute.html(), 10);
            if (p.showTime)
            {
                dateStr += " " + this.currentDate.hour + ":" + this.currentDate.minute;
            }
            this.inputText.val(dateStr);
            this.inputText.trigger("change").focus();
        },
        isDateTime: function (dateStr)
        {
            var g = this, p = this.options;
            var r = dateStr.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/);
            if (r == null) return false;
            var d = new Date(r[1], r[3] - 1, r[4]);
            if (d == "NaN") return false;
            return (d.getFullYear() == r[1] && (d.getMonth() + 1) == r[3] && d.getDate() == r[4]);
        },
        isLongDateTime: function (dateStr)
        {
            var g = this, p = this.options;
            var reg = /^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2}) (\d{1,2}):(\d{1,2})$/;
            var r = dateStr.match(reg);
            if (r == null) return false;
            var d = new Date(r[1], r[3] - 1, r[4], r[5], r[6]);
            if (d == "NaN") return false;
            return (d.getFullYear() == r[1] && (d.getMonth() + 1) == r[3] && d.getDate() == r[4] && d.getHours() == r[5] && d.getMinutes() == r[6]);
        },
        getFormatDate: function (date)
        {
            var g = this, p = this.options;
            if (date == "NaN") return null;
            var format = p.format;
            var o = {
                "M+": date.getMonth() + 1,
                "d+": date.getDate(),
                "h+": date.getHours(),
                "m+": date.getMinutes(),
                "s+": date.getSeconds(),
                "q+": Math.floor((date.getMonth() + 3) / 3),
                "S": date.getMilliseconds()
            }
            if (/(y+)/.test(format))
            {
                format = format.replace(RegExp.$1, (date.getFullYear() + "")
                .substr(4 - RegExp.$1.length));
            }
            for (var k in o)
            {
                if (new RegExp("(" + k + ")").test(format))
                {
                    format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
                    : ("00" + o[k]).substr(("" + o[k]).length));
                }
            }
            return format;
        },
        onTextChange: function ()
        {
            var g = this, p = this.options;
            var val = g.inputText.val();
            if (val == "")
            {
                g.selectedDate = null;
                return true;
            }
            if (!p.showTime && !g.isDateTime(val))
            {
                //恢复
                if (!g.usedDate)
                {
                    g.inputText.val("");
                } else
                {
                    g.inputText.val(g.getFormatDate(g.usedDate));
                }
            }
            else if (p.showTime && !g.isLongDateTime(val))
            {
                //恢复
                if (!g.usedDate)
                {
                    g.inputText.val("");
                } else
                {
                    g.inputText.val(g.getFormatDate(g.usedDate));
                }
            }
            else
            {
                val = val.replace(/-/g, "/");
                var formatVal = g.getFormatDate(new Date(val));
                if (formatVal == null)
                {
                    //恢复
                    if (!g.usedDate)
                    {
                        g.inputText.val("");
                    } else
                    {
                        g.inputText.val(g.getFormatDate(g.usedDate));
                    }
                }
                g.usedDate = new Date(val); //记录
                g.selectedDate = {
                    year: g.usedDate.getFullYear(),
                    month: g.usedDate.getMonth() + 1, //注意这里
                    day: g.usedDate.getDay(),
                    date: g.usedDate.getDate(),
                    hour: g.usedDate.getHours(),
                    minute: g.usedDate.getMinutes()
                };
                g.currentDate = {
                    year: g.usedDate.getFullYear(),
                    month: g.usedDate.getMonth() + 1, //注意这里
                    day: g.usedDate.getDay(),
                    date: g.usedDate.getDate(),
                    hour: g.usedDate.getHours(),
                    minute: g.usedDate.getMinutes()
                };
                g.inputText.val(formatVal);
                g.trigger('changeDate', [formatVal]);
                if ($(g.dateeditor).is(":visible"))
                    g.bulidContent();
            }
        },
        _setHeight: function (value)
        {
            var g = this;
            if (value > 4)
            {
                g.text.css({ height: value });
                g.inputText.css({ height: value });
                g.textwrapper.css({ height: value });
            }
        },
        _setWidth: function (value)
        {
            var g = this;
            if (value > 20)
            {
                g.text.css({ width: value });
                g.inputText.css({ width: value - 20 });
                g.textwrapper.css({ width: value });
            }
        },
        _setValue: function (value)
        {
            var g = this;
            if (!value) g.inputText.val('');
            if (typeof value == "string")
            {
                g.inputText.val(value);
            }
            else if (typeof value == "object")
            {
                if (value instanceof Date)
                {
                    g.inputText.val(g.getFormatDate(value));
                    g.onTextChange();
                }
            }
        },
        _getValue: function ()
        {
            return this.usedDate;
        },
        setEnabled: function ()
        {
            var g = this, p = this.options;
            this.inputText.removeAttr("readonly");
            this.text.removeClass('l-text-disabled');
            p.disabled = false;
        },
        setDisabled: function ()
        {
            var g = this, p = this.options;
            this.inputText.attr("readonly", "readonly");
            this.text.addClass('l-text-disabled');
            p.disabled = true;
        }
    });


})(jQuery);﻿/**
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


})(jQuery);﻿/**
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

})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerEasyTab = function ()
    {
        return $.ligerui.run.call(this, "ligerEasyTab", arguments);
    };
    $.fn.ligerGetEasyTabManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetEasyTabManager", arguments);
    };

    $.ligerDefaults.EasyTab = {};

    $.ligerMethos.EasyTab = {};

    $.ligerui.controls.EasyTab = function (element, options)
    {
        $.ligerui.controls.EasyTab.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.EasyTab.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'EasyTab';
        },
        __idPrev: function ()
        {
            return 'EasyTab';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.EasyTab;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.tabs = $(this.element);
            g.tabs.addClass("l-easytab");
            var selectedIndex = 0;
            if ($("> div[lselected=true]", g.tabs).length > 0)
                selectedIndex = $("> div", g.tabs).index($("> div[lselected=true]", g.tabs));
            g.tabs.ul = $('<ul class="l-easytab-header"></ul>');
            $("> div", g.tabs).each(function (i, box)
            {
                var li = $('<li><span></span></li>');
                if (i == selectedIndex)
                    $("span", li).addClass("l-selected");
                if ($(box).attr("title"))
                    $("span", li).html($(box).attr("title"));
                g.tabs.ul.append(li);
                if (!$(box).hasClass("l-easytab-panelbox")) $(box).addClass("l-easytab-panelbox");
            });
            g.tabs.ul.prependTo(g.tabs);
            //init  
            $(".l-easytab-panelbox:eq(" + selectedIndex + ")", g.tabs).show().siblings(".l-easytab-panelbox").hide(); 
            //add even 
            $("> ul:first span", g.tabs).click(function ()
            {
                if ($(this).hasClass("l-selected")) return;
                var i = $("> ul:first span", g.tabs).index(this);
                $(this).addClass("l-selected").parent().siblings().find("span.l-selected").removeClass("l-selected");
                $(".l-easytab-panelbox:eq(" + i + ")", g.tabs).show().siblings(".l-easytab-panelbox").hide();
            }).not("l-selected").hover(function ()
            {
                $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
            g.set(p);
        }
    });

})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{

    $.ligerDefaults = $.ligerDefaults || {};
    $.ligerDefaults.Form = {
        width: null
    };

    $.fn.ligerForm = function (options)
    {
        return this.each(function ()
        {
            var p = $.extend({}, $.ligerDefaults.Form, options || {});
            $("input[ltype=text],input[ltype=password]", this).ligerTextBox();

            $("input[ltype=select],select[ltype=select]", this).ligerComboBox();

            $("input[ltype=spinner]", this).ligerSpinner();

            $("input[ltype=date]", this).ligerDateEditor();

            $(":radio", this).ligerRadio();

            $(':checkbox', this).ligerCheckBox();
        });
    };

})(jQuery);﻿/**
* jQuery ligerUI  1.1.5
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{
    var l = $.ligerui;

    $.fn.ligerGrid = function (options)
    {
        return $.ligerui.run.call(this, "ligerGrid", arguments);
    };

    $.fn.ligerGetGridManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetGridManager", arguments);
    };

    $.ligerDefaults.Grid = {
        title: null,
        width: 'auto',                          //宽度值
        height: 'auto',                          //宽度值
        columnWidth: null,                      //默认列宽度
        resizable: true,                        //table是否可伸缩
        url: false,                             //ajax url
        usePager: true,                         //是否分页
        page: 1,                                //默认当前页 
        pageSize: 10,                           //每页默认的结果数
        pageSizeOptions: [10, 20, 30, 40, 50],  //可选择设定的每页结果数
        parms: [],                         //提交到服务器的参数
        columns: [],                          //数据源
        minColToggle: 1,                        //最小显示的列
        dataType: 'server',                     //数据源：本地(local)或(server),本地是将读取p.data。不需要配置，取决于设置了data或是url
        dataAction: 'server',                    //提交数据的方式：本地(local)或(server),选择本地方式时将在客服端分页、排序。 
        showTableToggleBtn: false,              //是否显示'显示隐藏Grid'按钮 
        switchPageSizeApplyComboBox: false,     //切换每页记录数是否应用ligerComboBox
        allowAdjustColWidth: true,              //是否允许调整列宽     
        checkbox: false,                         //是否显示复选框
        allowHideColumn: true,                 //是否显示'切换列层'按钮
        enabledEdit: false,                      //是否允许编辑
        isScroll: true,                         //是否滚动
        onDragCol: null,                       //拖动列事件
        onToggleCol: null,                     //切换列事件
        onChangeSort: null,                    //改变排序事件
        onSuccess: null,                       //成功获取服务器数据的事件
        onDblClickRow: null,                     //双击行事件
        onSelectRow: null,                    //选择行事件
        onUnSelectRow: null,                   //取消选择行事件
        onBeforeCheckRow: null,                 //选择前事件，可以通过return false阻止操作(复选框)
        onCheckRow: null,                    //选择事件(复选框) 
        onBeforeCheckAllRow: null,              //选择前事件，可以通过return false阻止操作(复选框 全选/全不选)
        onCheckAllRow: null,                    //选择事件(复选框 全选/全不选)
        onBeforeShowData: null,                  //显示数据前事件，可以通过reutrn false阻止操作
        onAfterShowData: null,                 //显示完数据事件
        onError: null,                         //错误事件
        onSubmit: null,                         //提交前事件
        dateFormat: 'yyyy-MM-dd',              //默认时间显示格式
        InWindow: true,                        //是否以窗口的高度为准 height设置为百分比时可用
        statusName: '__status',                    //状态名
        method: 'post',                         //提交方式
        async: false,
        fixedCellHeight: true,                       //是否固定单元格的高度
        heightDiff: 0,                         //高度补差,当设置height:100%时，可能会有高度的误差，可以通过这个属性调整
        cssClass: null,                    //类名
        root: 'Rows',                       //数据源字段名
        record: 'Total',                     //数据源记录数字段名
        pageParmName: 'page',               //页索引参数名，(提交给服务器)
        pagesizeParmName: 'pagesize',        //页记录数参数名，(提交给服务器)
        sortnameParmName: 'sortname',        //页排序列名(提交给服务器)
        sortorderParmName: 'sortorder',      //页排序方向(提交给服务器)
        onReload: null,                    //刷新事件，可以通过return false来阻止操作
        onToFirst: null,                     //第一页，可以通过return false来阻止操作
        onToPrev: null,                      //上一页，可以通过return false来阻止操作
        onToNext: null,                      //下一页，可以通过return false来阻止操作
        onToLast: null,                      //最后一页，可以通过return false来阻止操作
        allowUnSelectRow: false,           //是否允许反选行 
        alternatingRow: true,           //间隔行效果
        mouseoverRowCssClass: 'l-grid-row-over',
        enabledSort: true,                      //是否允许排序
        rowAttrRender: null,                  //行自定义属性渲染器(包括style，也可以定义)
        groupColumnName: null,                 //分组 - 列名
        groupColumnDisplay: '分组',             //分组 - 列显示名字
        groupRender: null,                     //分组 - 渲染器
        totalRender: null,                       //统计行(全部数据)
        delayLoad: false,                        //初始化时是否不加载
        where: null,                           //数据过滤查询函数,(参数一 data item，参数二 data item index)
        selectRowButtonOnly: false,            //复选框模式时，是否只允许点击复选框才能选择行
        onAfterAddRow: null,                     //增加行后事件
        onBeforeEdit: null,                      //编辑前事件
        onBeforeSubmitEdit: null,               //验证编辑器结果是否通过
        onAfterEdit: null,                       //结束编辑后事件
        onLoading: null,                        //加载时函数
        onLoaded: null,                          //加载完函数
        onContextmenu: null,                   //右击事件
        whenRClickToSelect: false,                //右击行时是否选中
        contentType: null,                     //Ajax contentType参数
        checkboxColWidth: 27,                  //复选框列宽度
        detailColWidth: 29,                     //明细列宽度
        clickToEdit: true,                      //是否点击单元格的时候就编辑
        detailToEdit: false,                     //是否点击明细的时候进入编辑
        onEndEdit: null,
        minColumnWidth: 80,
        tree: null,                            //treeGrid模式
        isChecked: null,                       //复选框 初始化函数
        frozen: true,                          //是否固定列
        frozenDetail: false,                    //明细按钮是否在固定列中
        frozenCheckbox: true,                  //复选框按钮是否在固定列中
        detailHeight: 260,
        rownumbers: false,                         //是否显示行序号
        frozenRownumbers: true,                  //行序号是否在固定列中
        rownumbersColWidth: 26,
        colDraggable: false,                       //是否允许表头拖拽
        rowDraggable: false,                         //是否允许行拖拽
        rowDraggingRender: null,
        autoCheckChildren: true,                  //是否自动选中子节点
        onRowDragDrop: null                    //行拖拽事件
    };
    $.ligerDefaults.GridString = {
        errorMessage: '发生错误',
        pageStatMessage: '显示从{from}到{to}，总 {total} 条 。每页显示：{pagesize}',
        pageTextMessage: 'Page',
        loadingMessage: '加载中...',
        findTextMessage: '查找',
        noRecordMessage: '没有符合条件的记录存在',
        isContinueByDataChanged: '数据已经改变,如果继续将丢失数据,是否继续?',
        cancelMessage: '取消',
        saveMessage: '保存',
        applyMessage: '应用',
        draggingMessage: '{count}行'
    };
    //接口方法扩展
    $.ligerMethos.Grid = $.ligerMethos.Grid || {};

    //排序器扩展
    $.ligerDefaults.Grid.sorters = $.ligerDefaults.Grid.sorters || {};

    //格式化器扩展
    $.ligerDefaults.Grid.formatters = $.ligerDefaults.Grid.formatters || {};

    //编辑器扩展
    $.ligerDefaults.Grid.editors = $.ligerDefaults.Grid.editors || {};


    $.ligerDefaults.Grid.sorters['date'] = function (val1, val2)
    {
        return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
    };
    $.ligerDefaults.Grid.sorters['int'] = function (val1, val2)
    {
        return parseInt(val1) < parseInt(val2) ? -1 : parseInt(val1) > parseInt(val2) ? 1 : 0;
    };
    $.ligerDefaults.Grid.sorters['float'] = function (val1, val2)
    {
        return parseFloat(val1) < parseFloat(val2) ? -1 : parseFloat(val1) > parseFloat(val2) ? 1 : 0;
    };
    $.ligerDefaults.Grid.sorters['string'] = function (val1, val2)
    {
        return val1.localeCompare(val2);
    };


    $.ligerDefaults.Grid.formatters['date'] = function (value, column)
    {
        function getFormatDate(date, dateformat)
        {
            var g = this, p = this.options;
            if (isNaN(date)) return null;
            var format = dateformat;
            var o = {
                "M+": date.getMonth() + 1,
                "d+": date.getDate(),
                "h+": date.getHours(),
                "m+": date.getMinutes(),
                "s+": date.getSeconds(),
                "q+": Math.floor((date.getMonth() + 3) / 3),
                "S": date.getMilliseconds()
            }
            if (/(y+)/.test(format))
            {
                format = format.replace(RegExp.$1, (date.getFullYear() + "")
            .substr(4 - RegExp.$1.length));
            }
            for (var k in o)
            {
                if (new RegExp("(" + k + ")").test(format))
                {
                    format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
                : ("00" + o[k]).substr(("" + o[k]).length));
                }
            }
            return format;
        }
        if (!value) return "";
        if (value instanceof Date)
        {
            var format = column.format || this.options.dateFormat || "yyyy-MM-dd";
            return getFormatDate(value, format);
        }
        else
        {
            return value.toString();
        }
    }


    $.ligerDefaults.Grid.editors['date'] =
    {
        create: function (container, editParm)
        {
            var column = editParm.column;
            var input = $("<input type='text'/>");
            container.append(input);
            var options = {};
            var ext = column.editor.p || column.editor.ext;
            if (ext)
            {
                var tmp = typeof (ext) == 'function' ?
                 ext(editParm.record, editParm.rowindex, editParm.value, column) : ext;
                $.extend(options, tmp);
            }
            input.ligerDateEditor(options);
            return input;
        },
        getValue: function (input, editParm)
        {
            return input.liger('option', 'value');
        },
        setValue: function (input, value, editParm)
        {
            input.liger('option', 'value', value);
        },
        resize: function (input, width, height, editParm)
        {
            input.liger('option', 'width', width);
            input.liger('option', 'height', height);
        },
        destroy: function (input, editParm)
        {
            input.liger('destroy');
        }
    };

    $.ligerDefaults.Grid.editors['select'] =
     $.ligerDefaults.Grid.editors['combobox'] =
     {
         create: function (container, editParm)
         {
             var column = editParm.column;
             var input = $("<input type='text'/>");
             container.append(input);
             var options = {
                 data: column.editor.data,
                 slide: false,
                 valueField: column.editor.valueField || column.editor.valueColumnName,
                 textField: column.editor.textField || column.editor.displayColumnName
             };
             var ext = column.editor.p || column.editor.ext;
             if (ext)
             {
                 var tmp = typeof (ext) == 'function' ?
                 ext(editParm.record, editParm.rowindex, editParm.value, column) : ext;
                 $.extend(options, tmp);
             }
             input.ligerComboBox(options);
             return input;
         },
         getValue: function (input, editParm)
         {
             return input.liger('option', 'value');
         },
         setValue: function (input, value, editParm)
         {
             input.liger('option', 'value', value);
         },
         resize: function (input, width, height, editParm)
         {
             input.liger('option', 'width', width);
             input.liger('option', 'height', height);
         },
         destroy: function (input, editParm)
         {
             input.liger('destroy');
         }
     };

    $.ligerDefaults.Grid.editors['int'] =
     $.ligerDefaults.Grid.editors['float'] =
     $.ligerDefaults.Grid.editors['spinner'] =
     {
         create: function (container, editParm)
         {
             var column = editParm.column;
             var input = $("<input type='text'/>");
             container.append(input);
             input.css({ border: '#6E90BE' })
             var options = {
                 type: column.editor.type == 'float' ? 'float' : 'int'
             };
             if (column.editor.minValue != undefined) options.minValue = column.editor.minValue;
             if (column.editor.maxValue != undefined) options.maxValue = column.editor.maxValue;
             input.ligerSpinner(options);
             return input;
         },
         getValue: function (input, editParm)
         {
             var column = editParm.column;
             var isInt = column.editor.type == "int";
             if (isInt)
                 return parseInt(input.val(), 10);
             else
                 return parseFloat(input.val());
         },
         setValue: function (input, value, editParm)
         {
             input.val(value);
         },
         resize: function (input, width, height, editParm)
         {
             input.liger('option', 'width', width);
             input.liger('option', 'height', height);
         },
         destroy: function (input, editParm)
         {
             input.liger('destroy');
         }
     };


    $.ligerDefaults.Grid.editors['string'] =
     $.ligerDefaults.Grid.editors['text'] = {
         create: function (container, editParm)
         {
             var input = $("<input type='text' class='l-text-editing'/>");
             container.append(input);
             input.ligerTextBox();
             return input;
         },
         getValue: function (input, editParm)
         {
             return input.val();
         },
         setValue: function (input, value, editParm)
         {
             input.val(value);
         },
         resize: function (input, width, height, editParm)
         {
             input.liger('option', 'width', width);
             input.liger('option', 'height', height);
         },
         destroy: function (input, editParm)
         {
             input.liger('destroy');
         }
     };

    $.ligerDefaults.Grid.editors['chk'] = $.ligerDefaults.Grid.editors['checkbox'] = {
        create: function (container, editParm)
        {
            var input = $("<input type='checkbox' />");
            container.append(input);
            input.ligerCheckBox();
            return input;
        },
        getValue: function (input, editParm)
        {
            return input[0].checked ? 1 : 0;
        },
        setValue: function (input, value, editParm)
        {
            input.val(value ? true : false);
        },
        resize: function (input, width, height, editParm)
        {
            input.liger('option', 'width', width);
            input.liger('option', 'height', height);
        },
        destroy: function (input, editParm)
        {
            input.liger('destroy');
        }
    };

    $.ligerui.controls.Grid = function (element, options)
    {
        $.ligerui.controls.Grid.base.constructor.call(this, element, options);
    };

    $.ligerui.controls.Grid.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return '$.ligerui.controls.Grid';
        },
        __idPrev: function ()
        {
            return 'grid';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Grid;
        },
        _init: function ()
        {
            $.ligerui.controls.Grid.base._init.call(this);
            var g = this, p = this.options;
            p.dataType = p.url ? "server" : "local";
            if (p.dataType == "local")
            {
                p.data = p.data || [];
                p.dataAction = "local";
            }
            if (p.isScroll == false)
            {
                p.height = 'auto';
            }
            if (!p.frozen)
            {
                p.frozenCheckbox = false;
                p.frozenDetail = false;
                p.frozenRownumbers = false;
            }
            if (p.detailToEdit)
            {
                p.enabledEdit = true;
                p.clickToEdit = false;
                p.detail = {
                    height: 'auto',
                    onShowDetail: function (record, container, callback)
                    {
                        $(container).addClass("l-grid-detailpanel-edit");
                        g.beginEdit(record, function (rowdata, column)
                        {
                            var editContainer = $("<div class='l-editbox'></div>");
                            editContainer.width(120).height(23);
                            editContainer.appendTo(container);
                            return editContainer;
                        });
                        function removeRow()
                        {
                            $(container).parent().parent().remove();
                            g.collapseDetail(record);
                        }
                        $("<div class='l-clear'></div>").appendTo(container);
                        $("<div class='l-button'>" + p.saveMessage + "</div>").appendTo(container).click(function ()
                        {
                            g.endEdit(record);
                            removeRow();
                        });
                        $("<div class='l-button'>" + p.applyMessage + "</div>").appendTo(container).click(function ()
                        {
                            g.submitEdit(record);
                        });
                        $("<div class='l-button'>" + p.cancelMessage + "</div>").appendTo(container).click(function ()
                        {
                            g.cancelEdit(record);
                            removeRow();
                        });
                    }
                };
            }
            if (p.tree)//启用分页模式
            {
                p.tree.childrenName = p.tree.childrenName || "children";
                p.tree.isParent = p.tree.isParent || function (rowData)
                {
                    var exist = p.tree.childrenName in rowData;
                    return exist;
                };
                p.tree.isExtend = p.tree.isExtend || function (rowData)
                {
                    if ('isextend' in rowData && rowData['isextend'] == false)
                        return false;
                    return true;
                };
            }
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.grid = $(g.element);
            g.grid.addClass("l-panel");
            var gridhtmlarr = [];
            gridhtmlarr.push("        <div class='l-panel-header'><span class='l-panel-header-text'></span></div>");
            gridhtmlarr.push("                    <div class='l-grid-loading'></div>");
            gridhtmlarr.push("        <div class='l-panel-bwarp'>");
            gridhtmlarr.push("            <div class='l-panel-body'>");
            gridhtmlarr.push("                <div class='l-grid'>");
            gridhtmlarr.push("                    <div class='l-grid-dragging-line'></div>");
            gridhtmlarr.push("                    <div class='l-grid-popup'><table cellpadding='0' cellspacing='0'><tbody></tbody></table></div>");

            gridhtmlarr.push("                  <div class='l-grid1'>");
            gridhtmlarr.push("                      <div class='l-grid-header l-grid-header1'>");
            gridhtmlarr.push("                          <div class='l-grid-header-inner'><table class='l-grid-header-table' cellpadding='0' cellspacing='0'><tbody></tbody></table></div>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                      <div class='l-grid-body l-grid-body1'>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                  </div>");

            gridhtmlarr.push("                  <div class='l-grid2'>");
            gridhtmlarr.push("                      <div class='l-grid-header l-grid-header2'>");
            gridhtmlarr.push("                          <div class='l-grid-header-inner'><table class='l-grid-header-table' cellpadding='0' cellspacing='0'><tbody></tbody></table></div>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                      <div class='l-grid-body l-grid-body2 l-scroll'>");
            gridhtmlarr.push("                      </div>");
            gridhtmlarr.push("                  </div>");


            gridhtmlarr.push("                 </div>");
            gridhtmlarr.push("              </div>");
            gridhtmlarr.push("         </div>");
            gridhtmlarr.push("         <div class='l-panel-bar'>");
            gridhtmlarr.push("            <div class='l-panel-bbar-inner'>");
            gridhtmlarr.push("                <div class='l-bar-group  l-bar-message'><span class='l-bar-text'></span></div>");
            gridhtmlarr.push("            <div class='l-bar-group l-bar-selectpagesize'></div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'>");
            gridhtmlarr.push("                    <div class='l-bar-button l-bar-btnfirst'><span></span></div>");
            gridhtmlarr.push("                    <div class='l-bar-button l-bar-btnprev'><span></span></div>");
            gridhtmlarr.push("                </div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'><span class='pcontrol'> <input type='text' size='4' value='1' style='width:20px' maxlength='3' /> / <span></span></span></div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'>");
            gridhtmlarr.push("                     <div class='l-bar-button l-bar-btnnext'><span></span></div>");
            gridhtmlarr.push("                    <div class='l-bar-button l-bar-btnlast'><span></span></div>");
            gridhtmlarr.push("                </div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");
            gridhtmlarr.push("                <div class='l-bar-group'>");
            gridhtmlarr.push("                     <div class='l-bar-button l-bar-btnload'><span></span></div>");
            gridhtmlarr.push("                </div>");
            gridhtmlarr.push("                <div class='l-bar-separator'></div>");

            gridhtmlarr.push("                <div class='l-clear'></div>");
            gridhtmlarr.push("            </div>");
            gridhtmlarr.push("         </div>");
            g.grid.html(gridhtmlarr.join(''));
            //头部
            g.header = $(".l-panel-header:first", g.grid);
            //主体
            g.body = $(".l-panel-body:first", g.grid);
            //底部工具条         
            g.toolbar = $(".l-panel-bar:first", g.grid);
            //显示/隐藏列      
            g.popup = $(".l-grid-popup:first", g.grid);
            //加载中
            g.gridloading = $(".l-grid-loading:first", g.grid);
            //调整列宽层 
            g.draggingline = $(".l-grid-dragging-line", g.grid);

            g.gridview = $(".l-grid:first", g.grid);
            g.gridview.attr("id", g.id + "grid");
            g.gridview1 = $(".l-grid1:first", g.gridview);
            g.gridview2 = $(".l-grid2:first", g.gridview);
            //表头     
            g.gridheader = $(".l-grid-header:first", g.gridview2);
            //表主体     
            g.gridbody = $(".l-grid-body:first", g.gridview2);

            //frozen
            g.f = {};
            //表头     
            g.f.gridheader = $(".l-grid-header:first", g.gridview1);
            //表主体     
            g.f.gridbody = $(".l-grid-body:first", g.gridview1);

            g.currentData = null;
            g.changedCells = {};
            g.editors = {};                 //多编辑器同时存在
            g.editor = { editing: false };  //单编辑器,配置clickToEdit
            if (p.height == "auto")
            {
                g.bind("SysGridHeightChanged", function ()
                {
                    if (g.enabledFrozen())
                        g.gridview.height(Math.max(g.gridview1.height(), g.gridview2.height()));
                });
            }

            var pc = $.extend({}, p);

            this._bulid();
            this._setColumns(p.columns);

            delete pc['columns'];
            delete pc['data'];
            delete pc['url']; 
            g.set(pc);  
            if (!p.delayLoad)
            {
                if(p.url)
                    g.set({ url: p.url});
                else if(p.data)
                    g.set({ data: p.data });
            }
        },
        _setFrozen: function (frozen)
        {
            if (frozen)
                this.grid.addClass("l-frozen");
            else
                this.grid.removeClass("l-frozen");
        },
        _setCssClass: function (value)
        {
            this.grid.addClass(value);
        },
        _setLoadingMessage: function (value)
        {
            this.gridloading.html(value);
        },
        _setHeight: function (h)
        {
            var g = this, p = this.options;
            g.unbind("SysGridHeightChanged");
            if (h == "auto")
            {
                g.bind("SysGridHeightChanged", function ()
                {
                    if (g.enabledFrozen())
                        g.gridview.height(Math.max(g.gridview1.height(), g.gridview2.height()));
                });
                return;
            }
            if (typeof h == "string" && h.indexOf('%') > 0)
            {
                if (p.inWindow)
                    h = $(window).height() * parseFloat(h) * 0.01;
                else
                    h = g.grid.parent().height() * parseFloat(h) * 0.01;
            }
            if (p.title) h -= 24;
            if (p.usePager) h -= 32;
            if (p.totalRender) h -= 25;
            var gridHeaderHeight = 23 * (g._columnMaxLevel - 1) + 22;
            h -= gridHeaderHeight;
            if (h > 0)
            {
                g.gridbody.height(h);
                if (h > 18) g.f.gridbody.height(h - 18);
                g.gridview.height(h + gridHeaderHeight);
            }
        },
        _updateFrozenWidth: function ()
        {
            var g = this, p = this.options;
            if (g.enabledFrozen())
            {
                g.gridview1.width(g.f.gridtablewidth);
                var view2width = g.gridview.width() - g.f.gridtablewidth;
                g.gridview2.css({ left: g.f.gridtablewidth });
                if (view2width > 0) g.gridview2.css({ width: view2width });
            }
        },
        _setWidth: function (value)
        {
            var g = this, p = this.options;
            if (g.enabledFrozen()) g._onResize();
        },
        _setUrl: function (value)
        {
            this.options.url = value;
            if (value)
            {
                this.options.dataType = "server";
                this.loadData(true);
            }
            else
            {
                this.options.dataType = "local";
            }
        },
        _setData: function (value)
        {
            this.loadData(this.options.data);
        },
        //刷新数据
        loadData: function (loadDataParm)
        { 
            var g = this, p = this.options;
            g.loading = true;
            var clause = null;
            var loadServer = true;
            if (typeof (loadDataParm) == "function")
            {
                clause = loadDataParm;
                loadServer = false;
            }
            else if (typeof (loadDataParm) == "boolean")
            {
                loadServer = loadDataParm;
            }
            else if (typeof (loadDataParm) == "object" && loadDataParm)
            {
                loadServer = false;
                p.dataType = "local";
                p.data = loadDataParm;
            }
            //参数初始化
            if (!p.newPage) p.newPage = 1;
            if (p.dataAction == "server")
            {
                if (!p.sortOrder) p.sortOrder = "asc";
            }
            var param = [];
            if (p.parms)
            {
                if (p.parms.length)
                {
                    $(p.parms).each(function ()
                    {
                        param.push({ name: this.name, value: this.value });
                    });
                }
                else if (typeof p.parms == "string")
                {
                    for (var name in p.parms)
                    {
                        param.push({ name: name, value: p.parms[name] });
                    }
                }
            }
            if (p.dataAction == "server")
            {
                if (p.usePager)
                {
                    param.push({ name: p.pageParmName, value: p.newPage });
                    param.push({ name: p.pagesizeParmName, value: p.pageSize });
                }
                if (p.sortName)
                {
                    param.push({ name: p.sortnameParmName, value: p.sortName });
                    param.push({ name: p.sortorderParmName, value: p.sortOrder });
                }
            };
            $(".l-bar-btnload span", g.toolbar).addClass("l-disabled");
            if (p.dataType == "local")
            {
                g.filteredData = g.data = p.data;
                if (clause)
                    g.filteredData[p.root] = g._searchData(g.filteredData[p.root], clause);
                if (p.usePager)
                    g.currentData = g._getCurrentPageData(g.filteredData);
                else
                {
                    g.currentData = g.filteredData;
                }
                g._showData();
            }
            else if (p.dataAction == "local" && !loadServer)
            {
                if (g.data && g.data[p.root])
                {
                    g.filteredData = g.data;
                    if (clause)
                        g.filteredData[p.root] = g._searchData(g.filteredData[p.root], clause);
                    g.currentData = g._getCurrentPageData(g.filteredData);
                    g._showData();
                }
            }
            else
            {
                if (g.hasBind('loading'))
                {
                    g.trigger('loading');
                }
                else
                {
                    g.toggleLoading.ligerDefer(g, 10, [true]);
                }
                g.loadServerData.ligerDefer(g, 10, [param, clause]);
            }
            g.loading = false;
        },
        loadServerData: function (param, clause)
        {
            var g = this, p = this.options;
            var ajaxOptions = {
                type: p.method,
                url: p.url,
                data: param,
                async: p.async,
                dataType: 'json',
                beforeSend: function ()
                {
                },
                success: function (data)
                {
                    g.trigger('success', [data, g]);
                    if (!data || !data[p.root] || !data[p.root].length)
                    {
                        g.currentData = g.data = {};
                        g.currentData[p.root] = g.data[p.root] = [];
                        g.currentData[p.record] = g.data[p.record] = 0;
                        g._showData();
                        return;
                    }
                    g.data = data;
                    if (p.dataAction == "server")
                    {
                        g.currentData = g.data;
                    }
                    else
                    {
                        g.filteredData = g.data;
                        if (clause) g.filteredData[p.root] = g._searchData(g.filteredData[p.root], clause);
                        if (p.usePager)
                            g.currentData = g._getCurrentPageData(g.filteredData);
                        else
                            g.currentData = g.filteredData;
                    }
                    g._showData.ligerDefer(g, 10, [g.currentData]);
                },
                complete: function ()
                {
                    if (g.hasBind('loaded'))
                    {
                        g.trigger('loaded', [g]);
                    }
                    else
                    {
                        g.toggleLoading.ligerDefer(g, 10, [false]);
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown)
                {
                    g.currentData = g.data = {};
                    g.currentData[p.root] = g.data[p.root] = [];
                    g.currentData[p.record] = g.data[p.record] = 0;
                    g.toggleLoading.ligerDefer(g, 10, [false]);
                    $(".l-bar-btnload span", g.toolbar).removeClass("l-disabled");
                    g.trigger('error', [XMLHttpRequest, textStatus, errorThrown]);
                }
            };
            if (p.contentType) ajaxOptions.contentType = p.contentType;
            $.ajax(ajaxOptions);
        },
        toggleLoading: function (show)
        {
            this.gridloading[show ? 'show' : 'hide']();
        },
        _createEditor: function (editor, container, editParm, width, height)
        {
            var editorInput = editor.create(container, editParm);
            if (editor.setValue) editor.setValue(editorInput, editParm.value, editParm);
            if (editor.resize) editor.resize(editorInput, width, height, editParm);
            return editorInput;
        },
        /*
        @description 使一行进入编辑状态
        @param  {rowParm} rowindex或者rowdata
        @param {containerBulider} 编辑器填充层构造器
        */
        beginEdit: function (rowParm, containerBulider)
        {
            var g = this, p = this.options;
            if (!p.enabledEdit || p.clickToEdit) return;
            var rowdata = g.getRow(rowParm);
            if (rowdata._editing) return;
            g.editors[rowdata['__id']] = {};
            rowdata._editing = true;
            g.reRender({ rowdata: rowdata });
            containerBulider = containerBulider || function (rowdata, column)
            {
                var cellobj = g.getCellObj(rowdata, column);
                var container = $(cellobj).html("");
                g.setCellEditing(rowdata, column, true);
                return container;
            };
            if (g.trigger('beginEdit', { record: rowdata, rowindex: rowdata['__index'] }) == false) return;
            for (var i = 0, l = g.columns.length; i < l; i++)
            {
                var column = g.columns[i];
                if (!column.name || !column.editor || !column.editor.type || !$.ligerDefaults.Grid.editors[column.editor.type]) continue;
                var editor = $.ligerDefaults.Grid.editors[column.editor.type];
                var editParm = { record: rowdata, value: rowdata[column.name], column: column, rowindex: rowdata['__index'] };
                var container = containerBulider(rowdata, column);
                var width = container.width(), height = container.height();
                var editorInput = g._createEditor(editor, container, editParm, width, height);
                g.editors[rowdata['__id']][column['__id']] = { editor: editor, input: editorInput, editParm: editParm, container: container };
            }
            g.trigger('afterBeginEdit', { record: rowdata, rowindex: rowdata['__index'] });

        },
        cancelEdit: function (rowParm)
        {
            var g = this;
            if (rowParm == undefined)
            {
                for (var rowid in g.editors)
                {
                    g.cancelEdit(rowid);
                }
            }
            else
            {
                var rowdata = g.getRow(rowParm);
                if (!g.editors[rowdata['__id']]) return;
                if (g.trigger('cancelEdit', { record: rowdata, rowindex: rowdata['__index'] }) == false) return;
                for (var columnid in g.editors[rowdata['__id']])
                {
                    var o = g.editors[rowdata['__id']][columnid];
                    if (o.editor.destroy) o.editor.destroy(o.input, o.editParm);
                }
                delete g.editors[rowdata['__id']];
                delete rowdata['_editing'];
                g.reRender({ rowdata: rowdata });
            }
        },
        addEditRow: function (rowdata)
        {
            var g = this;
            rowdata = g.add(rowdata);
            g.beginEdit(rowdata);
        },
        submitEdit: function (rowParm)
        {
            var g = this, p = this.options;
            if (rowParm == undefined)
            {
                for (var rowid in g.editors)
                {
                    g.submitEdit(rowid);
                }
            }
            else
            {
                var rowdata = g.getRow(rowParm);
                var newdata = {};
                if (!g.editors[rowdata['__id']]) return;
                for (var columnid in g.editors[rowdata['__id']])
                {
                    var o = g.editors[rowdata['__id']][columnid];
                    var column = o.editParm.column;
                    if (column.name)
                        newdata[column.name] = o.editor.getValue(o.input, o.editParm);
                }
                if (g.trigger('beforeSubmitEdit', { record: rowdata, rowindex: rowdata['__index'], newdata: newdata }) == false)
                    return false;
                g.updateRow(rowdata, newdata);
                g.trigger('afterSubmitEdit', { record: rowdata, rowindex: rowdata['__index'], newdata: newdata });
            }
        },
        endEdit: function (rowParm)
        {
            var g = this, p = this.options;
            if (g.editor.editing)
            {
                var o = g.editor;
                if (o.editor.destroy) o.editor.destroy(o.input, o.editParm);
                g.editor.container.remove();
                g.trigger('sysEndEdit', [g.editor.editParm]);
                g.trigger('endEdit', [g.editor.editParm]);
                g.editor.container;
                g.reRender({ rowdata: g.editor.editParm.record, column: g.editor.editParm.column });
                g.trigger('afterEdit', [g.editor.editParm]);
                g.editor = { editing: false };
            }
            else if (rowParm != undefined)
            {
                var rowdata = g.getRow(rowParm);
                if (!g.editors[rowdata['__id']]) return;
                if (g.submitEdit(rowParm) == false) return false;
                for (var columnid in g.editors[rowdata['__id']])
                {
                    var o = g.editors[rowdata['__id']][columnid];
                    if (o.editor.destroy) o.editor.destroy(o.input, o.editParm);
                }
                delete g.editors[rowdata['__id']];
                delete rowdata['_editing'];
                g.trigger('afterEdit', { record: rowdata, rowindex: rowdata['__index'] });
            }
            else
            {
                for (var rowid in g.editors)
                {
                    g.endEdit(rowid);
                }
            }
        },
        setWidth: function (w)
        {
            return this._setWidth(w);
        },
        setHeight: function (h)
        {
            return this._setHeight(h);
        },
        //是否启用复选框列
        enabledCheckbox: function ()
        {
            return this.options.checkbox ? true : false;
        },
        //是否固定列
        enabledFrozen: function ()
        {
            var g = this, p = this.options;
            if (!p.frozen) return false;
            var cols = g.columns || [];
            if (g.enabledDetail() && p.frozenDetail || g.enabledCheckbox() && p.frozenCheckbox
            || p.frozenRownumbers && p.rownumbers) return true;
            for (var i = 0, l = cols.length; i < l; i++)
            {
                if (cols[i].frozen)
                {
                    return true;
                }
            }
            this._setFrozen(false);
            return false;
        },
        //是否启用明细编辑
        enabledDetailEdit: function ()
        {
            if (!this.enabledDetail()) return false;
            return this.options.detailToEdit ? true : false;
        },
        //是否启用明细列
        enabledDetail: function ()
        {
            if (this.options.detail && this.options.detail.onShowDetail) return true;
            return false;
        },
        //是否启用分组
        enabledGroup: function ()
        {
            return this.options.groupColumnName ? true : false;
        },
        deleteSelectedRow: function ()
        {
            if (!this.selected) return;
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    this._deleteData.ligerDefer(this, 10, [o]);
            }
            this.reRender.ligerDefer(this, 20);
        },
        removeRange: function (rowArr)
        {
            var g = this, p = this.options;
            $.each(rowArr, function ()
            {
                g._removeData(this);
            });
            g.reRender();
        },
        remove: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            g._removeData(rowParm);
            g.reRender();
        },
        deleteRange: function (rowArr)
        {
            var g = this, p = this.options;
            $.each(rowArr, function ()
            {
                g._deleteData(this);
            });
            g.reRender();
        },
        deleteRow: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            g._deleteData(rowdata);
            g.reRender();
            g.isDataChanged = true;
        },
        _deleteData: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            rowdata[p.statusName] = 'delete';
            if (p.tree)
            {
                var children = g.getChildren(rowdata, true);
                if (children)
                {
                    for (var i = 0, l = children.length; i < l; i++)
                    {
                        children[i][p.statusName] = 'delete';
                    }
                }
            }
            g.deletedRows = g.deletedRows || [];
            g.deletedRows.push(rowdata);
            g._removeSelected(rowdata);
        },
        /*
        @param  {arg} column index、column name、column、单元格
        @param  {value} 值
        @param  {rowParm} rowindex或者rowdata
        */
        updateCell: function (arg, value, rowParm)
        {
            var g = this, p = this.options;
            var column, cellObj, rowdata;
            if (typeof (arg) == "string") //column name
            {
                for (var i = 0, l = g.columns.length; i < l; i++)
                {
                    if (g.columns[i].name == arg)
                    {
                        g.updateCell(i, value, rowParm);
                    }
                }
                return;
            }
            if (typeof (arg) == "number")
            {
                column = g.columns[arg];
                rowdata = g.getRow(rowParm);
                cellObj = g.getCellObj(rowdata, column);
            }
            else if (typeof (arg) == "object" && arg['__id'])
            {
                column = arg;
                rowdata = g.getRow(rowParm);
                cellObj = g.getCellObj(rowdata, column);
            }
            else
            {
                cellObj = arg;
                var ids = cellObj.id.split('|');
                var columnid = ids[ids.length - 1];
                column = g._columns[columnid];
                var row = $(cellObj).parent();
                rowdata = rowdata || g.getRow(row[0]);
            }
            if (value != null && column.name)
            {
                rowdata[column.name] = value;
                if (rowdata[p.statusName] != 'add')
                    rowdata[p.statusName] = 'update';
                g.isDataChanged = true;
            }
            g.reRender({ rowdata: rowdata, column: column });
        },
        addRows: function (rowdataArr, neardata, isBefore, parentRowData)
        {
            var g = this, p = this.options;
            $(rowdataArr).each(function ()
            {
                g.addRow(this, neardata, isBefore, parentRowData);
            });
        },
        _createRowid: function ()
        {
            return "r" + (1000 + this.recordNumber);
        },
        _isRowId: function (str)
        {
            return (str in this.records);
        },
        _addNewRecord: function (o, previd, pid)
        {
            var g = this, p = this.options;
            g.recordNumber++;
            o['__id'] = g._createRowid();
            o['__previd'] = previd;
            if (previd && previd != -1)
            {
                var prev = g.records[previd];
                if (prev['__nextid'] && prev['__nextid'] != -1)
                {
                    var prevOldNext = g.records[prev['__nextid']];
                    if (prevOldNext)
                        prevOldNext['__previd'] = o['__id'];
                }
                prev['__nextid'] = o['__id'];
                o['__index'] = prev['__index'] + 1;
            }
            else
            {
                o['__index'] = 0;
            }
            if (p.tree)
            {
                if (pid && pid != -1)
                {
                    var parent = g.records[pid];
                    o['__pid'] = pid;
                    o['__level'] = parent['__level'] + 1;
                }
                else
                {
                    o['__pid'] = -1;
                    o['__level'] = 1;
                }
                o['__hasChildren'] = o[p.tree.childrenName] ? true : false;
            }
            if (o[p.statusName] != "add")
                o[p.statusName] = "nochanged";
            g.rows[o['__index']] = o;
            g.records[o['__id']] = o;
            return o;
        },
        //将原始的数据转换成适合 grid的行数据 
        _getRows: function (data)
        {
            var g = this, p = this.options;
            var targetData = [];
            function load(data)
            {
                if (!data || !data.length) return;
                for (var i = 0, l = data.length; i < l; i++)
                {
                    var o = data[i];
                    targetData.push(o);
                    if (o[p.tree.childrenName])
                    {
                        load(o[p.tree.childrenName]);
                    }
                }
            }
            load(data);
            return targetData;
        },
        _updateGridData: function ()
        {
            var g = this, p = this.options;
            g.recordNumber = 0;
            g.rows = [];
            g.records = {};
            var previd = -1;
            function load(data, pid)
            {
                if (!data || !data.length) return;
                for (var i = 0, l = data.length; i < l; i++)
                {
                    var o = data[i];
                    g.formatRecord(o);
                    if (o[p.statusName] == "delete") continue;
                    g._addNewRecord(o, previd, pid);
                    previd = o['__id'];
                    if (o['__hasChildren'])
                    {
                        load(o[p.tree.childrenName], o['__id']);
                    }
                }
            }
            load(g.currentData[p.root], -1);
            return g.rows;
        },
        _moveData: function (from, to, isAfter)
        {
            var g = this, p = this.options;
            var fromRow = g.getRow(from);
            var toRow = g.getRow(to);
            var fromIndex, toIndex;
            var listdata = g._getParentChildren(fromRow);
            fromIndex = $.inArray(fromRow, listdata);
            listdata.splice(fromIndex, 1);
            listdata = g._getParentChildren(toRow);
            toIndex = $.inArray(toRow, listdata);
            listdata.splice(toIndex + (isAfter ? 1 : 0), 0, fromRow);
        },
        move: function (from, to, isAfter)
        {
            this._moveData(from, to, isAfter);
            this.reRender();
        },
        moveRange: function (rows, to, isAfter)
        {
            for (var i in rows)
            {
                this._moveData(rows[i], to, isAfter);
            }
            this.reRender();
        },
        up: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var listdata = g._getParentChildren(rowdata);
            var index = $.inArray(rowdata, listdata);
            if (index == -1 || index == 0) return;
            g.move(rowdata, listdata[index - 1], false);
        },
        down: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var listdata = g._getParentChildren(rowdata);
            var index = $.inArray(rowdata, listdata);
            if (index == -1 || index == listdata.length - 1) return;
            g.move(rowdata, listdata[index + 1], true);
        },
        addRow: function (rowdata, neardata, isBefore, parentRowData)
        {
            var g = this, p = this.options;
            rowdata = rowdata || {};
            g._addData(rowdata, parentRowData, neardata, isBefore);
            g.reRender();
            //标识状态
            rowdata[p.statusName] = 'add';
            if (p.tree)
            {
                var children = g.getChildren(rowdata, true);
                if (children)
                {
                    for (var i = 0, l = children.length; i < l; i++)
                    {
                        children[i][p.statusName] = 'add';
                    }
                }
            }
            g.isDataChanged = true;
            p.total = p.total ? (p.total + 1) : 1;
            p.pageCount = Math.ceil(p.total / p.pageSize);
            g._buildPager();
            g.trigger('SysGridHeightChanged');
            g.trigger('afterAddRow', [rowdata]);
            return rowdata;
        },
        updateRow: function (rowDom, newRowData)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowDom);
            //标识状态
            g.isDataChanged = true;
            $.extend(rowdata, newRowData || {});
            if (rowdata[p.statusName] != 'add')
                rowdata[p.statusName] = 'update';
            g.reRender.ligerDefer(g, 10, [{ rowdata: rowdata}]);
            return rowdata;
        },
        setCellEditing: function (rowdata, column, editing)
        {
            var g = this, p = this.options;
            var cell = g.getCellObj(rowdata, column);
            var methodName = editing ? 'addClass' : 'removeClass';
            $(cell)[methodName]("l-grid-row-cell-editing");
            if (rowdata['__id'] != 0)
            {
                var prevrowobj = $(g.getRowObj(rowdata['__id'])).prev();
                if (!prevrowobj.length) return;
                var prevrow = g.getRow(prevrowobj[0]);
                var cellprev = g.getCellObj(prevrow, column);
                if (!cellprev) return;
                $(cellprev)[methodName]("l-grid-row-cell-editing-topcell");
            }
            if (column['__previd'] != -1 && column['__previd'] != null)
            {
                var cellprev = $(g.getCellObj(rowdata, column)).prev();
                $(cellprev)[methodName]("l-grid-row-cell-editing-leftcell");
            }
        },
        reRender: function (e)
        {
            var g = this, p = this.options;
            e = e || {};
            var rowdata = e.rowdata, column = e.column;
            if (column && (column.isdetail || column.ischeckbox)) return;
            if (rowdata && rowdata[p.statusName] == "delete") return;
            if (rowdata && column)
            {
                var cell = g.getCellObj(rowdata, column);
                $(cell).html(g._getCellHtml(rowdata, column));
                if (!column.issystem)
                    g.setCellEditing(rowdata, column, false);
            }
            else if (rowdata)
            {
                $(g.columns).each(function () { g.reRender({ rowdata: rowdata, column: this }); });
            }
            else if (column)
            {
                for (var rowid in g.records) { g.reRender({ rowdata: g.records[rowid], column: column }); }
                for (var i = 0; i < g.totalNumber; i++)
                {
                    var tobj = document.getElementById(g.id + "|total" + i + "|" + column['__id']);
                    $("div:first", tobj).html(g._getTotalCellContent(column, g.groups && g.groups[i] ? g.groups[i] : g.currentData[p.root]));
                }
            }
            else
            {
                g._showData();
            }
        },
        getData: function (status, removeStatus)
        {
            var g = this, p = this.options;
            var data = [];
            for (var rowid in g.records)
            {
                var o = $.extend({}, g.records[rowid]);
                if (o[p.statusName] == status || status == undefined)
                {
                    data.push(g.formatRecord(o, removeStatus));
                }
            }
            return data;
        },
        //格式化数据
        formatRecord: function (o, removeStatus)
        {
            delete o['__id'];
            delete o['__previd'];
            delete o['__nextid'];
            delete o['__index'];
            if (this.options.tree)
            {
                delete o['__pid'];
                delete o['__level'];
                delete o['__hasChildren'];
            }
            if (removeStatus) delete o[this.options.statusName];
            return o;
        },
        getUpdated: function ()
        {
            return this.getData('update', true);
        },
        getDeleted: function ()
        {
            return this.deletedRows;
        },
        getAdded: function ()
        {
            return this.getData('add', true);
        },
        getColumn: function (columnParm)
        {
            var g = this, p = this.options;
            if (typeof columnParm == "string") // column id
            {
                if (g._isColumnId(columnParm))
                    return g._columns[columnParm];
                else
                    return g.columns[parseInt(columnParm)];
            }
            else if (typeof (columnParm) == "number") //column index
            {
                return g.columns[columnParm];
            }
            else if (typeof columnParm == "object" && columnParm.nodeType == 1) //column header cell
            {
                var ids = columnParm.id.split('|');
                var columnid = ids[ids.length - 1];
                return g._columns[columnid];
            }
            return columnParm;
        },
        getColumnType: function (columnname)
        {
            var g = this, p = this.options;
            for (i = 0; i < g.columns.length; i++)
            {
                if (g.columns[i].name == columnname)
                {
                    if (g.columns[i].type) return g.columns[i].type;
                    return "string";
                }
            }
            return null;
        },
        //是否包含汇总
        isTotalSummary: function ()
        {
            var g = this, p = this.options;
            for (var i = 0; i < g.columns.length; i++)
            {
                if (g.columns[i].totalSummary) return true;
            }
            return false;
        },
        //根据层次获取列集合
        //如果columnLevel为空，获取叶节点集合
        getColumns: function (columnLevel)
        {
            var g = this, p = this.options;
            var columns = [];
            for (var id in g._columns)
            {
                var col = g._columns[id];
                if (columnLevel != undefined)
                {
                    if (col['__level'] == columnLevel) columns.push(col);
                }
                else
                {
                    if (col['__leaf']) columns.push(col);
                }
            }
            return columns;
        },
        //改变排序
        changeSort: function (columnName, sortOrder)
        {
            var g = this, p = this.options;
            if (g.loading) return true;
            if (p.dataAction == "local")
            {
                var columnType = g.getColumnType(columnName);
                if (!g.sortedData)
                    g.sortedData = g.filteredData;
                if (p.sortName == columnName)
                {
                    g.sortedData[p.root].reverse();
                } else
                {
                    g.sortedData[p.root].sort(function (data1, data2)
                    {
                        return g._compareData(data1, data2, columnName, columnType);
                    });
                }
                if (p.usePager)
                    g.currentData = g._getCurrentPageData(g.sortedData);
                else
                    g.currentData = g.sortedData;
                g._showData();
            }
            p.sortName = columnName;
            p.sortOrder = sortOrder;
            if (p.dataAction == "server")
            {
                g.loadData(p.where);
            }
        },
        //改变分页
        changePage: function (ctype)
        {
            var g = this, p = this.options;
            if (g.loading) return true;
            if (p.dataAction != "local" && g.isDataChanged && !confirm(p.isContinueByDataChanged))
                return false;
            p.pageCount = parseInt($(".pcontrol span", g.toolbar).html());
            switch (ctype)
            {
                case 'first': if (p.page == 1) return; p.newPage = 1; break;
                case 'prev': if (p.page == 1) return; if (p.page > 1) p.newPage = parseInt(p.page) - 1; break;
                case 'next': if (p.page >= p.pageCount) return; p.newPage = parseInt(p.page) + 1; break;
                case 'last': if (p.page >= p.pageCount) return; p.newPage = p.pageCount; break;
                case 'input':
                    var nv = parseInt($('.pcontrol input', g.toolbar).val());
                    if (isNaN(nv)) nv = 1;
                    if (nv < 1) nv = 1;
                    else if (nv > p.pageCount) nv = p.pageCount;
                    $('.pcontrol input', g.toolbar).val(nv);
                    p.newPage = nv;
                    break;
            }
            if (p.newPage == p.page) return false;
            if (p.newPage == 1)
            {
                $(".l-bar-btnfirst span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).addClass("l-disabled");
            }
            else
            {
                $(".l-bar-btnfirst span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).removeClass("l-disabled");
            }
            if (p.newPage == p.pageCount)
            {
                $(".l-bar-btnlast span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).addClass("l-disabled");
            }
            else
            {
                $(".l-bar-btnlast span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).removeClass("l-disabled");
            }
            g.trigger('changePage', [p.newPage]);
            if (p.dataAction == "server")
            {
                g.loadData(p.where);
            }
            else
            {
                g.currentData = g._getCurrentPageData(g.filteredData);
                g._showData();
            }
        },
        getSelectedRow: function ()
        {
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    return o;
            }
            return null;
        },
        getSelectedRows: function ()
        {
            var arr = [];
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    arr.push(o);
            }
            return arr;
        },
        getSelectedRowObj: function ()
        {
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    return this.getRowObj(o);
            }
            return null;
        },
        getSelectedRowObjs: function ()
        {
            var arr = [];
            for (var i in this.selected)
            {
                var o = this.selected[i];
                if (o['__id'] in this.records)
                    arr.push(this.getRowObj(o));
            }
            return arr;
        },
        getCellObj: function (rowParm, column)
        {
            var rowdata = this.getRow(rowParm);
            column = this.getColumn(column);
            return document.getElementById(this._getCellDomId(rowdata, column));
        },
        getRowObj: function (rowParm, frozen)
        {
            var g = this, p = this.options;
            if (rowParm == null) return null;
            if (typeof (rowParm) == "string")
            {
                if (g._isRowId(rowParm))
                    return document.getElementById(g.id + (frozen ? "|1|" : "|2|") + rowParm);
                else
                    return document.getElementById(g.id + (frozen ? "|1|" : "|2|") + g.rows[parseInt(rowParm)]['__id']);
            }
            else if (typeof (rowParm) == "number")
            {
                return document.getElementById(g.id + (frozen ? "|1|" : "|2|") + g.rows[rowParm]['__id']);
            }
            else if (typeof (rowParm) == "object" && rowParm['__id']) //rowdata
            {
                return g.getRowObj(rowParm['__id'], frozen);
            }
            return rowParm;
        },
        getRow: function (rowParm)
        {
            var g = this, p = this.options;
            if (rowParm == null) return null;
            if (typeof (rowParm) == "string")
            {
                if (g._isRowId(rowParm))
                    return g.records[rowParm];
                else
                    return g.rows[parseInt(rowParm)];
            }
            else if (typeof (rowParm) == "number")
            {
                return g.rows[parseInt(rowParm)];
            }
            else if (typeof (rowParm) == "object" && rowParm.nodeType == 1 && !rowParm['__id']) //dom对象
            {
                return g._getRowByDomId(rowParm.id);
            }
            return rowParm;
        },
        _setColumnVisible: function (column, hide)
        {
            var g = this, p = this.options;
            if (!hide)  //显示
            {
                column._hide = false;
                document.getElementById(column['__domid']).style.display = "";
                //判断分组列是否隐藏,如果隐藏了则显示出来
                if (column['__pid'] != -1)
                {
                    var pcol = g._columns[column['__pid']];
                    if (pcol._hide)
                    {
                        document.getElementById(pcol['__domid']).style.display = "";
                        this._setColumnVisible(pcol, hide);
                    }
                }
            }
            else //隐藏
            {
                column._hide = true;
                document.getElementById(column['__domid']).style.display = "none";
                //判断同分组的列是否都隐藏,如果是则隐藏分组列
                if (column['__pid'] != -1)
                {
                    var hideall = true;
                    var pcol = this._columns[column['__pid']];
                    for (var i = 0; pcol && i < pcol.columns.length; i++)
                    {
                        if (!pcol.columns[i]._hide)
                        {
                            hideall = false;
                            break;
                        }
                    }
                    if (hideall)
                    {
                        pcol._hide = true;
                        document.getElementById(pcol['__domid']).style.display = "none";
                        this._setColumnVisible(pcol, hide);
                    }
                }
            }
        },
        //显示隐藏列
        toggleCol: function (columnparm, visible, toggleByPopup)
        {
            var g = this, p = this.options;
            var column;
            if (typeof (columnparm) == "number")
            {
                column = g.columns[columnparm];
            }
            else if (typeof (columnparm) == "object" && columnparm['__id'])
            {
                column = columnparm;
            }
            else if (typeof (columnparm) == "string")
            {
                if (g._isColumnId(columnparm)) // column id
                {
                    column = g._columns[columnparm];
                }
                else  // column name
                {
                    $(g.columns).each(function ()
                    {
                        if (this.name == columnparm)
                            g.toggleCol(this, visible, toggleByPopup);
                    });
                    return;
                }
            }
            if (!column) return;
            var columnindex = column['__leafindex'];
            var headercell = document.getElementById(column['__domid']);
            if (!headercell) return;
            headercell = $(headercell);
            var cells = [];
            for (var i in g.rows)
            {
                var obj = g.getCellObj(g.rows[i], column);
                if (obj) cells.push(obj);
            }
            for (var i = 0; i < g.totalNumber; i++)
            {
                var tobj = document.getElementById(g.id + "|total" + i + "|" + column['__id']);
                if (tobj) cells.push(tobj);
            }
            var colwidth = column._width;
            //显示列
            if (visible && column._hide)
            {
                if (column.frozen)
                    g.f.gridtablewidth += (parseInt(colwidth) + 1);
                else
                    g.gridtablewidth += (parseInt(colwidth) + 1);
                g._setColumnVisible(column, false);
                $(cells).show();
            }
            //隐藏列
            else if (!visible && !column._hide)
            {
                if (column.frozen)
                    g.f.gridtablewidth -= (parseInt(colwidth) + 1);
                else
                    g.gridtablewidth -= (parseInt(colwidth) + 1);
                g._setColumnVisible(column, true);
                $(cells).hide();
            }
            if (column.frozen)
            {
                $("div:first", g.f.gridheader).width(g.f.gridtablewidth);
                $("div:first", g.f.gridbody).width(g.f.gridtablewidth);
            }
            else
            {
                $("div:first", g.gridheader).width(g.gridtablewidth + 40);
                $("div:first", g.gridbody).width(g.gridtablewidth);
            }
            g._updateFrozenWidth();
            if (!toggleByPopup)
            {
                $(':checkbox[columnindex=' + columnindex + "]", g.popup).each(function ()
                {
                    this.checked = visible;
                    if ($.fn.ligerCheckBox)
                    {
                        var checkboxmanager = $(this).ligerGetCheckBoxManager();
                        if (checkboxmanager) checkboxmanager.updateStyle();
                    }
                });
            }
        },
        //设置列宽
        setColumnWidth: function (columnparm, newwidth)
        {
            var g = this, p = this.options;
            if (!newwidth) return;
            newwidth = parseInt(newwidth, 10);
            var column;
            if (typeof (columnparm) == "number")
            {
                column = g.columns[columnparm];
            }
            else if (typeof (columnparm) == "object" && columnparm['__id'])
            {
                column = columnparm;
            }
            else if (typeof (columnparm) == "string")
            {
                if (g._isColumnId(columnparm)) // column id
                {
                    column = g._columns[columnparm];
                }
                else  // column name
                {
                    $(g.columns).each(function ()
                    {
                        if (this.name == columnparm)
                            g.setColumnWidth(this, newwidth);
                    });
                    return;
                }
            }
            if (!column) return;
            var mincolumnwidth = p.minColumnWidth;
            if (column.minWidth) mincolumnwidth = column.minWidth;
            newwidth = newwidth < mincolumnwidth ? mincolumnwidth : newwidth;
            var diff = newwidth - column._width;
            if (g.trigger('beforeChangeColumnWidth', [column, newwidth]) == false) return;
            column._width = newwidth;
            if (column.frozen)
            {
                g.f.gridtablewidth += diff;
                $("div:first", g.f.gridheader).width(g.f.gridtablewidth);
                $("div:first", g.f.gridbody).width(g.f.gridtablewidth);
            }
            else
            {
                g.gridtablewidth += diff;
                $("div:first", g.gridheader).width(g.gridtablewidth + 40);
                $("div:first", g.gridbody).width(g.gridtablewidth);
            }
            $(document.getElementById(column['__domid'])).css('width', newwidth);
            var cells = [];
            for (var rowid in g.records)
            {
                var obj = g.getCellObj(g.records[rowid], column);
                if (obj) cells.push(obj);

                if (!g.enabledDetailEdit() && g.editors[rowid] && g.editors[rowid][column['__id']])
                {
                    var o = g.editors[rowid][column['__id']];
                    if (o.editor.resize) o.editor.resize(o.input, newwidth, o.container.height(), o.editParm);
                }
            }
            for (var i = 0; i < g.totalNumber; i++)
            {
                var tobj = document.getElementById(g.id + "|total" + i + "|" + column['__id']);
                if (tobj) cells.push(tobj);
            }
            $(cells).css('width', newwidth).find("> div.l-grid-row-cell-inner:first").css('width', newwidth - 8);

            g._updateFrozenWidth();


            g.trigger('afterChangeColumnWidth', [column, newwidth]);
        },
        //改变列表头内容
        changeHeaderText: function (columnparm, headerText)
        {
            var g = this, p = this.options;
            var column;
            if (typeof (columnparm) == "number")
            {
                column = g.columns[columnparm];
            }
            else if (typeof (columnparm) == "object" && columnparm['__id'])
            {
                column = columnparm;
            }
            else if (typeof (columnparm) == "string")
            {
                if (g._isColumnId(columnparm)) // column id
                {
                    column = g._columns[columnparm];
                }
                else  // column name
                {
                    $(g.columns).each(function ()
                    {
                        if (this.name == columnparm)
                            g.changeHeaderText(this, headerText);
                    });
                    return;
                }
            }
            if (!column) return;
            var columnindex = column['__leafindex'];
            var headercell = document.getElementById(column['__domid']);
            $(".l-grid-hd-cell-text", headercell).html(headerText);
            if (p.allowHideColumn)
            {
                $(':checkbox[columnindex=' + columnindex + "]", g.popup).parent().next().html(headerText);
            }
        },
        //改变列的位置
        changeCol: function (from, to, isAfter)
        {
            var g = this, p = this.options;
            if (!from || !to) return;
            var fromCol = g.getColumn(from);
            var toCol = g.getColumn(to);
            fromCol.frozen = toCol.frozen;
            var fromColIndex, toColIndex;
            var fromColumns = fromCol['__pid'] == -1 ? p.columns : g._columns[fromCol['__pid']].columns;
            var toColumns = toCol['__pid'] == -1 ? p.columns : g._columns[toCol['__pid']].columns;
            fromColIndex = $.inArray(fromCol, fromColumns);
            toColIndex = $.inArray(toCol, toColumns);
            var sameParent = fromColumns == toColumns;
            var sameLevel = fromCol['__level'] == toCol['__level'];
            toColumns.splice(toColIndex + (isAfter ? 1 : 0), 0, fromCol);
            if (!sameParent)
            {
                fromColumns.splice(fromColIndex, 1);
            }
            else
            {
                if (isAfter) fromColumns.splice(fromColIndex, 1);
                else fromColumns.splice(fromColIndex + 1, 1);
            }
            g._setColumns(p.columns);
            g.reRender();
        },


        collapseDetail: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            for (var i = 0, l = g.columns.length; i < l; i++)
            {
                if (g.columns[i].isdetail)
                {
                    var row = g.getRowObj(rowdata);
                    var cell = g.getCellObj(rowdata, g.columns[i]);
                    $(row).next("tr.l-grid-detailpanel").hide();
                    $(".l-grid-row-cell-detailbtn:first", cell).removeClass("l-open");
                    g.trigger('SysGridHeightChanged');
                    return;
                }
            }
        },
        extendDetail: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            for (var i = 0, l = g.columns; i < l; i++)
            {
                if (g.columns[i].isdetail)
                {
                    var row = g.getRowObj(rowdata);
                    var cell = g.getCellObj(rowdata, g.columns[i]);
                    $(row).next("tr.l-grid-detailpanel").show();
                    $(".l-grid-row-cell-detailbtn:first", cell).addClass("l-open");
                    g.trigger('SysGridHeightChanged');
                    return;
                }
            }
        },
        getParent: function (rowParm)
        {
            var g = this, p = this.options;
            if (!p.tree) return null;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return null;
            if (rowdata['__pid'] in g.records) return g.records[rowdata['__pid']];
            else return null;
        },
        getChildren: function (rowParm, deep)
        {
            var g = this, p = this.options;
            if (!p.tree) return null;
            var rowData = g.getRow(rowParm);
            if (!rowData) return null;
            var arr = [];
            function loadChildren(data)
            {
                if (data[p.tree.childrenName])
                {
                    for (var i = 0, l = data[p.tree.childrenName].length; i < l; i++)
                    {
                        var o = data[p.tree.childrenName][i];
                        if (o['__status'] == 'delete') continue;
                        arr.push(o);
                        if (deep)
                            loadChildren(o);
                    }
                }
            }
            loadChildren(rowData);
            return arr;
        },
        isLeaf: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata) return;
            return rowdata['__hasChildren'] ? false : true;
        },
        hasChildren: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = this.getRow(rowParm);
            if (!rowdata) return;
            return (rowdata[p.tree.childrenName] && rowdata[p.tree.childrenName].length) ? true : false;
        },
        existRecord: function (record)
        {
            for (var rowid in this.records)
            {
                if (this.records[rowid] == record) return true;
            }
            return false;
        },
        _removeSelected: function (rowdata)
        {
            var g = this, p = this.options;
            if (p.tree)
            {
                var children = g.getChildren(rowdata, true);
                if (children)
                {
                    for (var i = 0, l = children.length; i < l; i++)
                    {
                        var index2 = $.inArray(children[i], g.selected);
                        if (index2 != -1) g.selected.splice(index2, 1);
                    }
                }
            }
            var index = $.inArray(rowdata, g.selected);
            if (index != -1) g.selected.splice(index, 1);
        },
        _getParentChildren: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var listdata;
            if (p.tree && g.existRecord(rowdata) && rowdata['__pid'] in g.records)
            {
                listdata = g.records[rowdata['__pid']][p.tree.childrenName];
            }
            else
            {
                listdata = g.currentData[p.root];
            }
            return listdata;
        },
        _removeData: function (rowdata)
        {
            var g = this, p = this.options;
            var listdata = g._getParentChildren(rowdata);
            var index = $.inArray(rowdata, listdata);
            if (index != -1)
            {
                listdata.splice(index, 1);
            }
            g._removeSelected(rowdata);
        },
        _addData: function (rowdata, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            var listdata = g.currentData[p.root];
            if (neardata)
            {
                if (p.tree)
                {
                    if (parentdata)
                        listdata = parentdata[p.tree.childrenName];
                    else if (neardata['__pid'] in g.records)
                        listdata = g.records[neardata['__pid']][p.tree.childrenName];
                }
                var index = $.inArray(neardata, listdata);
                listdata.splice(index == -1 ? -1 : index + (isBefore ? 0 : 1), 0, rowdata);
            }
            else
            {
                if (p.tree && parentdata)
                {
                    listdata = parentdata[p.tree.childrenName];
                }
                listdata.push(rowdata);
            }
        },
        //移动数据(树)
        //@parm [parentdata] 附加到哪一个节点下级
        //@parm [neardata] 附加到哪一个节点的上方/下方
        //@parm [isBefore] 是否附加到上方
        _appendData: function (rowdata, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            rowdata[p.statusName] = "update";
            g._removeData(rowdata);
            g._addData(rowdata, parentdata, neardata, isBefore);
        },
        appendRange: function (rows, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            var toRender = false;
            $.each(rows, function (i, item)
            {
                if (item['__id'] && g.existRecord(item))
                {
                    if (g.isLeaf(parentdata)) g.upgrade(parentdata);
                    g._appendData(item, parentdata, neardata, isBefore);
                    toRender = true;
                }
                else
                {
                    g.appendRow(item, parentdata, neardata, isBefore);
                }
            });
            if (toRender) g.reRender();

        },
        appendRow: function (rowdata, parentdata, neardata, isBefore)
        {
            var g = this, p = this.options;
            if ($.isArray(rowdata))
            {
                g.appendRange(rowdata, parentdata, neardata, isBefore);
                return;
            }
            if (rowdata['__id'] && g.existRecord(rowdata))
            {
                g._appendData(rowdata, parentdata, neardata, isBefore);
                g.reRender();
                return;
            }
            if (parentdata && g.isLeaf(parentdata)) g.upgrade(parentdata);
            g.addRow(rowdata, neardata, isBefore ? true : false, parentdata);
        },
        upgrade: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata || !p.tree) return;
            rowdata[p.tree.childrenName] = rowdata[p.tree.childrenName] || [];
            rowdata['__hasChildren'] = true;
            var rowobjs = [g.getRowObj(rowdata)];
            if (g.enabledFrozen()) rowobjs.push(g.getRowObj(rowdata, true));
            $("> td > div > .l-grid-tree-space:last", rowobjs).addClass("l-grid-tree-link l-grid-tree-link-open");
        },
        demotion: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            if (!rowdata || !p.tree) return;
            var rowobjs = [g.getRowObj(rowdata)];
            if (g.enabledFrozen()) rowobjs.push(g.getRowObj(rowdata, true));
            $("> td > div > .l-grid-tree-space:last", rowobjs).removeClass("l-grid-tree-link l-grid-tree-link-open l-grid-tree-link-close");
            if (g.hasChildren(rowdata))
            {
                var children = g.getChildren(rowdata);
                for (var i = 0, l = children.length; i < l; i++)
                {
                    g.deleteRow(children[i]);
                }
            }
            rowdata['__hasChildren'] = false;
        },
        collapse: function (rowParm)
        {
            var g = this, p = this.options;
            var targetRowObj = g.getRowObj(rowParm);
            var linkbtn = $(".l-grid-tree-link", targetRowObj);
            if (linkbtn.hasClass("l-grid-tree-link-close")) return;
            g.toggle(rowParm);
        },
        expand: function (rowParm)
        {
            var g = this, p = this.options;
            var targetRowObj = g.getRowObj(rowParm);
            var linkbtn = $(".l-grid-tree-link", targetRowObj);
            if (linkbtn.hasClass("l-grid-tree-link-open")) return;
            g.toggle(rowParm);
        },
        toggle: function (rowParm)
        {
            if (!rowParm) return;
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var targetRowObj = [g.getRowObj(rowdata)];
            if (g.enabledFrozen()) targetRowObj.push(g.getRowObj(rowdata, true));
            var level = rowdata['__level'], indexInCollapsedRows;
            var linkbtn = $(".l-grid-tree-link:first", targetRowObj);
            var opening = true;
            g.collapsedRows = g.collapsedRows || [];
            if (linkbtn.hasClass("l-grid-tree-link-close")) //收缩
            {
                linkbtn.removeClass("l-grid-tree-link-close").addClass("l-grid-tree-link-open");
                indexInCollapsedRows = $.inArray(rowdata, g.collapsedRows);
                if (indexInCollapsedRows != -1) g.collapsedRows.splice(indexInCollapsedRows, 1);
            }
            else //折叠
            {
                opening = false;
                linkbtn.addClass("l-grid-tree-link-close").removeClass("l-grid-tree-link-open");
                indexInCollapsedRows = $.inArray(rowdata, g.collapsedRows);
                if (indexInCollapsedRows == -1) g.collapsedRows.push(rowdata);
            }
            var children = g.getChildren(rowdata, true);
            for (var i = 0, l = children.length; i < l; i++)
            {
                var o = children[i];
                var currentRow = $([g.getRowObj(o['__id'])]);
                if (g.enabledFrozen()) currentRow = currentRow.add(g.getRowObj(o['__id'], true));
                if (opening)
                {
                    $(".l-grid-tree-link", currentRow).removeClass("l-grid-tree-link-close").addClass("l-grid-tree-link-open");
                    currentRow.show();
                }
                else
                {
                    $(".l-grid-tree-link", currentRow).removeClass("l-grid-tree-link-open").addClass("l-grid-tree-link-close");
                    currentRow.hide();
                }
            }
        },
        _bulid: function ()
        {
            var g = this;
            g._clearGrid();
            //创建头部
            g._initBuildHeader();
            //宽度高度初始化
            g._initHeight();
            //创建底部工具条
            g._initFootbar();
            //创建分页
            g._buildPager();
            //创建事件
            g._setEvent();
        },
        _setColumns: function (columns)
        {
            var g = this;
            //初始化列
            g._initColumns();
            //创建表头
            g._initBuildGridHeader();
            //创建 显示/隐藏 列 列表
            g._initBuildPopup();
        },
        _initBuildHeader: function ()
        {
            var g = this, p = this.options;
            if (p.title)
                $(".l-panel-header-text", g.header).html(p.title);
            else
                g.header.hide();
        },
        _createColumnId: function (column)
        {
            if (column.id != null) return column.id.toString();
            return "c" + (100 + this._columnCount);
        },
        _isColumnId: function (str)
        {
            return (str in this._columns);
        },
        _initColumns: function ()
        {
            var g = this, p = this.options;
            g._columns = {};             //全部列的信息  
            g._columnCount = 0;
            g._columnLeafCount = 0;
            g._columnMaxLevel = 1;
            if (!p.columns) return;
            function removeProp(column, props)
            {
                for (var i in props)
                {
                    if (props[i] in column)
                        delete column[props[i]];
                }
            }
            //设置id、pid、level、leaf，返回叶节点数,如果是叶节点，返回1
            function setColumn(column, level, pid, previd)
            {
                removeProp(column, ['__id', '__pid', '__previd', '__nextid', '__domid', '__leaf', '__leafindex', '__level', '__colSpan', '__rowSpan']);
                if (level > g._columnMaxLevel) g._columnMaxLevel = level;
                g._columnCount++;
                column['__id'] = g._createColumnId(column);
                column['__domid'] = g.id + "|hcell|" + column['__id'];
                g._columns[column['__id']] = column;
                if (!column.columns || !column.columns.length)
                    column['__leafindex'] = g._columnLeafCount++;
                column['__level'] = level;
                column['__pid'] = pid;
                column['__previd'] = previd;
                if (!column.columns || !column.columns.length)
                {
                    column['__leaf'] = true;
                    return 1;
                }
                var leafcount = 0;
                var newid = -1;
                for (var i = 0, l = column.columns.length; i < l; i++)
                {
                    var col = column.columns[i];
                    leafcount += setColumn(col, level + 1, column['__id'], newid);
                    newid = col['__id'];
                }
                column['__leafcount'] = leafcount;
                return leafcount;
            }
            var lastid = -1;
            //行序号
            if (p.rownumbers)
            {
                var frozenRownumbers = g.enabledGroup() ? false : p.frozen && p.frozenRownumbers;
                var col = { isrownumber: true, issystem: true, width: p.rownumbersColWidth, frozen: frozenRownumbers };
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            //明细列
            if (g.enabledDetail())
            {
                var frozenDetail = g.enabledGroup() ? false : p.frozen && p.frozenDetail;
                var col = { isdetail: true, issystem: true, width: p.detailColWidth, frozen: frozenDetail };
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            //复选框列
            if (g.enabledCheckbox())
            {
                var frozenCheckbox = g.enabledGroup() ? false : p.frozen && p.frozenCheckbox;
                var col = { ischeckbox: true, issystem: true, width: p.detailColWidth, frozen: frozenCheckbox };
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            for (var i = 0, l = p.columns.length; i < l; i++)
            {
                var col = p.columns[i];
                setColumn(col, 1, -1, lastid);
                lastid = col['__id'];
            }
            //设置colSpan和rowSpan
            for (var id in g._columns)
            {
                var col = g._columns[id];
                if (col['__leafcount'] > 1)
                {
                    col['__colSpan'] = col['__leafcount'];
                }
                if (col['__leaf'] && col['__level'] != g._columnMaxLevel)
                {
                    col['__rowSpan'] = g._columnMaxLevel - col['__level'] + 1;
                }
            }
            //叶级别列的信息  
            g.columns = g.getColumns();
            $(g.columns).each(function (i, column)
            {
                column.columnname = column.name;
                column.columnindex = i;
                column.type = column.type || "string";
                column.islast = i == g.columns.length - 1;
                column.isSort = column.isSort == false ? false : true;
                column.frozen = column.frozen ? true : false;
                column._width = g._getColumnWidth(column);
                column._hide = column.hide ? true : false;
            });
        },
        _getColumnWidth: function (column)
        {
            var g = this, p = this.options;
            if (column._width) return column._width;
            var colwidth;
            if (column.width)
            {
                colwidth = column.width;
            }
            else if (p.columnWidth)
            {
                colwidth = p.columnWidth;
            }
            if (!colwidth)
            {
                var lwidth = 4;
                if (g.enabledCheckbox()) lwidth += p.checkboxColWidth;
                if (g.enabledDetail()) lwidth += p.detailColWidth;
                colwidth = parseInt((g.grid.width() - lwidth) / g.columns.length);
            }
            if (typeof (colwidth) == "string" && colwidth.indexOf('%') > 0)
            {
                column._width = colwidth = parseInt(parseInt(colwidth) * 0.01 * (g.grid.width() - g.columns.length));
            }
            if (column.minWidth && colwidth < column.minWidth) colwidth = column.minWidth;
            if (column.maxWidth && colwidth > column.maxWidth) colwidth = column.maxWidth;
            column._width = colwidth;
            return colwidth;
        },
        _createHeaderCell: function (column)
        {
            var g = this, p = this.options;
            var jcell = $("<td class='l-grid-hd-cell'><div class='l-grid-hd-cell-inner'><span class='l-grid-hd-cell-text'></span></div></td>");
            jcell.attr("id", column['__domid']);
            if (!column['__leaf'])
                jcell.addClass("l-grid-hd-cell-mul");
            if (column.columnindex == g.columns.length - 1)
            {
                jcell.addClass("l-grid-hd-cell-last");
            }
            if (column.isrownumber)
            {
                jcell.addClass("l-grid-hd-cell-rownumbers");
                jcell.html("<div class='l-grid-hd-cell-inner'></div>");
            }
            if (column.ischeckbox)
            {
                jcell.addClass("l-grid-hd-cell-checkbox");
                jcell.html("<div class='l-grid-hd-cell-inner'><div class='l-grid-hd-cell-text l-grid-hd-cell-btn-checkbox'></div></div>");
            }
            if (column.isdetail)
            {
                jcell.addClass("l-grid-hd-cell-detail");
                jcell.html("<div class='l-grid-hd-cell-inner'><div class='l-grid-hd-cell-text l-grid-hd-cell-btn-detail'></div></div>");
            }
            if (column['__colSpan']) jcell.attr("colSpan", column['__colSpan']);
            if (column['__rowSpan'])
            {
                jcell.attr("rowSpan", column['__rowSpan']);
                jcell.height(23 * column['__rowSpan']);
            } else
            {
                jcell.height(23);
            }
            if (column['__leaf'])
            {
                jcell.width(column['_width']);
                jcell.attr("columnindex", column['__leafindex']);
            }
            if (column._hide) jcell.hide();
            if (column.name) jcell.attr({ columnname: column.name });
            var headerText = "";
            if (column.display && column.display != "")
                headerText = column.display;
            else if (column.headerRender)
                headerText = column.headerRender(column);
            else
                headerText = "&nbsp;";
            $(".l-grid-hd-cell-text:first", jcell).html(headerText);
            if (!column.issystem && column['__leaf'] && column.resizable !== false && $.fn.ligerResizable)
            {
                g.colResizable[column['__id']] = jcell.ligerResizable({ handles: 'e',
                    onStartResize: function (e, ev)
                    {
                        this.proxy.hide();
                        g.draggingline.css({ height: g.body.height(), top: 0, left: ev.pageX - g.grid.offset().left + parseInt(g.body[0].scrollLeft) }).show();
                    },
                    onResize: function (e, ev)
                    {
                        g.colresizing = true;
                        g.draggingline.css({ left: ev.pageX - g.grid.offset().left + parseInt(g.body[0].scrollLeft) });
                        $('body').add(jcell).css('cursor', 'e-resize');
                    },
                    onStopResize: function (e)
                    {
                        g.colresizing = false;
                        $('body').add(jcell).css('cursor', 'default');
                        g.draggingline.hide();
                        g.setColumnWidth(column, column._width + e.diffX);
                        return false;
                    }
                });
            }
            return jcell;
        },
        _initBuildGridHeader: function ()
        {
            var g = this, p = this.options;
            g.gridtablewidth = 0;
            g.f.gridtablewidth = 0;
            if (g.colResizable)
            {
                for (var i in g.colResizable)
                {
                    g.colResizable[i].destroy();
                }
                g.colResizable = null;
            }
            g.colResizable = {};
            $("tbody:first", g.gridheader).html("");
            $("tbody:first", g.f.gridheader).html("");
            for (var level = 1; level <= g._columnMaxLevel; level++)
            {
                var columns = g.getColumns(level);           //获取level层次的列集合
                var islast = level == g._columnMaxLevel;     //是否最末级
                var tr = $("<tr class='l-grid-hd-row'></tr>");
                var trf = $("<tr class='l-grid-hd-row'></tr>");
                if (!islast) tr.add(trf).addClass("l-grid-hd-mul");
                $("tbody:first", g.gridheader).append(tr);
                $("tbody:first", g.f.gridheader).append(trf);
                $(columns).each(function (i, column)
                {
                    (column.frozen ? trf : tr).append(g._createHeaderCell(column));
                    if (column['__leaf'])
                    {
                        var colwidth = column['_width'];
                        if (!column.frozen)
                            g.gridtablewidth += (parseInt(colwidth) ? parseInt(colwidth) : 0) + 1;
                        else
                            g.f.gridtablewidth += (parseInt(colwidth) ? parseInt(colwidth) : 0) + 1;
                    }
                });
            }
            if (g._columnMaxLevel > 0)
            {
                var h = 23 * g._columnMaxLevel;
                g.gridheader.add(g.f.gridheader).height(h);
                if (p.rownumbers && p.frozenRownumbers) g.f.gridheader.find("td:first").height(h);
            }
            g._updateFrozenWidth();
            $("div:first", g.gridheader).width(g.gridtablewidth + 40);
        },
        _initBuildPopup: function ()
        {
            var g = this, p = this.options;
            $(':checkbox', g.popup).unbind();
            $('tbody tr', g.popup).remove();
            $(g.columns).each(function (i, column)
            {
                if (column.issystem) return;
                if (column.isAllowHide == false) return;
                var chk = 'checked="checked"';
                if (column._hide) chk = '';
                var header = column.display;
                $('tbody', g.popup).append('<tr><td class="l-column-left"><input type="checkbox" ' + chk + ' class="l-checkbox" columnindex="' + i + '"/></td><td class="l-column-right">' + header + '</td></tr>');
            });
            if ($.fn.ligerCheckBox)
            {
                $('input:checkbox', g.popup).ligerCheckBox(
                {
                    onBeforeClick: function (obj)
                    {
                        if (!obj.checked) return true;
                        if ($('input:checked', g.popup).length <= p.minColToggle)
                            return false;
                        return true;
                    }
                });
            }
            //表头 - 显示/隐藏'列控制'按钮事件
            if (p.allowHideColumn)
            {
                $('tr', g.popup).hover(function ()
                {
                    $(this).addClass('l-popup-row-over');
                },
                function ()
                {
                    $(this).removeClass('l-popup-row-over');
                });
                var onPopupCheckboxChange = function ()
                {
                    if ($('input:checked', g.popup).length + 1 <= p.minColToggle)
                    {
                        return false;
                    }
                    g.toggleCol(parseInt($(this).attr("columnindex")), this.checked, true);
                };
                if ($.fn.ligerCheckBox)
                    $(':checkbox', g.popup).bind('change', onPopupCheckboxChange);
                else
                    $(':checkbox', g.popup).bind('click', onPopupCheckboxChange);
            }
        },
        _initHeight: function ()
        {
            var g = this, p = this.options;
            if (p.height == 'auto')
            {
                g.gridbody.height('auto');
                g.f.gridbody.height('auto');
            }
            if (p.width)
            {
                g.grid.width(p.width);
            }
            g._onResize.call(g);
        },
        _initFootbar: function ()
        {
            var g = this, p = this.options;
            if (p.usePager)
            {
                //创建底部工具条 - 选择每页显示记录数
                var optStr = "";
                var selectedIndex = -1;
                $(p.pageSizeOptions).each(function (i, item)
                {
                    var selectedStr = "";
                    if (p.pageSize == item) selectedIndex = i;
                    optStr += "<option value='" + item + "' " + selectedStr + " >" + item + "</option>";
                });

                $('.l-bar-selectpagesize', g.toolbar).append("<select name='rp'>" + optStr + "</select>");
                if (selectedIndex != -1) $('.l-bar-selectpagesize select', g.toolbar)[0].selectedIndex = selectedIndex;
                if (p.switchPageSizeApplyComboBox && $.fn.ligerComboBox)
                {
                    $(".l-bar-selectpagesize select", g.toolbar).ligerComboBox(
                    {
                        onBeforeSelect: function ()
                        {
                            if (p.url && g.isDataChanged && !confirm(p.isContinueByDataChanged))
                                return false;
                            return true;
                        },
                        width: 45
                    });
                }
            }
            else
            {
                g.toolbar.hide();
            }
        },
        _searchData: function (data, clause)
        {
            var g = this, p = this.options;
            var newData = new Array();
            for (var i = 0; i < data.length; i++)
            {
                if (clause(data[i], i))
                {
                    newData[newData.length] = data[i];
                }
            }
            return newData;
        },
        _clearGrid: function ()
        {
            var g = this, p = this.options;
            for (var i in g.rows)
            {
                var rowobj = $(g.getRowObj(g.rows[i]));
                if (g.enabledFrozen())
                    rowobj = rowobj.add(g.getRowObj(g.rows[i], true));
                rowobj.unbind();
            }
            //清空数据
            g.gridbody.html("");
            g.f.gridbody.html("");
            g.recordNumber = 0;
            g.records = {};
            g.rows = [];
            if (!g.selected)
                g.selected = [];
            g.totalNumber = 0;
        },
        _fillGridBody: function (data, frozen)
        {
            var g = this, p = this.options;
            //加载数据 
            var gridhtmlarr = ['<div class="l-grid-body-inner"><table class="l-grid-body-table" cellpadding=0 cellspacing=0><tbody>'];
            if (g.enabledGroup()) //启用分组模式
            {
                var groups = []; //分组列名数组
                var groupsdata = []; //切成几块后的数据
                g.groups = groupsdata;
                for (var rowparm in data)
                {
                    var item = data[rowparm];
                    var groupColumnValue = item[p.groupColumnName];
                    var valueIndex = $.inArray(groupColumnValue, groups);
                    if (valueIndex == -1)
                    {
                        groups.push(groupColumnValue);
                        valueIndex = groups.length - 1;
                        groupsdata.push([]);
                    }
                    groupsdata[valueIndex].push(item);
                }
                $(groupsdata).each(function (i, item)
                {
                    if (groupsdata.length == 1)
                        gridhtmlarr.push('<tr class="l-grid-grouprow l-grid-grouprow-last l-grid-grouprow-first"');
                    if (i == groupsdata.length - 1)
                        gridhtmlarr.push('<tr class="l-grid-grouprow l-grid-grouprow-last"');
                    else if (i == 0)
                        gridhtmlarr.push('<tr class="l-grid-grouprow l-grid-grouprow-first"');
                    else
                        gridhtmlarr.push('<tr class="l-grid-grouprow"');
                    gridhtmlarr.push(' groupindex"=' + i + '" >');
                    gridhtmlarr.push('<td colSpan="' + g.columns.length + '" class="l-grid-grouprow-cell">');
                    gridhtmlarr.push('<span class="l-grid-group-togglebtn">&nbsp;&nbsp;&nbsp;&nbsp;</span>');
                    if (p.groupRender)
                        gridhtmlarr.push(p.groupRender(groups[i], p.groupColumnDisplay));
                    else
                        gridhtmlarr.push(p.groupColumnDisplay + ':' + groups[i]);


                    gridhtmlarr.push('</td>');
                    gridhtmlarr.push('</tr>');

                    gridhtmlarr.push(g._getHtmlFromData(item, frozen));
                    //汇总
                    if (g.isTotalSummary())
                        gridhtmlarr.push(g._getTotalSummaryHtml(item, "l-grid-totalsummary-group", frozen));
                });
            }
            else
            {
                gridhtmlarr.push(g._getHtmlFromData(data, frozen));
            }
            gridhtmlarr.push('</tbody></table></div>');
            (frozen ? g.f.gridbody : g.gridbody).html(gridhtmlarr.join(''));
            //分组时不需要            
            if (!g.enabledGroup())
            {
                //创建汇总行
                g._bulidTotalSummary(frozen);
            }
            $("> div:first", g.gridbody).width(g.gridtablewidth);
            g._onResize();
        },
        _showData: function ()
        {
            var g = this, p = this.options;
            var data = g.currentData[p.root];
            if (p.usePager)
            {
                //更新总记录数
                if (p.dataAction == "server" && g.data && g.data[p.record])
                    p.total = g.data[p.record];
                else if (g.filteredData && g.filteredData[p.root])
                    p.total = g.filteredData[p.root].length;
                else if (g.data && g.data[p.root])
                    p.total = g.data[p.root].length;
                else if (data)
                    p.total = data.length;

                p.page = p.newPage;
                if (!p.total) p.total = 0;
                if (!p.page) p.page = 1;
                p.pageCount = Math.ceil(p.total / p.pageSize);
                if (!p.pageCount) p.pageCount = 1;
                //更新分页
                g._buildPager();
            }
            //加载中
            $('.l-bar-btnloading:first', g.toolbar).removeClass('l-bar-btnloading');
            if (g.trigger('beforeShowData', [g.currentData]) == false) return;
            g._clearGrid();
            g.isDataChanged = false;
            if (!data) return;
            $(".l-bar-btnload:first span", g.toolbar).removeClass("l-disabled");
            g._updateGridData();
            if (g.enabledFrozen())
                g._fillGridBody(g.rows, true);
            g._fillGridBody(g.rows, false);
            g.trigger('SysGridHeightChanged');
            if (p.totalRender)
            {
                $(".l-panel-bar-total", g.element).remove();
                $(".l-panel-bar", g.element).before('<div class="l-panel-bar-total">' + p.totalRender(g.data, g.filteredData) + '</div>');
            }
            if (p.mouseoverRowCssClass)
            {
                for (var i in g.rows)
                {
                    var rowobj = $(g.getRowObj(g.rows[i]));
                    if (g.enabledFrozen())
                        rowobj = rowobj.add(g.getRowObj(g.rows[i], true));
                    rowobj.bind('mouseover.gridrow', function ()
                    {
                        g._onRowOver(this, true);
                    }).bind('mouseout.gridrow', function ()
                    {
                        g._onRowOver(this, false);
                    });
                }
            }
            g.gridbody.trigger('scroll.grid');
            g.trigger('afterShowData', [g.currentData]);
        },
        _getRowDomId: function (rowdata, frozen)
        {
            return this.id + "|" + (frozen ? "1" : "2") + "|" + rowdata['__id'];
        },
        _getCellDomId: function (rowdata, column)
        {
            return this._getRowDomId(rowdata, column.frozen) + "|" + column['__id'];
        },
        _getHtmlFromData: function (data, frozen)
        {
            if (!data) return "";
            var g = this, p = this.options;
            var gridhtmlarr = [];
            for (var rowparm in data)
            {
                var item = data[rowparm];
                var rowid = item['__id'];
                if (!item) continue;
                gridhtmlarr.push('<tr');
                gridhtmlarr.push(' id="' + g._getRowDomId(item, frozen) + '"');
                gridhtmlarr.push(' class="l-grid-row'); //class start 
                if (g.enabledCheckbox() && p.isChecked && p.isChecked(item))
                {
                    g.select(item);
                    gridhtmlarr.push(' l-selected');
                }
                if (g.isSelected(item))
                {
                    gridhtmlarr.push(' l-selected');
                }
                if (rowid % 2 == 1 && p.alternatingRow)
                    gridhtmlarr.push(' l-grid-row-alt');
                gridhtmlarr.push('" ');  //class end
                if (p.rowAttrRender) gridhtmlarr.push(p.rowAttrRender(item, rowid));
                if (p.tree && g.collapsedRows && g.collapsedRows.length)
                {
                    var isHide = function ()
                    {
                        var pitem = g.getParent(item);
                        while (pitem)
                        {
                            if ($.inArray(pitem, g.collapsedRows) != -1) return true;
                            pitem = g.getParent(pitem);
                        }
                        return false;
                    };
                    if (isHide()) gridhtmlarr.push(' style="display:none;" ');
                }
                gridhtmlarr.push('>');
                $(g.columns).each(function (columnindex, column)
                {
                    if (frozen != column.frozen) return;
                    gridhtmlarr.push('<td');
                    gridhtmlarr.push(' id="' + g._getCellDomId(item, this) + '"');
                    //如果是行序号(系统列)
                    if (this.isrownumber)
                    {
                        gridhtmlarr.push(' class="l-grid-row-cell l-grid-row-cell-rownumbers" style="width:' + this.width + 'px"><div class="l-grid-row-cell-inner">' + (parseInt(item['__index']) + 1) + '</div></td>');
                        return;
                    }
                    //如果是复选框(系统列)
                    if (this.ischeckbox)
                    {
                        gridhtmlarr.push(' class="l-grid-row-cell l-grid-row-cell-checkbox" style="width:' + this.width + 'px"><div class="l-grid-row-cell-inner"><span class="l-grid-row-cell-btn-checkbox"></span></div></td>');
                        return;
                    }
                    //如果是明细列(系统列)
                    else if (this.isdetail)
                    {
                        gridhtmlarr.push(' class="l-grid-row-cell l-grid-row-cell-detail" style="width:' + this.width + 'px"><div class="l-grid-row-cell-inner"><span class="l-grid-row-cell-detailbtn"></span></div></td>');
                        return;
                    }
                    var colwidth = this._width;
                    gridhtmlarr.push(' class="l-grid-row-cell ');
                    if (g.changedCells[rowid + "_" + this['__id']]) gridhtmlarr.push("l-grid-row-cell-edited ");
                    if (this.islast)
                        gridhtmlarr.push('l-grid-row-cell-last ');
                    gridhtmlarr.push('"');
                    //if (this.columnname) gridhtmlarr.push('columnname="' + this.columnname + '"');
                    gridhtmlarr.push(' style = "');
                    gridhtmlarr.push('width:' + colwidth + 'px; ');
                    if (column._hide)
                    {
                        gridhtmlarr.push('display:none;');
                    }
                    gridhtmlarr.push(' ">');
                    gridhtmlarr.push(g._getCellHtml(item, column));
                    gridhtmlarr.push('</td>');
                });
                gridhtmlarr.push('</tr>');
            }
            return gridhtmlarr.join('');
        },
        _getCellHtml: function (rowdata, column)
        {
            var g = this, p = this.options;
            if (column.isrownumber)
                return '<div class="l-grid-row-cell-inner">' + (parseInt(rowdata['__index']) + 1) + '</div>';
            var htmlarr = [];
            htmlarr.push('<div class="l-grid-row-cell-inner"');
            //htmlarr.push('<div');
            htmlarr.push(' style = "width:' + parseInt(column._width - 8) + 'px; ');
            if (p.fixedCellHeight) htmlarr.push('height:' + 22 + 'px;');
            if (column.align) htmlarr.push('text-align:' + column.align + ';');
            var content = g._getCellContent(rowdata, column);
            htmlarr.push('">' + content + '</div>');
            return htmlarr.join('');
        },
        _getCellContent: function (rowdata, column)
        {
            if (!rowdata || !column) return "";
            if (column.isrownumber) return parseInt(rowdata['__index']) + 1;
            var rowid = rowdata['__id'];
            var rowindex = rowdata['__index'];
            var value = column.name ? rowdata[column.name] : null;
            var g = this, p = this.options;
            var content = "";
            if (column.render)
            {
                content = column.render.call(g, rowdata, rowindex, value, column);
            }
            else if ($.ligerDefaults.Grid.formatters[column.type])
            {
                content = $.ligerDefaults.Grid.formatters[column.type].call(g, value, column);
            }
            else if (value != null)
            {
                content = value.toString();
            }
            if (p.tree && (p.tree.columnName != null && p.tree.columnName == column.name || p.tree.columnId != null && p.tree.columnId == column.id))
            {
                content = g._getTreeCellHtml(content, rowdata);
            }
            return content || "";
        },
        _getTreeCellHtml: function (oldContent, rowdata)
        {
            var level = rowdata['__level'];
            var g = this, p = this.options;
            //var isExtend = p.tree.isExtend(rowdata);
            var isExtend = $.inArray(rowdata, g.collapsedRows || []) == -1;
            var isParent = p.tree.isParent(rowdata);
            var content = "";
            level = parseInt(level) || 1;
            for (var i = 1; i < level; i++)
            {
                content += "<div class='l-grid-tree-space'></div>";
            }
            if (isExtend && isParent)
                content += "<div class='l-grid-tree-space l-grid-tree-link l-grid-tree-link-open'></div>";
            else if (isParent)
                content += "<div class='l-grid-tree-space l-grid-tree-link l-grid-tree-link-close'></div>";
            else
                content += "<div class='l-grid-tree-space'></div>";
            content += "<span class='l-grid-tree-content'>" + oldContent + "</span>";
            return content;
        },
        _applyEditor: function (obj)
        {
            var g = this, p = this.options;
            var rowcell = obj;
            var ids = rowcell.id.split('|');
            var columnid = ids[ids.length - 1];
            var column = g._columns[columnid];
            var row = $(rowcell).parent();
            var rowdata = g.getRow(row[0]);
            var rowid = rowdata['__id'];
            var rowindex = rowdata['__index'];
            if (!column || !column.editor) return;
            var columnname = column.name;
            var columnindex = column.columnindex;
            if (column.editor.type && $.ligerDefaults.Grid.editors[column.editor.type])
            {
                var currentdata = rowdata[columnname];
                var editParm = { record: rowdata, value: currentdata, column: column, rowindex: rowindex };
                if (g.trigger('beforeEdit', [editParm]) == false) return false;
                var editor = $.ligerDefaults.Grid.editors[column.editor.type];
                var jcell = $(rowcell), offset = $(rowcell).offset();
                jcell.html("");
                g.setCellEditing(rowdata, column, true);
                var width = $(rowcell).width(), height = $(rowcell).height();
                var container = $("<div class='l-grid-editor'></div>").appendTo('body');
                if ($.browser.mozilla)
                    container.css({ left: offset.left, top: offset.top }).show();
                else
                    container.css({ left: offset.left + 1, top: offset.top + 1 }).show();
                var editorInput = g._createEditor(editor, container, editParm, width, height);
                g.editor = { editing: true, editor: editor, input: editorInput, editParm: editParm, container: container };
                g.unbind('sysEndEdit');
                g.bind('sysEndEdit', function ()
                {
                    var newValue = editor.getValue(editorInput, editParm);
                    if (newValue != currentdata)
                    {
                        $(rowcell).addClass("l-grid-row-cell-edited");
                        g.changedCells[rowid + "_" + column['__id']] = true;
                        if (column.editor.onChange) column.editor.onChange(rowcell, newValue);
                        editParm.value = newValue;
                        if (g._checkEditAndUpdateCell(editParm))
                        {
                            if (column.editor.onChanged) column.editor.onChanged(rowcell, newValue);
                        }
                    }
                });
            }
        },
        _checkEditAndUpdateCell: function (editParm)
        {
            var g = this, p = this.options;
            if (g.trigger('beforeSubmitEdit', [editParm]) == false) return false;
            if (g.editor.editor && g.editor.editor.destroy)
                g.editor.editor.destroy(g.editor.input, g.editor.editParm);
            g.updateCell(editParm.column, editParm.value, editParm.record);
            if (editParm.column.render || g.enabledTotal()) g.reRender({ column: editParm.column });
            g.reRender({ rowdata: editParm.record });
            return true;
        },
        _getCurrentPageData: function (source)
        {
            var g = this, p = this.options;
            var data = {};
            data[p.root] = [];
            if (!source || !source[p.root] || !source[p.root].length)
            {
                data[p.record] = 0;
                return data;
            }
            data[p.record] = source[p.root].length;
            if (!p.newPage) p.newPage = 1;
            for (i = (p.newPage - 1) * p.pageSize; i < source[p.root].length && i < p.newPage * p.pageSize; i++)
            {
                data[p.root].push(source[p.root][i]);
            }
            return data;
        },
        //比较某一列两个数据
        _compareData: function (data1, data2, columnName, columnType)
        {
            var g = this, p = this.options;
            var val1 = data1[columnName], val2 = data2[columnName];
            if (val1 == null && val2 != null) return 1;
            else if (val1 == null && val2 == null) return 0;
            else if (val1 != null && val2 == null) return -1;
            if ($.ligerDefaults.Grid.sorters[columnType])
                return $.ligerDefaults.Grid.sorters[columnType].call(g, val1, val2);
            else
                return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
        },
        _getTotalCellContent: function (column, data)
        {
            var g = this, p = this.options;
            var totalsummaryArr = [];
            if (column.totalSummary)
            {
                var isExist = function (type)
                {
                    for (var i = 0; i < types.length; i++)
                        if (types[i].toLowerCase() == type.toLowerCase()) return true;
                    return false;
                };
                var sum = 0, count = 0, avg = 0;
                var max = parseFloat(data[0][column.name]);
                var min = parseFloat(data[0][column.name]);
                for (var i = 0; i < data.length; i++)
                {
                    count += 1;
                    var value = parseFloat(data[i][column.name]);
                    if (!value) continue;
                    sum += value;
                    if (value > max) max = value;
                    if (value < min) min = value;
                }
                avg = sum * 1.0 / data.length;
                if (column.totalSummary.render)
                {
                    var renderhtml = column.totalSummary.render({
                        sum: sum,
                        count: count,
                        avg: avg,
                        min: min,
                        max: max
                    }, column, g.data);
                    totalsummaryArr.push(renderhtml);
                }
                else if (column.totalSummary.type)
                {
                    var types = column.totalSummary.type.split(',');
                    if (isExist('sum'))
                        totalsummaryArr.push("<div>Sum=" + sum.toFixed(2) + "</div>");
                    if (isExist('count'))
                        totalsummaryArr.push("<div>Count=" + count + "</div>");
                    if (isExist('max'))
                        totalsummaryArr.push("<div>Max=" + max.toFixed(2) + "</div>");
                    if (isExist('min'))
                        totalsummaryArr.push("<div>Min=" + min.toFixed(2) + "</div>");
                    if (isExist('avg'))
                        totalsummaryArr.push("<div>Avg=" + avg.toFixed(2) + "</div>");
                }
            }
            return totalsummaryArr.join('');
        },
        _getTotalSummaryHtml: function (data, classCssName, frozen)
        {
            var g = this, p = this.options;
            var totalsummaryArr = [];
            if (classCssName)
                totalsummaryArr.push('<tr class="l-grid-totalsummary ' + classCssName + '">');
            else
                totalsummaryArr.push('<tr class="l-grid-totalsummary">');
            $(g.columns).each(function (columnindex, column)
            {
                if (this.frozen != frozen) return;
                //如果是行序号(系统列)
                if (this.isrownumber)
                {
                    totalsummaryArr.push('<td class="l-grid-totalsummary-cell l-grid-totalsummary-cell-rownumbers" style="width:' + this.width + 'px"><div>&nbsp;</div></td>');
                    return;
                }
                //如果是复选框(系统列)
                if (this.ischeckbox)
                {
                    totalsummaryArr.push('<td class="l-grid-totalsummary-cell l-grid-totalsummary-cell-checkbox" style="width:' + this.width + 'px"><div>&nbsp;</div></td>');
                    return;
                }
                //如果是明细列(系统列)
                else if (this.isdetail)
                {
                    totalsummaryArr.push('<td class="l-grid-totalsummary-cell l-grid-totalsummary-cell-detail" style="width:' + this.width + 'px"><div>&nbsp;</div></td>');
                    return;
                }
                totalsummaryArr.push('<td class="l-grid-totalsummary-cell');
                if (this.islast)
                    totalsummaryArr.push(" l-grid-totalsummary-cell-last");
                totalsummaryArr.push('" ');
                totalsummaryArr.push('id="' + g.id + "|total" + g.totalNumber + "|" + column.__id + '" ');
                totalsummaryArr.push('width="' + this._width + '" ');
                columnname = this.columnname;
                if (columnname)
                {
                    totalsummaryArr.push('columnname="' + columnname + '" ');
                }
                totalsummaryArr.push('columnindex="' + columnindex + '" ');
                totalsummaryArr.push('><div class="l-grid-totalsummary-cell-inner"');
                if (column.align)
                    totalsummaryArr.push(' style="text-Align:' + column.align + ';"');
                totalsummaryArr.push('>');
                totalsummaryArr.push(g._getTotalCellContent(column, data));
                totalsummaryArr.push('</div></td>');
            });
            totalsummaryArr.push('</tr>');
            if (!frozen) g.totalNumber++;
            return totalsummaryArr.join('');
        },
        _bulidTotalSummary: function (frozen)
        {
            var g = this, p = this.options;
            if (!g.isTotalSummary()) return false;
            if (!g.currentData || g.currentData[p.root].length == 0) return false;
            var totalRow = $(g._getTotalSummaryHtml(g.currentData[p.root], null, frozen));
            $("tbody:first", frozen ? g.f.gridbody : g.gridbody).append(totalRow);
        },
        _buildPager: function ()
        {
            var g = this, p = this.options;
            $('.pcontrol input', g.toolbar).val(p.page);
            if (!p.pageCount) p.pageCount = 1;
            $('.pcontrol span', g.toolbar).html(p.pageCount);
            var r1 = parseInt((p.page - 1) * p.pageSize) + 1.0;
            var r2 = parseInt(r1) + parseInt(p.pageSize) - 1;
            if (!p.total) p.total = 0;
            if (p.total < r2) r2 = p.total;
            if (!p.total) r1 = r2 = 0;
            if (r1 < 0) r1 = 0;
            if (r2 < 0) r2 = 0;
            var stat = p.pageStatMessage;
            stat = stat.replace(/{from}/, r1);
            stat = stat.replace(/{to}/, r2);
            stat = stat.replace(/{total}/, p.total);
            stat = stat.replace(/{pagesize}/, p.pageSize);
            $('.l-bar-text', g.toolbar).html(stat);
            if (!p.total)
            {
                $(".l-bar-btnfirst span,.l-bar-btnprev span,.l-bar-btnnext span,.l-bar-btnlast span", g.toolbar)
                    .addClass("l-disabled");
            }
            if (p.page == 1)
            {
                $(".l-bar-btnfirst span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).addClass("l-disabled");
            }
            else if (p.page > p.pageCount && p.pageCount > 0)
            {
                $(".l-bar-btnfirst span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnprev span", g.toolbar).removeClass("l-disabled");
            }
            if (p.page == p.pageCount)
            {
                $(".l-bar-btnlast span", g.toolbar).addClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).addClass("l-disabled");
            }
            else if (p.page < p.pageCount && p.pageCount > 0)
            {
                $(".l-bar-btnlast span", g.toolbar).removeClass("l-disabled");
                $(".l-bar-btnnext span", g.toolbar).removeClass("l-disabled");
            }
        },
        _getRowIdByDomId: function (domid)
        {
            var ids = domid.split('|');
            var rowid = ids[2];
            return rowid;
        },
        _getRowByDomId: function (domid)
        {
            return this.records[this._getRowIdByDomId(domid)];
        },
        _getSrcElementByEvent: function (e)
        {
            var g = this;
            var obj = (e.target || e.srcElement);
            var jobjs = $(obj).parents().add(obj);
            var fn = function (parm)
            {
                for (var i = 0, l = jobjs.length; i < l; i++)
                {
                    if (typeof parm == "string")
                    {
                        if ($(jobjs[i]).hasClass(parm)) return jobjs[i];
                    }
                    else if (typeof parm == "object")
                    {
                        if (jobjs[i] == parm) return jobjs[i];
                    }
                }
                return null;
            };
            if (fn("l-grid-editor")) return { editing: true, editor: fn("l-grid-editor") };
            if (jobjs.index(this.element) == -1) return { out: true };
            var indetail = false;
            if (jobjs.hasClass("l-grid-detailpanel") && g.detailrows)
            {
                for (var i = 0, l = g.detailrows.length; i < l; i++)
                {
                    if (jobjs.index(g.detailrows[i]) != -1)
                    {
                        indetail = true;
                        break;
                    }
                }
            }
            var r = {
                grid: fn("l-panel"),
                indetail: indetail,
                frozen: fn(g.gridview1[0]) ? true : false,
                header: fn("l-panel-header"), //标题
                gridheader: fn("l-grid-header"), //表格头 
                gridbody: fn("l-grid-body"),
                total: fn("l-panel-bar-total"), //总汇总 
                popup: fn("l-grid-popup"),
                toolbar: fn("l-panel-bar")
            };
            if (r.gridheader)
            {
                r.hrow = fn("l-grid-hd-row");
                r.hcell = fn("l-grid-hd-cell");
                r.hcelltext = fn("l-grid-hd-cell-text");
                r.checkboxall = fn("l-grid-hd-cell-checkbox");
                if (r.hcell)
                {
                    var columnid = r.hcell.id.split('|')[2];
                    r.column = g._columns[columnid];
                }
            }
            if (r.gridbody)
            {
                r.row = fn("l-grid-row");
                r.cell = fn("l-grid-row-cell");
                r.checkbox = fn("l-grid-row-cell-btn-checkbox");
                r.groupbtn = fn("l-grid-group-togglebtn");
                r.grouprow = fn("l-grid-grouprow");
                r.detailbtn = fn("l-grid-row-cell-detailbtn");
                r.detailrow = fn("l-grid-detailpanel");
                r.totalrow = fn("l-grid-totalsummary");
                r.totalcell = fn("l-grid-totalsummary-cell");
                r.rownumberscell = $(r.cell).hasClass("l-grid-row-cell-rownumbers") ? r.cell : null;
                r.detailcell = $(r.cell).hasClass("l-grid-row-cell-detail") ? r.cell : null;
                r.checkboxcell = $(r.cell).hasClass("l-grid-row-cell-checkbox") ? r.cell : null;
                r.treelink = fn("l-grid-tree-link");
                r.editor = fn("l-grid-editor");
                if (r.row) r.data = this._getRowByDomId(r.row.id);
                if (r.cell) r.editing = $(r.cell).hasClass("l-grid-row-cell-editing");
                if (r.editor) r.editing = true;
                if (r.editing) r.out = false;
            }
            if (r.toolbar)
            {
                r.first = fn("l-bar-btnfirst");
                r.last = fn("l-bar-btnlast");
                r.next = fn("l-bar-btnnext");
                r.prev = fn("l-bar-btnprev");
                r.load = fn("l-bar-btnload");
                r.button = fn("l-bar-button");
            }

            return r;
        },
        _setEvent: function ()
        {
            var g = this, p = this.options;
            g.grid.bind("mousedown.grid", function (e)
            {
                g._onMouseDown.call(g, e);
            });
            g.grid.bind("dblclick.grid", function (e)
            {
                g._onDblClick.call(g, e);
            });
            g.grid.bind("contextmenu.grid", function (e)
            {
                return g._onContextmenu.call(g, e);
            });
            $(document).bind("mouseup.grid", function (e)
            {
                g._onMouseUp.call(g, e);
            });
            $(document).bind("click.grid", function (e)
            {
                g._onClick.call(g, e);
            });
            $(window).bind("resize.grid", function (e)
            {
                g._onResize.call(g);
            });
            $(document).bind("keydown.grid", function (e)
            {
                if (e.ctrlKey) g.ctrlKey = true;
            });
            $(document).bind("keyup.grid", function (e)
            {
                delete g.ctrlKey;
            });
            //表体 - 滚动联动事件 
            g.gridbody.bind('scroll.grid', function ()
            {
                var scrollLeft = g.gridbody.scrollLeft();
                var scrollTop = g.gridbody.scrollTop();
                if (scrollLeft != null)
                    g.gridheader[0].scrollLeft = scrollLeft;
                if (scrollTop != null)
                    g.f.gridbody[0].scrollTop = scrollTop;
                g.trigger('SysGridHeightChanged');
            });
            //工具条 - 切换每页记录数事件
            $('select', g.toolbar).change(function ()
            {
                if (g.isDataChanged && !confirm(p.isContinueByDataChanged))
                    return false;
                p.newPage = 1;
                p.pageSize = this.value;
                g.loadData(p.where);
            });
            //工具条 - 切换当前页事件
            $('span.pcontrol :text', g.toolbar).blur(function (e)
            {
                g.changePage('input');
            });
            $("div.l-bar-button", g.toolbar).hover(function ()
            {
                $(this).addClass("l-bar-button-over");
            }, function ()
            {
                $(this).removeClass("l-bar-button-over");
            });
            //列拖拽支持
            if ($.fn.ligerDrag && p.colDraggable)
            {
                g.colDroptip = $("<div class='l-drag-coldroptip' style='display:none'><div class='l-drop-move-up'></div><div class='l-drop-move-down'></div></div>").appendTo('body');
                g.gridheader.add(g.f.gridheader).ligerDrag({ revert: true, animate: false,
                    proxyX: 0, proxyY: 0,
                    proxy: function (draggable, e)
                    {
                        var src = g._getSrcElementByEvent(e);
                        if (src.hcell && src.column)
                        {
                            var content = $(".l-grid-hd-cell-text:first", src.hcell).html();
                            var proxy = $("<div class='l-drag-proxy' style='display:none'><div class='l-drop-icon l-drop-no'></div></div>").appendTo('body');
                            proxy.append(content);
                            return proxy;
                        }
                    },
                    onRevert: function () { return false; },
                    onRendered: function ()
                    {
                        this.set('cursor', 'default');
                        g.children[this.id] = this;
                    },
                    onStartDrag: function (current, e)
                    {
                        if (e.button == 2) return false;
                        if (g.colresizing) return false;
                        this.set('cursor', 'default');
                        var src = g._getSrcElementByEvent(e);
                        if (!src.hcell || !src.column || src.column.issystem || src.hcelltext) return false;
                        if ($(src.hcell).css('cursor').indexOf('resize') != -1) return false;
                        this.draggingColumn = src.column;
                        g.coldragging = true;

                        var gridOffset = g.grid.offset();
                        this.validRange = {
                            top: gridOffset.top,
                            bottom: gridOffset.top + g.gridheader.height(),
                            left: gridOffset.left - 10,
                            right: gridOffset.left + g.grid.width() + 10
                        };
                    },
                    onDrag: function (current, e)
                    {
                        this.set('cursor', 'default');
                        var column = this.draggingColumn;
                        if (!column) return false;
                        if (g.colresizing) return false;
                        if (g.colDropIn == null)
                            g.colDropIn = -1;
                        var pageX = e.pageX;
                        var pageY = e.pageY;
                        var visit = false;
                        var gridOffset = g.grid.offset();
                        var validRange = this.validRange;
                        if (pageX < validRange.left || pageX > validRange.right
                            || pageY > validRange.bottom || pageY < validRange.top)
                        {
                            g.colDropIn = -1;
                            g.colDroptip.hide();
                            this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes").addClass("l-drop-no");
                            return;
                        }
                        for (var colid in g._columns)
                        {
                            var col = g._columns[colid];
                            if (column == col)
                            {
                                visit = true;
                                continue;
                            }
                            if (col.issystem) continue;
                            var sameLevel = col['__level'] == column['__level'];
                            var isAfter = !sameLevel ? false : visit ? true : false;
                            if (column.frozen != col.frozen) isAfter = col.frozen ? false : true;
                            if (g.colDropIn != -1 && g.colDropIn != colid) continue;
                            var cell = document.getElementById(col['__domid']);
                            var offset = $(cell).offset();
                            var range = {
                                top: offset.top,
                                bottom: offset.top + $(cell).height(),
                                left: offset.left - 10,
                                right: offset.left + 10
                            };
                            if (isAfter)
                            {
                                var cellwidth = $(cell).width();
                                range.left += cellwidth;
                                range.right += cellwidth;
                            }
                            if (pageX > range.left && pageX < range.right && pageY > range.top && pageY < range.bottom)
                            {
                                var height = 23;
                                if (col['__rowSpan']) height *= col['__rowSpan'];
                                g.colDroptip.css({
                                    left: range.left + 5,
                                    top: range.top - 9,
                                    height: height + 9 * 2
                                }).show();
                                g.colDropIn = colid;
                                g.colDropDir = isAfter ? "right" : "left";
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no").addClass("l-drop-yes");
                                break;
                            }
                            else if (g.colDropIn != -1)
                            {
                                g.colDropIn = -1;
                                g.colDroptip.hide();
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes").addClass("l-drop-no");
                            }
                        }
                    },
                    onStopDrag: function (current, e)
                    {
                        var column = this.draggingColumn;
                        g.coldragging = false;
                        if (g.colDropIn != -1)
                        {
                            g.changeCol.ligerDefer(g, 0, [column, g.colDropIn, g.colDropDir == "right"]);
                            g.colDropIn = -1;
                        }
                        g.colDroptip.hide();
                        this.set('cursor', 'default');
                    }
                });
            }
            //行拖拽支持
            if ($.fn.ligerDrag && p.rowDraggable)
            {
                g.rowDroptip = $("<div class='l-drag-rowdroptip' style='display:none'></div>").appendTo('body');
                g.gridbody.add(g.f.gridbody).ligerDrag({ revert: true, animate: false,
                    proxyX: 0, proxyY: 0,
                    proxy: function (draggable, e)
                    {
                        var src = g._getSrcElementByEvent(e);
                        if (src.row)
                        {
                            var content = p.draggingMessage.replace(/{count}/, draggable.draggingRows ? draggable.draggingRows.length : 1);
                            if (p.rowDraggingRender)
                            {
                                content = p.rowDraggingRender(draggable.draggingRows, draggable, g);
                            }
                            var proxy = $("<div class='l-drag-proxy' style='display:none'><div class='l-drop-icon l-drop-no'></div>" + content + "</div>").appendTo('body');
                            return proxy;
                        }
                    },
                    onRevert: function () { return false; },
                    onRendered: function ()
                    {
                        this.set('cursor', 'default');
                        g.children[this.id] = this;
                    },
                    onStartDrag: function (current, e)
                    {
                        if (e.button == 2) return false;
                        if (g.colresizing) return false;
                        if (!g.columns.length) return false;
                        this.set('cursor', 'default');
                        var src = g._getSrcElementByEvent(e);
                        if (!src.cell || !src.data || src.checkbox) return false;
                        var ids = src.cell.id.split('|');
                        var column = g._columns[ids[ids.length - 1]];
                        if (src.rownumberscell || src.detailcell || src.checkboxcell || column == g.columns[0])
                        {
                            if (g.enabledCheckbox())
                            {
                                this.draggingRows = g.getSelecteds();
                                if (!this.draggingRows || !this.draggingRows.length) return false;
                            }
                            else
                            {
                                this.draggingRows = [src.data];
                            }
                            this.draggingRow = src.data;
                            this.set('cursor', 'move');
                            g.rowdragging = true;
                            this.validRange = {
                                top: g.gridbody.offset().top,
                                bottom: g.gridbody.offset().top + g.gridbody.height(),
                                left: g.grid.offset().left - 10,
                                right: g.grid.offset().left + g.grid.width() + 10
                            };
                        }
                        else
                        {
                            return false;
                        }
                    },
                    onDrag: function (current, e)
                    {
                        var rowdata = this.draggingRow;
                        if (!rowdata) return false;
                        var rows = this.draggingRows ? this.draggingRows : [rowdata];
                        if (g.colresizing) return false;
                        if (g.rowDropIn == null) g.rowDropIn = -1;
                        var pageX = e.pageX;
                        var pageY = e.pageY;
                        var visit = false;
                        var validRange = this.validRange;
                        if (pageX < validRange.left || pageX > validRange.right
                            || pageY > validRange.bottom || pageY < validRange.top)
                        {
                            g.rowDropIn = -1;
                            g.rowDroptip.hide();
                            this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes l-drop-add").addClass("l-drop-no");
                            return;
                        }
                        for (var i in g.rows)
                        {
                            var rd = g.rows[i];
                            var rowid = rd['__id'];
                            if (rowdata == rd) visit = true;
                            if ($.inArray(rd, rows) != -1) continue;
                            var isAfter = visit ? true : false;
                            if (g.rowDropIn != -1 && g.rowDropIn != rowid) continue;
                            var rowobj = g.getRowObj(rowid);
                            var offset = $(rowobj).offset();
                            var range = {
                                top: offset.top - 4,
                                bottom: offset.top + $(rowobj).height() + 4,
                                left: g.grid.offset().left,
                                right: g.grid.offset().left + g.grid.width()
                            };
                            if (pageX > range.left && pageX < range.right && pageY > range.top && pageY < range.bottom)
                            {
                                var lineTop = offset.top;
                                if (isAfter) lineTop += $(rowobj).height();
                                g.rowDroptip.css({
                                    left: range.left,
                                    top: lineTop,
                                    width: range.right - range.left
                                }).show();
                                g.rowDropIn = rowid;
                                g.rowDropDir = isAfter ? "bottom" : "top";
                                if (p.tree && pageY > range.top + 5 && pageY < range.bottom - 5)
                                {
                                    this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no l-drop-yes").addClass("l-drop-add");
                                    g.rowDroptip.hide();
                                    g.rowDropInParent = true;
                                }
                                else
                                {
                                    this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no l-drop-add").addClass("l-drop-yes");
                                    g.rowDroptip.show();
                                    g.rowDropInParent = false;
                                }
                                break;
                            }
                            else if (g.rowDropIn != -1)
                            {
                                g.rowDropIn = -1;
                                g.rowDropInParent = false;
                                g.rowDroptip.hide();
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes  l-drop-add").addClass("l-drop-no");
                            }
                        }
                    },
                    onStopDrag: function (current, e)
                    {
                        var rows = this.draggingRows;
                        g.rowdragging = false;
                        for (var i = 0; i < rows.length; i++)
                        {
                            var children = rows[i].children;
                            if (children)
                            {
                                rows = $.grep(rows, function (node, i)
                                {
                                    var isIn = $.inArray(node, children) == -1;
                                    return isIn;
                                });
                            }
                        }
                        if (g.rowDropIn != -1)
                        {
                            if (p.tree)
                            {
                                var neardata, prow;
                                if (g.rowDropInParent)
                                {
                                    prow = g.getRow(g.rowDropIn);
                                }
                                else
                                {
                                    neardata = g.getRow(g.rowDropIn);
                                    prow = g.getParent(neardata);
                                }
                                g.appendRange(rows, prow, neardata, g.rowDropDir != "bottom");
                                g.trigger('rowDragDrop', {
                                    rows: rows,
                                    parent: prow,
                                    near: neardata,
                                    after: g.rowDropDir == "bottom"
                                });
                            }
                            else
                            {
                                g.moveRange(rows, g.rowDropIn, g.rowDropDir == "bottom");
                                g.trigger('rowDragDrop', {
                                    rows: rows,
                                    parent: prow,
                                    near: g.getRow(g.rowDropIn),
                                    after: g.rowDropDir == "bottom"
                                });
                            }

                            g.rowDropIn = -1;
                        }
                        g.rowDroptip.hide();
                        this.set('cursor', 'default');
                    }
                });
            }
        },
        _onRowOver: function (rowParm, over)
        {
            if (l.draggable.dragging) return;
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var methodName = over ? "addClass" : "removeClass";
            if (g.enabledFrozen())
                $(g.getRowObj(rowdata, true))[methodName](p.mouseoverRowCssClass);
            $(g.getRowObj(rowdata, false))[methodName](p.mouseoverRowCssClass);
        },
        _onMouseUp: function (e)
        {
            var g = this, p = this.options;
            if (l.draggable.dragging)
            {
                var src = g._getSrcElementByEvent(e);

                //drop in header cell
                if (src.hcell && src.column)
                {
                    g.trigger('dragdrop', [{ type: 'header', column: src.column, cell: src.hcell }, e]);
                }
                else if (src.row)
                {
                    g.trigger('dragdrop', [{ type: 'row', record: src.data, row: src.row }, e]);
                }
            }
        },
        _onMouseDown: function (e)
        {
            var g = this, p = this.options;
        },
        _onContextmenu: function (e)
        {
            var g = this, p = this.options;
            var src = g._getSrcElementByEvent(e);
            if (src.row)
            {
                if (p.whenRClickToSelect)
                    g.select(src.data);
                if (g.hasBind('contextmenu'))
                {
                    return g.trigger('contextmenu', [{ data: src.data, rowindex: src.data['__index'], row: src.row }, e]);
                }
            }
            else if (src.hcell)
            {
                if (!p.allowHideColumn) return true;
                var columnindex = $(src.hcell).attr("columnindex");
                if (columnindex == undefined) return true;
                var left = (e.pageX - g.body.offset().left + parseInt(g.body[0].scrollLeft));
                if (columnindex == g.columns.length - 1) left -= 50;
                g.popup.css({ left: left, top: g.gridheader.height() + 1 });
                g.popup.toggle();
                return false;
            }
        },
        _onDblClick: function (e)
        {
            var g = this, p = this.options;
            var src = g._getSrcElementByEvent(e);
            if (src.row)
            {
                g.trigger('dblClickRow', [src.data, src.data['__id'], src.row]);
            }
        },
        _onClick: function (e)
        {
            var obj = (e.target || e.srcElement);
            var g = this, p = this.options;
            var src = g._getSrcElementByEvent(e);
            if (src.out)
            {
                if (g.editor.editing && !$.ligerui.win.masking) g.endEdit();
                if (p.allowHideColumn) g.popup.hide();
                return;
            }
            if (src.indetail || src.editing)
            {
                return;
            }
            if (g.editor.editing)
            {
                g.endEdit();
            }
            if (p.allowHideColumn)
            {
                if (!src.popup)
                {
                    g.popup.hide();
                }
            }
            if (src.checkboxall) //复选框全选
            {
                var row = $(src.hrow);
                var uncheck = row.hasClass("l-checked");
                if (g.trigger('beforeCheckAllRow', [!uncheck, g.element]) == false) return false;
                if (uncheck)
                {
                    row.removeClass("l-checked");
                }
                else
                {
                    row.addClass("l-checked");
                }
                g.selected = [];
                for (var rowid in g.records)
                {
                    if (uncheck)
                        g.unselect(g.records[rowid]);
                    else
                        g.select(g.records[rowid]);
                }
                g.trigger('checkAllRow', [!uncheck, g.element]);
            }
            else if (src.hcelltext) //排序
            {
                var hcell = $(src.hcelltext).parent().parent();
                if (!p.enabledSort || !src.column) return;
                if (src.column.isSort == false) return;
                if (p.url && g.isDataChanged && !confirm(p.isContinueByDataChanged)) return;
                var sort = $(".l-grid-hd-cell-sort:first", hcell);
                var columnName = src.column.name;
                if (!columnName) return;
                if (sort.length > 0)
                {
                    if (sort.hasClass("l-grid-hd-cell-sort-asc"))
                    {
                        sort.removeClass("l-grid-hd-cell-sort-asc").addClass("l-grid-hd-cell-sort-desc");
                        hcell.removeClass("l-grid-hd-cell-asc").addClass("l-grid-hd-cell-desc");
                        g.changeSort(columnName, 'desc');
                    }
                    else if (sort.hasClass("l-grid-hd-cell-sort-desc"))
                    {
                        sort.removeClass("l-grid-hd-cell-sort-desc").addClass("l-grid-hd-cell-sort-asc");
                        hcell.removeClass("l-grid-hd-cell-desc").addClass("l-grid-hd-cell-asc");
                        g.changeSort(columnName, 'asc');
                    }
                }
                else
                {
                    hcell.removeClass("l-grid-hd-cell-desc").addClass("l-grid-hd-cell-asc");
                    $(src.hcelltext).after("<span class='l-grid-hd-cell-sort l-grid-hd-cell-sort-asc'>&nbsp;&nbsp;</span>");
                    g.changeSort(columnName, 'asc');
                }
                $(".l-grid-hd-cell-sort", g.gridheader).add($(".l-grid-hd-cell-sort", g.f.gridheader)).not($(".l-grid-hd-cell-sort:first", hcell)).remove();
            }
            //明细
            else if (src.detailbtn && p.detail)
            {
                var item = src.data;
                var row = $([g.getRowObj(item, false)]);
                if (g.enabledFrozen()) row = row.add(g.getRowObj(item, true));
                var rowid = item['__id'];
                if ($(src.detailbtn).hasClass("l-open"))
                {
                    if (p.detail.onCollapse)
                        p.detail.onCollapse(item, $(".l-grid-detailpanel-inner:first", nextrow)[0]);
                    row.next("tr.l-grid-detailpanel").hide();
                    $(src.detailbtn).removeClass("l-open");
                }
                else
                {
                    var nextrow = row.next("tr.l-grid-detailpanel");
                    if (nextrow.length > 0)
                    {
                        nextrow.show();
                        if (p.detail.onExtend)
                            p.detail.onExtend(item, $(".l-grid-detailpanel-inner:first", nextrow)[0]);
                        $(src.detailbtn).addClass("l-open");
                        g.trigger('SysGridHeightChanged');
                        return;
                    }
                    $(src.detailbtn).addClass("l-open");
                    var frozenColNum = 0;
                    for (var i = 0; i < g.columns.length; i++)
                        if (g.columns[i].frozen) frozenColNum++;
                    var detailRow = $("<tr class='l-grid-detailpanel'><td><div class='l-grid-detailpanel-inner' style='display:none'></div></td></tr>");
                    var detailFrozenRow = $("<tr class='l-grid-detailpanel'><td><div class='l-grid-detailpanel-inner' style='display:none'></div></td></tr>");
                    detailRow.attr("id", g.id + "|detail|" + rowid);
                    g.detailrows = g.detailrows || [];
                    g.detailrows.push(detailRow[0]);
                    g.detailrows.push(detailFrozenRow[0]);
                    var detailRowInner = $("div:first", detailRow);
                    detailRowInner.parent().attr("colSpan", g.columns.length - frozenColNum);
                    row.eq(0).after(detailRow);
                    if (frozenColNum > 0)
                    {
                        detailFrozenRow.find("td:first").attr("colSpan", frozenColNum);
                        row.eq(1).after(detailFrozenRow);
                    }
                    if (p.detail.onShowDetail)
                    {
                        p.detail.onShowDetail(item, detailRowInner[0], function ()
                        {
                            g.trigger('SysGridHeightChanged');
                        });
                        $("div:first", detailFrozenRow).add(detailRowInner).show().height(p.detail.height || p.detailHeight);
                    }
                    else if (p.detail.render)
                    {
                        detailRowInner.append(p.detail.render());
                        detailRowInner.show();
                    }
                    g.trigger('SysGridHeightChanged');
                }
            }
            else if (src.groupbtn)
            {
                var grouprow = $(src.grouprow);
                var opening = true;
                if ($(src.groupbtn).hasClass("l-grid-group-togglebtn-close"))
                {
                    $(src.groupbtn).removeClass("l-grid-group-togglebtn-close");

                    if (grouprow.hasClass("l-grid-grouprow-last"))
                    {
                        $("td:first", grouprow).width('auto');
                    }
                }
                else
                {
                    opening = false;
                    $(src.groupbtn).addClass("l-grid-group-togglebtn-close");
                    if (grouprow.hasClass("l-grid-grouprow-last"))
                    {
                        $("td:first", grouprow).width(g.gridtablewidth);
                    }
                }
                var currentRow = grouprow.next(".l-grid-row,.l-grid-totalsummary-group,.l-grid-detailpanel");
                while (true)
                {
                    if (currentRow.length == 0) break;
                    if (opening)
                    {
                        currentRow.show();
                        //如果是明细展开的行，并且之前的状态已经是关闭的，隐藏之
                        if (currentRow.hasClass("l-grid-detailpanel") && !currentRow.prev().find("td.l-grid-row-cell-detail:first span.l-grid-row-cell-detailbtn:first").hasClass("l-open"))
                        {
                            currentRow.hide();
                        }
                    }
                    else
                    {
                        currentRow.hide();
                    }
                    currentRow = currentRow.next(".l-grid-row,.l-grid-totalsummary-group,.l-grid-detailpanel");
                }
                g.trigger('SysGridHeightChanged');
            }
            //树 - 伸展/收缩节点
            else if (src.treelink)
            {
                g.toggle(src.data);
            }
            else if (src.row && g.enabledCheckbox()) //复选框选择行
            {
                //复选框
                var selectRowButtonOnly = p.selectRowButtonOnly ? true : false;
                if (p.enabledEdit) selectRowButtonOnly = true;
                if (src.checkbox || !selectRowButtonOnly)
                {
                    var row = $(src.row);
                    var uncheck = row.hasClass("l-selected");
                    if (g.trigger('beforeCheckRow', [!uncheck, src.data, src.data['__id'], src.row]) == false)
                        return false;
                    var met = uncheck ? 'unselect' : 'select';
                    g[met](src.data);
                    if (p.tree && p.autoCheckChildren)
                    {
                        var children = g.getChildren(src.data, true);
                        for (var i = 0, l = children.length; i < l; i++)
                        {
                            g[met](children[i]);
                        }
                    }
                    g.trigger('checkRow', [!uncheck, src.data, src.data['__id'], src.row]);
                }
                if (!src.checkbox && src.cell && p.enabledEdit && p.clickToEdit)
                {
                    g._applyEditor(src.cell);
                }
            }
            else if (src.row && !g.enabledCheckbox())
            {
                if (src.cell && p.enabledEdit && p.clickToEdit)
                {
                    g._applyEditor(src.cell);
                }

                //选择行
                if ($(src.row).hasClass("l-selected"))
                {
                    if (!p.allowUnSelectRow)
                    {
                        $(src.row).addClass("l-selected-again");
                        return;
                    }
                    g.unselect(src.data);
                }
                else
                {
                    g.select(src.data);
                }
            }
            else if (src.toolbar)
            {
                if (src.first)
                {
                    if (g.trigger('toFirst', [g.element]) == false) return false;
                    g.changePage('first');
                }
                else if (src.prev)
                {
                    if (g.trigger('toPrev', [g.element]) == false) return false;
                    g.changePage('prev');
                }
                else if (src.next)
                {
                    if (g.trigger('toNext', [g.element]) == false) return false;
                    g.changePage('next');
                }
                else if (src.last)
                {
                    if (g.trigger('toLast', [g.element]) == false) return false;
                    g.changePage('last');
                }
                else if (src.load)
                {
                    if ($("span", src.load).hasClass("l-disabled")) return false;
                    if (g.trigger('reload', [g.element]) == false) return false;
                    if (p.url && g.isDataChanged && !confirm(p.isContinueByDataChanged))
                        return false;
                    g.loadData(p.where);
                }
            }
        },
        select: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var rowid = rowdata['__id'];
            var rowobj = g.getRowObj(rowid);
            var rowobj1 = g.getRowObj(rowid, true);
            if (!g.enabledCheckbox() && !g.ctrlKey) //单选
            {
                for (var i in g.selected)
                {
                    var o = g.selected[i];
                    if (o['__id'] in g.records)
                    {
                        $(g.getRowObj(o)).removeClass("l-selected l-selected-again");
                        if (g.enabledFrozen())
                            $(g.getRowObj(o, true)).removeClass("l-selected l-selected-again");
                    }
                }
                g.selected = [];
            }
            if (rowobj) $(rowobj).addClass("l-selected");
            if (rowobj1) $(rowobj1).addClass("l-selected");
            g.selected[g.selected.length] = rowdata;
            g.trigger('selectRow', [rowdata, rowid, rowobj]);
        },
        unselect: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            var rowid = rowdata['__id'];
            var rowobj = g.getRowObj(rowid);
            var rowobj1 = g.getRowObj(rowid, true);
            $(rowobj).removeClass("l-selected l-selected-again");
            if (g.enabledFrozen())
                $(rowobj1).removeClass("l-selected l-selected-again");
            g._removeSelected(rowdata);
            g.trigger('unSelectRow', [rowdata, rowid, rowobj]);
        },
        isSelected: function (rowParm)
        {
            var g = this, p = this.options;
            var rowdata = g.getRow(rowParm);
            for (var i in g.selected)
            {
                if (g.selected[i] == rowdata) return true;
            }
            return false;
        },
        _onResize: function ()
        {
            var g = this, p = this.options;
            if (p.height && p.height != 'auto')
            {
                var windowHeight = $(window).height();
                //if(g.windowHeight != undefined && g.windowHeight == windowHeight) return;

                var h = 0;
                var parentHeight = null;
                if (typeof (p.height) == "string" && p.height.indexOf('%') > 0)
                {
                    var gridparent = g.grid.parent();
                    if (p.InWindow)
                    {
                        parentHeight = windowHeight;
                        parentHeight -= parseInt($('body').css('paddingTop'));
                        parentHeight -= parseInt($('body').css('paddingBottom'));
                    }
                    else
                    {
                        parentHeight = gridparent.height();
                    }
                    h = parentHeight * parseFloat(p.height) * 0.01;
                    if (p.InWindow || gridparent[0].tagName.toLowerCase() == "body")
                        h -= (g.grid.offset().top - parseInt($('body').css('paddingTop')));
                }
                else
                {
                    h = parseInt(p.height);
                }

                h += p.heightDiff;
                g.windowHeight = windowHeight;
                g._setHeight(h);
            }
            if (g.enabledFrozen())
            {
                var gridView1Width = g.gridview1.width();
                var gridViewWidth = g.gridview.width()
                g.gridview2.css({
                    width: gridViewWidth - gridView1Width
                });
            }
            g.trigger('SysGridHeightChanged');
        }
    });

    $.ligerui.controls.Grid.prototype.enabledTotal = $.ligerui.controls.Grid.prototype.isTotalSummary;
    $.ligerui.controls.Grid.prototype.add = $.ligerui.controls.Grid.prototype.addRow;
    $.ligerui.controls.Grid.prototype.update = $.ligerui.controls.Grid.prototype.updateRow;
    $.ligerui.controls.Grid.prototype.append = $.ligerui.controls.Grid.prototype.appendRow;
    $.ligerui.controls.Grid.prototype.getSelected = $.ligerui.controls.Grid.prototype.getSelectedRow;
    $.ligerui.controls.Grid.prototype.getSelecteds = $.ligerui.controls.Grid.prototype.getSelectedRows;
    $.ligerui.controls.Grid.prototype.getCheckedRows = $.ligerui.controls.Grid.prototype.getSelectedRows;
    $.ligerui.controls.Grid.prototype.getCheckedRowObjs = $.ligerui.controls.Grid.prototype.getSelectedRowObjs;
    $.ligerui.controls.Grid.prototype.setOptions = $.ligerui.controls.Grid.prototype.set;

})(jQuery);﻿/**
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

})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.ligerMenu = function (options)
    { 
        return $.ligerui.run.call(null, "ligerMenu", arguments);
    };

    $.ligerDefaults.Menu = {
        width: 120,
        top: 0,
        left: 0,
        items: null,
        shadow: true
    };

    $.ligerMethos.Menu = {};

    $.ligerui.controls.Menu = function (options)
    {
        $.ligerui.controls.Menu.base.constructor.call(this, null, options);
    };
    $.ligerui.controls.Menu.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Menu';
        },
        __idPrev: function ()
        {
            return 'Menu';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Menu;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.menuItemCount = 0;
            //全部菜单
            g.menus = {};
            //顶级菜单
            g.menu = g.createMenu();
            g.element = g.menu[0];
            g.menu.css({ top: p.top, left: p.left, width: p.width });

            p.items && $(p.items).each(function (i, item)
            {
                g.addItem(item);
            });

            $(document).bind('click.menu', function ()
            {
                for (var menuid in g.menus)
                {
                    var menu = g.menus[menuid];
                    if (!menu) return;
                    menu.hide();
                    if (menu.shadow) menu.shadow.hide();
                }
            });
            g.set(p);
        },
        show: function (options, menu)
        {
            var g = this, p = this.options;
            if (menu == undefined) menu = g.menu;
            if (options && options.left != undefined)
            {
                menu.css({ left: options.left });
            }
            if (options && options.top != undefined)
            {
                menu.css({ top: options.top });
            }
            menu.show();
            g.updateShadow(menu);
        },
        updateShadow: function (menu)
        {
            var g = this, p = this.options;
            if (!p.shadow) return;
            menu.shadow.css({
                left: menu.css('left'),
                top: menu.css('top'),
                width: menu.outerWidth(),
                height: menu.outerHeight()
            });
            if (menu.is(":visible"))
                menu.shadow.show();
            else
                menu.shadow.hide();
        },
        hide: function (menu)
        {
            var g = this, p = this.options;
            if (menu == undefined) menu = g.menu;
            g.hideAllSubMenu(menu);
            menu.hide();
            g.updateShadow(menu);
        },
        toggle: function ()
        {
            var g = this, p = this.options;
            g.menu.toggle();
            g.updateShadow(g.menu);
        },
        removeItem: function (itemid)
        {
            var g = this, p = this.options;
            $("> .l-menu-item[ligeruimenutemid=" + itemid + "]", g.menu.items).remove();
        },
        setEnabled: function (itemid)
        {
            var g = this, p = this.options;
            $("> .l-menu-item[ligeruimenutemid=" + itemid + "]", g.menu.items).removeClass("l-menu-item-disable");
        },
        setDisabled: function (itemid)
        {
            var g = this, p = this.options;
            $("> .l-menu-item[ligeruimenutemid=" + itemid + "]", g.menu.items).addClass("l-menu-item-disable");
        },
        isEnable: function (itemid)
        {
            var g = this, p = this.options;
            return !$("> .l-menu-item[ligeruimenutemid=" + itemid + "]", g.menu.items).hasClass("l-menu-item-disable");
        },
        getItemCount: function ()
        {
            var g = this, p = this.options;
            return $("> .l-menu-item", g.menu.items).length;
        },
        addItem: function (item, menu)
        {
            var g = this, p = this.options;
            if (!item) return;
            if (menu == undefined) menu = g.menu;

            if (item.line)
            {
                menu.items.append('<div class="l-menu-item-line"></div>');
                return;
            }
            var ditem = $('<div class="l-menu-item"><div class="l-menu-item-text"></div> </div>');
            var itemcount = $("> .l-menu-item", menu.items).length;
            menu.items.append(ditem);
            ditem.attr("ligeruimenutemid", ++g.menuItemCount);
            item.id && ditem.attr("menuitemid", item.id);
            item.text && $(">.l-menu-item-text:first", ditem).html(item.text);
            item.icon && ditem.prepend('<div class="l-menu-item-icon l-icon-' + item.icon + '"></div>');
            if (item.disable || item.disabled)
                ditem.addClass("l-menu-item-disable");
            if (item.children)
            {
                ditem.append('<div class="l-menu-item-arrow"></div>');
                var newmenu = g.createMenu(ditem.attr("ligeruimenutemid"));
                g.menus[ditem.attr("ligeruimenutemid")] = newmenu;
                newmenu.width(p.width);
                newmenu.hover(null, function ()
                {
                    if (!newmenu.showedSubMenu)
                        g.hide(newmenu);
                });
                $(item.children).each(function ()
                {
                    g.addItem(this, newmenu);
                });
            }
            item.click && ditem.click(function ()
            {
                if ($(this).hasClass("l-menu-item-disable")) return;
                item.click(item, itemcount);
            });
            item.dblclick && ditem.dblclick(function ()
            {
                if ($(this).hasClass("l-menu-item-disable")) return;
                item.dblclick(item, itemcount);
            });

            var menuover = $("> .l-menu-over:first", menu);
            ditem.hover(function ()
            {
                if ($(this).hasClass("l-menu-item-disable")) return;
                var itemtop = $(this).offset().top;
                var top = itemtop - menu.offset().top;
                menuover.css({ top: top });
                g.hideAllSubMenu(menu);
                if (item.children)
                {
                    var ligeruimenutemid = $(this).attr("ligeruimenutemid");
                    if (!ligeruimenutemid) return;
                    if (g.menus[ligeruimenutemid])
                    {
                        g.show({ top: itemtop, left: $(this).offset().left + $(this).width() - 5 }, g.menus[ligeruimenutemid]);
                        menu.showedSubMenu = true;
                    }
                }
            }, function ()
            {
                if ($(this).hasClass("l-menu-item-disable")) return;
                var ligeruimenutemid = $(this).attr("ligeruimenutemid");
                if (item.children)
                {
                    var ligeruimenutemid = $(this).attr("ligeruimenutemid");
                    if (!ligeruimenutemid) return;
                };
            });
        },
        hideAllSubMenu: function (menu)
        {
            var g = this, p = this.options;
            if (menu == undefined) menu = g.menu;
            $("> .l-menu-item", menu.items).each(function ()
            {
                if ($("> .l-menu-item-arrow", this).length > 0)
                {
                    var ligeruimenutemid = $(this).attr("ligeruimenutemid");
                    if (!ligeruimenutemid) return;
                    g.menus[ligeruimenutemid] && g.hide(g.menus[ligeruimenutemid]);
                }
            });
            menu.showedSubMenu = false;
        },
        createMenu: function (parentMenuItemID)
        {
            var g = this, p = this.options;
            var menu = $('<div class="l-menu" style="display:none"><div class="l-menu-yline"></div><div class="l-menu-over"><div class="l-menu-over-l"></div> <div class="l-menu-over-r"></div></div><div class="l-menu-inner"></div></div>');
            parentMenuItemID && menu.attr("ligeruiparentmenuitemid", parentMenuItemID);
            menu.items = $("> .l-menu-inner:first", menu);
            menu.appendTo('body');
            if (p.shadow)
            {
                menu.shadow = $('<div class="l-menu-shadow"></div>').insertAfter(menu);
                g.updateShadow(menu);
            }
            menu.hover(null, function ()
            {
                if (!menu.showedSubMenu)
                    $("> .l-menu-over:first", menu).css({ top: -24 });
            });
            if (parentMenuItemID)
                g.menus[parentMenuItemID] = menu;
            else
                g.menus[0] = menu;
            return menu;
        }
    });
    //旧写法保留
    $.ligerui.controls.Menu.prototype.setEnable = $.ligerui.controls.Menu.prototype.setEnabled;
    $.ligerui.controls.Menu.prototype.setDisable = $.ligerui.controls.Menu.prototype.setDisabled;



})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
* Depend on:
* 1,LigerMenu
*/
(function ($)
{
    $.fn.ligerMenuBar = function (options)
    {
        return $.ligerui.run.call(this, "ligerMenuBar", arguments);
    };
    $.fn.ligerGetMenuBarManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetMenuBarManager", arguments);
    };

    $.ligerDefaults.MenuBar = {};

    $.ligerMethos.MenuBar = {};

    $.ligerui.controls.MenuBar = function (element, options)
    {
        $.ligerui.controls.MenuBar.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.MenuBar.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'MenuBar';
        },
        __idPrev: function ()
        {
            return 'MenuBar';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.MenuBar;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.menubar = $(this.element);
            if (!g.menubar.hasClass("l-menubar")) g.menubar.addClass("l-menubar");
            if (p && p.items)
            {
                $(p.items).each(function (i, item)
                {
                    g.addItem(item);
                });
            }
            $(document).click(function ()
            {
                $(".l-panel-btn-selected", g.menubar).removeClass("l-panel-btn-selected");
            });
            g.set(p);
        },
        addItem: function (item)
        {
            var g = this, p = this.options;
            var ditem = $('<div class="l-menubar-item l-panel-btn"><span></span><div class="l-panel-btn-l"></div><div class="l-panel-btn-r"></div><div class="l-menubar-item-down"></div></div>');
            g.menubar.append(ditem);
            item.id && ditem.attr("menubarid", item.id);
            item.text && $("span:first", ditem).html(item.text);
            item.disable && ditem.addClass("l-menubar-item-disable");
            item.click && ditem.click(function () { item.click(item); });
            if (item.menu)
            {
                var menu = $.ligerMenu(item.menu);
                ditem.hover(function ()
                {
                    g.actionMenu && g.actionMenu.hide();
                    var left = $(this).offset().left;
                    var top = $(this).offset().top + $(this).height();
                    menu.show({ top: top, left: left });
                    g.actionMenu = menu;
                    $(this).addClass("l-panel-btn-over l-panel-btn-selected").siblings(".l-menubar-item").removeClass("l-panel-btn-selected");
                }, function ()
                {
                    $(this).removeClass("l-panel-btn-over");
                });
            }
            else
            {
                ditem.hover(function ()
                {
                    $(this).addClass("l-panel-btn-over");
                }, function ()
                {
                    $(this).removeClass("l-panel-btn-over");
                });
                $(".l-menubar-item-down", ditem).remove();
            }

        }
    });  

})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    
    $.ligerMessageBox = function (options)
    {
        return $.ligerui.run.call(null, "ligerMessageBox", arguments, { isStatic: true });
    }; 
    

    $.ligerDefaults.MessageBox = {
        isDrag: true
    };

    $.ligerMethos.MessageBox = {};

    $.ligerui.controls.MessageBox = function (options)
    {
        $.ligerui.controls.MessageBox.base.constructor.call(this, null, options);
    };
    $.ligerui.controls.MessageBox.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'MessageBox';
        },
        __idPrev: function ()
        {
            return 'MessageBox';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.MessageBox;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            var messageBoxHTML = "";
            messageBoxHTML += '<div class="l-messagebox">';
            messageBoxHTML += '        <div class="l-messagebox-lt"></div><div class="l-messagebox-rt"></div>';
            messageBoxHTML += '        <div class="l-messagebox-l"></div><div class="l-messagebox-r"></div> ';
            messageBoxHTML += '        <div class="l-messagebox-image"></div>';
            messageBoxHTML += '        <div class="l-messagebox-title">';
            messageBoxHTML += '            <div class="l-messagebox-title-inner"></div>';
            messageBoxHTML += '            <div class="l-messagebox-close"></div>';
            messageBoxHTML += '        </div>';
            messageBoxHTML += '        <div class="l-messagebox-content">';
            messageBoxHTML += '        </div>';
            messageBoxHTML += '        <div class="l-messagebox-buttons"><div class="l-messagebox-buttons-inner">';
            messageBoxHTML += '        </div></div>';
            messageBoxHTML += '    </div>'; 
            g.messageBox = $(messageBoxHTML);
            $('body').append(g.messageBox);
            g.messageBox.close = function ()
            {
                g._removeWindowMask();
                g.messageBox.remove();
            };
            //设置参数属性
            p.width && g.messageBox.width(p.width);
            p.title && $(".l-messagebox-title-inner", g.messageBox).html(p.title);
            p.content && $(".l-messagebox-content", g.messageBox).html(p.content);
            if (p.buttons)
            {
                $(p.buttons).each(function (i, item)
                {
                    var btn = $('<div class="l-messagebox-btn"><div class="l-messagebox-btn-l"></div><div class="l-messagebox-btn-r"></div><div class="l-messagebox-btn-inner"></div></div>');
                    $(".l-messagebox-btn-inner", btn).html(item.text);
                    $(".l-messagebox-buttons-inner", g.messageBox).append(btn);
                    item.width && btn.width(item.width);
                    item.onclick && btn.click(function () { item.onclick(item, i, g.messageBox) });
                });
                $(".l-messagebox-buttons-inner", g.messageBox).append("<div class='l-clear'></div>");
            }
            var boxWidth = g.messageBox.width();
            var sumBtnWidth = 0;
            $(".l-messagebox-buttons-inner .l-messagebox-btn", g.messageBox).each(function ()
            {
                sumBtnWidth += $(this).width();
            });
            $(".l-messagebox-buttons-inner", g.messageBox).css({ marginLeft: parseInt((boxWidth - sumBtnWidth) * 0.5) });
            //设置背景、拖动支持 和设置图片
            g._applyWindowMask();
            g._applyDrag();
            g._setImage();
            //设置事件
            $(".l-messagebox-btn", g.messageBox).hover(function ()
            {
                $(this).addClass("l-messagebox-btn-over");
            }, function ()
            {
                $(this).removeClass("l-messagebox-btn-over");
            });
            $(".l-messagebox-close", g.messageBox).hover(function ()
            {
                $(this).addClass("l-messagebox-close-over");
            }, function ()
            {
                $(this).removeClass("l-messagebox-close-over");
            }).click(function ()
            {
                g.messageBox.close();
            });
            g.set(p);
        },
        close: function ()
        {
            var g = this, p = this.options;
            this.g._removeWindowMask();
            this.messageBox.remove();
        },
        _applyWindowMask: function ()
        {
            var g = this, p = this.options;
            $(".l-window-mask").remove();
            $("<div class='l-window-mask' style='display: block;'></div>").appendTo($("body"));
        },
        _removeWindowMask: function ()
        {
            var g = this, p = this.options;
            $(".l-window-mask").remove();
        },
        _applyDrag: function ()
        {
            var g = this, p = this.options;
            if (p.isDrag && $.fn.ligerDrag)
                g.messageBox.ligerDrag({ handler: '.l-messagebox-title-inner', animate: false });
        },
        _setImage: function ()
        {
            var g = this, p = this.options;
            if (p.type)
            {
                if (p.type == 'success' || p.type == 'donne')
                {
                    $(".l-messagebox-image", g.messageBox).addClass("l-messagebox-image-donne").show();
                    $(".l-messagebox-content", g.messageBox).css({ paddingLeft: 64, paddingBottom: 30 });
                }
                else if (p.type == 'error')
                {
                    $(".l-messagebox-image", g.messageBox).addClass("l-messagebox-image-error").show();
                    $(".l-messagebox-content", g.messageBox).css({ paddingLeft: 64, paddingBottom: 30 });
                }
                else if (p.type == 'warn')
                {
                    $(".l-messagebox-image", g.messageBox).addClass("l-messagebox-image-warn").show();
                    $(".l-messagebox-content", g.messageBox).css({ paddingLeft: 64, paddingBottom: 30 });
                }
                else if (p.type == 'question')
                {
                    $(".l-messagebox-image", g.messageBox).addClass("l-messagebox-image-question").show();
                    $(".l-messagebox-content", g.messageBox).css({ paddingLeft: 64, paddingBottom: 40 });
                }
            }
        }
    });


    $.ligerMessageBox.show = function (p)
    {
        return $.ligerMessageBox(p);
    };
    $.ligerMessageBox.alert = function (title, content, type, onBtnClick)
    {
        title = title || "";
        content = content || title;
        var onclick = function (item, index, messageBox)
        {
            messageBox.close();
            if (onBtnClick)
                onBtnClick(item, index, messageBox);
        };
        p = {
            title: title,
            content: content,
            buttons: [{ text: '确定', onclick: onclick}]
        };
        if (type) p.type = type;
        return $.ligerMessageBox(p);
    };
    $.ligerMessageBox.confirm = function (title, content, callback)
    {
        var onclick = function (item, index, messageBox)
        {
            messageBox.close();
            if (callback)
            {
                callback(index == 0);
            }
        };
        p = {
            type: 'question',
            title: title,
            content: content,
            buttons: [{ text: '是', onclick: onclick }, { text: '否', onclick: onclick}]
        };
        return $.ligerMessageBox(p);
    };
    $.ligerMessageBox.success = function (title, content, onBtnClick)
    {
        return $.ligerMessageBox.alert(title, content, 'success', onBtnClick);
    };
    $.ligerMessageBox.error = function (title, content, onBtnClick)
    {
        return $.ligerMessageBox.alert(title, content, 'error', onBtnClick);
    };
    $.ligerMessageBox.warn = function (title, content, onBtnClick)
    {
        return $.ligerMessageBox.alert(title, content, 'warn', onBtnClick);
    };
    $.ligerMessageBox.question = function (title, content)
    {
        return $.ligerMessageBox.alert(title, content, 'question');
    };


})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{

    $.fn.ligerRadio = function ()
    {
        return $.ligerui.run.call(this, "ligerRadio", arguments);
    };

    $.fn.ligerGetRadioManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetRadioManager", arguments);
    };

    $.ligerDefaults.Radio = { disabled: false };

    $.ligerMethos.Radio = {};

    $.ligerui.controls.Radio = function (element, options)
    {
        $.ligerui.controls.Radio.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.Radio.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'Radio';
        },
        __idPrev: function ()
        {
            return 'Radio';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Radio;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.input = $(this.element);
            g.link = $('<a href="javascript:void(0)" class="l-radio"></a>');
            g.wrapper = g.input.addClass('l-hidden').wrap('<div class="l-radio-wrapper"></div>').parent();
            g.wrapper.prepend(g.link);
            g.input.change(function ()
            {
                if (this.checked)
                {
                    g.link.addClass('l-radio-checked');
                }
                else
                {
                    g.link.removeClass('l-radio-checked');
                }
                return true;
            });
            g.link.click(function ()
            {
                g._doclick();
            });
            g.wrapper.hover(function ()
            {
                if (!p.disabled)
                    $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
            this.element.checked && g.link.addClass('l-radio-checked');

            if (this.element.id)
            {
                $("label[for=" + this.element.id + "]").click(function ()
                {
                    g._doclick();
                });
            }
            g.set(p);
        },
        setValue: function (value)
        {
            var g = this, p = this.options;
            if (!value)
            {
                g.input[0].checked = false;
                g.link.removeClass('l-radio-checked');
            }
            else
            {
                g.input[0].checked = true;
                g.link.addClass('l-radio-checked');
            }
        },
        getValue: function ()
        {
            return this.input[0].checked;
        },
        setEnabled: function ()
        {
            this.input.attr('disabled', false);
            this.wrapper.removeClass("l-disabled");
            this.options.disabled = false;
        },
        setDisabled: function ()
        {
            this.input.attr('disabled', true);
            this.wrapper.addClass("l-disabled");
            this.options.disabled = true;
        },
        updateStyle: function ()
        {
            if (this.input.attr('disabled'))
            {
                this.wrapper.addClass("l-disabled");
                this.options.disabled = true;
            }
            if (this.input[0].checked)
            {
                this.link.addClass('l-checkbox-checked');
            }
            else
            {
                this.link.removeClass('l-checkbox-checked');
            }
        },
        _doclick: function ()
        {
            var g = this, p = this.options;
            if (g.input.attr('disabled')) { return false; }
            g.input.trigger('click').trigger('change');
            var formEle;
            if (g.input[0].form) formEle = g.input[0].form;
            else formEle = document;
            $("input:radio[name=" + g.input[0].name + "]", formEle).not(g.input).trigger("change");
            return false;
        }
    });


})(jQuery);/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerResizable = function (options)
    {
        return $.ligerui.run.call(this, "ligerResizable", arguments,
        {
            idAttrName: 'ligeruiresizableid', hasElement: false, propertyToElemnt: 'target'
        });
    };

    $.fn.ligerGetResizableManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetResizableManager", arguments,
        {
            idAttrName: 'ligeruiresizableid', hasElement: false, propertyToElemnt: 'target'
        });
    };


    $.ligerDefaults.Resizable = {
        handles: 'n, e, s, w, ne, se, sw, nw',
        maxWidth: 2000,
        maxHeight: 2000,
        minWidth: 20,
        minHeight: 20,
        scope: 3,
        animate: false,
        onStartResize: function (e) { },
        onResize: function (e) { },
        onStopResize: function (e) { },
        onEndResize: null
    };

    $.ligerui.controls.Resizable = function (options)
    {
        $.ligerui.controls.Resizable.base.constructor.call(this, null, options);
    };

    $.ligerui.controls.Resizable.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Resizable';
        },
        __idPrev: function ()
        {
            return 'Resizable';
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.target = $(p.target);
            g.set(p);

            g.target.mousemove(function (e)
            {
                if (p.disabled) return;
                g.dir = g._getDir(e);
                if (g.dir)
                    g.target.css('cursor', g.dir + '-resize');
                else if (g.target.css('cursor').indexOf('-resize') > 0)
                    g.target.css('cursor', 'default');
                if (p.target.ligeruidragid)
                {
                    var drag = $.ligerui.get(p.target.ligeruidragid);
                    if (drag && g.dir)
                    {
                        drag.set('disabled', true);
                    } else if (drag)
                    {
                        drag.set('disabled', false);
                    }
                }
            }).mousedown(function (e)
            {
                if (p.disabled) return;
                if (g.dir)
                {
                    g._start(e);
                }
            });
        },
        _rendered: function ()
        {
            this.options.target.ligeruiresizableid = this.id;
        },
        _getDir: function (e)
        {
            var g = this, p = this.options;
            var dir = '';
            var xy = g.target.offset();
            var width = g.target.width();
            var height = g.target.height();
            var scope = p.scope;
            var pageX = e.pageX || e.screenX;
            var pageY = e.pageY || e.screenY;
            if (pageY >= xy.top && pageY < xy.top + scope)
            {
                dir += 'n';
            }
            else if (pageY <= xy.top + height && pageY > xy.top + height - scope)
            {
                dir += 's';
            }
            if (pageX >= xy.left && pageX < xy.left + scope)
            {
                dir += 'w';
            }
            else if (pageX <= xy.left + width && pageX > xy.left + width - scope)
            {
                dir += 'e';
            }
            if (p.handles == "all" || dir == "") return dir;
            if ($.inArray(dir, g.handles) != -1) return dir;
            return '';
        },
        _setHandles: function (handles)
        {
            if (!handles) return;
            this.handles = handles.replace(/(\s*)/g, '').split(',');
        },
        _createProxy: function ()
        {
            var g = this;
            g.proxy = $('<div class="l-resizable"></div>');
            g.proxy.width(g.target.width()).height(g.target.height())
            g.proxy.attr("resizableid", g.id).appendTo('body');
        },
        _removeProxy: function ()
        {
            var g = this;
            if (g.proxy)
            {
                g.proxy.remove();
                g.proxy = null;
            }
        },
        _start: function (e)
        {
            var g = this, p = this.options;
            g._createProxy();
            g.proxy.css({
                left: g.target.offset().left,
                top: g.target.offset().top,
                position: 'absolute'
            });
            g.current = {
                dir: g.dir,
                left: g.target.offset().left,
                top: g.target.offset().top,
                startX: e.pageX || e.screenX,
                startY: e.pageY || e.clientY,
                width: g.target.width(),
                height: g.target.height()
            };
            $(document).bind("selectstart.resizable", function () { return false; });
            $(document).bind('mouseup.resizable', function ()
            {
                g._stop.apply(g, arguments);
            });
            $(document).bind('mousemove.resizable', function ()
            {
                g._drag.apply(g, arguments);
            });
            g.proxy.show();
            g.trigger('startResize', [g.current, e]);
        },
        changeBy: {
            t: ['n', 'ne', 'nw'],
            l: ['w', 'sw', 'nw'],
            w: ['w', 'sw', 'nw', 'e', 'ne', 'se'],
            h: ['n', 'ne', 'nw', 's', 'se', 'sw']
        },
        _drag: function (e)
        {
            var g = this, p = this.options;
            if (!g.current) return;
            if (!g.proxy) return;
            g.proxy.css('cursor', g.current.dir == '' ? 'default' : g.current.dir + '-resize');
            var pageX = e.pageX || e.screenX;
            var pageY = e.pageY || e.screenY;
            g.current.diffX = pageX - g.current.startX;
            g.current.diffY = pageY - g.current.startY;
            g._applyResize(g.proxy);
            g.trigger('resize', [g.current, e]);
        },
        _stop: function (e)
        {
            var g = this, p = this.options;

            if (g.hasBind('stopResize'))
            {
                if (g.trigger('stopResize', [g.current, e]) != false)
                    g._applyResize();
            }
            else
            {
                g._applyResize();
            }
            g._removeProxy();
            g.trigger('endResize', [g.current, e]);
            $(document).unbind("selectstart.resizable");
            $(document).unbind('mousemove.resizable');
            $(document).unbind('mouseup.resizable');
        },
        _applyResize: function (applyResultBody)
        {
            var g = this, p = this.options;
            var cur = {
                left: g.current.left,
                top: g.current.top,
                width: g.current.width,
                height: g.current.height
            };
            var applyToTarget = false;
            if (!applyResultBody)
            {
                applyResultBody = g.target;
                applyToTarget = true;
                if (!isNaN(parseInt(g.target.css('top'))))
                    cur.top = parseInt(g.target.css('top'));
                else
                    cur.top = 0;
                if (!isNaN(parseInt(g.target.css('left'))))
                    cur.left = parseInt(g.target.css('left'));
                else
                    cur.left = 0;
            }
            if ($.inArray(g.current.dir, g.changeBy.l) > -1)
            {
                cur.left += g.current.diffX;
                g.current.diffLeft = g.current.diffX;

            }
            else if (applyToTarget)
            {
                delete cur.left;
            }
            if ($.inArray(g.current.dir, g.changeBy.t) > -1)
            {
                cur.top += g.current.diffY;
                g.current.diffTop = g.current.diffY;
            }
            else if (applyToTarget)
            {
                delete cur.top;
            }
            if ($.inArray(g.current.dir, g.changeBy.w) > -1)
            {
                cur.width += (g.current.dir.indexOf('w') == -1 ? 1 : -1) * g.current.diffX;
                g.current.newWidth = cur.width;
            }
            else if (applyToTarget)
            {
                delete cur.width;
            }
            if ($.inArray(g.current.dir, g.changeBy.h) > -1)
            {
                cur.height += (g.current.dir.indexOf('n') == -1 ? 1 : -1) * g.current.diffY;
                g.current.newHeight = cur.height;
            }
            else if (applyToTarget)
            {
                delete cur.height;
            }
            if (applyToTarget && p.animate)
                applyResultBody.animate(cur);
            else
                applyResultBody.css(cur);
        }
    });



})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerSpinner = function ()
    {
        return $.ligerui.run.call(this, "ligerSpinner", arguments);
    };
    $.fn.ligerGetSpinnerManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetSpinnerManager", arguments);
    };

    $.ligerDefaults.Spinner = {
        type: 'float',     //类型 float:浮点数 int:整数 time:时间
        isNegative: true, //是否负数
        decimalplace: 2,   //小数位 type=float时起作用
        step: 0.1,         //每次增加的值
        interval: 50,      //间隔，毫秒
        onChangeValue: false,    //改变值事件
        minValue: null,        //最小值
        maxValue: null,         //最大值
        disabled: false
    };

    $.ligerMethos.Spinner = {};

    $.ligerui.controls.Spinner = function (element, options)
    {
        $.ligerui.controls.Spinner.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.Spinner.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'Spinner';
        },
        __idPrev: function ()
        {
            return 'Spinner';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Spinner;
        },
        _init: function ()
        {
            $.ligerui.controls.Spinner.base._init.call(this);
            var p = this.options;
            if (p.type == 'float')
            {
                p.step = 0.1;
                p.interval = 50;
            } else if (p.type == 'int')
            {
                p.step = 1;
                p.interval = 100;
            } else if (p.type == 'time')
            {
                p.step = 1;
                p.interval = 100;
            }
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.interval = null;
            g.inputText = null;
            g.value = null;
            g.textFieldID = "";
            if (this.element.tagName.toLowerCase() == "input" && this.element.type && this.element.type == "text")
            {
                g.inputText = $(this.element);
                if (this.element.id)
                    g.textFieldID = this.element.id;
            }
            else
            {
                g.inputText = $('<input type="text"/>');
                g.inputText.appendTo($(this.element));
            }
            if (g.textFieldID == "" && p.textFieldID)
                g.textFieldID = p.textFieldID;

            g.link = $('<div class="l-trigger"><div class="l-spinner-up"><div class="l-spinner-icon"></div></div><div class="l-spinner-split"></div><div class="l-spinner-down"><div class="l-spinner-icon"></div></div></div>');
            g.wrapper = g.inputText.wrap('<div class="l-text"></div>').parent();
            g.wrapper.append('<div class="l-text-l"></div><div class="l-text-r"></div>');
            g.wrapper.append(g.link).after(g.selectBox).after(g.valueField);
            g.link.up = $(".l-spinner-up", g.link);
            g.link.down = $(".l-spinner-down", g.link);
            g.inputText.addClass("l-text-field");

            if (p.disabled)
            {
                g.wrapper.addClass("l-text-disabled");
            }
            //初始化
            if (!g._isVerify(g.inputText.val()))
            {
                g.value = g._getDefaultValue();
                g.inputText.val(g.value);
            }
            //事件
            g.link.up.hover(function ()
            {
                if (!p.disabled)
                    $(this).addClass("l-spinner-up-over");
            }, function ()
            {
                clearInterval(g.interval);
                $(document).unbind("selectstart.spinner");
                $(this).removeClass("l-spinner-up-over");
            }).mousedown(function ()
            {
                if (!p.disabled)
                {
                    g._uping.call(g);
                    g.interval = setInterval(function ()
                    {
                        g._uping.call(g);
                    }, p.interval);
                    $(document).bind("selectstart.spinner", function () { return false; });
                }
            }).mouseup(function ()
            {
                clearInterval(g.interval);
                g.inputText.trigger("change").focus();
                $(document).unbind("selectstart.spinner");
            });
            g.link.down.hover(function ()
            {
                if (!p.disabled)
                    $(this).addClass("l-spinner-down-over");
            }, function ()
            {
                clearInterval(g.interval);
                $(document).unbind("selectstart.spinner");
                $(this).removeClass("l-spinner-down-over");
            }).mousedown(function ()
            {
                if (!p.disabled)
                {
                    g.interval = setInterval(function ()
                    {
                        g._downing.call(g);
                    }, p.interval);
                    $(document).bind("selectstart.spinner", function () { return false; });
                }
            }).mouseup(function ()
            {
                clearInterval(g.interval);
                g.inputText.trigger("change").focus();
                $(document).unbind("selectstart.spinner");
            });

            g.inputText.change(function ()
            {
                var value = g.inputText.val();
                g.value = g._getVerifyValue(value);
                g.trigger('changeValue', [g.value]);
                g.inputText.val(g.value);
            }).blur(function ()
            {
                g.wrapper.removeClass("l-text-focus");
            }).focus(function ()
            {
                g.wrapper.addClass("l-text-focus");
            });
            g.wrapper.hover(function ()
            {
                if (!p.disabled)
                    g.wrapper.addClass("l-text-over");
            }, function ()
            {
                g.wrapper.removeClass("l-text-over");
            });
            g.set(p);
        },
        _setWidth: function (value)
        {
            var g = this;
            if (value > 20)
            {
                g.wrapper.css({ width: value });
                g.inputText.css({ width: value - 20 });
            }
        },
        _setHeight: function (value)
        {
            var g = this;
            if (value > 10)
            {
                g.wrapper.height(value);
                g.inputText.height(value - 2);
                g.link.height(value - 4);
            }
        },
        _setDisabled: function (value)
        {
            if (value)
            {
                this.wrapper.addClass("l-text-disabled");
            }
            else
            {
                this.wrapper.removeClass("l-text-disabled");
            }
        },
        setValue: function (value)
        {
            this.inputText.val(value);
        },
        getValue: function ()
        {
            return this.inputText.val();
        },
        _round: function (v, e)
        {
            var g = this, p = this.options;
            var t = 1;
            for (; e > 0; t *= 10, e--);
            for (; e < 0; t /= 10, e++);
            return Math.round(v * t) / t;
        },
        _isInt: function (str)
        {
            var g = this, p = this.options;
            var strP = p.isNegative ? /^-?\d+$/ : /^\d+$/;
            if (!strP.test(str)) return false;
            if (parseFloat(str) != str) return false;
            return true;
        },
        _isFloat: function (str)
        {
            var g = this, p = this.options;
            var strP = p.isNegative ? /^-?\d+(\.\d+)?$/ : /^\d+(\.\d+)?$/;
            if (!strP.test(str)) return false;
            if (parseFloat(str) != str) return false;
            return true;
        },
        _isTime: function (str)
        {
            var g = this, p = this.options;
            var a = str.match(/^(\d{1,2}):(\d{1,2})$/);
            if (a == null) return false;
            if (a[1] > 24 || a[2] > 60) return false;
            return true;

        },
        _isVerify: function (str)
        {
            var g = this, p = this.options;
            if (p.type == 'float')
            {
                if (!g._isFloat(str)) return false;
                var value = parseFloat(str);
                if (p.minValue != undefined && p.minValue > value) return false;
                if (p.maxValue != undefined && p.maxValue < value) return false;
                return true;
            } else if (p.type == 'int')
            {
                if (!g._isInt(str)) return false;
                var value = parseInt(str);
                if (p.minValue != undefined && p.minValue > value) return false;
                if (p.maxValue != undefined && p.maxValue < value) return false;
                return true;
            } else if (p.type == 'time')
            {
                return g._isTime(str);
            }
            return false;
        },
        _getVerifyValue: function (value)
        {
            var g = this, p = this.options;
            var newvalue = null;
            if (p.type == 'float')
            {
                newvalue = g._round(value, p.decimalplace);
            } else if (p.type == 'int')
            {
                newvalue = parseInt(value);
            } else if (p.type == 'time')
            {
                newvalue = value;
            }
            if (!g._isVerify(newvalue))
            {
                return g.value;
            } else
            {
                return newvalue;
            }
        },
        _isOverValue: function (value)
        {
            var g = this, p = this.options;
            if (p.minValue != null && p.minValue > value) return true;
            if (p.maxValue != null && p.maxValue < value) return true;
            return false;
        },
        _getDefaultValue: function ()
        {
            var g = this, p = this.options;
            if (p.type == 'float' || p.type == 'int') { return 0; }
            else if (p.type == 'time') { return "00:00"; }
        },
        _addValue: function (num)
        {
            var g = this, p = this.options;
            var value = g.inputText.val();
            value = parseFloat(value) + num;
            if (g._isOverValue(value)) return;
            g.inputText.val(value);
            g.inputText.trigger("change");
        },
        _addTime: function (minute)
        {
            var g = this, p = this.options;
            var value = g.inputText.val();
            var a = value.match(/^(\d{1,2}):(\d{1,2})$/);
            newminute = parseInt(a[2]) + minute;
            if (newminute < 10) newminute = "0" + newminute;
            value = a[1] + ":" + newminute;
            if (g._isOverValue(value)) return;
            g.inputText.val(value);
            g.inputText.trigger("change");
        },
        _uping: function ()
        {
            var g = this, p = this.options;
            if (p.type == 'float' || p.type == 'int')
            {
                g._addValue(p.step);
            } else if (p.type == 'time')
            {
                g._addTime(p.step);
            }
        },
        _downing: function ()
        {
            var g = this, p = this.options;
            if (p.type == 'float' || p.type == 'int')
            {
                g._addValue(-1 * p.step);
            } else if (p.type == 'time')
            {
                g._addTime(-1 * p.step);
            }
        },
        _isDateTime: function (dateStr)
        {
            var g = this, p = this.options;
            var r = dateStr.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/);
            if (r == null) return false;
            var d = new Date(r[1], r[3] - 1, r[4]);
            if (d == "NaN") return false;
            return (d.getFullYear() == r[1] && (d.getMonth() + 1) == r[3] && d.getDate() == r[4]);
        },
        _isLongDateTime: function (dateStr)
        {
            var g = this, p = this.options;
            var reg = /^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2}) (\d{1,2}):(\d{1,2})$/;
            var r = dateStr.match(reg);
            if (r == null) return false;
            var d = new Date(r[1], r[3] - 1, r[4], r[5], r[6]);
            if (d == "NaN") return false;
            return (d.getFullYear() == r[1] && (d.getMonth() + 1) == r[3] && d.getDate() == r[4] && d.getHours() == r[5] && d.getMinutes() == r[6]);
        }
    });


})(jQuery);﻿/**
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



})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerTextBox = function ()
    {
        return $.ligerui.run.call(this, "ligerTextBox", arguments);
    };

    $.fn.ligerGetTextBoxManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetTextBoxManager", arguments);
    };

    $.ligerDefaults.TextBox = {
        onChangeValue: null,
        width: null,
        disabled: false,
        value: null     //初始化值 
    };


    $.ligerui.controls.TextBox = function (element, options)
    {
        $.ligerui.controls.TextBox.base.constructor.call(this, element, options);
    };

    $.ligerui.controls.TextBox.ligerExtend($.ligerui.controls.Input, {
        __getType: function ()
        {
            return 'TextBox'
        },
        __idPrev: function ()
        {
            return 'TextBox';
        },
        _init: function ()
        {
            $.ligerui.controls.TextBox.base._init.call(this);
            var g = this, p = this.options;
            if (!p.width)
            {
                p.width = $(g.element).width();
            }
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.inputText = $(this.element);
            //外层
            g.wrapper = g.inputText.wrap('<div class="l-text"></div>').parent();
            g.wrapper.append('<div class="l-text-l"></div><div class="l-text-r"></div>');
            if (!g.inputText.hasClass("l-text-field"))
                g.inputText.addClass("l-text-field");
            this._setEvent();

            g.set(p);
        },
        _getValue: function ()
        {
            return this.inputText.val();
        },
        _setEvent: function ()
        {
            var g = this, p = this.options;
            g.inputText.bind('blur.ligerTextBox', function ()
            {
                g.trigger('blur');
                g.wrapper.removeClass("l-text-focus");
            }).bind('focus.ligerTextBox', function ()
            {
                g.trigger('focus');
                g.wrapper.addClass("l-text-focus");
            })
            .change(function ()
            {
                g.trigger('changeValue', [this.value]);
            });
            g.wrapper.hover(function ()
            {
                g.trigger('mouseOver');
                g.wrapper.addClass("l-text-over");
            }, function ()
            {
                g.trigger('mouseOut');
                g.wrapper.removeClass("l-text-over");
            });
        },
        _setDisabled: function (value)
        {
            if (value)
            {
                this.inputText.attr("readonly", "readonly");
                this.wrapper.addClass("l-text-disabled");
            }
            else
            {
                this.inputText.removeAttr("readonly");
                this.wrapper.removeClass('l-text-disabled');
            }
        },
        _setWidth: function (value)
        {
            if (value > 20)
            {
                this.wrapper.css({ width: value });
                this.inputText.css({ width: value - 4 });
            }
        },
        _setHeight: function (value)
        {
            if (value > 10)
            {
                this.wrapper.height(value);
                this.inputText.height(value - 2);
            }
        },
        _setValue: function (value)
        {
            if (value != null)
                this.inputText.val(value);
        },
        _setLabel: function (value)
        {
            var g = this, p = this.options;
            if (!g.labelwrapper)
            {
                g.labelwrapper = g.wrapper.wrap('<div class="l-labeltext"></div>').parent();
                var lable = $('<div class="l-text-label" style="float:left;">' + value + ':&nbsp</div>');
                g.labelwrapper.prepend(lable);
                g.wrapper.css('float', 'left');
                if (!p.labelWidth)
                {
                    p.labelWidth = lable.width();
                }
                else
                {
                    g._setLabelWidth(p.labelWidth);
                }
                lable.height(g.wrapper.height());
                if (p.labelAlign)
                {
                    g._setLabelAlign(p.labelAlign);
                }
                g.labelwrapper.append('<br style="clear:both;" />');
                g.labelwrapper.width(p.labelWidth + p.width + 2);
            }
            else
            {
                g.labelwrapper.find(".l-text-label").html(value + ':&nbsp');
            }
        },
        _setLabelWidth: function (value)
        {
            var g = this, p = this.options;
            if (!g.labelwrapper) return;
            g.labelwrapper.find(".l-text-label").width(value);
        },
        _setLabelAlign: function (value)
        {
            var g = this, p = this.options;
            if (!g.labelwrapper) return;
            g.labelwrapper.find(".l-text-label").css('text-align', value);
        }
    });



})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/

(function ($)
{
    //气泡,可以在制定位置显示
    $.ligerTip = function (p)
    {
        return $.ligerui.run.call(null, "ligerTip", arguments);
    };

    //在指定Dom Element右侧显示气泡
    //target：将ligerui对象ID附加上
    $.fn.ligerTip = function (options)
    {
        this.each(function ()
        {
            var p = $.extend({}, $.ligerDefaults.ElementTip, options || {});
            p.target = p.target || this;
            if (p.target.ligeruitipid) return;
            p.x = $(this).offset().left + $(this).width() + (p.distanceX || 0);
            p.y = $(this).offset().top + (p.distanceY || 0);
            p.x = p.x || 0;
            p.y = p.y || 0;
            $.ligerTip(p);
        });
        return $.ligerui.get(this, 'ligeruitipid');
    };
    //关闭指定在Dom Element(附加了ligerui对象ID,属性名"ligeruitipid")显示的气泡
    $.fn.ligerHideTip = function (options)
    {
        return this.each(function ()
        {
            var p = options || {};
            if (p.isLabel == undefined)
            {
                //如果是lable，将查找指定的input，并找到ligerui对象ID
                p.isLabel = this.tagName.toLowerCase() == "label" && $(this).attr("for") != null;
            }
            var target = this;
            if (p.isLabel)
            {
                var forele = $("#" + $(this).attr("for"));
                if (forele.length == 0) return;
                target = forele[0];
            }
            var tipmanager = $.ligerui.managers[target.ligeruitipid];
            if (tipmanager)
            {
                tipmanager.remove();
            }
        });
    };


    $.fn.ligerGetTipManager = function ()
    {
        return $.ligerui.get(this);
    };


    $.ligerDefaults = $.ligerDefaults || {};


    //隐藏气泡
    $.ligerDefaults.HideTip = {};

    //气泡
    $.ligerDefaults.Tip = {
        content: null,
        callback: null,
        width: 150,
        height: null,
        x: 0,
        y: 0,
        appendIdTo: null,       //保存ID到那一个对象(jQuery)(待移除)
        target: null
    };

    //在指定Dom Element右侧显示气泡,通过$.fn.ligerTip调用
    $.ligerDefaults.ElementTip = {
        distanceX: 1,
        distanceY: -3
    };

    $.ligerMethos.Tip = {};

    $.ligerui.controls.Tip = function (options)
    {
        $.ligerui.controls.Tip.base.constructor.call(this, null, options);
    };
    $.ligerui.controls.Tip.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'Tip';
        },
        __idPrev: function ()
        {
            return 'Tip';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Tip;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            var tip = $('<div class="l-verify-tip"><div class="l-verify-tip-corner"></div><div class="l-verify-tip-content"></div></div>');
            g.tip = tip;
            g.tip.attr("id", g.id);
            if (p.content)
            {
                $(".l-verify-tip-content", tip).html(p.content);
                tip.appendTo('body');
            }
            else
            {
                return;
            }
            tip.css({ left: p.x, top: p.y }).show();
            p.width && $("> .l-verify-tip-content", tip).width(p.width - 8);
            p.height && $("> .l-verify-tip-content", tip).width(p.height);
            eee = p.appendIdTo;
            if (p.appendIdTo)
            {
                p.appendIdTo.attr("ligerTipId", g.id);
            }
            if (p.target)
            {
                $(p.target).attr("ligerTipId", g.id);
                p.target.ligeruitipid = g.id;
            }
            p.callback && p.callback(tip);
            g.set(p);
        },
        remove: function ()
        {
            if (this.options.appendIdTo)
            {
                this.options.appendIdTo.removeAttr("ligerTipId");
            }
            if (this.options.target)
            {
                $(this.options.target).removeAttr("ligerTipId");
                this.options.target.ligeruitipid = null;
            }
            this.tip.remove();
        }
    });



})(jQuery);

﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/ 
(function ($)
{

    $.fn.ligerToolBar = function (options)
    {
        return $.ligerui.run.call(this, "ligerToolBar", arguments);
    };

    $.fn.ligerGetToolBarManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetToolBarManager", arguments);
    };

    $.ligerDefaults.ToolBar = {};

    $.ligerMethos.ToolBar = {};

    $.ligerui.controls.ToolBar = function (element, options)
    {
        $.ligerui.controls.ToolBar.base.constructor.call(this, element, options);
    };
    $.ligerui.controls.ToolBar.ligerExtend($.ligerui.core.UIComponent, {
        __getType: function ()
        {
            return 'ToolBar';
        },
        __idPrev: function ()
        {
            return 'ToolBar';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.ToolBar;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.toolBar = $(this.element);
            g.toolBar.addClass("l-toolbar");
            g.set(p);
        },
        _setItems: function (items)
        {
            var g = this;
            $(items).each(function (i, item)
            {
                g.addItem(item);
            });
        },
        addItem: function (item)
        {
            var g = this, p = this.options;
            if (item.line)
            {
                g.toolBar.append('<div class="l-bar-separator"></div>');
                return;
            }
            var ditem = $('<div class="l-toolbar-item l-panel-btn"><span></span><div class="l-panel-btn-l"></div><div class="l-panel-btn-r"></div></div>');
            g.toolBar.append(ditem);
            item.id && ditem.attr("toolbarid", item.id);
            if (item.icon)
            {
                ditem.append("<div class='l-icon l-icon-" + item.icon + "'></div>");
                ditem.addClass("l-toolbar-item-hasicon");
            }
            item.text && $("span:first", ditem).html(item.text);
            item.disable && ditem.addClass("l-toolbar-item-disable");
            item.click && ditem.click(function () { item.click(item); });
            ditem.hover(function ()
            {
                $(this).addClass("l-panel-btn-over");
            }, function ()
            {
                $(this).removeClass("l-panel-btn-over");
            });
        }
    });
})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{
    $.fn.ligerTree = function (options)
    {
        return $.ligerui.run.call(this, "ligerTree", arguments);
    };

    $.fn.ligerGetTreeManager = function ()
    {
        return $.ligerui.run.call(this, "ligerGetTreeManager", arguments);
    };

    $.ligerDefaults.Tree = {
        url: null,
        data: null,
        checkbox: true,
        autoCheckboxEven: true,
        parentIcon: 'folder',
        childIcon: 'leaf',
        textFieldName: 'text',
        attribute: ['id', 'url'],
        treeLine: true,            //是否显示line
        nodeWidth: 90,
        statusName: '__status',
        isLeaf: null,              //是否子节点的判断函数
        single: false,               //是否单选
        onBeforeExpand: function () { },
        onContextmenu: function () { },
        onExpand: function () { },
        onBeforeCollapse: function () { },
        onCollapse: function () { },
        onBeforeSelect: function () { },
        onSelect: function () { },
        onBeforeCancelSelect: function () { },
        onCancelselect: function () { },
        onCheck: function () { },
        onSuccess: function () { },
        onError: function () { },
        onClick: function () { },
        idFieldName: 'id',
        parentIDFieldName: null,
        topParentIDValue: 0,
        onBeforeAppend: function () { },        //加载数据前事件，可以通过return false取消操作
        onAppend: function () { },             //加载数据时事件，对数据进行预处理以后
        onAfterAppend: function () { },         //加载数据完事件
        slide: true,          //是否以动画的形式显示
        iconFieldName: 'icon',
        nodeDraggable: false,             //是否允许拖拽
        nodeDraggingRender: null,
        btnClickToToggleOnly: true     //是否点击展开/收缩 按钮时才有效
    };

    $.ligerui.controls.Tree = function (element, options)
    {
        $.ligerui.controls.Tree.base.constructor.call(this, element, options);
    };

    $.ligerui.controls.Tree.ligerExtend($.ligerui.core.UIComponent, {
        _init: function ()
        {
            $.ligerui.controls.Tree.base._init.call(this);
            var g = this, p = this.options;
            if (p.single) p.autoCheckboxEven = false;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.set(p, true);
            g.tree = $(g.element);
            g.tree.addClass('l-tree');
            g.sysAttribute = ['isexpand', 'ischecked', 'href', 'style'];
            g.loading = $("<div class='l-tree-loading'></div>");
            g.tree.after(g.loading);
            g.data = [];
            g.maxOutlineLevel = 1;
            g.treedataindex = 0;
            g._applyTree();
            g._setTreeEven();

            g.set(p, false);
        },
        _setTreeLine: function (value)
        {
            if (value) this.tree.removeClass("l-tree-noline");
            else this.tree.addClass("l-tree-noline");
        },
        _setUrl: function (url)
        {
            if (url) this.loadData(null, url);
        },
        _setData: function (data)
        {
            if (data) this.append(null, data);
        },
        setData: function (data)
        {
            this.set('data', data);
        },
        getData: function ()
        {
            return this.data;
        },
        //是否包含子节点
        hasChildren: function (treenodedata)
        {
            if (this.options.isLeaf) return this.options.isLeaf(treenodedata);
            return treenodedata.children ? true : false;
        },
        //获取父节点 数据
        getParent: function (treenode, level)
        {
            var g = this;
            treenode = g.getNodeDom(treenode);
            var parentTreeNode = g.getParentTreeItem(treenode, level);
            if (!parentTreeNode) return null;
            var parentIndex = $(parentTreeNode).attr("treedataindex");
            return g._getDataNodeByTreeDataIndex(parentIndex);
        },
        //获取父节点
        getParentTreeItem: function (treenode, level)
        {
            var g = this;
            treenode = g.getNodeDom(treenode);
            var treeitem = $(treenode);
            if (treeitem.parent().hasClass("l-tree"))
                return null;
            if (level == undefined)
            {
                if (treeitem.parent().parent("li").length == 0)
                    return null;
                return treeitem.parent().parent("li")[0];
            }
            var currentLevel = parseInt(treeitem.attr("outlinelevel"));
            var currenttreeitem = treeitem;
            for (var i = currentLevel - 1; i >= level; i--)
            {
                currenttreeitem = currenttreeitem.parent().parent("li");
            }
            return currenttreeitem[0];
        },
        getChecked: function ()
        {
            var g = this, p = this.options;
            if (!this.options.checkbox) return null;
            var nodes = [];
            $(".l-checkbox-checked", g.tree).parent().parent("li").each(function ()
            {
                var treedataindex = parseInt($(this).attr("treedataindex"));
                nodes.push({ target: this, data: g._getDataNodeByTreeDataIndex(g.data, treedataindex) });
            });
            return nodes;
        },
        getSelected: function ()
        {
            var g = this, p = this.options;
            var node = {};
            node.target = $(".l-selected", g.tree).parent("li")[0];
            if (node.target)
            {
                var treedataindex = parseInt($(node.target).attr("treedataindex"));
                node.data = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
                return node;
            }
            return null;
        },
        //升级为父节点级别
        upgrade: function (treeNode)
        {
            var g = this, p = this.options;
            $(".l-note", treeNode).each(function ()
            {
                $(this).removeClass("l-note").addClass("l-expandable-open");
            });
            $(".l-note-last", treeNode).each(function ()
            {
                $(this).removeClass("l-note-last").addClass("l-expandable-open");
            });
            $("." + g._getChildNodeClassName(), treeNode).each(function ()
            {
                $(this)
                        .removeClass(g._getChildNodeClassName())
                        .addClass(g._getParentNodeClassName(true));
            });
        },
        //降级为叶节点级别
        demotion: function (treeNode)
        {
            var g = this, p = this.options;
            if (!treeNode && treeNode[0].tagName.toLowerCase() != 'li') return;
            var islast = $(treeNode).hasClass("l-last");
            $(".l-expandable-open", treeNode).each(function ()
            {
                $(this).removeClass("l-expandable-open")
                        .addClass(islast ? "l-note-last" : "l-note");
            });
            $(".l-expandable-close", treeNode).each(function ()
            {
                $(this).removeClass("l-expandable-close")
                        .addClass(islast ? "l-note-last" : "l-note");
            });
            $("." + g._getParentNodeClassName(true), treeNode).each(function ()
            {
                $(this)
                        .removeClass(g._getParentNodeClassName(true))
                        .addClass(g._getChildNodeClassName());
            });
        },
        collapseAll: function ()
        {
            var g = this, p = this.options;
            $(".l-expandable-open", g.tree).click();
        },
        expandAll: function ()
        {
            var g = this, p = this.options;
            $(".l-expandable-close", g.tree).click();
        },
        loadData: function (node, url, param)
        {
            var g = this, p = this.options;
            g.loading.show();
            var ajaxtype = param ? "post" : "get";
            param = param || [];
            //请求服务器
            $.ajax({
                type: ajaxtype,
                url: url,
                data: param,
                dataType: 'json',
                success: function (data)
                {
                    if (!data) return;
                    g.loading.hide();
                    g.append(node, data);
                    g.trigger('success', [data]);
                },
                error: function (XMLHttpRequest, textStatus, errorThrown)
                {
                    try
                    {
                        g.loading.hide();
                        g.trigger('error', [XMLHttpRequest, textStatus, errorThrown]);
                    }
                    catch (e)
                    {

                    }
                }
            });
        },
        //清空
        clear: function ()
        {
            var g = this, p = this.options;
            //g.tree.html("");
            $("> li", g.tree).each(function () { g.remove(this); });
        },
        //@parm [treeNode] dom节点(li)、节点数据 或者节点 dataindex
        getNodeDom: function (nodeParm)
        {
            var g = this, p = this.options;
            if (nodeParm == null) return nodeParm;
            if (typeof (nodeParm) == "string" || typeof (nodeParm) == "number")
            {
                return $("li[treedataindex=" + nodeParm + "]", g.tree).get(0);
            }
            else if (typeof (nodeParm) == "object" && 'treedataindex' in nodeParm) //nodedata
            {
                return g.getNodeDom(nodeParm['treedataindex']);
            }
            return nodeParm;
        },
        //@parm [treeNode] dom节点(li)、节点数据 或者节点 dataindex
        remove: function (treeNode)
        {
            var g = this, p = this.options;
            treeNode = g.getNodeDom(treeNode);
            var treedataindex = parseInt($(treeNode).attr("treedataindex"));
            var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
            if (treenodedata) g._setTreeDataStatus([treenodedata], 'delete');
            var parentNode = g.getParentTreeItem(treeNode);
            //复选框处理
            if (p.checkbox)
            {
                g._setParentCheckboxStatus($(treeNode));
            }
            $(treeNode).remove();
            g._updateStyle(parentNode ? $("ul:first", parentNode) : g.tree);
        },
        _updateStyle: function (ul)
        {
            var g = this, p = this.options;
            var itmes = $(" > li", ul);
            var treeitemlength = itmes.length;
            if (!treeitemlength) return;
            //遍历设置子节点的样式
            itmes.each(function (i, item)
            {
                if (i == 0 && !$(this).hasClass("l-first"))
                    $(this).addClass("l-first");
                if (i == treeitemlength - 1 && !$(this).hasClass("l-last"))
                    $(this).addClass("l-last");
                if (i == 0 && i == treeitemlength - 1)
                    $(this).addClass("l-onlychild");
                $("> div .l-note,> div .l-note-last", this)
                           .removeClass("l-note l-note-last")
                           .addClass(i == treeitemlength - 1 ? "l-note-last" : "l-note");
                g._setTreeItem(this, { isLast: i == treeitemlength - 1 });
            });
        },
        //@parm [domnode] dom节点(li)、节点数据 或者节点 dataindex
        update: function (domnode, newnodedata)
        {
            var g = this, p = this.options;
            domnode = g.getNodeDom(domnode);
            var treedataindex = parseInt($(domnode).attr("treedataindex"));
            nodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
            for (var attr in newnodedata)
            {
                nodedata[attr] = newnodedata[attr];
                if (attr == p.textFieldName)
                {
                    $("> .l-body > span", domnode).text(newnodedata[attr]);
                }
            }
        },
        //增加节点集合
        //@parm [newdata] 数据集合 Array
        //@parm [parentNode] dom节点(li)、节点数据 或者节点 dataindex
        //@parm [nearNode] 附加到节点的上方/下方(非必填)
        //@parm [isAfter] 附加到节点的下方(非必填)
        append: function (parentNode, newdata, nearNode, isAfter)
        {
            var g = this, p = this.options;
            parentNode = g.getNodeDom(parentNode);
            if (g.trigger('beforeAppend', [parentNode, newdata]) == false) return false;
            if (!newdata || !newdata.length) return false;
            if (p.idFieldName && p.parentIDFieldName)
                newdata = g.arrayToTree(newdata, p.idFieldName, p.parentIDFieldName);
            g._addTreeDataIndexToData(newdata);
            g._setTreeDataStatus(newdata, 'add');
            if (nearNode != null)
            {
                nearNode = g.getNodeDom(nearNode);
            }
            g.trigger('append', [parentNode, newdata])
            g._appendData(parentNode, newdata);
            if (parentNode == null)//增加到根节点
            {
                var gridhtmlarr = g._getTreeHTMLByData(newdata, 1, [], true);
                gridhtmlarr[gridhtmlarr.length - 1] = gridhtmlarr[0] = "";
                if (nearNode != null)
                {
                    $(nearNode)[isAfter ? 'after' : 'before'](gridhtmlarr.join(''));
                    g._updateStyle(parentNode ? $("ul:first", parentNode) : g.tree);
                }
                else
                {
                    //remove last node class
                    if ($("> li:last", g.tree).length > 0)
                        g._setTreeItem($("> li:last", g.tree)[0], { isLast: false });
                    g.tree.append(gridhtmlarr.join(''));
                }
                $(".l-body", g.tree).hover(function ()
                {
                    $(this).addClass("l-over");
                }, function ()
                {
                    $(this).removeClass("l-over");
                });

                g._upadteTreeWidth();
                g.trigger('afterAppend', [parentNode, newdata])
                return;
            }
            var treeitem = $(parentNode);
            var outlineLevel = parseInt(treeitem.attr("outlinelevel"));

            var hasChildren = $("> ul", treeitem).length > 0;
            if (!hasChildren)
            {
                treeitem.append("<ul class='l-children'></ul>");
                //设置为父节点
                g.upgrade(parentNode);
            }
            var isLast = [];
            for (var i = 1; i <= outlineLevel - 1; i++)
            {
                var currentParentTreeItem = $(g.getParentTreeItem(parentNode, i));
                isLast.push(currentParentTreeItem.hasClass("l-last"));
            }
            isLast.push(treeitem.hasClass("l-last"));
            var gridhtmlarr = g._getTreeHTMLByData(newdata, outlineLevel + 1, isLast, true);
            gridhtmlarr[gridhtmlarr.length - 1] = gridhtmlarr[0] = "";
            if (nearNode != null)
            {
                $(nearNode)[isAfter ? 'after' : 'before'](gridhtmlarr.join(''));
                g._updateStyle(parentNode ? $("ul:first", parentNode) : g.tree);
            }
            else
            {
                //remove last node class  
                if ($("> .l-children > li:last", treeitem).length > 0)
                    g._setTreeItem($("> .l-children > li:last", treeitem)[0], { isLast: false });
                $(">.l-children", parentNode).append(gridhtmlarr.join(''));
            }
            g._upadteTreeWidth();
            $(">.l-children .l-body", parentNode).hover(function ()
            {
                $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
            g.trigger('afterAppend', [parentNode, newdata]);
        },
        //@parm [nodeParm] dom节点(li)、节点数据 或者节点 dataindex
        cancelSelect: function (nodeParm)
        {
            var g = this, p = this.options;
            var domNode = g.getNodeDom(nodeParm);
            var treeitem = $(domNode);
            var treedataindex = parseInt(treeitem.attr("treedataindex"));
            var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
            var treeitembody = $(">div:first", treeitem);
            if (p.checkbox)
                $(".l-checkbox", treeitembody).removeClass("l-checkbox-checked").addClass("l-checkbox-unchecked");
            else
                treeitembody.removeClass("l-selected");
            g.trigger('cancelSelect', [{ data: treenodedata, target: treeitem[0]}]);
        },
        //选择节点(参数：条件函数、Dom节点或ID值)
        selectNode: function (selectNodeParm)
        {
            var g = this, p = this.options;
            var clause = null;
            if (typeof (selectNodeParm) == "function")
            {
                clause = selectNodeParm;
            }
            else if (typeof (selectNodeParm) == "object")
            {
                var treeitem = $(selectNodeParm);
                var treedataindex = parseInt(treeitem.attr("treedataindex"));
                var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
                var treeitembody = $(">div:first", treeitem);
                if (p.checkbox)
                    $(".l-checkbox", treeitembody).removeClass("l-checkbox-unchecked").addClass("l-checkbox-checked");
                else
                    treeitembody.addClass("l-selected");

                g.trigger('select', [{ data: treenodedata, target: treeitem[0]}]);
                return;
            }
            else
            {
                clause = function (data)
                {
                    if (!data[p.idFieldName]) return false;
                    return data[p.idFieldName].toString() == selectNodeParm.toString();
                };
            }
            $("li", g.tree).each(function ()
            {
                var treeitem = $(this);
                var treedataindex = parseInt(treeitem.attr("treedataindex"));
                var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
                if (clause(treenodedata, treedataindex))
                {
                    g.selectNode(this);
                }
                else
                {
                    g.cancelSelect(this);
                }
            });
        },
        getTextByID: function (id)
        {
            var g = this, p = this.options;
            var data = g.getDataByID(id);
            if (!data) return null;
            return data[p.textFieldName];
        },
        getDataByID: function (id)
        {
            var g = this, p = this.options;
            var data = null;
            $("li", g.tree).each(function ()
            {
                if (data) return;
                var treeitem = $(this);
                var treedataindex = parseInt(treeitem.attr("treedataindex"));
                var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
                if (treenodedata[p.idFieldName].toString() == id.toString())
                {
                    data = treenodedata;
                }
            });
            return data;
        },
        arrayToTree: function (data, id, pid)      //将ID、ParentID这种数据格式转换为树格式
        {
            if (!data || !data.length) return [];
            var targetData = [];                    //存储数据的容器(返回) 
            var records = {};
            var itemLength = data.length;           //数据集合的个数
            for (var i = 0; i < itemLength; i++)
            {
                var o = data[i];
                records[o[id]] = o;
            }
            for (var i = 0; i < itemLength; i++)
            {
                var currentData = data[i];
                var parentData = records[currentData[pid]];
                if (!parentData)
                {
                    targetData.push(currentData);
                    continue;
                }
                parentData.children = parentData.children || [];
                parentData.children.push(currentData);
            }
            return targetData;
        },
        //根据数据索引获取数据
        _getDataNodeByTreeDataIndex: function (data, treedataindex)
        {
            var g = this, p = this.options;
            for (var i = 0; i < data.length; i++)
            {
                if (data[i].treedataindex == treedataindex)
                    return data[i];
                if (data[i].children)
                {
                    var targetData = g._getDataNodeByTreeDataIndex(data[i].children, treedataindex);
                    if (targetData) return targetData;
                }
            }
            return null;
        },
        //设置数据状态
        _setTreeDataStatus: function (data, status)
        {
            var g = this, p = this.options;
            $(data).each(function ()
            {
                this[p.statusName] = status;
                if (this.children)
                {
                    g._setTreeDataStatus(this.children, status);
                }
            });
        },
        //设置data 索引
        _addTreeDataIndexToData: function (data)
        {
            var g = this, p = this.options;
            $(data).each(function ()
            {
                if (this.treedataindex != undefined) return;
                this.treedataindex = g.treedataindex++;
                if (this.children)
                {
                    g._addTreeDataIndexToData(this.children);
                }
            });
        },
        _addToNodes: function (data)
        {
            var g = this, p = this.options;
            g.nodes = g.nodes || [];
            if ($.inArray(data, g.nodes) == -1)
                g.nodes.push(data);
            if (data.children)
            {
                $(data.children).each(function (i, item)
                {
                    g._addToNodes(item);
                });
            }
        },
        //添加项到g.data
        _appendData: function (treeNode, data)
        {
            var g = this, p = this.options;

            var treedataindex = parseInt($(treeNode).attr("treedataindex"));
            var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
            if (g.treedataindex == undefined) g.treedataindex = 0;
            if (treenodedata && treenodedata.children == undefined) treenodedata.children = [];
            $(data).each(function (i, item)
            {
                if (treenodedata)
                    treenodedata.children[treenodedata.children.length] = item;
                else
                    g.data[g.data.length] = item;
                g._addToNodes(item);
            });
        },
        _setTreeItem: function (treeNode, options)
        {
            var g = this, p = this.options;
            if (!options) return;
            treeNode = g.getNodeDom(treeNode);
            var treeItem = $(treeNode);
            var outlineLevel = parseInt(treeItem.attr("outlinelevel"));
            if (options.isLast != undefined)
            {
                if (options.isLast == true)
                {
                    treeItem.removeClass("l-last").addClass("l-last");
                    $("> div .l-note", treeItem).removeClass("l-note").addClass("l-note-last");
                    $(".l-children li", treeItem)
                            .find(".l-box:eq(" + (outlineLevel - 1) + ")")
                            .removeClass("l-line");
                }
                else if (options.isLast == false)
                {
                    treeItem.removeClass("l-last");
                    $("> div .l-note-last", treeItem).removeClass("l-note-last").addClass("l-note");

                    $(".l-children li", treeItem)
                            .find(".l-box:eq(" + (outlineLevel - 1) + ")")
                            .removeClass("l-line")
                            .addClass("l-line");
                }
            }
        },
        _upadteTreeWidth: function ()
        {
            var g = this, p = this.options;
            var treeWidth = g.maxOutlineLevel * 22;
            if (p.checkbox) treeWidth += 22;
            if (p.parentIcon || p.childIcon) treeWidth += 22;
            treeWidth += p.nodeWidth;
            g.tree.width(treeWidth);
        },
        _getChildNodeClassName: function ()
        {
            var g = this, p = this.options;
            return 'l-tree-icon-' + p.childIcon;
        },
        _getParentNodeClassName: function (isOpen)
        {
            var g = this, p = this.options;
            var nodeclassname = 'l-tree-icon-' + p.parentIcon;
            if (isOpen) nodeclassname += '-open';
            return nodeclassname;
        },
        //根据data生成最终完整的tree html
        _getTreeHTMLByData: function (data, outlineLevel, isLast, isExpand)
        {
            var g = this, p = this.options;
            if (g.maxOutlineLevel < outlineLevel)
                g.maxOutlineLevel = outlineLevel;
            isLast = isLast || [];
            outlineLevel = outlineLevel || 1;
            var treehtmlarr = [];
            if (!isExpand) treehtmlarr.push('<ul class="l-children" style="display:none">');
            else treehtmlarr.push("<ul class='l-children'>");
            for (var i = 0; i < data.length; i++)
            {
                var isFirst = i == 0;
                var isLastCurrent = i == data.length - 1;
                var isExpandCurrent = true;
                var o = data[i];
                if (o.isexpand == false || o.isexpand == "false") isExpandCurrent = false;

                treehtmlarr.push('<li ');
                if (o.treedataindex != undefined)
                    treehtmlarr.push('treedataindex="' + o.treedataindex + '" ');
                if (isExpandCurrent)
                    treehtmlarr.push('isexpand=' + o.isexpand + ' ');
                treehtmlarr.push('outlinelevel=' + outlineLevel + ' ');
                //增加属性支持
                for (var j = 0; j < g.sysAttribute.length; j++)
                {
                    if ($(this).attr(g.sysAttribute[j]))
                        data[dataindex][g.sysAttribute[j]] = $(this).attr(g.sysAttribute[j]);
                }
                for (var j = 0; j < p.attribute.length; j++)
                {
                    if (o[p.attribute[j]])
                        treehtmlarr.push(p.attribute[j] + '="' + o[p.attribute[j]] + '" ');
                }

                //css class
                treehtmlarr.push('class="');
                isFirst && treehtmlarr.push('l-first ');
                isLastCurrent && treehtmlarr.push('l-last ');
                isFirst && isLastCurrent && treehtmlarr.push('l-onlychild ');
                treehtmlarr.push('"');
                treehtmlarr.push('>');
                treehtmlarr.push('<div class="l-body">');
                for (var k = 0; k <= outlineLevel - 2; k++)
                {
                    if (isLast[k]) treehtmlarr.push('<div class="l-box"></div>');
                    else treehtmlarr.push('<div class="l-box l-line"></div>');
                }
                if (g.hasChildren(o))
                {
                    if (isExpandCurrent) treehtmlarr.push('<div class="l-box l-expandable-open"></div>');
                    else treehtmlarr.push('<div class="l-box l-expandable-close"></div>');
                    if (p.checkbox)
                    {
                        if (o.ischecked)
                            treehtmlarr.push('<div class="l-box l-checkbox l-checkbox-checked"></div>');
                        else
                            treehtmlarr.push('<div class="l-box l-checkbox l-checkbox-unchecked"></div>');
                    }
                    if (p.parentIcon)
                    {
                        //node icon
                        treehtmlarr.push('<div class="l-box l-tree-icon ');
                        treehtmlarr.push(g._getParentNodeClassName(p.parentIcon ? true : false) + " ");
                        if (p.iconFieldName && o[p.iconFieldName])
                            treehtmlarr.push('l-tree-icon-none');
                        treehtmlarr.push('">');
                        if (p.iconFieldName && o[p.iconFieldName])
                            treehtmlarr.push('<img src="' + o[p.iconFieldName] + '" />');
                        treehtmlarr.push('</div>');
                    }
                }
                else
                {
                    if (isLastCurrent) treehtmlarr.push('<div class="l-box l-note-last"></div>');
                    else treehtmlarr.push('<div class="l-box l-note"></div>');
                    if (p.checkbox)
                    {
                        if (o.ischecked)
                            treehtmlarr.push('<div class="l-box l-checkbox l-checkbox-checked"></div>');
                        else
                            treehtmlarr.push('<div class="l-box l-checkbox l-checkbox-unchecked"></div>');
                    }
                    if (p.childIcon)
                    {
                        //node icon 
                        treehtmlarr.push('<div class="l-box l-tree-icon ');
                        treehtmlarr.push(g._getChildNodeClassName() + " ");
                        if (p.iconFieldName && o[p.iconFieldName])
                            treehtmlarr.push('l-tree-icon-none');
                        treehtmlarr.push('">');
                        if (p.iconFieldName && o[p.iconFieldName])
                            treehtmlarr.push('<img src="' + o[p.iconFieldName] + '" />');
                        treehtmlarr.push('</div>');
                    }
                }

                treehtmlarr.push('<span>' + o[p.textFieldName] + '</span></div>');
                if (g.hasChildren(o))
                {
                    var isLastNew = [];
                    for (var k = 0; k < isLast.length; k++)
                    {
                        isLastNew.push(isLast[k]);
                    }
                    isLastNew.push(isLastCurrent);
                    treehtmlarr.push(g._getTreeHTMLByData(o.children, outlineLevel + 1, isLastNew, isExpandCurrent).join(''));
                }
                treehtmlarr.push('</li>');
            }
            treehtmlarr.push("</ul>");
            return treehtmlarr;

        },
        //根据简洁的html获取data
        _getDataByTreeHTML: function (treeDom)
        {
            var g = this, p = this.options;
            var data = [];
            $("> li", treeDom).each(function (i, item)
            {
                var dataindex = data.length;
                data[dataindex] =
                        {
                            treedataindex: g.treedataindex++
                        };
                data[dataindex][p.textFieldName] = $("> span,> a", this).html();
                for (var j = 0; j < g.sysAttribute.length; j++)
                {
                    if ($(this).attr(g.sysAttribute[j]))
                        data[dataindex][g.sysAttribute[j]] = $(this).attr(g.sysAttribute[j]);
                }
                for (var j = 0; j < p.attribute.length; j++)
                {
                    if ($(this).attr(p.attribute[j]))
                        data[dataindex][p.attribute[j]] = $(this).attr(p.attribute[j]);
                }
                if ($("> ul", this).length > 0)
                {
                    data[dataindex].children = g._getDataByTreeHTML($("> ul", this));
                }
            });
            return data;
        },
        _applyTree: function ()
        {
            var g = this, p = this.options;
            g.data = g._getDataByTreeHTML(g.tree);
            var gridhtmlarr = g._getTreeHTMLByData(g.data, 1, [], true);
            gridhtmlarr[gridhtmlarr.length - 1] = gridhtmlarr[0] = "";
            g.tree.html(gridhtmlarr.join(''));
            g._upadteTreeWidth();
            $(".l-body", g.tree).hover(function ()
            {
                $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
        },
        _applyTreeEven: function (treeNode)
        {
            var g = this, p = this.options;
            $("> .l-body", treeNode).hover(function ()
            {
                $(this).addClass("l-over");
            }, function ()
            {
                $(this).removeClass("l-over");
            });
        },
        _getSrcElementByEvent: function (e)
        {
            var g = this;
            var obj = (e.target || e.srcElement);
            var tag = obj.tagName.toLowerCase();
            var jobjs = $(obj).parents().add(obj);
            var fn = function (parm)
            {
                for (var i = jobjs.length - 1; i >= 0; i--)
                {
                    if ($(jobjs[i]).hasClass(parm)) return jobjs[i];
                }
                return null;
            };
            if (jobjs.index(this.element) == -1) return { out: true };
            var r = {
                tree: fn("l-tree"),
                node: fn("l-body"),
                checkbox: fn("l-checkbox"),
                icon: fn("l-tree-icon"),
                text: tag == "span"
            };
            if (r.node)
            {
                var treedataindex = parseInt($(r.node).parent().attr("treedataindex"));
                r.data = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
            }
            return r;
        },
        _setTreeEven: function ()
        {
            var g = this, p = this.options;
            if (g.hasBind('contextmenu'))
            {
                g.tree.bind("contextmenu", function (e)
                {
                    var obj = (e.target || e.srcElement);
                    var treeitem = null;
                    if (obj.tagName.toLowerCase() == "a" || obj.tagName.toLowerCase() == "span" || $(obj).hasClass("l-box"))
                        treeitem = $(obj).parent().parent();
                    else if ($(obj).hasClass("l-body"))
                        treeitem = $(obj).parent();
                    else if (obj.tagName.toLowerCase() == "li")
                        treeitem = $(obj);
                    if (!treeitem) return;
                    var treedataindex = parseInt(treeitem.attr("treedataindex"));
                    var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
                    return g.trigger('contextmenu', [{ data: treenodedata, target: treeitem[0] }, e]);
                });
            }
            g.tree.click(function (e)
            {
                var obj = (e.target || e.srcElement);
                var treeitem = null;
                if (obj.tagName.toLowerCase() == "a" || obj.tagName.toLowerCase() == "span" || $(obj).hasClass("l-box"))
                    treeitem = $(obj).parent().parent();
                else if ($(obj).hasClass("l-body"))
                    treeitem = $(obj).parent();
                else
                    treeitem = $(obj);
                if (!treeitem) return;
                var treedataindex = parseInt(treeitem.attr("treedataindex"));
                var treenodedata = g._getDataNodeByTreeDataIndex(g.data, treedataindex);
                var treeitembtn = $("div.l-body:first", treeitem).find("div.l-expandable-open:first,div.l-expandable-close:first");
                var clickOnTreeItemBtn = $(obj).hasClass("l-expandable-open") || $(obj).hasClass("l-expandable-close");
                if (!$(obj).hasClass("l-checkbox") && !clickOnTreeItemBtn)
                {
                    if ($(">div:first", treeitem).hasClass("l-selected"))
                    {
                        if (g.trigger('beforeCancelSelect', [{ data: treenodedata, target: treeitem[0]}]) == false)
                            return false;

                        $(">div:first", treeitem).removeClass("l-selected");
                        g.trigger('cancelSelect', [{ data: treenodedata, target: treeitem[0]}]);
                    }
                    else
                    {
                        if (g.trigger('beforeSelect', [{ data: treenodedata, target: treeitem[0]}]) == false)
                            return false;
                        $(".l-body", g.tree).removeClass("l-selected");
                        $(">div:first", treeitem).addClass("l-selected");
                        g.trigger('select', [{ data: treenodedata, target: treeitem[0]}])
                    }
                }
                //chekcbox even
                if ($(obj).hasClass("l-checkbox"))
                {
                    if (p.autoCheckboxEven)
                    {
                        //状态：未选中
                        if ($(obj).hasClass("l-checkbox-unchecked"))
                        {
                            $(obj).removeClass("l-checkbox-unchecked").addClass("l-checkbox-checked");
                            $(".l-children .l-checkbox", treeitem)
                                    .removeClass("l-checkbox-incomplete l-checkbox-unchecked")
                                    .addClass("l-checkbox-checked");
                            g.trigger('check', [{ data: treenodedata, target: treeitem[0] }, true]);
                        }
                        //状态：选中
                        else if ($(obj).hasClass("l-checkbox-checked"))
                        {
                            $(obj).removeClass("l-checkbox-checked").addClass("l-checkbox-unchecked");
                            $(".l-children .l-checkbox", treeitem)
                                    .removeClass("l-checkbox-incomplete l-checkbox-checked")
                                    .addClass("l-checkbox-unchecked");
                            g.trigger('check', [{ data: treenodedata, target: treeitem[0] }, false]);
                        }
                        //状态：未完全选中
                        else if ($(obj).hasClass("l-checkbox-incomplete"))
                        {
                            $(obj).removeClass("l-checkbox-incomplete").addClass("l-checkbox-checked");
                            $(".l-children .l-checkbox", treeitem)
                                    .removeClass("l-checkbox-incomplete l-checkbox-unchecked")
                                    .addClass("l-checkbox-checked");
                            g.trigger('check', [{ data: treenodedata, target: treeitem[0] }, true]);
                        }
                        g._setParentCheckboxStatus(treeitem);
                    }
                    else
                    {
                        //状态：未选中
                        if ($(obj).hasClass("l-checkbox-unchecked"))
                        {
                            $(obj).removeClass("l-checkbox-unchecked").addClass("l-checkbox-checked");
                            //是否单选
                            if (p.single)
                            {
                                $(".l-checkbox", g.tree).not(obj).removeClass("l-checkbox-checked").addClass("l-checkbox-unchecked");
                            }
                            g.trigger('check', [{ data: treenodedata, target: treeitem[0] }, true]);
                        }
                        //状态：选中
                        else if ($(obj).hasClass("l-checkbox-checked"))
                        {
                            $(obj).removeClass("l-checkbox-checked").addClass("l-checkbox-unchecked");
                            g.trigger('check', [{ data: treenodedata, target: treeitem[0] }, false]);
                        }
                    }
                }
                //状态：已经张开
                else if (treeitembtn.hasClass("l-expandable-open") && (!p.btnClickToToggleOnly || clickOnTreeItemBtn))
                {
                    if (g.trigger('beforeCollapse', [{ data: treenodedata, target: treeitem[0]}]) == false)
                        return false;
                    treeitembtn.removeClass("l-expandable-open").addClass("l-expandable-close");
                    if (p.slide)
                        $("> .l-children", treeitem).slideToggle('fast');
                    else
                        $("> .l-children", treeitem).toggle();
                    $("> div ." + g._getParentNodeClassName(true), treeitem)
                            .removeClass(g._getParentNodeClassName(true))
                            .addClass(g._getParentNodeClassName());
                    g.trigger('collapse', [{ data: treenodedata, target: treeitem[0]}]);
                }
                //状态：没有张开
                else if (treeitembtn.hasClass("l-expandable-close") && (!p.btnClickToToggleOnly || clickOnTreeItemBtn))
                {
                    if (g.trigger('beforeExpand', [{ data: treenodedata, target: treeitem[0]}]) == false)
                        return false;
                    treeitembtn.removeClass("l-expandable-close").addClass("l-expandable-open");
                    var callback = function ()
                    {
                        g.trigger('expand', [{ data: treenodedata, target: treeitem[0]}]);
                    };
                    if (p.slide)
                    {
                        $("> .l-children", treeitem).slideToggle('fast', callback);
                    }
                    else
                    {
                        $("> .l-children", treeitem).toggle();
                        callback();
                    }
                    $("> div ." + g._getParentNodeClassName(), treeitem)
                            .removeClass(g._getParentNodeClassName())
                            .addClass(g._getParentNodeClassName(true));
                }
                g.trigger('click', [{ data: treenodedata, target: treeitem[0]}]);
            });

            //节点拖拽支持
            if ($.fn.ligerDrag && p.nodeDraggable)
            {
                g.nodeDroptip = $("<div class='l-drag-nodedroptip' style='display:none'></div>").appendTo('body');
                g.tree.ligerDrag({ revert: true, animate: false,
                    proxyX: 20, proxyY: 20,
                    proxy: function (draggable, e)
                    {
                        var src = g._getSrcElementByEvent(e);
                        if (src.node)
                        {
                            var content = "dragging";
                            if (p.nodeDraggingRender)
                            {
                                content = p.nodeDraggingRender(draggable.draggingNodes, draggable, g);
                            }
                            else
                            {
                                content = "";
                                var appended = false;
                                for (var i in draggable.draggingNodes)
                                {
                                    var node = draggable.draggingNodes[i];
                                    if (appended) content += ",";
                                    content += node.text;
                                    appended = true;
                                }
                            }
                            var proxy = $("<div class='l-drag-proxy' style='display:none'><div class='l-drop-icon l-drop-no'></div>" + content + "</div>").appendTo('body');
                            return proxy;
                        }
                    },
                    onRevert: function () { return false; },
                    onRendered: function ()
                    {
                        this.set('cursor', 'default');
                        g.children[this.id] = this;
                    },
                    onStartDrag: function (current, e)
                    {
                        if (e.button == 2) return false;
                        this.set('cursor', 'default');
                        var src = g._getSrcElementByEvent(e);
                        if (src.checkbox) return false;
                        if (p.checkbox)
                        {
                            var checked = g.getChecked();
                            this.draggingNodes = [];
                            for (var i in checked)
                            {
                                this.draggingNodes.push(checked[i].data);
                            }
                            if (!this.draggingNodes || !this.draggingNodes.length) return false;
                        }
                        else
                        {
                            this.draggingNodes = [src.data];
                        }
                        this.draggingNode = src.data;
                        this.set('cursor', 'move');
                        g.nodedragging = true;
                        this.validRange = {
                            top: g.tree.offset().top,
                            bottom: g.tree.offset().top + g.tree.height(),
                            left: g.tree.offset().left,
                            right: g.tree.offset().left + g.tree.width()
                        };
                    },
                    onDrag: function (current, e)
                    {
                        var nodedata = this.draggingNode;
                        if (!nodedata) return false;
                        var nodes = this.draggingNodes ? this.draggingNodes : [nodedata];
                        if (g.nodeDropIn == null) g.nodeDropIn = -1;
                        var pageX = e.pageX;
                        var pageY = e.pageY;
                        var visit = false;
                        var validRange = this.validRange;
                        if (pageX < validRange.left || pageX > validRange.right
                            || pageY > validRange.bottom || pageY < validRange.top)
                        {

                            g.nodeDropIn = -1;
                            g.nodeDroptip.hide();
                            this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes l-drop-add").addClass("l-drop-no");
                            return;
                        }
                        for (var i = 0, l = g.nodes.length; i < l; i++)
                        {
                            var nd = g.nodes[i];
                            var treedataindex = nd['treedataindex'];
                            if (nodedata['treedataindex'] == treedataindex) visit = true;
                            if ($.inArray(nd, nodes) != -1) continue;
                            var isAfter = visit ? true : false;
                            if (g.nodeDropIn != -1 && g.nodeDropIn != treedataindex) continue;
                            var jnode = $("li[treedataindex=" + treedataindex + "] div:first", g.tree);
                            var offset = jnode.offset();
                            var range = {
                                top: offset.top,
                                bottom: offset.top + jnode.height(),
                                left: g.tree.offset().left,
                                right: g.tree.offset().left + g.tree.width()
                            };
                            if (pageX > range.left && pageX < range.right && pageY > range.top && pageY < range.bottom)
                            {
                                var lineTop = offset.top;
                                if (isAfter) lineTop += jnode.height();
                                g.nodeDroptip.css({
                                    left: range.left,
                                    top: lineTop,
                                    width: range.right - range.left
                                }).show();
                                g.nodeDropIn = treedataindex;
                                g.nodeDropDir = isAfter ? "bottom" : "top";
                                if (pageY > range.top + 7 && pageY < range.bottom - 7)
                                {
                                    this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no l-drop-yes").addClass("l-drop-add");
                                    g.nodeDroptip.hide();
                                    g.nodeDropInParent = true;
                                }
                                else
                                {
                                    this.proxy.find(".l-drop-icon:first").removeClass("l-drop-no l-drop-add").addClass("l-drop-yes");
                                    g.nodeDroptip.show();
                                    g.nodeDropInParent = false;
                                }
                                break;
                            }
                            else if (g.nodeDropIn != -1)
                            {
                                g.nodeDropIn = -1;
                                g.nodeDropInParent = false;
                                g.nodeDroptip.hide();
                                this.proxy.find(".l-drop-icon:first").removeClass("l-drop-yes  l-drop-add").addClass("l-drop-no");
                            }
                        }
                    },
                    onStopDrag: function (current, e)
                    {
                        var nodes = this.draggingNodes;
                        g.nodedragging = false;
                        if (g.nodeDropIn != -1)
                        {
                            for (var i = 0; i < nodes.length; i++)
                            {
                                var children = nodes[i].children;
                                if (children)
                                {
                                    nodes = $.grep(nodes, function (node, i)
                                    {
                                        var isIn = $.inArray(node, children) == -1;
                                        return isIn;
                                    });
                                }
                            }
                            for (var i in nodes)
                            {
                                var node = nodes[i];
                                if (g.nodeDropInParent)
                                {
                                    g.remove(node);
                                    g.append(g.nodeDropIn, [node]);
                                }
                                else
                                {
                                    g.remove(node);
                                    g.append(g.getParent(g.nodeDropIn), [node], g.nodeDropIn, g.nodeDropDir == "bottom")
                                }
                            }
                            g.nodeDropIn = -1;
                        }
                        g.nodeDroptip.hide();
                        this.set('cursor', 'default');
                    }
                });
            }
        },
        //递归设置父节点的状态
        _setParentCheckboxStatus: function (treeitem)
        {
            var g = this, p = this.options;
            //当前同级别或低级别的节点是否都选中了
            var isCheckedComplete = $(".l-checkbox-unchecked", treeitem.parent()).length == 0;
            //当前同级别或低级别的节点是否都没有选中
            var isCheckedNull = $(".l-checkbox-checked", treeitem.parent()).length == 0;
            if (isCheckedComplete)
            {
                treeitem.parent().prev().find(".l-checkbox")
                                    .removeClass("l-checkbox-unchecked l-checkbox-incomplete")
                                    .addClass("l-checkbox-checked");
            }
            else if (isCheckedNull)
            {
                treeitem.parent().prev().find("> .l-checkbox")
                                    .removeClass("l-checkbox-checked l-checkbox-incomplete")
                                    .addClass("l-checkbox-unchecked");
            }
            else
            {
                treeitem.parent().prev().find("> .l-checkbox")
                                    .removeClass("l-checkbox-unchecked l-checkbox-checked")
                                    .addClass("l-checkbox-incomplete");
            }
            if (treeitem.parent().parent("li").length > 0)
                g._setParentCheckboxStatus(treeitem.parent().parent("li"));
        }
    });


})(jQuery);﻿/**
* jQuery ligerUI 1.1.6
* 
* Author leoxie [ gd_star@163.com ] 
* 
*/
(function ($)
{

    var l = $.ligerui;

    l.windowCount = 0;

    $.ligerWindow = function (options)
    {
        return l.run.call(null, "ligerWindow", arguments, { isStatic: true });
    };

    $.ligerWindow.show = function (p)
    {
        return $.ligerWindow(p);
    };

    $.ligerDefaults.Window = {
        showClose: true,
        showMax: true,
        showToggle: true,
        showMin: true,
        title: 'window',
        load: false,
        onLoaded: null,
        modal: false     //是否模态窗口
    };

    $.ligerMethos.Window = {};

    l.controls.Window = function (options)
    {
        l.controls.Window.base.constructor.call(this, null, options);
    };
    l.controls.Window.ligerExtend(l.core.Win, {
        __getType: function ()
        {
            return 'Window';
        },
        __idPrev: function ()
        {
            return 'Window';
        },
        _extendMethods: function ()
        {
            return $.ligerMethos.Window;
        },
        _render: function ()
        {
            var g = this, p = this.options;
            g.window = $('<div class="l-window"><div class="l-window-header"><div class="l-window-header-buttons"><div class="l-window-toggle"></div><div class="l-window-max"></div><div class="l-window-close"></div><div class="l-clear"></div></div><div class="l-window-header-inner"></div></div><div class="l-window-content"></div></div>');
            g.element = g.window[0];
            g.window.content = $(".l-window-content", g.window);
            g.window.header = $(".l-window-header", g.window);
            g.window.buttons = $(".l-window-header-buttons:first", g.window);
            if (p.url)
            {
                if (p.load)
                {
                    g.window.content.load(p.url, function ()
                    {
                        g.trigger('loaded');
                    });
                    g.window.content.addClass("l-window-content-scroll");
                }
                else
                {
                    var iframe = $("<iframe frameborder='0' src='" + p.url + "'></iframe>");
                    var framename = "ligeruiwindow" + l.windowCount++;
                    if (p.name) framename = p.name;
                    iframe.attr("name", framename).attr("id", framename);
                    p.framename = framename;
                    iframe.appendTo(g.window.content);
                    g.iframe = iframe;
                }
            }
            else if (p.content)
            {
                var content = $("<div>" + p.content + "</div>");
                content.appendTo(g.window.content);
            }
            else if (p.target)
            {
                g.window.content.append(p.target);
                p.target.show();
            }



            this.mask();

            g.active();

            $('body').append(g.window);

            g.set({ width: p.width, height: p.height });
            //位置初始化
            var left = 0;
            var top = 0;
            if (p.left != null) left = p.left;
            else p.left = left = 0.5 * ($(window).width() - g.window.width());
            if (p.top != null) top = p.top;
            else p.top = top = 0.5 * ($(window).height() - g.window.height()) + $(window).scrollTop() - 10;
            if (left < 0) p.left = left = 0;
            if (top < 0) p.top = top = 0;


            g.set(p);

            p.framename && $(">iframe", g.window.content).attr('name', p.framename);
            if (!p.showToggle) $(".l-window-toggle", g.window).remove();
            if (!p.showMax) $(".l-window-max", g.window).remove();
            if (!p.showClose) $(".l-window-close", g.window).remove();

            g._saveStatus();

            //拖动支持
            if ($.fn.ligerDrag)
            {
                g.draggable = g.window.drag = g.window.ligerDrag({ handler: '.l-window-header-inner', onStartDrag: function ()
                {
                    g.active();
                }, onStopDrag: function ()
                {
                    g._saveStatus();
                }, animate: false
                });
            }
            //改变大小支持
            if ($.fn.ligerResizable)
            {
                g.resizeable = g.window.resizable = g.window.ligerResizable({
                    onStartResize: function ()
                    {
                        g.active();
                        $(".l-window-max", g.window).removeClass("l-window-regain");
                    },
                    onStopResize: function (current, e)
                    {
                        var top = 0;
                        var left = 0;
                        if (!isNaN(parseInt(g.window.css('top'))))
                            top = parseInt(g.window.css('top'));
                        if (!isNaN(parseInt(g.window.css('left'))))
                            left = parseInt(g.window.css('left'));
                        if (current.diffTop)
                            g.window.css({ top: top + current.diffTop });
                        if (current.diffLeft)
                            g.window.css({ left: left + current.diffLeft });
                        if (current.newWidth)
                            g.window.width(current.newWidth);
                        if (current.newHeight)
                            g.window.content.height(current.newHeight - 28);

                        g._saveStatus();
                        return false;
                    }
                });
                g.window.append("<div class='l-btn-nw-drop'></div>");
            }
            //设置事件 
            $(".l-window-toggle", g.window).click(function ()
            {
                if ($(this).hasClass("l-window-toggle-close"))
                {
                    g.collapsed = false;
                    $(this).removeClass("l-window-toggle-close");
                } else
                {
                    g.collapsed = true;
                    $(this).addClass("l-window-toggle-close");
                }
                g.window.content.slideToggle();
            }).hover(function ()
            {
                if (g.window.drag)
                    g.window.drag.set('disabled', true);
            }, function ()
            {
                if (g.window.drag)
                    g.window.drag.set('disabled', false);
            });
            $(".l-window-close", g.window).click(function ()
            {
                if (g.trigger('close') == false) return false;
                g.window.hide();
                l.win.removeTask(g);
            }).hover(function ()
            {
                if (g.window.drag)
                    g.window.drag.set('disabled', true);
            }, function ()
            {
                if (g.window.drag)
                    g.window.drag.set('disabled', false);
            });
            $(".l-window-max", g.window).click(function ()
            {
                if ($(this).hasClass("l-window-regain"))
                {
                    if (g.trigger('regain') == false) return false;
                    g.window.width(g._width).css({ left: g._left, top: g._top });
                    g.window.content.height(g._height - 28);
                    $(this).removeClass("l-window-regain");
                }
                else
                {
                    if (g.trigger('max') == false) return false;
                    g.window.width($(window).width() - 2).css({ left: 0, top: 0 });
                    g.window.content.height($(window).height() - 28).show();
                    $(this).addClass("l-window-regain");
                }
            });
        },
        _saveStatus: function ()
        {
            var g = this;
            g._width = g.window.width();
            g._height = g.window.height();
            var top = 0;
            var left = 0;
            if (!isNaN(parseInt(g.window.css('top'))))
                top = parseInt(g.window.css('top'));
            if (!isNaN(parseInt(g.window.css('left'))))
                left = parseInt(g.window.css('left'));
            g._top = top;
            g._left = left;
        },
        min: function ()
        {
            this.window.hide();
            this.minimize = true;
            this.actived = false;
        },
        _setShowMin: function (value)
        {
            var g = this, p = this.options;
            if (value)
            {
                if (!g.winmin)
                {
                    g.winmin = $('<div class="l-window-min"></div>').prependTo(g.window.buttons)
                    .click(function ()
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
        },
        _setLeft: function (value)
        {
            if (value != null)
                this.window.css({ left: value });
        },
        _setTop: function (value)
        {
            if (value != null)
                this.window.css({ top: value });
        },
        _setWidth: function (value)
        {
            if (value > 0)
                this.window.width(value);
        },
        _setHeight: function (value)
        {
            if (value > 28)
                this.window.content.height(value - 28);
        },
        _setTitle: function (value)
        {
            if (value)
                $(".l-window-header-inner", this.window.header).html(value);
        },
        _setUrl: function (url)
        {
            var g = this, p = this.options;
            p.url = url;
            if (p.load)
            {
                g.window.content.html("").load(p.url, function ()
                {
                    if (g.trigger('loaded') == false) return false;
                });
            }
            else if (g.jiframe)
            {
                g.jiframe.attr("src", p.url);
            }
        },
        hide: function ()
        {
            var g = this, p = this.options;
            this.unmask();
            this.window.hide();
        },
        show: function ()
        {
            var g = this, p = this.options;
            this.mask();
            this.window.show();
        },
        remove: function ()
        {
            var g = this, p = this.options;
            this.unmask();
            this.window.remove();
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
                g.set({ width: width, height: height, left: left, top: top });
            }
            g.actived = true;
            g.minimize = false;
            l.win.setFront(g);
            g.show();
            l.win.setFront(this);
        },
        setUrl: function (url)
        {
            return _setUrl(url);
        }
    });

})(jQuery);