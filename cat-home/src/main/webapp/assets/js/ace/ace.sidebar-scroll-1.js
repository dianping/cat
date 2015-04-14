/**
 <b>Scrollbars for sidebar</b>. This approach can <span class="text-danger">only</span> be used on <u>fixed</u> sidebar.
 It doesn't use <u>"overflow:hidden"</u> CSS property and therefore can be used with <u>.hover</u> submenus and minimized sidebar.
 Except when in mobile view and menu toggle button is not in the navbar.
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
		smooth_scroll = options.smooth_scroll || false,
		scrollbars_outside = options.outside || false,
		only_if_fixed = true;
		
		
		
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
			if( !sidebar_fixed ) return;//eligible??
			//return if we want scrollbars only on "fixed" sidebar and sidebar is not "fixed" yet!

			//initiate once
			$nav.wrap('<div style="position: relative;" />');
			$nav.after('<div><div></div></div>');

			$nav.wrap('<div class="nav-wrap" />');
			if(!include_toggle) $toggle.css({'z-index': 1});
			if(!include_shortcuts) $shortcuts.css({'z-index': 99});

			scroll_div = $nav.parent().next()
			.ace_scroll({
				size: scrollbars.available_height(),
				reset: true,
				mouseWheelLock: true,
				hoverReset: false,
				dragEvent: true,
				touchDrag: false//disable touch drag event on scrollbars, we'll add a custom one later
			})
			.closest('.ace-scroll').addClass('nav-scroll');
			
			ace_scroll = scroll_div.data('ace_scroll');

			scroll_content = scroll_div.find('.scroll-content').eq(0);
			scroll_content_div = scroll_content.find(' > div').eq(0);
			bar = scroll_div.find('.scroll-bar').eq(0);

			if(include_shortcuts) {
				$nav.parent().prepend($shortcuts).wrapInner('<div />');
				$nav = $nav.parent();
			}
			if(include_toggle) {
				$nav.append($toggle);
				$nav.closest('.nav-wrap').addClass('nav-wrap-t');//it just helps to remove toggle button's top border and restore li:last-child's bottom border
			}

			$nav.css({position: 'relative'});
			if( scrollbars_outside === true ) scroll_div.addClass('scrollout');
			
			nav = $nav.get(0);
			nav.style.top = 0;
			scroll_content.on('scroll.nav', function() {
				nav.style.top = (-1 * this.scrollTop) + 'px';
			});
			$nav.on('mousewheel.ace_scroll DOMMouseScroll.ace_scroll', function(event){
				//transfer $nav's mousewheel event to scrollbars
				return scroll_div.trigger(event);
			});


			/**$(document.body).on('touchmove.nav', function(event) {
				if( is_scrolling && $.contains(sidebar, event.target) ) {
					event.preventDefault();
					return false;
				}
			});*/

			//you can also use swipe event in a similar way //swipe.nav
			var content = scroll_content.get(0);
			$nav.on('ace_drag.nav', function(event) {
				if( !is_scrolling ) {
					event.retval.cancel = true;
					return;
				}

				if(event.direction == 'up' || event.direction == 'down') {
					
					ace_scroll.move_bar(true);
					
					var distance = event.dy;
					
					distance = parseInt(Math.min($avail_height, distance))
					if(Math.abs(distance) > 2) distance = distance * 2;
					
					if(distance != 0) {
						content.scrollTop = content.scrollTop + distance;
						nav.style.top = (-1 * content.scrollTop) + 'px';
					}
				}
			});
			

			//for drag only
			if(smooth_scroll) {
				$nav
				.on('touchstart.nav MSPointerDown.nav pointerdown.nav', function(event) {
					$nav.css('transition-property', 'none');
					bar.css('transition-property', 'none');
				})
				.on('touchend.nav touchcancel.nav MSPointerUp.nav MSPointerCancel.nav pointerup.nav pointercancel.nav', function(event) {
					$nav.css('transition-property', 'top');
					bar.css('transition-property', 'top');
				});
			}
			
			

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

				if( scroll_to_active ) {
					scrollbars.scroll_to_active();
				}
				scroll_to_active = false;
			}
			
			
			
			if( typeof smooth_scroll === 'number' && smooth_scroll > 0) {
				$nav.css({'transition-property': 'top', 'transition-duration': (smooth_scroll / 1000).toFixed(2)+'s'})
				bar.css({'transition-property': 'top', 'transition-duration': (smooth_scroll / 1500).toFixed(2)+'s'})
				
				scroll_div
				.on('drag.start', function(e) {
					e.stopPropagation();
					$nav.css('transition-property', 'none')
				})
				.on('drag.end', function(e) {
					e.stopPropagation();
					$nav.css('transition-property', 'top')
				});
			}
			
			if(ace.vars['android']) {
				//force hide address bar, because its changes don't trigger window resize and become kinda ugly
				var val = ace.helper.scrollTop();
				if(val < 2) {
					window.scrollTo( val, 0 );
					setTimeout( function() {
						scrollbars.reset();
					}, 20 );
				}
				
				var last_height = ace.helper.winHeight() , new_height;
				$(window).on('scroll.ace_scroll', function() {
					if(is_scrolling && ace_scroll.is_active()) {
						new_height = ace.helper.winHeight();
						if(new_height != last_height) {
							last_height = new_height;
							scrollbars.reset();
						}
					}
				});
			}

		},
		
		scroll_to_active: function() {
			if( !ace_scroll || !ace_scroll.is_active() ) return;
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
					nav.style.top = -scroll_amount + 'px';
					scroll_content.scrollTop(scroll_amount);
				}
			}catch(e){}
		},
		
		reset: function() {
			if( !sidebar_fixed ) {
				scrollbars.disable();
				return;//eligible??
			}
			//return if we want scrollbars only on "fixed" sidebar and sidebar is not "fixed" yet!

			if( !_initiated ) scrollbars.initiate();
			//initiate scrollbars if not yet
			

			

			//enable if:
			//menu is not collapsible mode (responsive navbar-collapse mode which has default browser scrollbar)
			//menu is not horizontal or horizontal but mobile view (which is not navbar-collapse)
			//and available height is less than nav's height
			
			var enable_scroll = !ace.vars['collapsible']
								&& (!horizontal || (horizontal && ace.vars['mobile_view']))
								&& ($avail_height = scrollbars.available_height()) < ($content_height = nav.scrollHeight);

			is_scrolling = true;
			if( enable_scroll ) {
				scroll_content_div.css({height: $content_height, width: 8});
				scroll_div.prev().css({'max-height' : $avail_height})
				ace_scroll.update({size: $avail_height}).enable().reset();
			}
			if( !enable_scroll || !ace_scroll.is_active() ) {
				if(is_scrolling) scrollbars.disable();
			}
			else {
				$sidebar.addClass('sidebar-scroll');
			}

			//return is_scrolling;
		},
		disable : function() {
			is_scrolling = false;
			if(scroll_div) {
				scroll_div.css({'height' : '', 'max-height' : ''});
				scroll_content_div.css({height: '', width: ''});//otherwise it will have height and takes up some space even when invisible
				scroll_div.prev().css({'max-height' : ''})
				ace_scroll.disable();
			}

			if(parseInt(nav.style.top) < 0 && smooth_scroll && ace.vars['transition']) {
				$nav.one('transitionend.trans webkitTransitionEnd.trans mozTransitionEnd.trans oTransitionEnd.trans', function() {
					$sidebar.removeClass('sidebar-scroll');
					$nav.off('.trans');
				});
			} else {
				$sidebar.removeClass('sidebar-scroll');
			}

			nav.style.top = 0;
		},
		prehide: function(height_change) {
			if(!is_scrolling || ace.vars['minimized']) return;

			if(scrollbars.content_height() + height_change < scrollbars.available_height()) {
				scrollbars.disable();
			}
			else if(height_change < 0) {
				//if content height is decreasing
				//let's move nav down while a submenu is being hidden
				var scroll_top = scroll_content.scrollTop() + height_change
				if(scroll_top < 0) return;

				nav.style.top = (-1 * scroll_top) + 'px';
			}
		},
		_reset: function() {
			if(ace.vars['webkit']) 
				setTimeout(function() { scrollbars.reset() } , 0);
			else scrollbars.reset();
		}
	}
	scrollbars.initiate(true);//true = on_page_load

	//reset on document and window changes
	$(document).on('settings.ace.scroll', function(ev, event_name, event_val){
		if( event_name == 'sidebar_collapsed' && sidebar_fixed ) {
			scrollbars.reset();
		}
		else if( event_name === 'sidebar_fixed' || event_name === 'navbar_fixed' ) {
			//sidebar_fixed = event_val;
			sidebar_fixed = is_sidebar_fixed()
			
			if(sidebar_fixed && !is_scrolling) {
				scrollbars.reset();
			}
			else if( !sidebar_fixed ) {
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
			scrollbars._reset();
		}
	});

}