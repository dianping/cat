/**
 Required. Ace's Basic File to Initiliaze Different Parts & Some Variables.
*/
if( !('ace' in window) ) window['ace'] = {}
if( !('helper' in window['ace']) ) window['ace'].helper = {}
if( !('options' in window['ace']) ) window['ace'].options = {}
if( !('vars' in window['ace']) ) {
  window['ace'].vars = {
	 'icon'	: ' ace-icon ',
	'.icon'	: '.ace-icon'
  }
}
ace.vars['touch']	= 'ontouchstart' in document.documentElement;


jQuery(function($) {
	//sometimes we try to use 'tap' event instead of 'click' if jquery mobile plugin is available
	ace.click_event = ace.vars['touch'] && $.fn.tap ? 'tap' : 'click';

	//sometimes the only good way to work around browser's pecularities is to detect them using user-agents
	//though it's not accurate
	var agent = navigator.userAgent
	ace.vars['webkit'] = !!agent.match(/AppleWebKit/i)
	ace.vars['safari'] = !!agent.match(/Safari/i) && !agent.match(/Chrome/i);
	ace.vars['android'] = ace.vars['safari'] && !!agent.match(/Android/i)
	ace.vars['ios_safari'] = !!agent.match(/OS ([4-9])(_\d)+ like Mac OS X/i) && !agent.match(/CriOS/i)
	ace.vars['old_ie'] = document.all && !document.addEventListener;
	
	ace.vars['non_auto_fixed'] = ace.vars['android'] || ace.vars['ios_safari'];
	// for android and ios we don't use "top:auto" when breadcrumbs is fixed
	if(ace.vars['non_auto_fixed']) {
		$('body').addClass('mob-safari');
	}

	var docStyle = document.documentElement.style;
	ace.vars['transition'] = 'transition' in docStyle || 'WebkitTransition' in docStyle || 'MozTransition' in docStyle || 'OTransition' in docStyle

	/////////////////////////////

	//a list of available functions with their arguments
	// >>> null means enable
	// >>> false means disable
	// >>> other means function arguments (object)
	var available_functions = {
		'general_vars' : null,//general_vars should come first
		'add_touch_drag' : null,
		'general_things' : null,

		'handle_side_menu' : null,
				
		'sidebar_scrollable' : {
							 //'only_fixed': true, //enable only if sidebar is fixed , for 2nd approach only
							 'scroll_to_active': true, //scroll to selected item? (one time only on page load)
							 'include_shortcuts': true, //true = include shortcut buttons in the scrollbars
							 'include_toggle': false || ace.vars['safari'] || ace.vars['ios_safari'], //true = include toggle button in the scrollbars
							 'smooth_scroll': 200, //> 0 means smooth_scroll, time in ms, used in first approach only, better to be almost the same amount as submenu transition time
							 'outside': false//true && ace.vars['touch'] //used in first approach only, true means the scrollbars should be outside of the sidebar
							},

		'sidebar_hoverable' : {'sub_scroll': false},
									  //automatically move up a submenu, if some part of it goes out of window
									  //set sub_scroll to `true`, to enable native browser scrollbars on submenus when needed (touch devices only)

		'widget_boxes' : null,
		'widget_reload_handler' : null,

		'settings_box' : null,//settings box
		'settings_rtl' : null,
		'settings_skin' : null,

		'enable_searchbox_autocomplete' : null,

		'auto_hide_sidebar' : false,//disable?
		'auto_padding' : false,//disable
		'auto_container' : false//disable
	};

	//enable these functions with related params
	for(var func_name in available_functions) {
		if(!(func_name in ace)) continue;

		var args = available_functions[func_name];
		if(args === false) continue;//don't run this function
		 else if(args === null) args = [jQuery];
		  else if(args instanceof Array) args.unshift(jQuery);//prepend jQuery
		   else args = [jQuery, args];

		ace[func_name].apply(null, args);
	}

})



ace.general_vars = function($) {
	var minimized_menu_class  = 'menu-min';
	var responsive_min_class  = 'responsive-min';
	var horizontal_menu_class = 'h-sidebar';
	
	var sidebar = $('#sidebar').eq(0);
	//differnet mobile menu styles
	ace.vars['mobile_style'] = 1;//default responsive mode with toggle button inside navbar
	if(sidebar.hasClass('responsive') && !$('#menu-toggler').hasClass('navbar-toggle')) ace.vars['mobile_style'] = 2;//toggle button behind sidebar
	 else if(sidebar.hasClass(responsive_min_class)) ace.vars['mobile_style'] = 3;//minimized menu
	  else if(sidebar.hasClass('navbar-collapse')) ace.vars['mobile_style'] = 4;//collapsible (bootstrap style)

	//update some basic variables
	$(window).on('resize.ace.vars' , function(){
		ace.vars['window'] = {width: parseInt($(this).width()), height: parseInt($(this).height())}
		ace.vars['mobile_view'] = ace.vars['mobile_style'] < 4 && ace.helper.mobile_view();
		ace.vars['collapsible'] = !ace.vars['mobile_view'] && ace.helper.collapsible();
		ace.vars['nav_collapse'] = (ace.vars['collapsible'] || ace.vars['mobile_view']) && $('#navbar').hasClass('navbar-collapse');

		var sidebar = $(document.getElementById('sidebar'));
		ace.vars['minimized'] = 
		(!ace.vars['collapsible'] && sidebar.hasClass(minimized_menu_class))
		 ||
		(ace.vars['mobile_style'] == 3 && ace.vars['mobile_view'] && sidebar.hasClass(responsive_min_class))

		ace.vars['horizontal'] = !(ace.vars['mobile_view'] || ace.vars['collapsible']) && sidebar.hasClass(horizontal_menu_class)
	}).triggerHandler('resize.ace.vars');
}

