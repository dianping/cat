/**
 <b>Scrollbars for sidebar</b>. This approach can be used on fixed or normal sidebar.
 It uses <u>"overflow:hidden"</u> so you can't use <u>.hover</u> submenus and it will be disabled when sidebar is minimized.
 It may also be marginally (negligibily) faster especially when resizing browser window.
 Needs some work! Has a few issues!
*/

ace.sidebar_scrollable = function($ , options) {
	if( !$.fn.ace_scroll ) return;

	var old_safari = ace.vars['safari'] && navigator.userAgent.match(/version\/[1-5]/i)
	//NOTE
	//Safari on windows has not been updated for a long time.
	//And it has a problem when sidebar is fixed&scrollable and there is a CSS3 animation inside page content.
	//Very probably windows users of safari have migrated to another browser by now!


	var $sidebar = $('.sidebar'),
		$navbar = $('.navbar'),
		$nav = $sidebar.find('.nav-list'),
		$toggle = $sidebar.find('.sidebar-toggle'),
		$shortcuts = $sidebar.find('.sidebar-shortcuts'),
		$window = $(window),

		sidebar = $sidebar.get(0),
		nav = $nav.get(0);

		if(!sidebar || !nav) return;

	var submenu_hover = function() {
		return sidebar.first('li.hover > .submenu').css('position') == 'absolute'
	}

	
	var scroll_div = null,
		scroll_content = null,
		scroll_content_div = null,
		bar = null,
		ace_scroll = null;

	var is_scrolling = false,
		_initiated = false;
		
	var scroll_to_active = options.scroll_to_active || false,
		include_shortcuts = options.include_shortcuts || false,
		include_toggle = options.include_toggle || false,
		only_if_fixed = options.only_if_fixed && true;
		
	var is_sidebar_fixed =
	'getComputedStyle' in window ?
	//sidebar.offsetHeight is used to force redraw and recalculate 'sidebar.style.position' esp for webkit!
	function() { sidebar.offsetHeight; return window.getComputedStyle(sidebar).position == 'fixed' }
	:
	function() { sidebar.offsetHeight; return $sidebar.css('position') == 'fixed' }
	//sometimes when navbar is fixed, sidebar automatically becomes fixed without needing ".sidebar-fixed" class
	//currently when mobile_style == 1

	var $avail_height, $content_height;
	var sidebar_fixed = is_sidebar_fixed(),
		horizontal = $sidebar.hasClass('h-sidebar');


	var scrollbars = ace.helper.sidebar_scroll = {
		available_height: function() {
			//available window space
			var offset = $nav.parent().offset();//because `$nav.offset()` considers the "scrolled top" amount as well
			if(sidebar_fixed) offset.top -= ace.helper.scrollTop();

			return $window.innerHeight() - offset.top - ( include_toggle ? 0 : $toggle.outerHeight() );
		},
		content_height: function() {
			return nav.scrollHeight;
		},
		initiate: function(on_page_load) {
			if( _initiated ) return;
			if( (only_if_fixed && !sidebar_fixed) || submenu_hover() ) return;//eligible??
			//return if we want scrollbars only on "fixed" sidebar and sidebar is not "fixed" yet!

			//initiate once
			$nav.wrap('<div />');
			if(include_shortcuts) $nav.parent().prepend($shortcuts);
			if(include_toggle) $nav.parent().append($toggle);

			scroll_div = $nav.parent()
			.ace_scroll({
				size: scrollbars.available_height(),
				reset: true,
				mouseWheelLock: true,
				hoverReset: false
			})
			.closest('.ace-scroll').addClass('nav-scroll');
			
			ace_scroll = scroll_div.data('ace_scroll');

			scroll_content = scroll_div.find('.scroll-content').eq(0);

			if(old_safari && !include_toggle) {
				var toggle = $toggle.get(0);
				if(toggle) scroll_content.on('scroll.safari', function() {
					ace.helper.redraw(toggle);
				});
			}

			_initiated = true;

			//if the active item is not visible, scroll down so that it becomes visible
			//only the first time, on page load
			if(on_page_load == true) {
				scrollbars.reset();//try resetting at first

				if( scroll_to_active && ace_scroll && ace_scroll.is_active() ) {
					try {
						//sometimes there's no active item or not 'offsetTop' property
						var $active;

						var nav_list = $sidebar.find('.nav-list')
						if(ace.vars['minimized'] && !ace.vars['collapsible']) {
							$active = nav_list.find('> .active')
						}
						else {
							$active = $nav.find('> .active.hover')
							if($active.length == 0)	$active = $nav.find('.active:not(.open)')
						}

						var top = $active.outerHeight();

						nav_list = nav_list.get(0);
						var active = $active.get(0);
						while(active != nav_list) {
							top += active.offsetTop;
							active = active.parentNode;
						}

						var scroll_amount = top - scroll_div.height();
						if(scroll_amount > 0) {
							scroll_content.scrollTop(scroll_amount);
						}
					}catch(e){}

				}
				scroll_to_active = false;
			}
		},
		
		reset: function() {
			if( (only_if_fixed && !sidebar_fixed) || submenu_hover() ) {
				scrollbars.disable();
				return;//eligible??
			}
			//return if we want scrollbars only on "fixed" sidebar and sidebar is not "fixed" yet!

			if( !_initiated ) scrollbars.initiate();
			//initiate scrollbars if not yet
			
			$sidebar.addClass('sidebar-scroll');
			

			//enable if:
			//menu is not minimized
			//menu is not collapsible mode (responsive navbar-collapse mode which has default browser scroller)
			//menu is not horizontal or horizontal but mobile view (which is not navbar-collapse)
			//and available height is less than nav's height			
			var enable_scroll = !ace.vars['minimized'] && !ace.vars['collapsible']
								&& (!horizontal || (horizontal && ace.vars['mobile_view']))
								&& ($avail_height = scrollbars.available_height()) < ($content_height = nav.parentNode.scrollHeight);

			is_scrolling = true;
			if( enable_scroll && ace_scroll ) {
				//scroll_content_div.css({height: $content_height, width: 8});
				//scroll_div.prev().css({'max-height' : $avail_height})
				ace_scroll.update({size: $avail_height}).enable().reset();
			}
			if( !enable_scroll || !ace_scroll.is_active() ) {
				if(is_scrolling) scrollbars.disable();
			}
			//return is_scrolling;
		},
		disable : function() {
			is_scrolling = false;
			if(ace_scroll) ace_scroll.disable();
			
			$sidebar.removeClass('sidebar-scroll');
		},
		prehide: function(height_change) {
			if( !is_scrolling || ace.vars['minimized'] ) return;

			if(scrollbars.content_height() + height_change < scrollbars.available_height()) {
				scrollbars.disable();
			}
			else if(height_change < 0) {
				//if content height is decreasing
				//let's move nav down while a submenu is being hidden
				var scroll_top = scroll_content.scrollTop() + height_change
				if(scroll_top < 0) return;

				scroll_content.scrollTop(scroll_top);
			}
		}
	}
	scrollbars.initiate(true);//true = on_page_load

	//reset on document and window changes
	$(document).on('settings.ace.scroll', function(ev, event_name, event_val){
		if( event_name == 'sidebar_collapsed' ) {
			if(event_val == true) scrollbars.disable();//disable scroll if collapsed
			else scrollbars.reset();
		}
		else if( event_name === 'sidebar_fixed' || event_name === 'navbar_fixed' ) {
			//sidebar_fixed = event_val;
			sidebar_fixed = is_sidebar_fixed()
			
			if(sidebar_fixed && !is_scrolling) {
				scrollbars.reset();
			}
			else if(!sidebar_fixed && only_if_fixed) {
				scrollbars.disable();
			}
		}
	});
	$window.on('resize.ace.scroll', function(){
		sidebar_fixed = is_sidebar_fixed()
		scrollbars.reset();
	})
	

	//change scrollbar size after a submenu is hidden/shown
	//but don't change if sidebar is minimized
	$sidebar.on('hidden.ace.submenu shown.ace.submenu', '.submenu', function(e) {
		e.stopPropagation();

		if(!ace.vars['minimized']) {
			//webkit has a little bit of a glitch!!!
			if(ace.vars['webkit']) setTimeout(function() { scrollbars.reset() } , 0);
			else scrollbars.reset();
		}
	});
}