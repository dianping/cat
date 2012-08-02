/**
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

})(jQuery);