//
ace.general_things = function($) {
	//add scrollbars for user dropdowns
	var has_scroll = !!$.fn.ace_scroll;
	if(has_scroll) $('.dropdown-content').ace_scroll({reset: false, mouseWheelLock: true})
	/**
	//add scrollbars to body
	 if(has_scroll) $('body').ace_scroll({size: ace.helper.winHeight()})
	 $('body').css('position', 'static')
	*/

	//reset scrolls bars on window resize
	$(window).on('resize.reset_scroll', function() {
		if(!has_scroll) return;
		$('.ace-scroll').ace_scroll('reset');
		/**
		 //reset body scrollbars
		 $('body').ace_scroll('update', {size : ace.helper.winHeight()})
		*/
	});
	if(has_scroll) $(document).on('settings.ace.reset_scroll', function(e, name) {
		if(name == 'sidebar_collapsed') $('.ace-scroll').ace_scroll('reset');
	});


	//change a dropdown to "dropup" depending on its position
	$(document).on('click.dropdown.pos', '.dropdown-toggle[data-position="auto"]', function() {
		var offset = $(this).offset();
		var parent = $(this.parentNode);

		if ( parseInt(offset.top + $(this).height()) + 50 
				>
			(ace.helper.scrollTop() + ace.helper.winHeight() - parent.find('.dropdown-menu').eq(0).height()) 
			) parent.addClass('dropup');
		else parent.removeClass('dropup');
	});
	
	
	//prevent dropdowns from hiding when a tab is selected
	$(document).on('click', '.dropdown-navbar .nav-tabs', function(e){
		e.stopPropagation();
		var $this , href
		var that = e.target
		if( ($this = $(e.target).closest('[data-toggle=tab]')) && $this.length > 0) {
			$this.tab('show');
			e.preventDefault();
		}
	});
	
	//prevent dropdowns from hiding when a from is clicked
	/**$(document).on('click', '.dropdown-navbar form', function(e){
		e.stopPropagation();		
	});*/


	//disable navbar icon animation upon click
	$('.ace-nav [class*="icon-animated-"]').closest('a').one('click', function(){
		var icon = $(this).find('[class*="icon-animated-"]').eq(0);
		var $match = icon.attr('class').match(/icon\-animated\-([\d\w]+)/);
		icon.removeClass($match[0]);
	});


	//tooltip in sidebar items
	$('.sidebar .nav-list .badge[title],.sidebar .nav-list .badge[title]').each(function() {
		var tooltip_class = $(this).attr('class').match(/tooltip\-(?:\w+)/);
		tooltip_class = tooltip_class ? tooltip_class[0] : 'tooltip-error';
		$(this).tooltip({
			'placement': function (context, source) {
				var offset = $(source).offset();

				if( parseInt(offset.left) < parseInt(document.body.scrollWidth / 2) ) return 'right';
				return 'left';
			},
			container: 'body',
			template: '<div class="tooltip '+tooltip_class+'"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
		});
	});
	
	//or something like this if items are dynamically inserted
	/**
	$('.sidebar').tooltip({
		'placement': function (context, source) {
			var offset = $(source).offset();

			if( parseInt(offset.left) < parseInt(document.body.scrollWidth / 2) ) return 'right';
			return 'left';
		},
		selector: '.nav-list .badge[title],.nav-list .label[title]',
		container: 'body',
		template: '<div class="tooltip tooltip-error"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
	});
	*/
	
	


	//the scroll to top button
	var scroll_btn = $('.btn-scroll-up');
	if(scroll_btn.length > 0) {
		var is_visible = false;
		$(window).on('scroll.scroll_btn', function() {
			var scroll = ace.helper.scrollTop();
			var h = ace.helper.winHeight();
			var body_sH = document.body.scrollHeight;
			if(scroll > parseInt(h / 4) || (scroll > 0 && body_sH >= h && h + scroll >= body_sH - 1)) {//|| for smaller pages, when reached end of page
				if(!is_visible) {
					scroll_btn.addClass('display');
					is_visible = true;
				}
			} else {
				if(is_visible) {
					scroll_btn.removeClass('display');
					is_visible = false;
				}
			}
		}).triggerHandler('scroll.scroll_btn');

		scroll_btn.on(ace.click_event, function(){
			var duration = Math.min(500, Math.max(100, parseInt(ace.helper.scrollTop() / 3)));
			$('html,body').animate({scrollTop: 0}, duration);
			return false;
		});
	}


	//chrome and webkit have a problem here when resizing from 479px to more
	//we should force them redraw the navbar!
	if( ace.vars['webkit'] ) {
		var ace_nav = $('.ace-nav').get(0);
		if( ace_nav ) $(window).on('resize.webkit' , function(){
			ace.helper.redraw(ace_nav);
		});
	}
	
	
	//fix an issue with ios safari, when an element is fixed and an input receives focus
	if(ace.vars['ios_safari']) {
	  $(document).on('ace.settings.ios_fix', function(e, event_name, event_val) {
		if(event_name != 'navbar_fixed') return;

		$(document).off('focus.ios_fix blur.ios_fix', 'input,textarea,.wysiwyg-editor');
		if(event_val == true) {
		  $(document).on('focus.ios_fix', 'input,textarea,.wysiwyg-editor', function() {
			$(window).on('scroll.ios_fix', function() {
				var navbar = $('#navbar').get(0);
				if(navbar) ace.helper.redraw(navbar);
			});
		  }).on('blur.ios_fix', 'input,textarea,.wysiwyg-editor', function() {
			$(window).off('scroll.ios_fix');
		  })
		}
	  }).triggerHandler('ace.settings.ios_fix', ['navbar_fixed', $('#navbar').css('position') == 'fixed']);
	}


	/**
	//TODO ... modal like display of navbar dropdowns in small devices
	$('.ace-nav > li').on('shown.bs.dropdown', function(e) {
	})
	*/

}




//some functions
ace.helper.collapsible = function() {
	var toggle
	return (document.querySelector('#sidebar.navbar-collapse') != null)
	&& ((toggle = document.querySelector('.navbar-toggle[data-target*=".sidebar"]')) != null)
	&&  toggle.scrollHeight > 0
	//sidebar is collapsible and collapse button is visible?
}
ace.helper.mobile_view = function() {
	var toggle
	return ((toggle = document.getElementById('menu-toggler')) != null	&& toggle.scrollHeight > 0)
}

ace.helper.redraw = function(elem) {
	var saved_val = elem.style['display'];
	elem.style.display = 'none';
	elem.offsetHeight;
	elem.style.display = saved_val;
}

