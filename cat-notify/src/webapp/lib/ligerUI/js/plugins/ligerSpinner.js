/**
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


})(jQuery);