/**
 <b>Treeview</b>. A wrapper for FuelUX treeview element.
 It's just a wrapper so you still need to include FuelUX treeview script first.
*/
(function($ , undefined) {
	var $options = {
		'open-icon' : ace.vars['icon'] + 'fa fa-folder-open',
		'close-icon' : ace.vars['icon'] + 'fa fa-folder',
		'selectable' : true,
		'selected-icon' : ace.vars['icon'] + 'fa fa-check',
		'unselected-icon' : ace.vars['icon'] + 'fa fa-times'
	}

	$.fn.ace_tree = function(options) {
		$options = $.extend({}, $options, options)
		this.each(function() {
			var $this = $(this);
			$this.html('<div class="tree-folder" style="display:none;">\
				<div class="tree-folder-header">\
					<i class="'+ ace.vars['icon'] + $options['close-icon']+'"></i>\
					<div class="tree-folder-name"></div>\
				</div>\
				<div class="tree-folder-content"></div>\
				<div class="tree-loader" style="display:none"></div>\
			</div>\
			<div class="tree-item" style="display:none;">\
				'+($options['unselected-icon'] == null ? '' : '<i class="'+ ace.vars['icon'] + $options['unselected-icon']+'"></i>')+'\
				<div class="tree-item-name"></div>\
			</div>');
			$this.addClass($options['selectable'] == true ? 'tree-selectable' : 'tree-unselectable');
			
			$this.tree($options);
		});

		return this;
	}

})(window.jQuery);