ace.helper.scrollTop = function() {
	return document.scrollTop || document.documentElement.scrollTop || document.body.scrollTop
	//return $(window).scrollTop();
}
ace.helper.winHeight = function() {
	return window.innerHeight || document.documentElement.clientHeight;
	//return $(window).innerHeight();
}
ace.helper.camelCase = function(str) {
	return str.replace(/-([\da-z])/gi, function(match, chr) {
	  return chr ? chr.toUpperCase() : '';
	});
}
ace.helper.removeStyle = 
  'removeProperty' in document.documentElement.style
  ?
  function(elem, prop) { elem.style.removeProperty(prop) }
  :
  function(elem, prop) { elem.style[ace.helper.camelCase(prop)] = '' }


ace.helper.hasClass = 
  'classList' in document.documentElement
  ?
  function(elem, className) { return elem.classList.contains(className); }
  :
  function(elem, className) { return elem.className.indexOf(className) > -1; };

/**
 <b>Custom drag event for touch devices</b> used in scrollbars.
 For better touch event handling and extra options a more advanced solution such as <u>Hammer.js</u> is recommended.
*/

//based on but not dependent on jQuery mobile
/*
* jQuery Mobile v1.3.2
* http://jquerymobile.com
*
* Copyright 2010, 2013 jQuery Foundation, Inc. and other contributors
* Released under the MIT license.
* http://jquery.org/license
*
*/
ace.add_touch_drag = function($) {
	if(!ace.vars['touch']) return;

	var touchStartEvent = "touchstart MSPointerDown pointerdown",// : "mousedown",
			touchStopEvent  =  "touchend touchcancel MSPointerUp MSPointerCancel pointerup pointercancel",// : "mouseup",
			touchMoveEvent  =  "touchmove MSPointerMove MSPointerHover pointermove";// : "mousemove";


	$.event.special.ace_drag = {
		setup: function() {
			var min_threshold = 0;
		
			var $this = $(this);
			$this.on(touchStartEvent, function(event) {		
				var data = event.originalEvent.touches ?
					event.originalEvent.touches[ 0 ] :
					event,
					start = {
						//time: Date.now(),
						coords: [ data.pageX, data.pageY ],
						origin: $(event.target)
					},
					stop;
					//start.origin.trigger({'type' : 'ace_dragStart', 'start':(start || [-1,-1])});
					
					var direction = false, dx = 0, dy = 0;

				function moveHandler(event) {
					if (!start) {
						return;
					}
					var data = event.originalEvent.touches ?
							event.originalEvent.touches[ 0 ] :
							event;
					stop = {
						coords: [ data.pageX, data.pageY ]
					};
					
					// prevent scrolling
					//if ( Math.abs(start.coords[1] - stop.coords[1]) > 0 || Math.abs(start.coords[0] - stop.coords[01]) > 0 ) {
						//event.preventDefault();
					//}


					if (start && stop) {
						dx = 0;
						dy = 0;

						direction = 
							(
							 Math.abs(dy = start.coords[ 1 ] - stop.coords[ 1 ]) > min_threshold
								&& 
							 Math.abs(dx = start.coords[ 0 ] - stop.coords[ 0 ]) <= Math.abs(dy)
							)
							? 
							(dy > 0 ? 'up' : 'down')
							:
							(
							 Math.abs(dx = start.coords[ 0 ] - stop.coords[ 0 ]) > min_threshold
								&& 
							 Math.abs( dy ) <= Math.abs(dx)
							)
							?
							(dx > 0 ? 'left' : 'right')
							:
							false;
							

							if( direction !== false ) {
							 var retval = {cancel: false}
							 start.origin.trigger({
								'type': 'ace_drag',
								//'start': start.coords,
								//'stop': stop.coords,
								'direction': direction,
								'dx': dx,
								'dy': dy,
								'retval': retval
							 })

		 					  // prevent document scrolling unless retval.cancel == true
							  if( retval.cancel == false ) event.preventDefault();
							}
					}
					start.coords[0] = stop.coords[0];
					start.coords[1] = stop.coords[1];
				}

				$this
				.on(touchMoveEvent, moveHandler)
				.one(touchStopEvent, function(event) {
					$this.off(touchMoveEvent, moveHandler);
					//start.origin.trigger({'type' : 'ace_dragEnd', 'stop':(stop || [-1,-1])});
					
					start = stop = undefined;
				
				});
			});
		}
	}
};

/**
 <b>Sidebar functions</b>. Collapsing/expanding, toggling mobile view menu and other sidebar functions.
*/

