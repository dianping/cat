/**
 <b>Submenu hover adjustment</b>. Automatically move up a submenu to fit into screen when some part of it goes beneath window.
 Pass a "true" value as an argument and submenu will have native browser scrollbars when necessary.
*/

ace.sidebar_hoverable = function($, options) {
 if( !('querySelector' in document) || !('removeProperty' in document.documentElement.style) ) return;
 //ignore IE8 & below
 
 var sub_scroll = options.sub_scroll || false;

 //on window resize or sidebar expand/collapse a previously "pulled up" submenu should be reset back to its default position
 //for example if "pulled up" in "responsive-min" mode, in "fullmode" should not remain "pulled up"
 ace.helper.sidebar_hover = {
	reset : function() {
		$sidebar.find('.submenu').each(function() {
			var sub = this, li = this.parentNode;
			if(sub) {
				sub.style.removeProperty('top')
				sub.style.removeProperty('bottom');
				
				var menu_text = li.querySelector('.menu-text');
				if(menu_text) {
					menu_text.style.removeProperty('margin-top')
				}
			}

			if( li.className.lastIndexOf('_up') >= 0 ) {//has .pull_up
				$(li).removeClass('pull_up');
			}
		});
	}
 }


var is_element_pos =
	'getComputedStyle' in window ?
	//el.offsetHeight is used to force redraw and recalculate 'el.style.position' esp for webkit!
	function(el, pos) { el.offsetHeight; return window.getComputedStyle(el).position == pos }
	:
	function(el, pos) { el.offsetHeight; return $(el).css('position') == pos }

 $(window).on('resize.ace_hover', function() {
	navbar_fixed = is_element_pos(navbar, 'fixed');
	ace.helper.sidebar_hover.reset();
 })
 $(document).on('settings.ace.hover', function(e, event_name, event_val) {
	if(event_name == 'sidebar_collapsed') ace.helper.sidebar_hover.reset();
	else if(event_name == 'navbar_fixed') navbar_fixed = event_val;
 })

 ///////////////////////////////////////////////
 var $sidebar = $('.sidebar').eq(0),
	 sidebar = $sidebar.get(0),
	 nav_list = $sidebar.find('.nav-list').get(0);

 var $navbar = $('.navbar').eq(0),
	navbar = $navbar.get(0),
	horizontal = $sidebar.hasClass('h-sidebar'),

	navbar_fixed = $navbar.css('position') == 'fixed';

 $sidebar.find('.submenu').parent().addClass('hsub');//add .hsub (has-sub) class

 var sub_scroll = (sub_scroll && ace.vars['touch']) || false;

 //some mobile browsers don't have mouseenter
 $sidebar.on('mouseenter.ace_hover', '.nav-list li.hsub', function (e) {
	//ignore if collapsible mode (mobile view .navbar-collapse) so it doesn't trigger submenu movements
	//or return if horizontal but not mobile_view (style 1&3)
	if( ace.vars['collapsible'] || (horizontal && !ace.vars['mobile_view']) ) return;

	var sub = this.querySelector('.submenu');
	if(sub) {
		//try to move/adjust submenu if the parent is a li.hover
		if( ace.helper.hasClass(this, 'hover') && is_element_pos(sub, 'absolute') ) {//for example in small device .hover > .submenu may not be absolute anymore!
			adjust_submenu.call(this, sub);
		}
		//or if submenu is minimized
		else if( this.parentNode == nav_list && ace.vars['minimized'] ) {
			
			adjust_submenu.call(this, sub);
		}
	}
 })


 var $diff = 50;
 function adjust_submenu(sub) {
	var $sub = $(sub);
	sub.style.removeProperty('top')
	sub.style.removeProperty('bottom');

	var menu_text = null
	if( ace.vars['minimized'] && (menu_text = sub.parentNode.querySelector('.menu-text')) ) {
		//2nd level items don't have .menu-text
		menu_text.style.removeProperty('margin-top')
	}

	var off = $sub.offset();
	var scroll = ace.helper.scrollTop();
	var pull_up = false;

	var $scroll = scroll
	if( navbar_fixed ) {
		$scroll += navbar.clientHeight + 1;
		//let's avoid our submenu from going below navbar
		//because of chrome z-index stacking issue and firefox's normal .submenu over fixed .navbar flicker issue
	}


	var sub_h = sub.scrollHeight;
	if(menu_text) {
		sub_h += 40;
		off.top -= 40;
	}
	var sub_bottom = parseInt(off.top + sub_h)

	var diff
	var winh = window.innerHeight;
	//if the bottom of menu is going to go below visible window
	if( (diff = sub_bottom - (winh + scroll - 50)) > 0 ) {

		//if it needs to be moved top a lot! use bottom unless it makes it go out of window top
		if(sub_h - diff < $diff && off.top - diff > $scroll ) {
			sub.style.top = 'auto';
			sub.style.bottom = '-10px';

			if( menu_text ) {
				//menu_text.style.marginTop = -(sub_h - 10)+'px';
				menu_text.style.marginTop = -(sub_h - 50)+'px';// -10 - 40 for the above  extra 40
				pull_up = true;
			}
		}
		else {
			//when top of menu goes out of browser window's top or below fixed navbar
			if( off.top - diff < $scroll ) {
				diff = off.top - $scroll;
			}

			//when bottom of menu goes above bottom of parent LI
			/** else */
			if(sub_bottom - diff < off.top + $diff) {
				diff -= $diff;
			}

			var at_least = menu_text ? 40 : 20;//it we are going to move up less than at_least, then ignore
			if( diff > at_least ) {
				sub.style.top = -(diff) + 'px';
				if( menu_text ) {
					menu_text.style.marginTop = -(diff) + 'px';
					pull_up = true;
				}
			}
		}
	}


	

	//pull_up means, pull the menu up a little bit, and some styling may need to change
	var pos = this.className.lastIndexOf('pull_up');//pull_up
	if (pull_up) {
		if (pos == -1)
			this.className = this.className + ' pull_up';

		if(sub_scroll) {
			var h = sub_h + off.top - diff;
			if( winh - h < 0 ) {
				$(sub)
				.css({'max-height': (sub_h + winh - h - 50), 'overflow-x': 'hidden', 'overflow-y': 'scroll'})
				.on('mousewheel.sub_scroll DOMMouseScroll.sub_scroll ace_drag.sub_scroll', function(event) {
					event.stopPropagation();
				});
			}
			else {
				$(sub)
				.css({'max-height': '', 'overflow-x': '', 'overflow-y': ''})
				.off('mousewheel.sub_scroll DOMMouseScroll.sub_scroll ace_drag.sub_scroll', function(event) {
					event.stopPropagation();
				});
			}
		}

	} else {
		if (pos >= 0)
			this.className = this.className.replace(/(^|\s)pull_up($|\s)/ , ' ');
	}


	//again force redraw for safari!
	if( ace.vars['safari'] ) {
		ace.helper.redraw(sub)
	}

 }
 
}


