/**
 <b>Custom color picker element</b>. Converts html select elements to a dropdown color picker.
*/
(function($ , undefined) {
	var Ace_Colorpicker = function(element, option) {
		var options = $.extend({}, $.fn.ace_colorpicker.defaults, option);

		var $element = $(element);
		var color_list = '';
		var color_selected = '';
		var selection = null;
		var color_array = [];
		
		$element.addClass('hide').find('option').each(function() {
			var $class = 'colorpick-btn';
			var color = this.value.replace(/[^\w\s,#\(\)\.]/g, '');
			if(this.value != color) this.value = color;
			if(this.selected) {
				$class += ' selected';
				color_selected = color;
			}
			color_array.push(color)
			color_list += '<li><a class="'+$class+'" href="#" style="background-color:'+color+';" data-color="'+color+'"></a></li>';
		}).
		end()
		.on('change.color', function(){
			$element.next().find('.btn-colorpicker').css('background-color', this.value);
		})
		.after('<div class="dropdown dropdown-colorpicker">\
		<a data-toggle="dropdown" class="dropdown-toggle" '+(options.auto_pos ? 'data-position="auto"' : '')+' href="#"><span class="btn-colorpicker" style="background-color:'+color_selected+'"></span></a><ul class="dropdown-menu'+(options.caret? ' dropdown-caret' : '')+(options.pull_right ? ' dropdown-menu-right' : '')+'">'+color_list+'</ul></div>')

		
		var dropdown = $element.next().find('.dropdown-menu')
		dropdown.on(ace.click_event, function(e) {
			var a = $(e.target);
			if(!a.is('.colorpick-btn')) return false;

			if(selection) selection.removeClass('selected');
			selection = a;
			selection.addClass('selected');
			var color = selection.data('color');

			$element.val(color).trigger('change');

			e.preventDefault();
			return true;//to hide dropdown
		})
		selection = $element.next().find('a.selected');

		this.pick = function(index, insert) {
			if(typeof index === 'number') {
				if(index >= color_array.length) return;
				element.selectedIndex = index;
				dropdown.find('a:eq('+index+')').trigger(ace.click_event);
			}
			else if(typeof index === 'string') {
				var color = index.replace(/[^\w\s,#\(\)\.]/g, '');
				index = color_array.indexOf(color);
				//add this color if it doesn't exist
				if(index == -1 && insert === true) {
					color_array.push(color);
					
					$('<option />')
					.appendTo($element)
					.val(color);
					
					$('<li><a class="colorpick-btn" href="#"></a></li>')
					.appendTo(dropdown)
					.find('a')
					.css('background-color', color)
					.data('color', color);
					
					index = color_array.length - 1;
				}
				if(index == -1) return;
				dropdown.find('a:eq('+index+')').trigger(ace.click_event);
			}
		}

		this.destroy = function() {
			$element.removeClass('hide').off('change.color')
			.next().remove();
			color_array = [];
		}
	}


	$.fn.ace_colorpicker = function(option, value) {
		var retval;

		var $set = this.each(function () {
			var $this = $(this);
			var data = $this.data('ace_colorpicker');
			var options = typeof option === 'object' && option;

			if (!data) $this.data('ace_colorpicker', (data = new Ace_Colorpicker(this, options)));
			if (typeof option === 'string') retval = data[option](value);
		});

		return (retval === undefined) ? $set : retval;
	}
	
	$.fn.ace_colorpicker.defaults = {
		'pull_right' : false,
		'caret': true,
		'auto_pos': true
	}
	
})(window.jQuery);