ace.handle_side_menu = function($) {
	var sidebar = $('.sidebar').eq(0);

	$(document).on(ace.click_event+'.ace.menu', '#menu-toggler', function(){
		sidebar.toggleClass('display');
		$(this).toggleClass('display');

		if( $(this).hasClass('display') && 'sidebar_scroll' in ace.helper )
		{
			ace.helper.sidebar_scroll.reset();
		}

		return false;
	})
	//sidebar collapse/expand button
	.on(ace.click_event+'.ace.menu', '.sidebar-collapse', function(){
		if(ace.vars['collapsible'] || ace.vars['horizontal']) return;

		//var minimized = sidebar.hasClass('menu-min');
		ace.vars['minimized'] = !ace.vars['minimized'];
		ace.settings.sidebar_collapsed.call(this, ace.vars['minimized']);//@ ace-extra.js
		//ace.settings.sidebar_collapsed(ace.vars['minimized']);
		
		//force redraw for ie8
		if(ace.vars['old_ie']) ace.helper.redraw(sidebar.get(0));
	})
	//this button is used in `mobile_style = 3` responsive menu style to expand minimized sidebar
	.on(ace.click_event+'.ace.menu', '.sidebar-expand', function(){

		if( ace.vars['minimized'] /**sidebar.hasClass('menu-min')*/ ) {
			ace.settings.sidebar_collapsed.call(this, false , false);
			//unminimize (remove .menu-min) but don't save changes to cookies
		}

		var icon = $(this).find(ace.vars['.icon']);
		var $icon1 = icon.attr('data-icon1');//the icon for expanded state
		var $icon2 = icon.attr('data-icon2');//the icon for collapsed state
		if( sidebar.hasClass('responsive-min') ) {
			icon.removeClass($icon1).addClass($icon2);
			sidebar.removeClass('responsive-min');
			sidebar.addClass('display responsive-max');

			ace.vars['minimized'] = false
		}
		else {
			icon.removeClass($icon2).addClass($icon1);
			sidebar.removeClass('display responsive-max');
			sidebar.addClass('responsive-min');

			ace.vars['minimized'] = true
		}

		$(document).triggerHandler('settings.ace', ['sidebar_collapsed' , ace.vars['minimized']]);
	});



	//ios safari only has a bit of a problem not navigating to link address when scrolling down
	var ios_fix = ace.vars['ios_safari'];//navigator.userAgent.match(/OS (5|6|7)(_\d)+ like Mac OS X/i);
	//toggling submenu
	$(document).on(ace.click_event+'.ace.submenu', '.sidebar .nav-list', function (ev) {
		var nav_list = this;

		//check to see if we have clicked on an element which is inside a .dropdown-toggle element?!
		//if so, it means we should toggle a submenu
		var link_element = $(ev.target).closest('a');
		if(!link_element || link_element.length == 0) return;//return if not clicked inside a link element

		var minimized  = ace.vars['minimized'] && !ace.vars['collapsible'];
		//if .sidebar is .navbar-collapse and in small device mode, then let minimized be uneffective

		if( !link_element.hasClass('dropdown-toggle') ) {//it doesn't have a submenu return
			//just one thing before we return
			//if sidebar is collapsed(minimized) and we click on a first level menu item
			//and the click is on the icon, not on the menu text then let's cancel event and cancel navigation
			//Good for touch devices, that when the icon is tapped to see the menu text, navigation is cancelled
			//navigation is only done when menu text is tapped

			if( ace.click_event == "tap"
				&&
				minimized
				&&
				link_element.get(0).parentNode.parentNode == nav_list )//only level-1 links
			{
				var text = link_element.find('.menu-text').get(0);
				if( ev.target != text && !$.contains(text , ev.target) ) {//not clicking on the text or its children
					ev.preventDefault();
					return false;
				}
			}

			//some browsers need to be forced 
			
			//specify data-link attribute to ignore this
			if(ios_fix /**&& ace.vars['ajax_content'] !== true*/ && link_element.attr('data-link') !== 'false') {
				//only ios safari has a bit of a problem not navigating to link address when scrolling down
				//please see issues section in documentation
				document.location = link_element.attr('href');
				ev.preventDefault();
				return false;
			}
			
			/**
			ev.preventDefault();
			var href = link_element.attr('href');
			history.pushState({'url': href, 'hash': link_element.attr('data-url')}, null, href);
			*/

			return;
		}


		var sub = link_element.siblings('.submenu').get(0);
		if(!sub) return false;

		var height_change = 0;//the amount of height change in .nav-list
		var duration = 250;//transition duration

		var parent_ul = sub.parentNode.parentNode;
		if
		(
			( minimized && parent_ul == nav_list )
			 || 
			( ( $(sub.parentNode).hasClass('hover') && $(sub).css('position') == 'absolute' ) && !ace.vars['collapsible'] )
		)
		{
			ev.preventDefault();
			return false;
		}

		//if not open and visible, let's open it and make it visible
		if( sub.scrollHeight == 0 ) {
		  $(parent_ul).find('> .open > .submenu').each(function() {
			//close all other open submenus except for the active one
			if(this != sub && !$(this.parentNode).hasClass('active')) {
				height_change -= this.scrollHeight;
				ace.submenu.hide(this, duration);
			}
		  })
		}


		var toggle = 0;
		if( (toggle = ace.submenu.toggle(sub , duration)) == 1 ) {
			//== 1 means submenu is being shown
			//if a submenu is being shown and another one previously started to hide, then we may need to update/hide scrollbars
			//but if no previous submenu is being hidden, then no need to check if we need to hide the scrollbars in advance
			if(height_change != 0) height_change += sub.scrollHeight;
		} else if(toggle == -1) {
			height_change -= sub.scrollHeight;
			//== -1 means submenu is being hidden
		}

		//hide scrollbars if content is going to be small enough that scrollbars is not needed anymore
		//do this almost before submenu hiding begins
		if (height_change != 0 && 'sidebar_scroll' in ace.helper) {
			ace.helper.sidebar_scroll.prehide(height_change);
		}

		ev.preventDefault();
		return false;
	 })
}
;

/**
 <b>Load content via Ajax </b>. For more information please refer to documentation #basics/ajax
*/

