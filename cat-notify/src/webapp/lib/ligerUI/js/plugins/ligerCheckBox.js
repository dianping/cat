/**
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
})(jQuery);