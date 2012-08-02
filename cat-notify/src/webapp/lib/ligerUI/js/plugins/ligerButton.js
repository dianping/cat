/**
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


 

})(jQuery);