/**
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


})(jQuery);