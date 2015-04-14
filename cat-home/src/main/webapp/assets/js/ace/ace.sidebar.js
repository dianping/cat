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
