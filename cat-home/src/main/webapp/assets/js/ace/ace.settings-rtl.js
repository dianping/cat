/**
<b>RTL</b> (right-to-left direction for Arabic, Hebrew, Persian languages).
It's good for demo only.
You should hard code RTL-specific changes inside your HTML/server-side code.
Dynamically switching to RTL using Javascript is not a good idea.
Please refer to documentation for more info.
*/


ace.settings_rtl = function($) {
 //Switching to RTL (right to left) Mode
 $('#ace-settings-rtl').removeAttr('checked').on('click', function(){
	ace.switch_direction(jQuery);
 });
}

//>>> you should hard code changes inside HTML for RTL direction
//you shouldn't use this function to switch direction
//this is only for dynamically switching for demonstration
//take a look at this function to see what changes should be made
//also take a look at docs for some tips
ace.switch_direction = function($) {
	var $body = $(document.body);
	$body
	.toggleClass('rtl')
	//toggle pull-right class on dropdown-menu
	.find('.dropdown-menu:not(.datepicker-dropdown,.colorpicker)').toggleClass('dropdown-menu-right')
	.end()
	//swap pull-left & pull-right
	.find('.pull-right:not(.dropdown-menu,blockquote,.profile-skills .pull-right)').removeClass('pull-right').addClass('tmp-rtl-pull-right')
	.end()
	.find('.pull-left:not(.dropdown-submenu,.profile-skills .pull-left)').removeClass('pull-left').addClass('pull-right')
	.end()
	.find('.tmp-rtl-pull-right').removeClass('tmp-rtl-pull-right').addClass('pull-left')
	.end()
	
	.find('.chosen-select').toggleClass('chosen-rtl').next().toggleClass('chosen-rtl');
	

	function swap_classes(class1, class2) {
		$body
		 .find('.'+class1).removeClass(class1).addClass('tmp-rtl-'+class1)
		 .end()
		 .find('.'+class2).removeClass(class2).addClass(class1)
		 .end()
		 .find('.tmp-rtl-'+class1).removeClass('tmp-rtl-'+class1).addClass(class2)
	}

	swap_classes('align-left', 'align-right');
	swap_classes('no-padding-left', 'no-padding-right');
	swap_classes('arrowed', 'arrowed-right');
	swap_classes('arrowed-in', 'arrowed-in-right');
	swap_classes('tabs-left', 'tabs-right');
	swap_classes('messagebar-item-left', 'messagebar-item-right');//for inbox page
	
	//mirror all icons and attributes that have a "fa-*-right|left" attrobute
	$('.fa').each(function() {
		if(this.className.match(/ui-icon/) || $(this).closest('.fc-button').length > 0) return;
		//skip mirroring icons of plugins that have built in RTL support

		var l = this.attributes.length;
		for(var i = 0 ; i < l ; i++) {
			var val = this.attributes[i].value;
			if(val.match(/fa\-(?:[\w\-]+)\-left/)) 
				this.attributes[i].value = val.replace(/fa\-([\w\-]+)\-(left)/i , 'fa-$1-right')
			 else if(val.match(/fa\-(?:[\w\-]+)\-right/)) 
				this.attributes[i].value = val.replace(/fa\-([\w\-]+)\-(right)/i , 'fa-$1-left')
		}
	});
	
	//browsers are incosistent with horizontal scroll and RTL
	//so let's make our scrollbars LTR and wrap the content inside RTL
	var rtl = $body.hasClass('rtl');
	if(rtl)	{
		$('.scroll-hz').addClass('make-ltr')
		.find('.scroll-content')
		.wrapInner('<div class="make-rtl" />');
	}
	else {
		//remove the wrap
		$('.scroll-hz').removeClass('make-ltr')
		.find('.make-rtl').children().unwrap();
	}
	if($.fn.ace_scroll) $('.scroll-hz').ace_scroll('reset') //to reset scrollLeft

	//redraw the traffic pie chart on homepage with a different parameter
	try {
		var placeholder = $('#piechart-placeholder');
		if(placeholder.length > 0) {
			var pos = $(document.body).hasClass('rtl') ? 'nw' : 'ne';//draw on north-west or north-east?
			placeholder.data('draw').call(placeholder.get(0) , placeholder, placeholder.data('chart'), pos);
		}
	}catch(e) {}
	
	
	//force redraw(because of webkit)
	/**setTimeout(function() {
		ace.helper.redraw(document.body);
		ace.helper.redraw($('.main-content').get(0));
	}, 10);*/
}
