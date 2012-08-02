/**
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
})(jQuery);