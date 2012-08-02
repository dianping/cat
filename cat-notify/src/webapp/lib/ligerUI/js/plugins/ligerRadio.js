/**
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


})(jQuery);