ace.enable_ajax_content = function($, options) {
	//var has_history = 'history' in window && typeof window.history.pushState === 'function';
	
	 var content_url = options.content_url || false
	 var default_url = options.default_url || false;
	var loading_icon = options.loading_icon || 'fa-spinner fa-2x orange';
	var loading_text = options.loading_text || '';
	var update_breadcrumbs = options.update_breadcrumbs || typeof options.update_breadcrumbs === 'undefined';
	var update_title = options.update_title || typeof options.update_title === 'undefined';
	var update_active = options.update_active || typeof options.update_active === 'undefined';
	var close_active = options.close_active || typeof options.close_active === 'undefined';

	$(window)
	.off('hashchange.ajax')
	.on('hashchange.ajax', function(e, manual_trigger) {
		var hash = $.trim(window.location.hash);
		if(!hash || hash.length == 0) return;
		
		hash = hash.replace(/^(\#\!)?\#/, '');
		var url = false;
		
		if(typeof content_url === 'function') url = content_url(hash);
		if(typeof url === 'string') getUrl(url, hash, manual_trigger || false);
	}).trigger('hashchange.ajax', [true]);
	
	/**
	if(has_history) {
		window.onpopstate = function(event) {
		  JSON.stringify(event.state);
		  //getUrl(event.state.url, event.state.hash, true);
		}
	}
	*/
	
	if(default_url && window.location.hash == '') window.location.hash = default_url;


	function getUrl(url, hash, manual_trigger) {
		var event
		$(document).trigger(event = $.Event('ajaxloadstart'), {url: url, hash: hash})
		if (event.isDefaultPrevented()) return;

		
		var contentArea = $('.page-content-area');
		contentArea
		.css('opacity', 0.25)
		
		var loader = $('<div style="position: fixed; z-index: 2000;" class="ajax-loading-overlay"><i class="ajax-loading-icon fa fa-spin '+loading_icon+'"></i> '+loading_text+'</div>').insertBefore(contentArea);
		var offset = contentArea.offset();
		loader.css({top: offset.top, left: offset.left})
	
		$.ajax({
			'url': url
		})
		.complete(function() {
			contentArea.css('opacity', 0.8)
			$(document).on('ajaxscriptsloaded', function() {
				contentArea.css('opacity', 1)
				contentArea.prevAll('.ajax-loading-overlay').remove();
			});
		})
		.error(function() {
			$(document).trigger('ajaxloaderror', {url: url, hash: hash});
		})
		.done(function(result) {
			$(document).trigger('ajaxloaddone', {url: url, hash: hash});
		
			var link_element = $('a[data-url="'+hash+'"]');
			var link_text = '';
			if(link_element.length > 0) {
				var nav = link_element.closest('.nav');
				if(nav.length > 0) {
					if(update_active) {
						nav.find('.active').each(function(){
							var $class = 'active';
							if( $(this).hasClass('hover') || close_active ) $class += ' open';
							
							$(this).removeClass($class);							
							if(close_active) {
								$(this).find(' > .submenu').css('display', '');
								//var sub = $(this).find(' > .submenu').get(0);
								//if(sub) ace.submenu.hide(sub, 200)
							}
						})
						link_element.closest('li').addClass('active').parents('.nav li').addClass('active open');
						if('sidebar_scroll' in ace.helper) {
							ace.helper.sidebar_scroll.reset();
							//first time only
							if(manual_trigger) ace.helper.sidebar_scroll.scroll_to_active();
						}
					}
					if(update_breadcrumbs) {
						link_text = updateBreadcrumbs(link_element);
					}
				}
			}

			//convert "title" and "link" tags to "div" tags for later processing
			result = String(result)
				.replace(/<(title|link)([\s\>])/gi,'<div class="hidden ajax-append-$1"$2')
				.replace(/<\/(title|link)\>/gi,'</div>')
		
			contentArea.empty().html(result);
			contentArea.css('opacity', 0.6);

			//remove previous stylesheets inserted via ajax
			setTimeout(function() {
				$('head').find('link.ajax-stylesheet').remove();
				var ace_style = $('head').find('link#main-ace-style');
				contentArea.find('.ajax-append-link').each(function(e) {
					var $link = $(this);
					if ( $link.attr('href') ) {
						var new_link = jQuery('<link />', {type : 'text/css', rel: 'stylesheet', 'class': 'ajax-stylesheet'})
						if( ace_style.length > 0 ) new_link.insertBefore(ace_style);
						else new_link.appendTo('head');
						new_link.attr('href', $link.attr('href'));//we set "href" after insertion, for IE to work
					}
					$link.remove();
				})
			}, 10);

			//////////////////////

			if(update_title) updateTitle(link_text, contentArea);
			if( !manual_trigger ) {
				$('html,body').animate({scrollTop: 0}, 250);
			}

			//////////////////////
			$(document).trigger('ajaxloadcomplete', {url: url, hash: hash});
		})
	 }
	 

	 
	 function updateBreadcrumbs(link_element) {
		var link_text = '';
	 
		//update breadcrumbs
		var breadcrumbs = $('.breadcrumb');
		if(breadcrumbs.length > 0 && breadcrumbs.is(':visible')) {
			breadcrumbs.find('> li:not(:first-child)').remove();

			var i = 0;		
			link_element.parents('.nav li').each(function() {
				var link = $(this).find('> a');
				
				var link_clone = link.clone();
				link_clone.find('i,.fa,.glyphicon,.ace-icon,.menu-icon,.badge,.label').remove();
				var text = link_clone.text();
				link_clone.remove();
				
				var href = link.attr('href');

				if(i == 0) {
					var li = $('<li class="active"></li>').appendTo(breadcrumbs);
					li.text(text);
					link_text = text;
				}
				else {
					var li = $('<li><a /></li>').insertAfter(breadcrumbs.find('> li:first-child'));
					li.find('a').attr('href', href).text(text);
				}
				i++;
			})
		}
		
		return link_text;
	 }
	 
	 function updateTitle(link_text, contentArea) {
		var $title = contentArea.find('.ajax-append-title');
		if($title.length > 0) {
			document.title = $title.text();
			$title.remove();
		}
		else if(link_text.length > 0) {
			var extra = $.trim(String(document.title).replace(/^(.*)[\-]/, ''));//for example like " - Ace Admin"
			if(extra) extra = ' - ' + extra;
			link_text = $.trim(link_text) + extra;
		}
	 }

}

ace.load_ajax_scripts = function(scripts, callback) {

 jQuery.ajaxPrefilter('script', function(opts) {opts.cache = true});
 setTimeout(function() {

	//let's keep a list of loaded scripts so that we don't load them more than once!
	if(! ('ajax_loaded_scripts' in ace.vars) ) ace.vars['ajax_loaded_scripts'] = {}

	var deferreds = [];
	for(var i = 0; i < scripts.length; i++) if(scripts[i]) {
		(function() {
			var script_name = "js-"+scripts[i].replace(/[^\w\d\-]/g, '-').replace(/\-\-/g, '-');
			//only load scripts that are not loaded yet!
			if(! (script_name in ace.vars['ajax_loaded_scripts']) ) {
				deferreds.push( jQuery.getScript(scripts[i]).done(function() {
					ace.vars['ajax_loaded_scripts'][script_name] = true;
				}));
			}
		})()
	}

	if(deferreds.length > 0) {
		deferreds.push(jQuery.Deferred(function( deferred ){jQuery( deferred.resolve )}));

		jQuery.when.apply( null, deferreds ).then(function() {
			if(typeof callback === 'function') callback();
			jQuery('.btn-group[data-toggle="buttons"] > .btn').button();
			
			$(document).trigger('ajaxscriptsloaded');
		})
	}
	else {
		if(typeof callback === 'function') callback();
		jQuery('.btn-group[data-toggle="buttons"] > .btn').button();
		$(document).trigger('ajaxscriptsloaded');
	}

 }, 10)
}
;

/**
<b>Toggle sidebar submenus</b>. This approach uses <u>CSS3</u> transitions.
It's a bit smoother but the transition does not work on IE9 and below and it is sometimes glitchy on Android's default browser.
*/

//CSS3 transition version, no animation on IE9 and below
ace.submenu = {
 show : function(sub, duration) {
	var $ = window.jQuery;
	var $sub = $(sub);

	var event;
	$sub.trigger(event = $.Event('show.ace.submenu'))
	if (event.isDefaultPrevented()) return false;

	$sub
	.css({
		height: 0,
		overflow: 'hidden',
		display: 'block'
	})
	.removeClass('nav-hide').addClass('nav-show')//only for window < @grid-float-breakpoint and .navbar-collapse.menu-min
	.parent().addClass('open');

	if( duration > 0 ) {
	  $sub.css({height: sub.scrollHeight,
		'transition-property': 'height',
		'transition-duration': (duration/1000)+'s'})
	}

	var complete = function(ev, trigger) {
		ev && ev.stopPropagation();
		$sub
		.css({'transition-property': '', 'transition-duration': '', overflow:'', height: ''})
		//if(ace.vars['webkit']) ace.helper.redraw(sub);//little Chrome issue, force redraw ;)

		if(ace.vars['transition']) $sub.off('.trans');
		if(trigger !== false) $sub.trigger($.Event('shown.ace.submenu'))
	}
	if( duration > 0 && ace.vars['transition'] ) {
	  $sub.one('transitionend.trans webkitTransitionEnd.trans mozTransitionEnd.trans oTransitionEnd.trans', complete);
	}
	else complete();
	
	//there is sometimes a glitch, so maybe retry
	if(ace.vars['android']) {
		setTimeout(function() {
			complete(null, false);
		}, duration + 20);
	}

	return true;
 }
 ,
 hide : function(sub, duration) {
	var $ = window.jQuery;
	var $sub = $(sub);

	var event;
	$sub.trigger(event = $.Event('hide.ace.submenu'))
	if (event.isDefaultPrevented()) return false;

	$sub
	.css({
		height: sub.scrollHeight,
		overflow: 'hidden'
	})
	.parent().removeClass('open');

	sub.offsetHeight;
	//forces the "sub" to re-consider the new 'height' before transition

	if( duration > 0 ) {
	  $sub.css({'height': 0,
		'transition-property': 'height',
		'transition-duration': (duration/1000)+'s'});
	}


	var complete = function(ev, trigger) {
		ev && ev.stopPropagation();
		$sub
		.css({display: 'none', overflow:'', height: '', 'transition-property': '', 'transition-duration': ''})
		.removeClass('nav-show').addClass('nav-hide')//only for window < @grid-float-breakpoint and .navbar-collapse.menu-min

		if(ace.vars['transition']) $sub.off('.trans');
		if(trigger !== false) $sub.trigger($.Event('hidden.ace.submenu'))
	}
	if( duration > 0 && ace.vars['transition'] ) {
	  $sub.one('transitionend.trans webkitTransitionEnd.trans mozTransitionEnd.trans oTransitionEnd.trans', complete);
	}
	else complete();


	//there is sometimes a glitch, so maybe retry
	if(ace.vars['android']) {
		setTimeout(function() {
			complete(null, false);
		}, duration + 20);
	}

	return true;
 }
 ,
 toggle : function(element, duration) {
	if( element.scrollHeight == 0 ) {//if an element is hidden scrollHeight becomes 0
		if(ace.submenu.show(element, duration)) return 1;
	} else {
		if(ace.submenu.hide(element, duration)) return -1;
	}
	return 0;
 }
}
;

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

};

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


;

/**
 <b>Widget boxes</b>
*/
ace.widget_boxes = function($) {
	//bootstrap collapse component icon toggle
	$(document).on('hide.bs.collapse show.bs.collapse', function (ev) {
		var panel_id = ev.target.getAttribute('id')
		var panel = $('a[href*="#'+ panel_id+'"]');
		if(panel.length == 0) panel = $('a[data-target*="#'+ panel_id+'"]');
		if(panel.length == 0) return;

		panel.find(ace.vars['.icon']).each(function(){
			var $icon = $(this)

			var $match
			var $icon_down = null
			var $icon_up = null
			if( ($icon_down = $icon.attr('data-icon-show')) ) {
				$icon_up = $icon.attr('data-icon-hide')
			}
			else if( $match = $icon.attr('class').match(/fa\-(.*)\-(up|down)/) ) {
				$icon_down = 'fa-'+$match[1]+'-down'
				$icon_up = 'fa-'+$match[1]+'-up'
			}

			if($icon_down) {
				if(ev.type == 'show') $icon.removeClass($icon_down).addClass($icon_up)
					else $icon.removeClass($icon_up).addClass($icon_down)
					
				return false;//ignore other icons that match, one is enough
			}

		});
	})


	var Widget_Box = function(box, options) {
		this.$box = $(box);
		var that = this;
		//this.options = $.extend({}, $.fn.widget_box.defaults, options);

		this.reload = function() {
			var $box = this.$box;
			var $remove_position = false;
			if($box.css('position') == 'static') {
				$remove_position = true;
				$box.addClass('position-relative');
			}
			$box.append('<div class="widget-box-overlay"><i class="'+ ace.vars['icon'] + 'loading-icon fa fa-spinner fa-spin fa-2x white"></i></div>');

			$box.one('reloaded.ace.widget', function() {
				$box.find('.widget-box-overlay').remove();
				if($remove_position) $box.removeClass('position-relative');
			});
		}

		this.close = function() {
			var $box = this.$box;
			var closeSpeed = 300;
			$box.fadeOut(closeSpeed , function(){
					$box.trigger('closed.ace.widget');
					$box.remove();
				}
			)
		}
		
		this.toggle = function(type, button) {
			var $box = this.$box;
			var $body = $box.find('.widget-body');
			var $icon = null;
			
			var event_name = typeof type !== 'undefined' ? type : ($box.hasClass('collapsed') ? 'show' : 'hide');
			var event_complete_name = event_name == 'show' ? 'shown' : 'hidden';

			if(typeof button === 'undefined') {
				button = $box.find('> .widget-header a[data-action=collapse]').eq(0);
				if(button.length == 0) button = null;
			}

			if(button) {
				$icon = button.find(ace.vars['.icon']).eq(0);

				var $match
				var $icon_down = null
				var $icon_up = null
				if( ($icon_down = $icon.attr('data-icon-show')) ) {
					$icon_up = $icon.attr('data-icon-hide')
				}
				else if( $match = $icon.attr('class').match(/fa\-(.*)\-(up|down)/) ) {
					$icon_down = 'fa-'+$match[1]+'-down'
					$icon_up = 'fa-'+$match[1]+'-up'
				}
			}

			var expandSpeed   = 250;
			var collapseSpeed = 200;

			if( event_name == 'show' ) {
				if($icon) $icon.removeClass($icon_down).addClass($icon_up);

				$body.hide();
				$box.removeClass('collapsed');
				$body.slideDown(expandSpeed, function(){
					$box.trigger(event_complete_name+'.ace.widget')
				})
			}
			else {
				if($icon) $icon.removeClass($icon_up).addClass($icon_down);
				$body.slideUp(collapseSpeed, function(){
						$box.addClass('collapsed')
						$box.trigger(event_complete_name+'.ace.widget')
					}
				);
			}
		}
		
		this.hide = function() {
			this.toggle('hide');
		}
		this.show = function() {
			this.toggle('show');
		}
		
		
		this.fullscreen = function() {
			var $icon = this.$box.find('> .widget-header a[data-action=fullscreen]').find(ace.vars['.icon']).eq(0);
			var $icon_expand = null
			var $icon_compress = null
			if( ($icon_expand = $icon.attr('data-icon1')) ) {
				$icon_compress = $icon.attr('data-icon2')
			}
			else {
				$icon_expand = 'fa-expand';
				$icon_compress = 'fa-compress';
			}
			
			
			if(!this.$box.hasClass('fullscreen')) {
				$icon.removeClass($icon_expand).addClass($icon_compress);
				this.$box.addClass('fullscreen');
			}
			else {
				$icon.addClass($icon_expand).removeClass($icon_compress);
				this.$box.removeClass('fullscreen');
			}
			
			this.$box.trigger('fullscreened.ace.widget')
		}

	}
	
	$.fn.widget_box = function (option, value) {
		var method_call;

		var $set = this.each(function () {
			var $this = $(this);
			var data = $this.data('widget_box');
			var options = typeof option === 'object' && option;

			if (!data) $this.data('widget_box', (data = new Widget_Box(this, options)));
			if (typeof option === 'string') method_call = data[option](value);
		});

		return (method_call === undefined) ? $set : method_call;
	};


	$(document).on('click.ace.widget', '.widget-header a[data-action]', function (ev) {
		ev.preventDefault();

		var $this = $(this);
		var $box = $this.closest('.widget-box');
		if( $box.length == 0 || $box.hasClass('ui-sortable-helper') ) return;

		var $widget_box = $box.data('widget_box');
		if (!$widget_box) {
			$box.data('widget_box', ($widget_box = new Widget_Box($box.get(0))));
		}

		var $action = $this.data('action');
		if($action == 'collapse') {
			var event_name = $box.hasClass('collapsed') ? 'show' : 'hide';

			var event
			$box.trigger(event = $.Event(event_name+'.ace.widget'))
			if (event.isDefaultPrevented()) return

			$widget_box.toggle(event_name, $this);
		}
		else if($action == 'close') {
			var event
			$box.trigger(event = $.Event('close.ace.widget'))
			if (event.isDefaultPrevented()) return

			$widget_box.close();
		}
		else if($action == 'reload') {
			$this.blur();
			var event
			$box.trigger(event = $.Event('reload.ace.widget'))
			if (event.isDefaultPrevented()) return

			$widget_box.reload();
		}
		else if($action == 'fullscreen') {
			var event
			$box.trigger(event = $.Event('fullscreen.ace.widget'))
			if (event.isDefaultPrevented()) return
		
			$widget_box.fullscreen();
		}
		else if($action == 'settings') {
			$box.trigger('setting.ace.widget')
		}

	});

};

/**
 <b>Settings box</b>. It's good for demo only. You don't need this.
*/
ace.settings_box = function($) {
 $('#ace-settings-btn').on(ace.click_event, function(e){
	e.preventDefault();

	$(this).toggleClass('open');
	$('#ace-settings-box').toggleClass('open');
 });

 $('#ace-settings-navbar').on('click', function(){
	ace.settings.navbar_fixed(this.checked);//@ ace-extra.js
	//$(window).triggerHandler('resize.navbar');
	
	//force redraw?
	//if(ace.vars['webkit']) ace.helper.redraw(document.body);
 }).each(function(){this.checked = ace.settings.is('navbar', 'fixed')})

 $('#ace-settings-sidebar').on('click', function(){
	ace.settings.sidebar_fixed(this.checked);//@ ace-extra.js
	
	//if(ace.vars['webkit']) ace.helper.redraw(document.body);
 }).each(function(){this.checked = ace.settings.is('sidebar', 'fixed')})

 $('#ace-settings-breadcrumbs').on('click', function(){
	ace.settings.breadcrumbs_fixed(this.checked);//@ ace-extra.js
	
	//if(ace.vars['webkit']) ace.helper.redraw(document.body);
 }).each(function(){this.checked = ace.settings.is('breadcrumbs', 'fixed')})

 $('#ace-settings-add-container').on('click', function(){
	ace.settings.main_container_fixed(this.checked);//@ ace-extra.js
	
	//if(ace.vars['webkit']) ace.helper.redraw(document.body);
 }).each(function(){this.checked = ace.settings.is('main-container', 'fixed')})



 $('#ace-settings-compact').removeAttr('checked').on('click', function(){
	if(this.checked) {
		$('#sidebar').addClass('compact');
		var hover = $('#ace-settings-hover');
		if( hover.length > 0 && !hover.get(0).checked ) {
			hover.removeAttr('checked').trigger('click');
		}
	}
	else {
		$('#sidebar').removeClass('compact');
		if('sidebar_scroll' in ace.helper) ace.helper.sidebar_scroll.reset();
	}
 });


 $('#ace-settings-highlight').removeAttr('checked').on('click', function(){
	if(this.checked) $('#sidebar .nav-list > li').addClass('highlight');
	else $('#sidebar .nav-list > li').removeClass('highlight');
 });


 $('#ace-settings-hover').removeAttr('checked').on('click', function(){
	if($('.sidebar').hasClass('h-sidebar')) return;
	if(this.checked) {
		$('#sidebar li').addClass('hover')
		.filter('.open').removeClass('open').find('> .submenu').css('display', 'none');
		//and remove .open items
	}
	else {
		$('#sidebar li.hover').removeClass('hover');

		var compact = $('#ace-settings-compact');
		if( compact.length > 0 && compact.get(0).checked ) {
			compact.trigger('click');
		}
		
		if('sidebar_hover' in ace.helper) ace.helper.sidebar_hover.reset();
	}
	
	if('sidebar_scroll' in ace.helper) ace.helper.sidebar_scroll.reset();
 });

};

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
;

/**
 <b>Select a different skin</b>. It's good for demo only.
 You should hard code skin-specific changes inside your HTML/server-side code.
 Please refer to documentation for more info.
*/

ace.settings_skin = function($) {
  try {
	$('#skin-colorpicker').ace_colorpicker();
  } catch(e) {}

  $('#skin-colorpicker').on('change', function(){
	var skin_class = $(this).find('option:selected').data('skin');
	//skin cookie tip

	var body = $(document.body);
	body.removeClass('no-skin skin-1 skin-2 skin-3');

	//if(skin_class != 'skin-0') {
		body.addClass(skin_class);
		ace.data.set('skin', skin_class);
		//save the selected skin to cookies
		//which can later be used by your server side app to set the skin
		//for example: <body class="<?php echo $_COOKIE['ace_skin']; ?>"
	//} else ace.data.remove('skin');
	
	var skin3_colors = ['red', 'blue', 'green', ''];
	
	
		//undo skin-1
		$('.ace-nav > li.grey').removeClass('dark');
		
		//undo skin-2
		$('.ace-nav > li').removeClass('no-border margin-1');
		$('.ace-nav > li:not(:last-child)').removeClass('light-pink').find('> a > '+ace.vars['.icon']).removeClass('pink').end().eq(0).find('.badge').removeClass('badge-warning');
		$('.sidebar-shortcuts .btn')
		.removeClass('btn-pink btn-white')
		.find(ace.vars['.icon']).removeClass('white');
		
		//undo skin-3
		$('.ace-nav > li.grey').removeClass('red').find('.badge').removeClass('badge-yellow');
		$('.sidebar-shortcuts .btn').removeClass('btn-primary btn-white')
		var i = 0;
		$('.sidebar-shortcuts .btn').each(function() {
			$(this).find(ace.vars['.icon']).removeClass(skin3_colors[i++]);
		})
	
	

	var skin0_buttons = ['btn-success', 'btn-info', 'btn-warning', 'btn-danger'];
	if(skin_class == 'no-skin') {
		var i = 0;
		$('.sidebar-shortcuts .btn').each(function() {
			$(this).attr('class', 'btn ' + skin0_buttons[i++%4]);
		})
	}

	else if(skin_class == 'skin-1') {
		$('.ace-nav > li.grey').addClass('dark');
		var i = 0;
		$('.sidebar-shortcuts')
		.find('.btn').each(function() {
			$(this).attr('class', 'btn ' + skin0_buttons[i++%4]);
		})
	}

	else if(skin_class == 'skin-2') {
		$('.ace-nav > li').addClass('no-border margin-1');
		$('.ace-nav > li:not(:last-child)').addClass('light-pink').find('> a > '+ace.vars['.icon']).addClass('pink').end().eq(0).find('.badge').addClass('badge-warning');
		
		$('.sidebar-shortcuts .btn').attr('class', 'btn btn-white btn-pink')
		.find(ace.vars['.icon']).addClass('white');
	}

	//skin-3
	//change shortcut buttons classes, this should be hard-coded if you want to choose this skin
	else if(skin_class == 'skin-3') {
		body.addClass('no-skin');//because skin-3 has many parts of no-skin as well
		
		$('.ace-nav > li.grey').addClass('red').find('.badge').addClass('badge-yellow');
		
		var i = 0;
		$('.sidebar-shortcuts .btn').each(function() {
			$(this).attr('class', 'btn btn-primary btn-white');
			$(this).find(ace.vars['.icon']).addClass(skin3_colors[i++]);
		})

	}

	//some sizing differences may be there in skins, so reset scrollbar size
	if('sidebar_scroll' in ace.helper) ace.helper.sidebar_scroll.reset();

 });
};

/**
 The widget box reload button/event handler. You should use your own handler. An example is available at <i class="text-info">examples/widgets.html</i>.
 <u><i class="glyphicon glyphicon-flash"></i> You don't need this. Used for demo only</u>
*/

ace.widget_reload_handler = function($) {
	//***default action for reload in this demo
	//you should remove this and add your own handler for each specific .widget-box
	//when data is finished loading or processing is done you can call $box.trigger('reloaded.ace.widget')
	$(document).on('reload.ace.widget', '.widget-box', function (ev) {
		var $box = $(this);
		
		//trigger the reloaded event to remove the spinner icon after 1-2 seconds
		setTimeout(function() {
			$box.trigger('reloaded.ace.widget');
		}, parseInt(Math.random() * 1000 + 1000));
	});

	//you may want to do something like this:
	/**
	$('#my-widget-box').on('reload.ace.widget', function(){
		//load new data here
		//and when finished trigger "reloaded" event
		$(this).trigger('reloaded.ace.widget');
	});
	*/
};

/**
The autocomplete dropdown when typing inside search box.
<u><i class="glyphicon glyphicon-flash"></i> You don't need this. Used for demo only</u>
*/
ace.enable_searchbox_autocomplete = function($) {
	ace.vars['US_STATES'] = ["Alabama","Alaska","Arizona","Arkansas","California","Colorado","Connecticut","Delaware","Florida","Georgia","Hawaii","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Dakota","North Carolina","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming"]
	try {
		$('#nav-search-input').bs_typeahead({
			source: ace.vars['US_STATES'],
			updater:function (item) {
				//when an item is selected from dropdown menu, focus back to input element
				$('#nav-search-input').focus();
				return item;
			}
		});
	} catch(e) {}
}
;

